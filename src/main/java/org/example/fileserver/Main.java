package org.example.fileserver;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        Vertx vertx = Vertx.vertx();
//        HttpServerOptions httpServerOptions = new HttpServerOptions().setPort(8080);
//        httpServerOptions.setSsl(true);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/").respond(RoutingHelper::renderHomePage);
        router.get("/file/download/:fileId").respond(RoutingHelper::downloadFile);
        router.get("/api/items").respond(RoutingHelper::listItems);
        router.delete("/file/:fileId").respond(RoutingHelper::deleteFile);
        router.post("/upload").handler(BodyHandler.create()
                .setHandleFileUploads(true)
                .setUploadsDirectory("uploads")).respond(RoutingHelper::uploadFile);

        server.requestHandler(router).listen(8080);
    }



}