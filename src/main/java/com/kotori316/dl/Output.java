package com.kotori316.dl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class Output {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Pattern CSV_ENTRY = Pattern.compile("(.+),(\\d+),(\\d+),(\\d+)");
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
                    buffer.write("Date,Download,Total,Monthly");
                    buffer.newLine();
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
        int lastMonthly = 0;
        try {
            var allLines = Files.readAllLines(csvPath);
            var lines = allLines.stream().filter(s -> !s.isEmpty()).collect(Collectors.toUnmodifiableList());
            if (lines.size() > 1) {
                var lastLine = lines.get(lines.size() - 1);
                Matcher matcher = CSV_ENTRY.matcher(lastLine);
                if (matcher.matches()) {
                    lastDownload = Integer.parseInt(matcher.group(3));
                    lastMonthly = Integer.parseInt(matcher.group(4));
                }
            } else if (lines.size() == 1) {
                // First line of new month, so we get last entry of last month.
                var lastMonthPath = Path.of(name, time.minusWeeks(1).format(YEAR_MONTH) + ".csv");
                if (Files.exists(lastMonthPath)) {
                    var lastLines = Files.readAllLines(lastMonthPath).stream().filter(s -> !s.isEmpty()).collect(Collectors.toUnmodifiableList());
                    var lastLine = lastLines.get(lastLines.size() - 1);
                    var matcher = CSV_ENTRY.matcher(lastLine);
                    if (matcher.matches()) {
                        lastDownload = Integer.parseInt(matcher.group(3));
                        lastMonthly = Integer.parseInt(matcher.group(4));
                    }
                }
            }
            try (var writer = Files.newBufferedWriter(csvPath, StandardOpenOption.APPEND)) {
                var todayCount = count - lastDownload;
                writer.write(String.format("%s,%d,%d,%d%n", time.withZoneSameInstant(ZoneOffset.UTC).format(LOCAL_DATE), todayCount, count, lastMonthly + todayCount));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void appendAll(ZonedDateTime time, Map<String, Integer> countMap) {
        countMap.forEach((name, downloads) -> appendCsv(time, name, downloads));
    }
}
