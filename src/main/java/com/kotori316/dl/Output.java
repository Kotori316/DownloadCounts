package com.kotori316.dl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class Output {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Pattern CSV_ENTRY = Pattern.compile("(.+),(\\d+),(\\d+)");
    private static final DateTimeFormatter LOCAL_DATE = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral('T')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .optionalStart()
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .toFormatter();

    static Path createCsv(String projectName, ZonedDateTime time) {
        Path csvPath = Path.of(projectName, time.format(YEAR_MONTH) + ".csv");
        if (Files.notExists(csvPath)) {
            try {
                if (Files.notExists(csvPath.getParent())) {
                    Files.createDirectories(csvPath.getParent());
                }
                try (
                    var buffer = Files.newBufferedWriter(csvPath)
                ) {
                    buffer.write("Date,Download,Total");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return csvPath;
    }

    static void appendCsv(ZonedDateTime time, String name, int count) {
        var csvPath = createCsv(name, time);
        int lastDownload = count;
        try {
            var lines = Files.readAllLines(csvPath);
            if (lines.size() > 1) {
                var lastLine = lines.get(lines.size() - 1);
                Matcher matcher = CSV_ENTRY.matcher(lastLine);
                if (matcher.matches()) {
                    lastDownload = Integer.parseInt(matcher.group(3));
                }
            }
            try (var writer = Files.newBufferedWriter(csvPath, StandardOpenOption.APPEND)) {
                writer.newLine();
                writer.write(String.format("%s,%d,%d", time.format(LOCAL_DATE), count - lastDownload, count));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void appendAll(ZonedDateTime time, Map<String, Integer> countMap) {
        countMap.forEach((name, downloads) -> appendCsv(time, name, downloads));
    }
}
