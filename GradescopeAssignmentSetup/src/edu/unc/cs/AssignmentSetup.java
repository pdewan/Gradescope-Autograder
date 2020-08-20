package edu.unc.cs;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

public class AssignmentSetup {

    private static final String DEFAULT_COURSE_NAME = "COMP401F18";
    private static final int DEFAULT_ASSINGMENT_NUMBER = 1;
    private static String COURSE_NAME;
    private static String ASSIGNMENT;
    private static String ASSIGNMENT_NO_SPACE;

    private static final String PATH_SEPARATOR = System.getProperty("path.separator");

    private static final String ROOT_DIRECOTRY = Paths.get(".").toAbsolutePath().getRoot().resolve("autograder").toString();

    private static final String GRADER_SOURCE_DIRECTORY = "source";

    private static final String GRADER_MAIN_CLASS = "gradingTools.Comp401Driver";
    private static final String GRADER_JAR_FILE = "comp401-grader-11.12-jar-with-dependencies.jar";
    private static final String GRADER_JAR_PATH = Paths.get(ROOT_DIRECOTRY, GRADER_SOURCE_DIRECTORY, GRADER_JAR_FILE).toString();
    private static final String CLASSPATH_EXTRA = "";
//    private static String CLASSPATH = "." + (CLASSPATH_EXTRA.isEmpty() ?? "" : PATH_SEPARATOR + CLASSPATH_EXTRA);
    private static final String CLASSPATH = "." + PATH_SEPARATOR + GRADER_JAR_PATH
            + (CLASSPATH_EXTRA.isEmpty() ? "" : PATH_SEPARATOR + CLASSPATH_EXTRA)
            + (System.getProperty("java.class.path").isEmpty() ? "" : PATH_SEPARATOR + System.getProperty("java.class.path"));

    private static final String GRADER_SCRIPT_NAME = "run-grader";

    private static String ONYEN = "student";
    private static String FIRST_NAME = "me";
    private static String LAST_NAME = "grade";
    private static final String USER_DIRECTORY = LAST_NAME + ", " + FIRST_NAME + "(" + ONYEN + ")";

    private static final String FEEDBACK_DIRECOTRY = "Feedback Attachment(s)";
    private static final String SUBMISSION_DIRECOTRY = "Submission attachment(s)";

    private static final String GRADE_SPREADSHEET = "grades.csv";
    private static final String TIMESTAMP_FILE = "timestamp.txt";

    private static final String GRADER_DIRECTORY = "graderProgram";
    private static final String GRADER_CONFIG_FOLDER = "config";
    private static final String GRADER_CONFIG_NAME = "config.properties";

    private static final String ASSIGNMENT_DATA_FOLDER = "log/AssignmentData";

    private static final String SUBMISSION_LOCATION = "submission";
    private static final String SUBMISSION_ARCHIVE_NAME = "submission.zip";

    private static final String RESULT_FILE_PROCESSOR = "GradescopeRunner.jar";
    private static final String GRADESCOPE_RESULT_DIR = "results";
    private static final String GRADESCOPE_JSON_FILE = "results.json";

    private static final String GRADER_RESULT_DIR = FEEDBACK_DIRECOTRY;
    private static final String GRADER_JSON_FILE = "results.json";

    private static final String METADATA_FILE_LOCATION = ROOT_DIRECOTRY;
    private static final String METADATA_FILE = "submission_metadata.json";

    private static final String EXECUTION_SHELL = "/bin/bash";

    private static final DateTimeFormatter SOURCE_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter TARGET_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmssSSS");

    static {
        setAssignment(DEFAULT_COURSE_NAME, DEFAULT_ASSINGMENT_NUMBER);
    }

    public static void main(String[] args) {
//    	System.out.println("classpath: " + System.getProperty("java.class.path"));
//    	CLASSPATH += (System.getProperty("java.class.path").isEmpty() ? "" : PATH_SEPARATOR + System.getProperty("java.class.path"));
        if (args.length == 2) {
            COURSE_NAME = args[0];
            try {
                int assignmentNum = Integer.parseInt(args[1]);
                setAssignment(args[0], assignmentNum);
            } catch (NumberFormatException e) {
                System.err.println("Argument 2 is not a number, defaulting to " + COURSE_NAME + " " + DEFAULT_ASSINGMENT_NUMBER);
            }
        }
        Path testBase = Paths.get(ROOT_DIRECOTRY, GRADER_SOURCE_DIRECTORY, ASSIGNMENT_NO_SPACE);
        Path graderBase = Paths.get(ROOT_DIRECOTRY, GRADER_SOURCE_DIRECTORY);
        try {
            purge(testBase);
            buildDirectories(testBase);
            buildFiles(testBase);

            IGraderConfigWriter configWriter = buildConfigWriter(testBase);
            setupGrader(graderBase, configWriter);
            buildRunScript(graderBase, configWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setAssignment(String name, int number) {
        COURSE_NAME = name;
        ASSIGNMENT = "Assignment " + number;
        ASSIGNMENT_NO_SPACE = ASSIGNMENT.replaceAll(" ", "");
    }

    private static IGraderConfigWriter buildConfigWriter(Path base) {
        IGraderConfigWriter configWriter = new GraderConfigWriter();
        configWriter.setAssignmentName(ASSIGNMENT_NO_SPACE);
        configWriter.setController(IGraderConfigWriter.HEADLESS_GRADING_MANAGER);
        configWriter.setPath(base.toString());
        configWriter.setStartOnyen(ONYEN);
        configWriter.setEndOnyen(ONYEN);
        configWriter.setCourseName(COURSE_NAME);
        configWriter.setLogging(IGraderConfigWriter.FEEDBACK_JSON, IGraderConfigWriter.FEEDBACK_TXT,
                IGraderConfigWriter.LOCAL_JSON, IGraderConfigWriter.LOCAL_TXT);

        return configWriter;
    }

    private static void setupGrader(Path base, IGraderConfigWriter configWriter) throws IOException {
        Files.createDirectories(base.resolve(ASSIGNMENT_DATA_FOLDER).resolve(ASSIGNMENT_NO_SPACE));
        Path configDir = base.resolve(GRADER_CONFIG_FOLDER);
        Files.createDirectories(configDir);
        Path configFilePath = configDir.resolve(GRADER_CONFIG_NAME);
        if (Files.notExists(configFilePath)) {
        	configWriter.write(configFilePath);
        }
    }

    private static void buildRunScript(Path base, IGraderConfigWriter configWriter) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#! ").append(EXECUTION_SHELL).append("\n");
        Path userDir = base.resolve(ASSIGNMENT_NO_SPACE).resolve(USER_DIRECTORY);
        sb.append("zip -r \"").append(userDir.resolve(SUBMISSION_DIRECOTRY).resolve(SUBMISSION_ARCHIVE_NAME));
        sb.append("\" \"").append(Paths.get(ROOT_DIRECOTRY, SUBMISSION_LOCATION)).append("\"\n");
        sb.append("cd ").append(base).append("\n");
        sb.append("java ").append("-cp ").append(CLASSPATH);
        sb.append(" ").append(GRADER_MAIN_CLASS);
        Arrays.stream(configWriter.getCommandArgs()).forEach(arg -> sb.append(' ').append(arg));
        sb.append("\n");

        Path graderResultFile = base.resolve(ASSIGNMENT_NO_SPACE).resolve(USER_DIRECTORY).resolve(GRADER_RESULT_DIR).resolve(GRADER_JSON_FILE);
        Path gradescopeResultFile = Paths.get(ROOT_DIRECOTRY, GRADESCOPE_RESULT_DIR, GRADESCOPE_JSON_FILE);

        sb.append("java -jar ").append(RESULT_FILE_PROCESSOR)
                .append(" \"").append(graderResultFile.toString())
                .append("\" \"").append(gradescopeResultFile.toString()).append("\"");

        Files.write(base.resolve(GRADER_SCRIPT_NAME), sb.toString().getBytes());
    }

    private static void buildDirectories(Path base) throws IOException {
        Files.createDirectories(base);
        Path userDir = base.resolve(USER_DIRECTORY);
        Files.createDirectories(userDir);
        Files.createDirectories(userDir.resolve(FEEDBACK_DIRECOTRY));
        Files.createDirectories(userDir.resolve(SUBMISSION_DIRECOTRY));
    }

    private static void buildFiles(Path base) throws IOException {
        Path gradeSpreadsheet = base.resolve(GRADE_SPREADSHEET);
        Files.deleteIfExists(gradeSpreadsheet);
        Files.createFile(gradeSpreadsheet);
        writeGradeSpreadsheet(gradeSpreadsheet);
        Path userDir = base.resolve(USER_DIRECTORY);
        createTimestamp(userDir);
//        copyAll(Paths.get(ROOT_DIRECOTRY, SUBMISSION_LOCATION), userDir.resolve(SUBMISSION_DIRECOTRY));
    }

    private static void writeGradeSpreadsheet(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(ASSIGNMENT + ",Points,,,");
        lines.add(",,,,");
        lines.add("Display ID,ID,Last Name,First Name,grade");
        lines.add(ONYEN + "," + ONYEN + "," + LAST_NAME + "," + FIRST_NAME + ",0.0");
        Files.write(file, lines);
    }

    private static void createTimestamp(Path userDir) throws IOException {
        String rawTimestamp = getRawTimestamp();
        TemporalAccessor gradescopeTimestamp = SOURCE_DATE_TIME_FORMATTER.parse(rawTimestamp);
        String sakaiTimestamp = TARGET_DATE_TIME_FORMATTER.format(gradescopeTimestamp);
        Files.write(userDir.resolve(TIMESTAMP_FILE), sakaiTimestamp.getBytes());
    }

    private static String getRawTimestamp() throws IOException {
        Path metadataFile = Paths.get(METADATA_FILE_LOCATION, METADATA_FILE);

        StringBuilder sb = new StringBuilder();
        Files.readAllLines(metadataFile).forEach(line -> sb.append(line).append("\n"));

        JSONObject metadata = new JSONObject(sb.toString());
        return metadata.getString("created_at");
    }

    private static void purge(Path root) throws IOException {
        Files.walkFileTree(root, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    private static void copyAll(Path source, Path dest) throws IOException {
    	System.out.println("COPY FROM: " + source.toString() + " TO: " + dest.toString());
        Files.walkFileTree(source, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("DIR START: " + dir.toString());
            	Path relative = source.relativize(dir);
                Files.createDirectories(dest.resolve(relative));
                System.out.println("    save as " + dest.resolve(relative));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            	System.out.println("FILE: " + file.toString());
                Path relative = source.relativize(file);
                Files.copy(file, dest.resolve(relative));
                System.out.println("    save as " + dest.resolve(relative));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            	System.out.println("FILE ERR: " + file.toString());
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            	System.out.println("DIR END: " + dir.toString());
                return FileVisitResult.CONTINUE;
            }

        });
    }
}
