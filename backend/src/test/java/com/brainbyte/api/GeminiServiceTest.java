package com.brainbyte.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GeminiServiceTest {
    @Test
    void buildsGenerateContentRequestWithCurrentJsonSchemaField() {
        GeminiService service = new GeminiService(
                new ObjectMapper(),
                new RateLimiter(),
                "test-key",
                "gemini-test",
                "https://example.invalid/v1beta/models");

        var objectMapper = new ObjectMapper();
        var generationConfig = objectMapper.valueToTree(service.requestBody("course notes"))
                .path("generationConfig");

        assertEquals("application/json", generationConfig.path("responseMimeType").asText());
        assertTrue(generationConfig.has("responseJsonSchema"));
        assertFalse(generationConfig.has("responseSchema"));
        assertEquals("object", generationConfig.path("responseJsonSchema").path("type").asText());
    }
}
