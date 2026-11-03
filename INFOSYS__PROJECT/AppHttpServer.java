package com.bugdiagnosis.server;

import com.bugdiagnosis.agent.AgentOrchestrator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class AppHttpServer {

    private final int port;
    private final AgentOrchestrator orchestrator;
    private HttpServer server;
    private final String webRootDir;

    public AppHttpServer(int port, AgentOrchestrator orchestrator, String webRootDir) {
        this.port = port;
        this.orchestrator = orchestrator;
        this.webRootDir = webRootDir;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/api", new ApiHandler(orchestrator));
        server.createContext("/", new StaticFileHandler(webRootDir));

        server.start();
        System.out.println("==================================================================");
        System.out.println("  Intelligent Bug Diagnosis Platform with Fix Recommendation");
        System.out.println("==================================================================");
        System.out.println("  Web Dashboard:  http://localhost:" + port);
        System.out.println("  API Endpoint:   http://localhost:" + port + "/api/submit");
        System.out.println("  Knowledge Base: http://localhost:" + port + "/api/kb/defects");
        System.out.println("  Engine Status:  Java 21 LTS - Multi-Agent Active");
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) server.stop(1);
    }

    private static class StaticFileHandler implements HttpHandler {
        private final String webRootDir;

        public StaticFileHandler(String webRootDir) {
            this.webRootDir = webRootDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) path = "/index.html";

            Path filePath = Paths.get(webRootDir, path);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                exchange.getResponseHeaders().set("Content-Type", getMimeType(path));
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                return;
            }

            try (InputStream is = getClass().getResourceAsStream("/web" + path)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    exchange.getResponseHeaders().set("Content-Type", getMimeType(path));
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
                    return;
                }
            }

            String notFound = "<h1>404 Not Found</h1>";
            exchange.sendResponseHeaders(404, notFound.length());
            try (OutputStream os = exchange.getResponseBody()) { os.write(notFound.getBytes(StandardCharsets.UTF_8)); }
        }

        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            return "text/plain; charset=UTF-8";
        }
    }
}
