package hexlet.code;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static void clearTables(DataSource ds) throws SQLException {
        String sql = "TRUNCATE TABLE urls RESTART IDENTITY CASCADE";
        try (
                Connection conn = ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.executeUpdate();
        }
    }
}
