# Java Course Examples

These are standalone Java source files for the 25CSE0202 revision topics. They are separate from the React/TypeScript UI and require JDK 21 or newer.

## Compile and run

From this directory in PowerShell:

```powershell
javac -d out *.java
java -cp out OopAndLanguageBasics
java -cp out CollectionsStreamsGenerics
java -cp out ConcurrencyAndThreads
java -cp out FileHandlingAndCsv
```

`FileHandlingAndCsv` writes demonstration files to a temporary directory and prints its path.

JDBC is integrated into the real Spring Boot application backend in `backend/`;
it is not a standalone database demo. See the root README for setup and startup.
