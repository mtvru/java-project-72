package hexlet.code;

import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static void clearTables(DataSource ds) throws SQLException {
        UrlRepository urlRepo = new UrlRepository(ds);
        UrlCheckRepository urlCheckRepo = new UrlCheckRepository(ds);
        try (
                Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.addBatch("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.addBatch("TRUNCATE TABLE " + urlRepo.getTableName() + " RESTART IDENTITY;");
            stmt.addBatch("TRUNCATE TABLE " + urlCheckRepo.getTableName() + " RESTART IDENTITY;");
            stmt.addBatch("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.executeBatch();
        }
    }

    public static long insertLocalhostToUrl(DataSource ds) throws SQLException {
        String sql = """
                INSERT INTO urls (name, created_at)
                VALUES ('http://somehost:7171', CURRENT_TIMESTAMP)
                """;
        try (
                Connection conn = ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to insert test URL");
    }
}
