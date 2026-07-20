package com.payment.gateway.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.flywaydb.core.Flyway;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DisplayName("Entity <-> Flyway schema drift guard")
class EntitySchemaDriftTest {

    private static final String BASE_PACKAGE = "com.payment.gateway";
    private static final String ALLOWLIST_RESOURCE = "/known-schema-drift.txt";

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                    DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("driftdb")
            .withUsername("drift")
            .withPassword("drift");

    @BeforeAll
    static void migrate() {
        postgres.start();
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();
    }

    @AfterAll
    static void stop() {
        postgres.stop();
    }

    @Test
    @DisplayName("Should have no entity/schema drift outside the known-drift register")
    void shouldHaveNoDriftOutsideRegister() throws Exception {
        Map<String, SchemaTable> schema = readSchema();
        Metadata entityMetadata = buildEntityMetadata();

        Set<String> drift = new TreeSet<>();
        Set<String> mappedColumns = new HashSet<>();

        for (Table table : entityMetadata.collectTableMappings()) {
            String tableName = table.getName().toLowerCase(Locale.ROOT);
            SchemaTable schemaTable = schema.get(tableName);
            assertThat(schemaTable)
                    .as("entity table %s must exist in the migrated schema", tableName)
                    .isNotNull();

            for (Column column : table.getColumns()) {
                String columnName = column.getName().toLowerCase(Locale.ROOT);
                String key = tableName + "." + columnName;
                mappedColumns.add(key);
                SchemaColumn sc = schemaTable.columns.get(columnName);
                if (sc == null) {
                    drift.add(key + " | MISSING_IN_SCHEMA | entity column has no schema column");
                    continue;
                }
                compareColumn(key, column, sc, drift);
            }

            for (UniqueKey uk : table.getUniqueKeys().values()) {
                Set<String> ukCols = new TreeSet<>();
                uk.getColumns().forEach(c -> ukCols.add(c.getName().toLowerCase(Locale.ROOT)));
                schemaTable.uniqueKeys.remove(ukCols);
            }
            for (Column column : table.getColumns()) {
                if (column.isUnique()) {
                    Set<String> ukCols = new TreeSet<>();
                    ukCols.add(column.getName().toLowerCase(Locale.ROOT));
                    schemaTable.uniqueKeys.remove(ukCols);
                }
            }
            for (Set<String> undeclared : schemaTable.uniqueKeys) {
                drift.add(tableName + ".<uk:" + String.join("+", undeclared)
                        + "> | UNIQUE_MISSING | schema unique constraint not declared on entity");
            }
        }

        for (SchemaTable schemaTable : schema.values()) {
            for (SchemaColumn sc : schemaTable.columns.values()) {
                String key = schemaTable.name + "." + sc.name();
                if (!mappedColumns.contains(key) && !sc.nullable() && sc.defaultValue() == null) {
                    drift.add(key + " | UNMAPPED_REQUIRED | NOT NULL column without default is unmapped");
                }
            }
        }

        Set<String> allowlist = readAllowlist();

        Set<String> newDrift = new TreeSet<>(drift);
        newDrift.removeIf(d -> allowlist.contains(keyAndKind(d)));
        Set<String> staleEntries = new TreeSet<>(allowlist);
        drift.forEach(d -> staleEntries.remove(keyAndKind(d)));

        assertThat(newDrift)
                .as(
                        "NEW drift not in known-schema-drift.txt — fix the entity or (only if truly known debt) register it")
                .isEmpty();
        assertThat(staleEntries)
                .as(
                        "STALE register entries — this drift no longer exists; remove the lines from known-schema-drift.txt")
                .isEmpty();
    }

    private static String keyAndKind(String driftLine) {
        int second = driftLine.indexOf(" | ", driftLine.indexOf(" | ") + 3);
        return driftLine.substring(0, second);
    }

    private void compareColumn(String key, Column entity, SchemaColumn sc, Set<String> drift) {
        boolean schemaIsText = sc.typeName().equals("text");
        boolean entityDeclaresText = entity.getSqlType() != null
                && entity.getSqlType().toLowerCase(Locale.ROOT).startsWith("text");

        if (schemaIsText && !entityDeclaresText) {
            drift.add(key + " | TYPE | schema=text entity=varchar(" + entity.getLength() + ")");
        } else if (sc.typeName().contains("varchar") && entity.getLength() != null) {
            if (entity.getLength().intValue() != sc.size()) {
                drift.add(key + " | LENGTH | entity=" + entity.getLength() + " schema=" + sc.size());
            }
        } else if (sc.typeName().equals("numeric")) {
            Integer p = entity.getPrecision();
            Integer s = entity.getScale();
            if (p != null && s != null && (p != sc.size() || s != sc.decimalDigits())) {
                drift.add(key + " | SCALE | entity=(" + p + "," + s + ") schema=(" + sc.size() + ","
                        + sc.decimalDigits() + ")");
            }
        }

        // Policy: entity may be stricter than schema, never looser.
        if (entity.isNullable() && !sc.nullable()) {
            drift.add(key + " | NULLABLE | entity=nullable schema=NOT NULL");
        }
    }

    private Metadata buildEntityMetadata() throws ClassNotFoundException {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting(
                        "hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .build();
        MetadataSources sources = new MetadataSources(registry);
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            sources.addAnnotatedClass(Class.forName(candidate.getBeanClassName()));
        }
        return sources.buildMetadata();
    }

    private Map<String, SchemaTable> readSchema() throws Exception {
        Map<String, SchemaTable> result = new HashMap<>();
        try (Connection conn =
                DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet tables = meta.getTables(null, "public", "%", new String[] {"TABLE"})) {
                while (tables.next()) {
                    String name = tables.getString("TABLE_NAME").toLowerCase(Locale.ROOT);
                    if (name.equals("flyway_schema_history")) {
                        continue;
                    }
                    result.put(name, new SchemaTable(name));
                }
            }
            for (SchemaTable table : result.values()) {
                try (ResultSet cols = meta.getColumns(null, "public", table.name, "%")) {
                    while (cols.next()) {
                        SchemaColumn c = new SchemaColumn(
                                cols.getString("COLUMN_NAME").toLowerCase(Locale.ROOT),
                                cols.getString("TYPE_NAME").toLowerCase(Locale.ROOT),
                                cols.getInt("COLUMN_SIZE"),
                                cols.getInt("DECIMAL_DIGITS"),
                                "YES".equals(cols.getString("IS_NULLABLE")),
                                cols.getString("COLUMN_DEF"));
                        table.columns.put(c.name(), c);
                    }
                }
                Set<String> pkColumns = new HashSet<>();
                try (ResultSet pk = meta.getPrimaryKeys(null, "public", table.name)) {
                    while (pk.next()) {
                        pkColumns.add(pk.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                    }
                }
                Map<String, Set<String>> indexes = new HashMap<>();
                try (ResultSet idx = meta.getIndexInfo(null, "public", table.name, true, false)) {
                    while (idx.next()) {
                        String idxName = idx.getString("INDEX_NAME");
                        String col = idx.getString("COLUMN_NAME");
                        if (idxName != null && col != null) {
                            indexes.computeIfAbsent(idxName, k -> new TreeSet<>())
                                    .add(col.toLowerCase(Locale.ROOT));
                        }
                    }
                }
                for (Set<String> cols : indexes.values()) {
                    if (!cols.equals(pkColumns)) {
                        table.uniqueKeys.add(cols);
                    }
                }
            }
        }
        return result;
    }

    private Set<String> readAllowlist() throws Exception {
        Set<String> entries = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(ALLOWLIST_RESOURCE), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|");
                entries.add(parts[0].trim() + " | " + parts[1].trim());
            }
        }
        return entries;
    }

    private static final class SchemaTable {
        final String name;
        final Map<String, SchemaColumn> columns = new HashMap<>();
        final List<Set<String>> uniqueKeys = new ArrayList<>();

        SchemaTable(String name) {
            this.name = name;
        }
    }

    private record SchemaColumn(
            String name, String typeName, int size, int decimalDigits, boolean nullable, String defaultValue) {}
}
