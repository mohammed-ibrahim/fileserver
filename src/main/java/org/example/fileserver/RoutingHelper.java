package org.example.fileserver;

import io.vertx.core.Future;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class RoutingHelper {

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
        routingContext.response().end("{\n" +
                "  \"items\": [\n" +
                "    \"a\",\n" +
                "    \"b\"\n" +
                "  ]\n" +
                "}");
        return Future.succeededFuture();
    }
}
