package pt.isec.skysystem.model.data;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Utility class to run database migrations.
 */
public class MigrationRunner {

    /**
     * Runs the migration from v1 to v2 schema.
     * 
     * @return true if migration successful, false otherwise
     */
    public static boolean runMigrationV1ToV2() {
        System.out.println("=== Starting Database Migration v1 → v2 ===");

        try {
            String migrationSQL = readResourceFile("/schema/migration_v1_to_v2.sql");

            if (migrationSQL == null || migrationSQL.trim().isEmpty()) {
                System.err.println("Migration SQL file is empty or not found!");
                return false;
            }

            // Execute migration
            try (Connection conn = DataBaseManager.getConnection();
                    Statement stmt = conn.createStatement()) {

                System.out.println("Executing migration script...");

                String[] statements = migrationSQL.split(";");
                int executedCount = 0;

                for (String sql : statements) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty() &&
                            !trimmed.startsWith("--") &&
                            !trimmed.startsWith("/*")) {

                        try {
                            stmt.execute(trimmed);
                            executedCount++;
                        } catch (Exception e) {
                            if (!trimmed.contains("DROP TABLE IF EXISTS") &&
                                    !trimmed.contains("DROP TRIGGER IF EXISTS")) {
                                System.err.println("Warning executing statement: " + e.getMessage());
                            }
                        }
                    }
                }

                System.out.println("Migration completed! Executed " + executedCount + " statements.");

                return verifyMigration(conn);

            }

        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies that the migration was successful.
     */
    private static boolean verifyMigration(Connection conn) {
        System.out.println("\n=== Verifying Migration ===");

        try (Statement stmt = conn.createStatement()) {
            // Check if new tables exist
            var rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('FLIGHTS', 'SAVED_TRIPS')");

            int tableCount = 0;
            while (rs.next()) {
                String tableName = rs.getString("name");
                System.out.println("✓ Table exists: " + tableName);
                tableCount++;
            }

            if (tableCount != 2) {
                System.err.println("✗ Expected 2 tables (FLIGHTS, SAVED_TRIPS), found " + tableCount);
                return false;
            }

            // Check row counts
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM FLIGHTS");
            if (rs.next()) {
                System.out.println("✓ FLIGHTS table has " + rs.getInt("count") + " rows");
            }

            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM SAVED_TRIPS");
            if (rs.next()) {
                System.out.println("✓ SAVED_TRIPS table has " + rs.getInt("count") + " rows");
            }

            // Check for orphaned trips
            rs = stmt.executeQuery(
                    "SELECT COUNT(*) as count FROM SAVED_TRIPS st " +
                            "LEFT JOIN FLIGHTS f ON st.outbound_flight_id = f.flight_id " +
                            "WHERE f.flight_id IS NULL");

            if (rs.next()) {
                int orphaned = rs.getInt("count");
                if (orphaned > 0) {
                    System.err.println("✗ Found " + orphaned + " orphaned trips!");
                    return false;
                } else {
                    System.out.println("✓ No orphaned trips found");
                }
            }

            System.out.println("\n=== Migration Verification PASSED ===\n");
            return true;

        } catch (Exception e) {
            System.err.println("Verification failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads a resource file from classpath.
     */
    private static String readResourceFile(String resourcePath) {
        try (InputStream is = MigrationRunner.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }

            try (Scanner scanner = new Scanner(is).useDelimiter("\\A")) {
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (Exception e) {
            System.err.println("Error reading resource: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Main method for testing migration.
     */
    public static void main(String[] args) {
        System.out.println("Database Migration Tool");
        System.out.println("=======================\n");

        boolean success = runMigrationV1ToV2();

        if (success) {
            System.out.println("\n✓ Migration completed successfully!");
            System.exit(0);
        } else {
            System.err.println("\n✗ Migration failed!");
            System.exit(1);
        }
    }
}
