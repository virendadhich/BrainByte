package com.brainbyte.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class StudySetValidatorTest {
    private final StudySetValidator validator = new StudySetValidator();

    @Test
    void acceptsValidStudySet() {
        assertDoesNotThrow(() -> validator.validate(validStudySet()));
    }

    @Test
    void rejectsQuestionWithoutCorrectIndex() {
        StudySet valid = validStudySet();
        StudySet.QuizQuestion question = new StudySet.QuizQuestion(
                "q1",
                "Which answer is correct?",
                List.of("A", "B", "C", "D"),
                null);
        StudySet missingIndex = new StudySet(
                valid.id(),
                valid.title(),
                valid.sourceExcerpt(),
                valid.createdAt(),
                valid.flashcards(),
                List.of(question));

        assertThrows(ApiException.class, () -> validator.validate(missingIndex));
    }

    @Test
    void rejectsCreatedAtOutsideJavaScriptSafeIntegerRange() {
        StudySet valid = validStudySet();
        StudySet invalidTimestamp = new StudySet(
                valid.id(),
                valid.title(),
                valid.sourceExcerpt(),
                9_007_199_254_740_992L,
                valid.flashcards(),
                valid.questions());

        assertThrows(ApiException.class, () -> validator.validate(invalidTimestamp));
    }

    private static StudySet validStudySet() {
        return new StudySet(
                "set-1",
                "Java revision",
                "Course notes excerpt",
                1L,
                List.of(new StudySet.Flashcard("card-1", "Question", "Answer")),
                List.of(new StudySet.QuizQuestion(
                        "q1",
                        "Which answer is correct?",
                        List.of("A", "B", "C", "D"),
                        1)));
    }
}
