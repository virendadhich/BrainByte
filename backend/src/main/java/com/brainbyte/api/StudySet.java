package com.brainbyte.api;

import java.util.List;

public record StudySet(
        String id,
        String title,
        String sourceExcerpt,
        Long createdAt,
        List<Flashcard> flashcards,
        List<QuizQuestion> questions) {
    public record Flashcard(String id, String front, String back) {}

    public record QuizQuestion(String id, String prompt, List<String> options, Integer correctIndex) {}
}
