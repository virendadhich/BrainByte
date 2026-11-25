import java.util.Arrays;
import java.util.Locale;

public class OopAndLanguageBasics {
    interface Describable {
        String describe();
    }

    abstract static class Person implements Describable {
        private final String name;

        Person(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }
    }

    static final class Student extends Person {
        private static int createdCount;
        private final int id;
        private final int score;

        Student(String name, int score) throws InvalidScoreException {
            super(name);
            this.id = ++createdCount;
            this.score = validateScore(Integer.toString(score));
        }

        @Override
        public String describe() {
            return String.format(Locale.ROOT, "Student %d: %s (%d)", id, getName(), score);
        }

        static int getCreatedCount() {
            return createdCount;
        }
    }

    static final class InvalidScoreException extends Exception {
        InvalidScoreException(String message) {
            super(message);
        }
    }

    private static int validateScore(String value) throws InvalidScoreException {
        int score = Integer.parseInt(value);
        if (score < 0 || score > 100) {
            throw new InvalidScoreException("Score must be between 0 and 100.");
        }
        return score;
    }

    public static void main(String[] args) throws InvalidScoreException {
        Person person = new Student("Ava", 87);
        System.out.println(person.describe());
        System.out.println("Students created: " + Student.getCreatedCount());

        int[][] marks = {
            {87, 91},
            {76, 84}
        };
        System.out.println("Two-dimensional array: " + Arrays.deepToString(marks));

        String pooledOne = "Java";
        String pooledTwo = "Java";
        String separatelyCreated = new String("Java");
        System.out.println("Same pooled reference: " + (pooledOne == pooledTwo));
        System.out.println("Same content: " + pooledOne.equals(separatelyCreated));
        System.out.println("Formatted: " + String.format(Locale.ROOT, "%s score: %d", "Ava", marks[0][0]));

        StringBuilder builder = new StringBuilder("Java");
        builder.append(" StringBuilder");
        StringBuffer buffer = new StringBuffer("Java");
        buffer.append(" StringBuffer");
        System.out.println(builder);
        System.out.println(buffer);

        Integer wrappedScore = Integer.valueOf("87");
        System.out.println("Wrapper value: " + wrappedScore);

        try {
            validateScore("101");
        } catch (NumberFormatException | InvalidScoreException error) {
            System.out.println("Input validation failed: " + error.getMessage());
        } finally {
            System.out.println("Validation attempt finished.");
        }
    }
}
