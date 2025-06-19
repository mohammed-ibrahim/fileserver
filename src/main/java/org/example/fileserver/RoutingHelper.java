package org.example.fileserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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

    public static void renderHomePage(RoutingContext routingContext) {
        String homePage = loadHtmlFromResources("homePageV2.html");
        routingContext.response().end(homePage);
    }

    public static boolean verifyToken(String token) {

        try {
            String decoded = URLDecoder.decode(token, StandardCharsets.UTF_8.name());
            String [] parts = TokenUtils.decodeToken(decoded);
            return isValidUser(Utils.getUserAuthFilePath(), parts[0], parts[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isValidUser(String fileName, String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("=")) continue;

                String[] parts = line.split("=", 2);
                String fileUser = parts[0].trim();
                String filePass = parts[1].trim();

                if (fileUser.equals(username) && filePass.equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return false;
    }

    public static void performLogin(RoutingContext routingContext) {
        String requestBody = routingContext.body().asString();

        if (requestBody == null) {
            routingContext.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "Invalid JSON").encode());
            return;
        }

        LoginRequest loginRequest;
        try {
            loginRequest = MAPPER.readValue(requestBody, LoginRequest.class);
        } catch (IOException e) {
            fail(routingContext);
            return;
        }

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        try {
            if (isValidUser(Utils.getUserAuthFilePath(), username, password)) {
                routingContext.response()
                        .putHeader("Content-Type", "application/json")
                        .end(MAPPER.writeValueAsString(new LoginResponse(true, TokenUtils.generateToken(username, password))));

            } else {
                routingContext.response()
                        .putHeader("Content-Type", "application/json")
                        .end(MAPPER.writeValueAsString(new LoginResponse(false, "aaa")));

            }
        } catch (IOException e) {
            fail(routingContext);
        }

    }



    public static void unauthorized(RoutingContext routingContext) {
        routingContext.response().setStatusCode(401).end("NOT FOUND");
    }

    public static void renderLoginPage(RoutingContext routingContext) {
        String homePage = loadHtmlFromResources("loginPage.html");
        routingContext.response().end(homePage);
    }

    public static void listItems(RoutingContext routingContext) {
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
            fail(routingContext);
        }
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

    public static void fail(RoutingContext ctx) {
        ctx.response().setStatusCode(500);
        ctx.response().end("Internal Server Error");
    }

    public static void uploadFile(RoutingContext routingContext) {
        routingContext.fileUploads().forEach( f -> {
            copyFileFromUploadsDirToWebDir(f);
        });
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

    public static void downloadFile(RoutingContext routingContext) {
        String fileId = routingContext.request().getParam("fileId");
        File downloadable = Paths.get(Utils.getWebDirectory(), fileId).toFile();

        if (downloadable.isFile()) {
            routingContext.response()
                    .putHeader("Content-Disposition", "attachment; filename=" + downloadable.getName())
                    .putHeader("Content-Type", "application/octet-stream")
                    .sendFile(downloadable.getPath());
        } else {
            routingContext.response().setStatusCode(404);
            routingContext.response().end("Not Found");
        }
    }

    public static void deleteFile(RoutingContext routingContext) {
        String fileId = routingContext.request().getParam("fileId");
        File deletableFile = Paths.get(Utils.getWebDirectory(), fileId).toFile();

        if (deletableFile.isFile()) {
            deletableFile.delete();
            routingContext.response().setStatusCode(200).end();
        } else {
            routingContext.response().setStatusCode(404);
            routingContext.response().end("Not Found");
        }
    }
}
