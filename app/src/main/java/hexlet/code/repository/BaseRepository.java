package hexlet.code.repository;

import lombok.AllArgsConstructor;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public abstract class BaseRepository<T> {
    protected static final String COLUMN_ID = "id";
    protected static final String COLUMN_CREATED_AT = "created_at";
    private final DataSource dataSource;

    public final Optional<T> find(Long id) throws SQLException {
        String sql = "SELECT * FROM " + this.getTableName() + " WHERE " + COLUMN_ID + " = ?";
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(this.mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    protected final Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public abstract String getTableName();

    protected abstract T mapRow(ResultSet rs) throws SQLException;

}
