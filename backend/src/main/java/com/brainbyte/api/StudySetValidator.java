package com.brainbyte.api;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class StudySetValidator {
    private static final Pattern ID_PATTERN = Pattern.compile("^[\\w-]{1,100}$");

    public void validate(StudySet set) {
        if (set == null
                || set.id() == null
                || !ID_PATTERN.matcher(set.id()).matches()
                || set.title() == null
                || set.title().isBlank()
                || set.title().length() > 200
                || set.sourceExcerpt() == null
                || set.sourceExcerpt().length() > 220
                || set.createdAt() == null
                || set.createdAt() < 0
                || set.createdAt() > 9_007_199_254_740_991L
                || set.flashcards() == null
                || set.flashcards().isEmpty()
                || set.flashcards().size() > 12
                || set.questions() == null
                || set.questions().isEmpty()
                || set.questions().size() > 10) {
            throw invalidStudySet();
        }

        boolean validCards = set.flashcards().stream().allMatch(card ->
                card != null
                        && card.id() != null
                        && validText(card.front(), 1000, false)
                        && validText(card.back(), 1000, false));
        boolean validQuestions = set.questions().stream().allMatch(question ->
                question != null
                        && question.id() != null
                        && validText(question.prompt(), 1000, false)
                        && question.options() != null
                        && question.options().size() == 4
                        && question.options().stream().allMatch(option -> validText(option, 300, false))
                        && question.correctIndex() != null
                        && question.correctIndex() >= 0
                        && question.correctIndex() < question.options().size());
        if (!validCards || !validQuestions) {
            throw invalidStudySet();
        }
    }

    private static boolean validText(String value, int maximumLength, boolean mustNotBeBlank) {
        return value != null
                && value.length() <= maximumLength
                && (!mustNotBeBlank || !value.isBlank());
    }

    private static ApiException invalidStudySet() {
        return new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST,
                "The study set has an invalid format.");
    }
}
