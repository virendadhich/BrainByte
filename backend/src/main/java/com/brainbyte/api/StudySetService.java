package com.brainbyte.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class StudySetService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final StudySetValidator validator;
    private final TransactionTemplate transactionTemplate;

    public StudySetService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            StudySetValidator validator,
            TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.transactionTemplate = transactionTemplate;
    }

    public List<StudySet> list(long userId) {
        return jdbcTemplate.query(
                """
                SELECT payload FROM study_sets
                WHERE user_id = ? ORDER BY created_at DESC, id DESC
                """,
                (result, row) -> {
                    try {
                        return objectMapper.readValue(result.getString("payload"), StudySet.class);
                    } catch (JsonProcessingException error) {
                        throw new IllegalStateException("A saved study set contains invalid JSON.", error);
                    }
                },
                userId);
    }

    public StudySet save(long userId, StudySet set) {
        validator.validate(set);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(set);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Could not serialize the study set.", error);
        }

        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update(
                    """
                    INSERT INTO study_sets (user_id, id, title, created_at, payload)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE title = VALUES(title), created_at = VALUES(created_at),
                      payload = VALUES(payload)
                    """,
                    userId,
                    set.id(),
                    set.title(),
                    set.createdAt(),
                    payload);
            jdbcTemplate.update(
                    """
                    DELETE FROM study_sets
                    WHERE user_id = ? AND id NOT IN (
                      SELECT id FROM (
                        SELECT id FROM study_sets WHERE user_id = ?
                        ORDER BY created_at DESC, id DESC LIMIT 20
                      ) AS recent_sets
                    )
                    """,
                    userId,
                    userId);
        });
        return set;
    }
}
