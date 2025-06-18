package org.example.fileserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.file.AsyncFile;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RoutingHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String loadHtmlFromResources(String filename) {
        ClassLoader classLoader = RoutingHelper.class.getClassLoader();

        try {
            try (InputStream inputStream = classLoader.getResourceAsStream(filename)) {
                if (inputStream == null) {
                    throw new IOException("File not found in resources: " + filename);
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public static Future<Object> renderHomePage(RoutingContext routingContext) {
        String homePage = loadHtmlFromResources("homePageV2.html");
        routingContext.response().end(homePage);
        return Future.succeededFuture(homePage);
    }

    public static Future<Object> listItems(RoutingContext routingContext) {
        try {
            File directory = new File(Utils.getWebDirectory());
            File[] files = directory.listFiles();
            List<FileDetails> results = Arrays.stream(files)
                    .sorted(getFileTimeComparator())
                    .map(file -> {
                        FileDetails fileDetails = new FileDetails();
                        fileDetails.setFileName(file.getName());
                        fileDetails.setDate(getRelativeCreationDate(file));
                        fileDetails.setSize(formatSize(file.length()));
                        return fileDetails;
                    })
                    .collect(Collectors.toList());
            String body = MAPPER.writeValueAsString(Collections.singletonMap("items", results));
            routingContext.response().end(body);
        } catch (Exception e) {
            e.printStackTrace();
            return fail(routingContext);
        }
        return Future.succeededFuture();
    }

    public static String getRelativeCreationDate(File file) {
        if (file == null || !file.exists()) {
            return "Unknown";
        }

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            Instant createdInstant = attrs.creationTime().toInstant();
            Instant now = Instant.now();

            Duration duration = Duration.between(createdInstant, now);
            long seconds = duration.getSeconds();
            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();

            if (seconds < 60) {
                return "just now";
            } else if (minutes < 60) {
                return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
            } else if (days < 1) {
                return hours == 1 ? "1 hour ago" : hours + " hours ago";
            } else if (days == 1) {
                return "1 day ago";
            } else if (days < 30) {
                return days + " days ago";
            } else if (days < 365) {
                long months = days / 30;
                return months == 1 ? "1 month ago" : months + " months ago";
            } else {
                long years = days / 365;
                return years == 1 ? "1 year ago" : years + " years ago";
            }

        } catch (IOException e) {
            return "Unknown";
        }
    }


    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), unit);
    }


    public static Comparator<File> getFileTimeComparator() {
        return (f1, f2) -> {
            try {
                BasicFileAttributes attr1 = Files.readAttributes(f1.toPath(), BasicFileAttributes.class);
                BasicFileAttributes attr2 = Files.readAttributes(f2.toPath(), BasicFileAttributes.class);
                return attr2.creationTime().compareTo(attr1.creationTime());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file attributes", e);
            }
        };
    }

    public static Future<Object> fail(RoutingContext ctx) {
        ctx.response().setStatusCode(500);
        ctx.response().end("Internal Server Error");
        return Future.failedFuture("Internal Server Error");
    }

    public static Future<Object> uploadFile(RoutingContext routingContext) {
        routingContext.fileUploads().forEach( f -> {
            copyFileFromUploadsDirToWebDir(f);
        });
        
        return Future.succeededFuture();
    }

    private static void copyFileFromUploadsDirToWebDir(FileUpload f) {
        System.out.println(f.uploadedFileName());
        String filePath = FileNameHelper.getFileName(Utils.getWebDirectory(), f.fileName());

        try {
            FileUtils.copyFile(new File(f.uploadedFileName()), new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new File(f.uploadedFileName()).delete();
    }

    public static Future<Object> downloadFile(RoutingContext routingContext) {
        String fileId = routingContext.request().getParam("fileId");
        File downloadable = Paths.get(Utils.getWebDirectory(), fileId).toFile();

        if (downloadable.isFile()) {
            routingContext.response()
                    .putHeader("Content-Disposition", "attachment; filename=" + downloadable.getName())
                    .putHeader("Content-Type", "application/octet-stream")
                    .sendFile(downloadable.getPath());
            return Future.succeededFuture();
        } else {
            routingContext.response().setStatusCode(404);
            routingContext.response().end("Not Found");
            return Future.succeededFuture();
        }
    }

    public static Future<Object> deleteFile(RoutingContext routingContext) {
        String fileId = routingContext.request().getParam("fileId");
        File deletableFile = Paths.get(Utils.getWebDirectory(), fileId).toFile();

        if (deletableFile.isFile()) {
            deletableFile.delete();
            routingContext.response().setStatusCode(200).end();
            return Future.succeededFuture();
        } else {
            routingContext.response().setStatusCode(404);
            routingContext.response().end("Not Found");
            return Future.succeededFuture();
        }
    }
}
