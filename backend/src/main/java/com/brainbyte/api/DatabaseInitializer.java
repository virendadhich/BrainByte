package com.brainbyte.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                  email VARCHAR(254) NOT NULL UNIQUE,
                  password_hash VARCHAR(255) NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sessions (
                  token_hash CHAR(64) NOT NULL PRIMARY KEY,
                  user_id INT UNSIGNED NOT NULL,
                  expires_at DATETIME NOT NULL,
                  INDEX idx_sessions_expires_at (expires_at),
                  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id)
                    REFERENCES users(id) ON DELETE CASCADE
                ) ENGINE=InnoDB
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS study_sets (
                  user_id INT UNSIGNED NOT NULL,
                  id VARCHAR(100) NOT NULL,
                  title VARCHAR(200) NOT NULL,
                  created_at BIGINT UNSIGNED NOT NULL,
                  payload JSON NOT NULL,
                  PRIMARY KEY (user_id, id),
                  INDEX idx_study_sets_created_at (user_id, created_at),
                  CONSTRAINT fk_study_sets_user FOREIGN KEY (user_id)
                    REFERENCES users(id) ON DELETE CASCADE
                ) ENGINE=InnoDB
                """);
    }
}
