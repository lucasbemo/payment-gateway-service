package com.payment.gateway.commons.event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for integration events published to Kafka.
 * Includes schema versioning for backward compatibility.
 */
public abstract class IntegrationEvent {

    private final String id;
    private final Instant occurredOn;
    private final String aggregateId;
    private final String schemaVersion;
    private final String eventType;

    protected IntegrationEvent(String aggregateId, String schemaVersion, String eventType) {
        this.id = java.util.UUID.randomUUID().toString();
        this.occurredOn = Instant.now();
        this.aggregateId = aggregateId;
        this.schemaVersion = schemaVersion;
        this.eventType = eventType;
    }

    protected IntegrationEvent(String id, Instant occurredOn, String aggregateId,
                                String schemaVersion, String eventType) {
        this.id = id;
        this.occurredOn = occurredOn;
        this.aggregateId = aggregateId;
        this.schemaVersion = schemaVersion;
        this.eventType = eventType;
    }

    public String getId() {
        return id;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getEventType() {
        return eventType;
    }

    /**
     * Check if this event is compatible with a given schema version.
     * Supports major version compatibility by default.
     */
    public boolean isCompatibleWith(String targetVersion) {
        if (this.schemaVersion == null || targetVersion == null) {
            return true; // Be permissive if version info is missing
        }

        String[] thisParts = this.schemaVersion.split("\\.");
        String[] targetParts = targetVersion.split("\\.");

        // Compare major version for backward compatibility
        if (thisParts.length > 0 && targetParts.length > 0) {
            try {
                int thisMajor = Integer.parseInt(thisParts[0]);
                int targetMajor = Integer.parseInt(targetParts[0]);
                return thisMajor == targetMajor;
            } catch (NumberFormatException e) {
                return true; // Be permissive if parsing fails
            }
        }

        return this.schemaVersion.equals(targetVersion);
    }

    /**
     * Migrate this event from an older schema version.
     * Override in subclasses to handle schema migrations.
     */
    public IntegrationEvent migrateFrom(String fromVersion) {
        // Default implementation returns this event as-is
        return this;
    }

    /**
     * Convert this event to a map for serialization.
     * Includes schema version for backward compatibility.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("occurredOn", getOccurredOn());
        map.put("aggregateId", getAggregateId());
        map.put("schemaVersion", getSchemaVersion());
        map.put("eventType", getEventType());
        populateMap(map);
        return map;
    }

    /**
     * Populate the map with event-specific data.
     * Override in subclasses to add event fields.
     */
    protected void populateMap(Map<String, Object> map) {
        // Override in subclasses
    }
}
