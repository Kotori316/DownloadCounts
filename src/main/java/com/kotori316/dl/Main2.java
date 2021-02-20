package com.kotori316.dl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;

public class Main2 {
    private static final Pattern CSV_ENTRY = Pattern.compile("(.+),(\\d+),(\\d+)");
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM");

    public static void main(String[] args) throws CurseException {
        var time = ZonedDateTime.now();
        var projectIDs = List.of(282837, 291006, 320926);
        for (int id : projectIDs) {
            CurseAPI.project(id).ifPresent(p -> {
                int count = p.downloadCount();
                var csvPath = createCsv(p.name(), time);
                int lastDownload = 0;
                try {
                    var lines = Files.readAllLines(csvPath);
                    if (lines.size() > 1) {
                        var lastLine = lines.get(lines.size() - 1);
                        Matcher matcher = CSV_ENTRY.matcher(lastLine);
                        if (matcher.matches()) {
                            lastDownload = Integer.parseInt(matcher.group(2));
                        }
                    }
                    try (var writer = Files.newBufferedWriter(csvPath, StandardOpenOption.APPEND)) {
                        writer.newLine();
                        writer.write(String.format("%s,%d,%d", time.format(DateTimeFormatter.ISO_LOCAL_DATE), count - lastDownload, count));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        System.exit(0);
    }


    static Path createCsv(String projectName, ZonedDateTime time) {
        Path csvPath = Path.of(projectName, time.format(format) + ".csv");
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
}
