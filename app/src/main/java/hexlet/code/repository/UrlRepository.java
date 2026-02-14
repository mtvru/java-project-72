package hexlet.code.repository;

import hexlet.code.model.Url;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class UrlRepository extends BaseRepository<Url> {
    private static final String COLUMN_NAME = "name";
    private static final String TABLE_NAME = "urls";

    public UrlRepository(DataSource dataSource) {
        super(dataSource, TABLE_NAME);
    }

    public void save(Url url) throws SQLException {
        String sql = String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?)",
            this.getTableName(), COLUMN_NAME, COLUMN_CREATED_AT
        );
        try (
                Connection conn = getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, Timestamp.from(url.getCreatedAt()));
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    url.assignId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("DB have not returned an id after saving an entity");
                }
            }
        }
    }

    public List<Url> findAll() throws SQLException {
        String sql = String.format("SELECT * FROM %s ORDER BY %s DESC", this.getTableName(), COLUMN_CREATED_AT);
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet resultSet = stmt.executeQuery(sql)) {
            List<Url> result = new ArrayList<>();
            while (resultSet.next()) {
                Url url = this.mapRow(resultSet);
                result.add(url);
            }
            return result;
        }
    }

    @Override
    protected Url mapRow(ResultSet resultSet) throws SQLException {
        Url url = new Url(
                resultSet.getString(COLUMN_NAME),
                resultSet.getTimestamp(COLUMN_CREATED_AT).toInstant()
        );
        url.assignId(resultSet.getLong(COLUMN_ID));

        return url;
    }
}
