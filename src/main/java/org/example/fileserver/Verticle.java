//package org.example.fileserver;
//
//import io.vertx.core.AbstractVerticle;
//import io.vertx.core.Future;
//
//public class Verticle extends AbstractVerticle {
//
//    @Override
//    public void start(Future<Void> future) {
//        vertx.createHttpServer()
//                .requestHandler(r -> r.response().end("Welcome to Vert.x Intro"))
//                .listen(8080, result -> {
//                    if (result.succeeded()) {
//                        future.complete();
//                    } else {
//                        future.fail(result.cause());
//                    }
//                });
//
//        System.out.println("Welcome to Vertx");
//    }
//
//    @Override
//    public void stop() {
//        System.out.println("Shutting down application");
//    }
//}