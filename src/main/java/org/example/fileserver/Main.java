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
        router.get("/api/items").respond(RoutingHelper::listItems);
        router.post("/upload").handler(BodyHandler.create()
                .setHandleFileUploads(true)
                .setUploadsDirectory("uploads")).respond(RoutingHelper::uploadFile);


        router
                .put("/a/b")
                .respond(RoutingHelper::handlePutRequest);

        server.requestHandler(router).listen(8080);
    }



}