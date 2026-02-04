package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceFactory {
    public static DataSource createDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(AppConfig.getDatabaseUrl());
        hikariConfig.setMaximumPoolSize(AppConfig.getMaximumPoolSize());
        return new HikariDataSource(hikariConfig);
    }
}
