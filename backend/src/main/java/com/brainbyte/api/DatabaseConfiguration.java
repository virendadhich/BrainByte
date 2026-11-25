package com.brainbyte.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Locale;

@Configuration
public class DatabaseConfiguration {
    @Bean
    DataSource dataSource(
            @Value("${brainbyte.mysql.host}") String host,
            @Value("${brainbyte.mysql.port}") int port,
            @Value("${brainbyte.mysql.database}") String database,
            @Value("${brainbyte.mysql.user}") String user,
            @Value("${brainbyte.mysql.password}") String password,
            @Value("${brainbyte.mysql.ssl}") boolean ssl) {
        if (user.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Set MYSQL_USER and MYSQL_PASSWORD before starting the API.");
        }

        boolean localHost = switch (host.toLowerCase(Locale.ROOT)) {
            case "localhost", "127.0.0.1", "::1" -> true;
            default -> false;
        };
        boolean allowPublicKeyRetrieval = !ssl && localHost;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://%s:%d/%s?sslMode=%s&allowPublicKeyRetrieval=%s"
                .formatted(
                        host,
                        port,
                        database,
                        ssl ? "VERIFY_IDENTITY" : "DISABLED",
                        allowPublicKeyRetrieval));
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);
        config.addDataSourceProperty("connectionTimeZone", "SERVER");
        config.addDataSourceProperty("forceConnectionTimeZoneToSession", "true");
        return new HikariDataSource(config);
    }
}
