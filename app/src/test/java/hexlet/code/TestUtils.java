package hexlet.code;

import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static void clearTables(DataSource ds) throws SQLException {
        UrlRepository urlRepo = new UrlRepository(ds);
        UrlCheckRepository urlCheckRepo = new UrlCheckRepository(ds);
        try (
                Connection conn = ds.getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.addBatch("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.addBatch("TRUNCATE TABLE " + urlRepo.getTableName() + " RESTART IDENTITY;");
            stmt.addBatch("TRUNCATE TABLE " + urlCheckRepo.getTableName() + " RESTART IDENTITY;");
            stmt.addBatch("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.executeBatch();
        }
    }
}
