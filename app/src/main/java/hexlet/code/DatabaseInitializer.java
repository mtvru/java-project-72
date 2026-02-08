package hexlet.code;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseInitializer {
    private static final String SCHEMA_NAME = "schema.sql";

    public static void runMigrations(DataSource dataSource) throws SQLException, IOException {
        String sql = readResourceFile();
        executeSql(dataSource, sql);
    }

    private static String readResourceFile() throws IOException {
        // Get the path to the file in src/main/resources
        try (InputStream url = DatabaseInitializer.class.getClassLoader().getResourceAsStream(SCHEMA_NAME)) {
            if (url == null) {
                throw new IllegalStateException("File " + SCHEMA_NAME + " not found in resources.");
            }
            return new BufferedReader(new InputStreamReader(url, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private static void executeSql(DataSource ds, String sql) throws SQLException {
        try (
                Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
