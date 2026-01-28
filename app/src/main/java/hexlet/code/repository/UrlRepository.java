package hexlet.code.repository;

import hexlet.code.model.Url;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    private static final String COLUMN_NAME = "name";

    public UrlRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void save(Url url) throws SQLException {
        String sql = "INSERT INTO " + this.getTableName()
                + " (" + COLUMN_NAME + ", " + COLUMN_CREATED_AT + ") VALUES (?, ?)";
        try (
                Connection conn = getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, url.getCreatedAt());
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    url.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("DB have not returned an id after saving an entity");
                }
            }
        }
    }

    public Optional<Url> find(Long id) throws SQLException {
        String sql = "SELECT * FROM " + this.getTableName() + " WHERE " + COLUMN_ID + " = ?";
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    Url url = this.mapRow(resultSet);
                    return Optional.of(url);
                }
                return Optional.empty();
            }
        }
    }

    public List<Url> findAll() throws SQLException {
        String sql = "SELECT * FROM " + this.getTableName() + " ORDER BY " + COLUMN_CREATED_AT + " DESC";
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                List<Url> result = new ArrayList<>();
                while (resultSet.next()) {
                    Url url = this.mapRow(resultSet);
                    result.add(url);
                }
                return result;
            }
        }
    }

    @Override
    protected String getTableName() {
        return "urls";
    }

    private Url mapRow(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong(COLUMN_ID);
        String name = resultSet.getString(COLUMN_NAME);
        Timestamp createdAt = resultSet.getTimestamp(COLUMN_CREATED_AT);
        Url url = new Url(name);
        url.setId(id);
        url.setCreatedAt(createdAt);

        return url;
    }
}
