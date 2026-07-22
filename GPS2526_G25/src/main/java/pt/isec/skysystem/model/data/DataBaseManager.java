package pt.isec.skysystem.model.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.sql.ResultSet;

public class DataBaseManager {

    private static String DB_FILE_PATH = "./database/skysystem.db";
    private static String URL = "jdbc:sqlite:" + DB_FILE_PATH;

    private static final String SCHEMA_RESOURCE_PATH = "/schema/schema.sql";

    public static synchronized void initializeDatabase() {
        try {
            createDatabaseDirectory();

            if (Files.exists(Paths.get(DB_FILE_PATH))) {
                System.out.println("Database already exists. Initialization ignored.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(URL);
                 Statement stmt = conn.createStatement()) {

                String schemaSQL = readResourceFile(SCHEMA_RESOURCE_PATH);

                if (schemaSQL.trim().isEmpty()) {
                    throw new IOException("The SCHEMA.sql file is empty or not read.");
                }

                stmt.executeUpdate(schemaSQL);

                System.out.println("Database SQLite created and initialized successfully at: " + DB_FILE_PATH);

            }

        } catch (Exception e) {
            System.err.println("Critical error initializing the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads the content of the SQL file embedded in the JAR/CLASSPATH.
     * This is the correct way to read resource files in Java applications.
     */
    private static String readResourceFile(String resourcePath) throws IOException {
        try (InputStream is = DataBaseManager.class.getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new IOException(
                        "Resource not found. Ensure that 'schema.sql' is in 'src/main/resources/database/'. Path: "
                                + resourcePath);
            }

            try (Scanner scanner = new Scanner(is).useDelimiter("\\A")) {
                return scanner.hasNext() ? scanner.next() : "";
            }

        }
    }

    /**
     * Verifies and creates the parent directory for the database file.
     */
    private static void createDatabaseDirectory() throws IOException {
        Path dbPath = Paths.get(DB_FILE_PATH);
        Path parentDir = dbPath.getParent();

        if (parentDir != null && Files.notExists(parentDir)) {
            Files.createDirectories(parentDir);
            System.out.println("Database directory created: " + parentDir.toAbsolutePath());
        }
    }

    public static synchronized Connection getConnection() throws Exception {
        createDatabaseDirectory();

        try {
            org.sqlite.SQLiteConfig config = new org.sqlite.SQLiteConfig();
            config.enforceForeignKeys(true); // <--- OBRIGATÓRIO PARA O TESTE PASSAR

            return DriverManager.getConnection(URL, config.toProperties());

        } catch (SQLException e) {
            throw new Exception("Failed to obtain database connection: " + e.getMessage(), e);
        }
    }

    public static synchronized void setTestMode(boolean active) {
        if (active) {
            DB_FILE_PATH = "database/skysystem_test.db";
        } else {
            DB_FILE_PATH = "database/skysystem.db";
        }
        // Atualiza o URL de conexão
        URL = "jdbc:sqlite:" + DB_FILE_PATH;
    }

    private static boolean runScript(Statement stmt, String resourcePath) {
        try {
            String sqlScript = readResourceFile(resourcePath);
            if (sqlScript == null || sqlScript.trim().isEmpty()) {
                // Se o ficheiro não existir (comum se apagaste os .sql), apenas ignora ou avisa
                System.out.println("Script not found or empty: " + resourcePath + " (Skipping)");
                return false;
            }

            // Separa por ; para executar instrução a instrução
            String[] commands = sqlScript.split(";");
            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error executing script " + resourcePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Runs database migration from v1 to v2 schema.
     * Checks if SAVED_TRIPS table exists, if not, runs migration.
     */
    public static synchronized boolean runMigrationIfNeeded() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Verificação Inicial
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='SAVED_TRIPS'");
            if (!rs.next()) {
                // Se não existe a tabela principal, assume-se DB nova
                return true;
            }

            // 2. Migração V1 -> V2 (Se necessário)
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='FAVORITES_FLIGHTS'");
            if (rs.next()) {
                System.out.println("Running Migration V1 -> V2...");
                runScript(stmt, "/schema/migration_v1_to_v2.sql");
            }

            // 3. Migração V2 -> V3 (Airline Names)
            rs = stmt.executeQuery("PRAGMA table_info(FLIGHTS)");
            boolean hasAirlineName = false;
            while (rs.next()) {
                if ("airline_name".equals(rs.getString("name"))) {
                    hasAirlineName = true;
                    break;
                }
            }
            if (!hasAirlineName) {
                System.out.println("Running Migration V2 -> V3...");
                runScript(stmt, "/schema/migration_v2_to_v3.sql");
            }

            // ====================================================================
            // 4. Migração V3 -> V4 (Trip Notifications) - SQL DIRETO NO CÓDIGO
            // ====================================================================
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='trip_notifications'");
            boolean hasNotificationsTable = rs.next();

            if (!hasNotificationsTable) {
                System.out.println("=== Applying Migration V4 (Trip Notifications) ===");

                // SQL Inserido diretamente para evitar erros de leitura de ficheiro
                String sqlCreateTable =
                        "CREATE TABLE IF NOT EXISTS trip_notifications (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "trip_id INTEGER NOT NULL, " +
                                "message TEXT NOT NULL, " +
                                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "is_read BOOLEAN DEFAULT 0, " +
                                "FOREIGN KEY(trip_id) REFERENCES SAVED_TRIPS(trip_id) ON DELETE CASCADE" +
                                ")";

                stmt.execute(sqlCreateTable);

                // Criar o índice
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_trip_notifications_trip_id ON trip_notifications(trip_id)");

                System.out.println("✓ Tabela 'trip_notifications' criada com sucesso via Java.");
            }

            return true;

        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}