package hexlet.code.exception;

import java.sql.SQLException;

public class DbConnectionException extends IllegalArgumentException {
    public DbConnectionException(SQLException e) {
        super(e);
    }
}
