package hexlet.code.repository;

import lombok.AllArgsConstructor;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
abstract public class BaseRepository {
    protected static final String COLUMN_ID = "id";
    protected static final String COLUMN_CREATED_AT = "created_at";
    protected final DataSource dataSource;

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    abstract protected String getTableName();
}
