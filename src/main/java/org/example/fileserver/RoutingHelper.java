package org.example.fileserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
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
        String homePage = loadHtmlFromResources("homePage.html");
        routingContext.response().end(homePage);
        return Future.succeededFuture(homePage);
    }

    public static Future<Object> handlePutRequest(RoutingContext ctx) {
        return null;
    }

    public static Future<Object> listItems(RoutingContext routingContext) {
        try {
            File directory = new File("web");
            File[] files = directory.listFiles();
            List<String> results = Arrays.asList(files).stream().map(file -> file.getName()).collect(Collectors.toList());
            String body = MAPPER.writeValueAsString(Collections.singletonMap("items", results));
            routingContext.response().end(body);
        } catch (Exception e) {
            e.printStackTrace();
            return fail(routingContext);
        }
        return Future.succeededFuture();
    }

    public static Future<Object> fail(RoutingContext ctx) {
        ctx.response().setStatusCode(500);
        ctx.response().end("Internal Server Error");
        return Future.failedFuture("Internal Server Error");
    }

    public static Future<Object> uploadFile(RoutingContext routingContext) {
        routingContext.fileUploads().forEach( f -> {
            dumpFileContents(f);
        });
        
        return Future.succeededFuture();
    }

    private static void dumpFileContents(FileUpload f) {
        System.out.println(f.uploadedFileName());
    }
}
