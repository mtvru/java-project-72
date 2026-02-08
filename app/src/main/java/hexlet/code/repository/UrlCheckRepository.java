package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

public final class UrlCheckRepository extends BaseRepository<UrlCheck> {
    private static final String COLUMN_URL_ID = "url_id";
    private static final String COLUMN_STATUS_CODE = "status_code";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_H1 = "h1";
    private static final String COLUMN_DESCRIPTION = "description";

    public UrlCheckRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void save(UrlCheck urlCheck) throws SQLException {
        String sql = String.format(
                "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                getTableName(), COLUMN_URL_ID, COLUMN_STATUS_CODE, COLUMN_TITLE, COLUMN_H1, COLUMN_DESCRIPTION,
                COLUMN_CREATED_AT);
        try (
                Connection conn = getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            final int pUrlId = 1;
            final int pStatusCode = 2;
            final int pTitle = 3;
            final int pH1 = 4;
            final int pDescription = 5;
            final int pCreatedAt = 6;
            preparedStatement.setLong(pUrlId, urlCheck.getUrlId());
            preparedStatement.setObject(pStatusCode, urlCheck.getStatusCode(), Types.INTEGER);
            preparedStatement.setString(pTitle, urlCheck.getTitle());
            preparedStatement.setString(pH1, urlCheck.getH1());
            preparedStatement.setString(pDescription, urlCheck.getDescription());
            preparedStatement.setTimestamp(pCreatedAt, urlCheck.getCreatedAt());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    urlCheck.assignId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("DB have not returned an id after saving an entity");
                }
            }
        }
    }

    public List<UrlCheck> findByUrlId(Long urlId) throws SQLException {
        String sql = "SELECT * FROM " + this.getTableName()
                + " WHERE " + COLUMN_URL_ID + " = ? ORDER BY " + COLUMN_ID + " DESC";
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                List<UrlCheck> result = new ArrayList<>();
                while (resultSet.next()) {
                    UrlCheck urlCheck = mapRow(resultSet);
                    result.add(urlCheck);
                }
                return result;
            }
        }
    }

    public Map<Long, UrlCheck> findLatestUrlChecksByUrls(List<Url> urls) throws SQLException {
        if (urls.isEmpty()) {
            return Collections.emptyMap();
        }
        String idList = urls.stream()
                .map(url -> String.valueOf(url.getId()))
                .collect(Collectors.joining(", "));
        String sql = String.format(
                "SELECT * FROM %s WHERE id IN (SELECT MAX(id) FROM %s GROUP BY %s) AND %s IN (%s)",
                this.getTableName(), this.getTableName(), COLUMN_URL_ID, COLUMN_URL_ID, idList);
        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet resultSet = stmt.executeQuery(sql)) {
            Map<Long, UrlCheck> result = new HashMap<>();
            while (resultSet.next()) {
                UrlCheck urlCheck = mapRow(resultSet);
                result.put(urlCheck.getUrlId(), urlCheck);
            }
            return result;
        }
    }

    @Override
    protected UrlCheck mapRow(ResultSet resultSet) throws SQLException {
        UrlCheck urlCheck = new UrlCheck(
                resultSet.getLong(COLUMN_URL_ID),
                resultSet.getObject(COLUMN_STATUS_CODE, Integer.class),
                resultSet.getString(COLUMN_TITLE),
                resultSet.getString(COLUMN_H1),
                resultSet.getString(COLUMN_DESCRIPTION),
                resultSet.getTimestamp(COLUMN_CREATED_AT));
        urlCheck.assignId(resultSet.getLong(COLUMN_ID));
        return urlCheck;
    }

    @Override
    public String getTableName() {
        return "url_checks";
    }
}
