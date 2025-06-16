package org.example.fileserver;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/").respond(RoutingHelper::renderHomePage);
        router.get("/api/items").respond(RoutingHelper::listItems);

        router
                .get("/some/path")
                .respond(
                        ctx -> ctx
                                .response()
                                .putHeader("Content-Type", "text/plain")
                                .end("hello world!"));

        router
                .put("/a/b")
                .respond(RoutingHelper::handlePutRequest);

        server.requestHandler(router).listen(8080);
    }



}