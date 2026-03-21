#!/bin/bash
# =============================================================================
# PostgreSQL Database Backup Script
# =============================================================================
# Creates a backup of the payment_gateway database
#
# Usage:
#   ./backup-database.sh [options]
#
# Options:
#   -c, --container   Docker container name (default: payment-gateway-postgres)
#   -u, --user        Database user (default: admin)
#   -d, --database    Database name (default: payment_gateway)
#   -o, --output      Output directory (default: ./backups)
#   -h, --help        Show this help message
#
# Environment variables:
#   PGPASSWORD        Database password
# =============================================================================

set -e

# Default values
CONTAINER_NAME="${POSTGRES_CONTAINER:-payment-gateway-postgres}"
DB_USER="${POSTGRES_USER:-admin}"
DB_NAME="${DB_NAME:-payment_gateway}"
OUTPUT_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${OUTPUT_DIR}/payment_gateway_${TIMESTAMP}.dump"

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
        -o|--output)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        -h|--help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  -c, --container   Docker container name (default: payment-gateway-postgres)"
            echo "  -u, --user        Database user (default: admin)"
            echo "  -d, --database    Database name (default: payment_gateway)"
            echo "  -o, --output      Output directory (default: ./backups)"
            echo "  -h, --help        Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Create output directory
mkdir -p "${OUTPUT_DIR}"

echo "=============================================="
echo "Payment Gateway Database Backup"
echo "=============================================="
echo "Container: ${CONTAINER_NAME}"
echo "Database:  ${DB_NAME}"
echo "User:      ${DB_USER}"
echo "Output:    ${BACKUP_FILE}"
echo "=============================================="

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "ERROR: Container '${CONTAINER_NAME}' is not running"
    exit 1
fi

# Create backup
echo "Creating backup..."
docker exec "${CONTAINER_NAME}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" -F c -f "/tmp/backup.dump"

# Copy backup from container
echo "Copying backup from container..."
docker cp "${CONTAINER_NAME}:/tmp/backup.dump" "${BACKUP_FILE}"

# Clean up temp file in container
docker exec "${CONTAINER_NAME}" rm -f /tmp/backup.dump

# Get file size
FILE_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)

echo "=============================================="
echo "Backup completed successfully!"
echo "File: ${BACKUP_FILE}"
echo "Size: ${FILE_SIZE}"
echo "=============================================="

# Optional: Upload to S3
if [ -n "${AWS_S3_BUCKET}" ]; then
    echo "Uploading to S3..."
    aws s3 cp "${BACKUP_FILE}" "s3://${AWS_S3_BUCKET}/backups/$(basename ${BACKUP_FILE})"
    echo "Upload complete!"
fi

# Optional: Clean up old backups (keep last 7 days)
find "${OUTPUT_DIR}" -name "payment_gateway_*.dump" -mtime +7 -delete 2>/dev/null || true

echo "Done."