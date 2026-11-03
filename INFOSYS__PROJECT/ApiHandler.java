package com.bugdiagnosis.server;

import com.bugdiagnosis.agent.AgentOrchestrator;
import com.bugdiagnosis.ingestion.BugSubmissionService;
import com.bugdiagnosis.knowledgebase.HistoricalDefectKnowledgeBase;
import com.bugdiagnosis.model.BugReport;
import com.bugdiagnosis.model.HistoricalDefect;
import com.bugdiagnosis.model.StructuredBugContext;
import com.bugdiagnosis.validation.AccuracyValidator;
import com.bugdiagnosis.validation.ValidationReport;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class ApiHandler implements HttpHandler {

    private final AgentOrchestrator orchestrator;
    private final BugSubmissionService submissionService;
    private final AccuracyValidator validator;
    private final HistoricalDefectKnowledgeBase knowledgeBase;

    public ApiHandler(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
        this.submissionService = new BugSubmissionService();
        this.knowledgeBase = orchestrator.getKnowledgeBase();
        this.validator = new AccuracyValidator(orchestrator);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("/api/submit".equals(path) && "POST".equalsIgnoreCase(method)) {
            String body = readRequestBody(exchange);
            String title = JsonHelper.extractJsonField(body, "title");
            String description = JsonHelper.extractJsonField(body, "description");
            String rawLog = JsonHelper.extractJsonField(body, "rawLog");
            String moduleHint = JsonHelper.extractJsonField(body, "affectedModuleHint");
            String environment = JsonHelper.extractJsonField(body, "environment");

            BugReport report = submissionService.processSubmission(title, description, rawLog, moduleHint, environment);
            StructuredBugContext context = orchestrator.orchestrate(report);
            sendJsonResponse(exchange, 200, JsonHelper.toJson(context));

        } else if ("/api/kb/defects".equals(path) && "GET".equalsIgnoreCase(method)) {
            Collection<HistoricalDefect> defects = knowledgeBase.getAllDefects();
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (HistoricalDefect d : defects) {
                sb.append(JsonHelper.toJson(d));
                if (++count < defects.size()) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());

        } else if ("/api/validation/run".equals(path) && "POST".equalsIgnoreCase(method)) {
            ValidationReport report = validator.runValidation();
            sendJsonResponse(exchange, 200, JsonHelper.toJson(report));

        } else {
            sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int read;
        while ((read = is.read(buffer)) != -1) baos.write(buffer, 0, read);
        return baos.toString(StandardCharsets.UTF_8);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }
}