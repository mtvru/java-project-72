package hexlet.code;

import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    private static final HikariDataSource DATA_SOURCE = HikariDataSourceFactory.create();

    public static void clearTables() throws SQLException {
        UrlRepository urlRepo = new UrlRepository(DATA_SOURCE);
        UrlCheckRepository urlCheckRepo = new UrlCheckRepository(DATA_SOURCE);
        try (
                Connection conn = DATA_SOURCE.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.addBatch("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.addBatch("TRUNCATE TABLE " + urlRepo.getTableName() + " RESTART IDENTITY;");
            stmt.addBatch("TRUNCATE TABLE " + urlCheckRepo.getTableName() + " RESTART IDENTITY;");
            stmt.addBatch("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.executeBatch();
        }
    }

    public static long insertLocalhostToUrl() throws SQLException {
        String sql = """
                INSERT INTO urls (name, created_at)
                VALUES ('http://somehost:7171', CURRENT_TIMESTAMP)
                """;
        try (
                Connection conn = DATA_SOURCE.getConnection();
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

    public static List<UrlCheck> findUrlChecksByUrlId(Long urlId) throws SQLException {
        UrlCheckRepository urlCheckRepository = new UrlCheckRepository(DATA_SOURCE);
        return urlCheckRepository.findByUrlId(urlId);
    }
}
