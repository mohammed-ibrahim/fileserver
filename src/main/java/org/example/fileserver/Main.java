package org.example.fileserver;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.KeyStoreOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
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
        router.get("/").handler(routingContext -> withRedirectToLoginPage(routingContext, () -> RoutingHelper.renderHomePage(routingContext)));
        router.post("/login").handler(BodyHandler.create()).handler(routingContext -> {
            RoutingHelper.performLogin(routingContext);
        });
        router.get("/file/download/:fileId").handler(routingContext -> withAuth(routingContext, () -> RoutingHelper.downloadFile(routingContext)));
        router.get("/api/items").handler(routingContext -> withAuth(routingContext, () -> RoutingHelper.listItems(routingContext)));
        router.delete("/file/:fileId").handler(routingContext -> withAuth(routingContext, () -> RoutingHelper.deleteFile(routingContext)));

        router.post("/upload").handler(ctx -> {
            ctx.request().setExpectMultipart(true);

            final boolean[] fileUploaded = {false};  // flag to track if any file was uploaded

            ctx.request().uploadHandler(upload -> {
                fileUploaded[0] = true;  // at least one file detected
                String filename = upload.filename();
                String filePath = FileNameHelper.getFileName(Utils.getWebDirectory(), filename);
                String uploadPath = filePath;

                System.out.println("Receiving file: " + filename);
                upload.streamToFileSystem(uploadPath);

                upload.exceptionHandler(err -> {
                    System.err.println("Upload failed: " + err.getMessage());
                    if (!ctx.response().ended()) {
                        ctx.response().setStatusCode(500).end("Upload failed");
                    }
                });

                upload.endHandler(v -> {
                    System.out.println("Upload complete: " + filename);
                    if (!ctx.response().ended()) {
                        ctx.response().setStatusCode(200).end("File uploaded");
                    }
                });
            });

            ctx.request().endHandler(v -> {
                if (!fileUploaded[0] && !ctx.response().ended()) {
                    ctx.response().setStatusCode(400).end("No file uploaded");
                }
            });
        });

        router.route().failureHandler(ctx -> {
            Throwable failure = ctx.failure();

            if (failure != null) {
                System.err.println("Unmanaged error: " + failure.getMessage());
                failure.printStackTrace();

                ctx.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json")
                        .end("{\"error\": \"Internal Server Error\"}");
            }

        });


        server.requestHandler(router).listen()
                .onSuccess(res -> System.out.println("HTTP server started on port " + Utils.getPort()))
                .onFailure(err -> {
                    System.err.println("HTTP server failed to start: " + err.getMessage());
                    err.printStackTrace();
                });
    }

    private static Future<Object> submitAction(ExecutorService executorService, RoutingContext routingContext, Supplier action) {
        CompletableFuture.runAsync(() -> action.get(), executorService);
        return Future.succeededFuture(new Object());
    }


    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static void withAuth(RoutingContext routingContext, Runnable action) {
        Cookie authz = routingContext.request().getCookie("auth_token");
        if (authz != null && RoutingHelper.verifyToken(authz.getValue())) {
            try {
                executorService.submit(action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            RoutingHelper.unauthorized(routingContext);
        }
    }

    private static Future<Object> withRedirectToLoginPage(RoutingContext routingContext, Runnable action) {
        Cookie authz = routingContext.request().getCookie("auth_token");
        if (authz != null && RoutingHelper.verifyToken(authz.getValue())) {
            try {
                action.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            RoutingHelper.renderLoginPage(routingContext);
        }
        return Future.succeededFuture(new Object());
    }


}