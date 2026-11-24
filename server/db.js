import mysql from 'mysql2/promise'

const ssl = process.env.MYSQL_SSL === 'true' ? { rejectUnauthorized: true } : undefined

export const pool = mysql.createPool({
  host: process.env.MYSQL_HOST || '127.0.0.1',
  port: Number(process.env.MYSQL_PORT || 3306),
  user: process.env.MYSQL_USER,
  password: process.env.MYSQL_PASSWORD,
  database: process.env.MYSQL_DATABASE || 'brainbyte',
  waitForConnections: true,
  connectionLimit: 10,
  timezone: 'Z',
  ssl,
})

export async function initializeDatabase() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS users (
      id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
      email VARCHAR(254) NOT NULL UNIQUE,
      password_hash VARCHAR(255) NOT NULL,
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB
  `)

  await pool.query(`
    CREATE TABLE IF NOT EXISTS sessions (
      token_hash CHAR(64) NOT NULL PRIMARY KEY,
      user_id INT UNSIGNED NOT NULL,
      expires_at DATETIME NOT NULL,
      INDEX idx_sessions_expires_at (expires_at),
      CONSTRAINT fk_sessions_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB
  `)

  await pool.query(`
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
  `)
}
