#!/bin/bash
# Kafka Topic Initialization Script
# This script creates all required Kafka topics for the Payment Gateway Service

set -e

KAFKA_BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"

echo "Creating Kafka topics on bootstrap server: $KAFKA_BOOTSTRAP_SERVER"

# Function to create a topic
create_topic() {
    local topic_name=$1
    local partitions=${2:-3}
    local replication_factor=${3:-1}
    local retention_ms=${4:-604800000}  # 7 days default

    echo "Creating topic: $topic_name (partitions=$partitions, replication=$replication_factor, retention=$retention_ms ms)"

    kafka-topics.sh --create \
        --bootstrap-server "$KAFKA_BOOTSTRAP_SERVER" \
        --topic "$topic_name" \
        --partitions "$partitions" \
        --replication-factor "$replication_factor" \
        --config retention.ms="$retention_ms" \
        --if-not-exists

    echo "Topic '$topic_name' created successfully"
}

# Payment Events Topics
create_topic "payment-events" 3 1 604800000
create_topic "payment-created" 3 1 604800000
create_topic "payment-completed" 3 1 604800000
create_topic "payment-failed" 3 1 604800000
create_topic "payment-cancelled" 3 1 604800000

# Transaction Events Topics
create_topic "transaction-events" 3 1 604800000
create_topic "transaction-created" 3 1 604800000
create_topic "transaction-completed" 3 1 604800000
create_topic "transaction-failed" 3 1 604800000

# Refund Events Topics
create_topic "refund-events" 3 1 604800000
create_topic "refund-processed" 3 1 604800000
create_topic "refund-failed" 3 1 604800000

# Customer Events Topics
create_topic "customer-events" 3 1 604800000
create_topic "customer-created" 3 1 604800000
create_topic "customer-updated" 3 1 604800000

# Merchant Events Topics
create_topic "merchant-events" 3 1 604800000
create_topic "merchant-activated" 3 1 604800000
create_topic "merchant-suspended" 3 1 604800000

# Outbox Events Topics (for transactional outbox pattern)
create_topic "outbox-events" 3 1 604800000

# Dead Letter Queue Topics
create_topic "payment-events-dlq" 1 1 2592000000  # 30 days
create_topic "transaction-events-dlq" 1 1 2592000000
create_topic "refund-events-dlq" 1 1 2592000000

# Audit/Compliance Topics
create_topic "audit-events" 3 1 7776000000  # 90 days

# Reconciliation Topics
create_topic "reconciliation-events" 3 1 604800000

# Notification Topics
create_topic "notification-events" 3 1 604800000

echo ""
echo "All Kafka topics created successfully!"
echo ""
echo "Listing all topics:"
kafka-topics.sh --list --bootstrap-server "$KAFKA_BOOTSTRAP_SERVER"
