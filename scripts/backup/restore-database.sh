#!/bin/bash
# =============================================================================
# PostgreSQL Database Restore Script
# =============================================================================
# Restores a backup of the payment_gateway database
#
# Usage:
#   ./restore-database.sh <backup_file> [options]
#
# Options:
#   -c, --container   Docker container name (default: payment-gateway-postgres)
#   -u, --user        Database user (default: admin)
#   -d, --database    Database name (default: payment_gateway)
#   -h, --help        Show this help message
#
# WARNING: This will overwrite existing data!
# =============================================================================

set -e

# Default values
CONTAINER_NAME="${POSTGRES_CONTAINER:-payment-gateway-postgres}"
DB_USER="${POSTGRES_USER:-admin}"
DB_NAME="${DB_NAME:-payment_gateway}"
BACKUP_FILE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--container)
            CONTAINER_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 <backup_file> [options]"
            echo ""
            echo "Arguments:"
            echo "  backup_file       Path to the backup file (.dump)"
            echo ""
            echo "Options:"
            echo "  -c, --container   Docker container name (default: payment-gateway-postgres)"
            echo "  -u, --user        Database user (default: admin)"
            echo "  -d, --database    Database name (default: payment_gateway)"
            echo "  -h, --help        Show this help message"
            echo ""
            echo "WARNING: This will overwrite existing data!"
            exit 0
            ;;
        *)
            if [ -z "${BACKUP_FILE}" ]; then
                BACKUP_FILE="$1"
            else
                echo "Unknown option: $1"
                exit 1
            fi
            shift
            ;;
    esac
done

# Validate backup file
if [ -z "${BACKUP_FILE}" ]; then
    echo "ERROR: No backup file specified"
    echo "Usage: $0 <backup_file> [options]"
    exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "ERROR: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

# Confirm restore
echo "=============================================="
echo "Payment Gateway Database Restore"
echo "=============================================="
echo "Container: ${CONTAINER_NAME}"
echo "Database:  ${DB_NAME}"
echo "User:      ${DB_USER}"
echo "Backup:    ${BACKUP_FILE}"
echo "=============================================="
echo ""
echo "WARNING: This will overwrite all existing data!"
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "${confirm}" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "ERROR: Container '${CONTAINER_NAME}' is not running"
    exit 1
fi

# Copy backup to container
echo "Copying backup to container..."
docker cp "${BACKUP_FILE}" "${CONTAINER_NAME}:/tmp/restore.dump"

# Terminate existing connections
echo "Terminating existing connections..."
docker exec "${CONTAINER_NAME}" psql -U "${DB_USER}" -d postgres -c \
    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${DB_NAME}' AND pid <> pg_backend_pid();" || true

# Drop and recreate database
echo "Recreating database..."
docker exec "${CONTAINER_NAME}" psql -U "${DB_USER}" -d postgres -c "DROP DATABASE IF EXISTS ${DB_NAME};"
docker exec "${CONTAINER_NAME}" psql -U "${DB_USER}" -d postgres -c "CREATE DATABASE ${DB_NAME};"

# Restore backup
echo "Restoring backup..."
docker exec "${CONTAINER_NAME}" pg_restore -U "${DB_USER}" -d "${DB_NAME}" -v /tmp/restore.dump

# Clean up
docker exec "${CONTAINER_NAME}" rm -f /tmp/restore.dump

echo "=============================================="
echo "Restore completed successfully!"
echo "=============================================="