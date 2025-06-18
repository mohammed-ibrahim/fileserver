package org.example.fileserver;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        Vertx vertx = Vertx.vertx();

        HttpServerOptions httpServerOptions = new HttpServerOptions().setPort(Utils.getPort());
        if (Utils.enableSsl()) {
            httpServerOptions.setSsl(true);
            KeyStoreOptions keyStoreOptions = null;
            try {
                String password = FileUtils.readFileToString(new File(Utils.getPfxPasswordFile()), StandardCharsets.UTF_8).trim();
                keyStoreOptions = new KeyStoreOptions()
                        .setType("PKCS12")
                        .setPassword(password)
                        .setPath(Utils.getPfxFilePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            httpServerOptions.setKeyCertOptions(keyStoreOptions);
        }

        HttpServer server = vertx.createHttpServer(httpServerOptions);
        Router router = Router.router(vertx);
        router.get("/").respond(RoutingHelper::renderHomePage);
        router.get("/file/download/:fileId").respond(RoutingHelper::downloadFile);
        router.get("/api/items").respond(RoutingHelper::listItems);
        router.delete("/file/:fileId").respond(RoutingHelper::deleteFile);
        router.post("/upload").handler(BodyHandler.create()
                .setHandleFileUploads(true)
                .setUploadsDirectory(Utils.getTemporaryUploadsDirectory())).respond(RoutingHelper::uploadFile);

        server.requestHandler(router).listen()
                .onSuccess(res -> System.out.println("HTTP server started on port " + Utils.getPort()))
                .onFailure(err -> {
                    System.err.println("HTTP server failed to start: " + err.getMessage());
                    err.printStackTrace();
                });
    }



}