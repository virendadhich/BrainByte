import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileHandlingAndCsv {
    record StudentRecord(String name, int score) {
        StudentRecord {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Student name cannot be blank.");
            }
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("Score must be between 0 and 100.");
            }
        }
    }

    private static List<StudentRecord> importCsv(Path path) throws IOException {
        List<StudentRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
            new FileReader(path.toFile(), StandardCharsets.UTF_8)
        )) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                try {
                    if (fields.length != 2) {
                        throw new IllegalArgumentException("Expected name and score.");
                    }
                    records.add(new StudentRecord(fields[0].trim(), Integer.parseInt(fields[1].trim())));
                } catch (IllegalArgumentException error) {
                    System.err.println("Skipping invalid CSV row " + lineNumber + ": " + error.getMessage());
                }
            }
        }
        return records;
    }

    private static void exportCsv(Path path, List<StudentRecord> records) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
            new FileWriter(path.toFile(), StandardCharsets.UTF_8)
        )) {
            writer.write("name,score");
            writer.newLine();
            for (StudentRecord record : records) {
                writer.write(record.name() + "," + record.score());
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path workDirectory = Files.createTempDirectory("java-file-handling-");

        Path binaryFile = workDirectory.resolve("sample.bin");
        try (FileOutputStream output = new FileOutputStream(binaryFile.toFile())) {
            output.write(new byte[] {65, 66, 67});
        }
        try (FileInputStream input = new FileInputStream(binaryFile.toFile())) {
            System.out.println("Byte stream contents: " + new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }

        Path notesFile = workDirectory.resolve("notes.txt");
        try (FileWriter writer = new FileWriter(notesFile.toFile(), StandardCharsets.UTF_8)) {
            writer.write("Java streams 101 2025");
        }
        try (FileReader reader = new FileReader(notesFile.toFile(), StandardCharsets.UTF_8)) {
            StreamTokenizer tokenizer = new StreamTokenizer(reader);
            System.out.print("Tokens:");
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
                    System.out.print(" " + (int) tokenizer.nval);
                } else {
                    System.out.print(" " + tokenizer.sval);
                }
            }
            System.out.println();
        }

        Path inputCsv = workDirectory.resolve("students.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(inputCsv, StandardCharsets.UTF_8)) {
            writer.write("name,score");
            writer.newLine();
            writer.write("Ava,91");
            writer.newLine();
            writer.write("Mina,84");
            writer.newLine();
            writer.write("Invalid,not-a-number");
            writer.newLine();
        }

        List<StudentRecord> records = importCsv(inputCsv);
        Path exportCsv = workDirectory.resolve("students-export.csv");
        exportCsv(exportCsv, records);
        System.out.println("Imported records: " + records);
        System.out.println("CSV exported to: " + exportCsv);
        System.out.println("Demo files are in: " + workDirectory);
    }
}
