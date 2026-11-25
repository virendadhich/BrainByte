package com.brainbyte.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Duration SESSION_LIFETIME = Duration.ofDays(7);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AuthenticatedAccount register(String rawEmail, String password) {
        String email = normalizeEmail(rawEmail);
        validateEmail(email);
        validatePassword(password, true);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update((PreparedStatementCreator) connection -> {
                var statement = connection.prepareStatement(
                        "INSERT INTO users (email, password_hash) VALUES (?, ?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, email);
                statement.setString(2, passwordEncoder.encode(password));
                return statement;
            }, keyHolder);
            Number generatedId = keyHolder.getKey();
            if (generatedId == null || generatedId.longValue() <= 0) {
                throw new IllegalStateException("Database did not return the new account id.");
            }
            return createSession(new Account(generatedId.longValue(), email));
        } catch (DuplicateKeyException error) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with that email already exists.");
        }
    }

    public Optional<AuthenticatedAccount> login(String rawEmail, String password) {
        String email = normalizeEmail(rawEmail);
        validatePassword(password, false);
        if (email.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter your email and password.");
        }

        var users = jdbcTemplate.query(
                "SELECT id, email, password_hash FROM users WHERE email = ?",
                (result, row) -> new UserRow(
                        result.getLong("id"),
                        result.getString("email"),
                        result.getString("password_hash")),
                email);
        if (users.isEmpty() || !passwordEncoder.matches(password, users.getFirst().passwordHash())) {
            return Optional.empty();
        }

        UserRow user = users.getFirst();
        return Optional.of(createSession(new Account(user.id(), user.email())));
    }

    public Optional<Account> currentAccount(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        var accounts = jdbcTemplate.query(
                """
                SELECT users.id, users.email
                FROM sessions JOIN users ON users.id = sessions.user_id
                WHERE sessions.token_hash = ? AND sessions.expires_at > UTC_TIMESTAMP()
                """,
                (result, row) -> new Account(result.getLong("id"), result.getString("email")),
                hashToken(token));
        return accounts.stream().findFirst();
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            jdbcTemplate.update("DELETE FROM sessions WHERE token_hash = ?", hashToken(token));
        }
    }

    private AuthenticatedAccount createSession(Account account) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbcTemplate.update(
                "INSERT INTO sessions (token_hash, user_id, expires_at) VALUES (?, ?, ?)",
                hashToken(token),
                account.id(),
                java.sql.Timestamp.from(java.time.Instant.now().plus(SESSION_LIFETIME)));
        return new AuthenticatedAccount(account, token);
    }

    private static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static void validateEmail(String email) {
        if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }
    }

    private static void validatePassword(String password, boolean registration) {
        if (password == null
                || password.length() < 10
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            if (registration) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Password must be at least 10 characters and no more than 72 bytes.");
            }
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter your email and password.");
        }
    }

    private static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable.", error);
        }
    }

    public record AuthenticatedAccount(Account account, String token) {}

    private record UserRow(long id, String email, String passwordHash) {}
}
