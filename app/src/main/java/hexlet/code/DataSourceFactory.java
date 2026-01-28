package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceFactory {
    private final static Integer MAX_POOL_SIZE = 25;

    public static DataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(AppConfig.getDatabaseUrl());
        hikariConfig.setMaximumPoolSize(MAX_POOL_SIZE);
        return new HikariDataSource(hikariConfig);
    }
}
