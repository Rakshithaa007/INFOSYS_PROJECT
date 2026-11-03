package com.bugdiagnosis.knowledgebase;

import com.bugdiagnosis.model.BugReport;
import com.bugdiagnosis.model.HistoricalDefect;
import com.bugdiagnosis.model.LogAnalysisResult;

import java.util.Collection;
import java.util.List;

public class HistoricalDefectKnowledgeBase {

    private final VectorStore vectorStore;
    private final TextChunker textChunker;
    private final EmbeddingGenerator embeddingGenerator;

    public HistoricalDefectKnowledgeBase() {
        this.vectorStore = new VectorStore();
        this.textChunker = new TextChunker(100, 20);
        this.embeddingGenerator = new EmbeddingGenerator();
        initSeededKnowledge();
    }

    private void initSeededKnowledge() {
        List<HistoricalDefect> seeded = SeedDatasetLoader.loadSeededDefects();
        for (HistoricalDefect defect : seeded) {
            indexDefect(defect);
        }
    }

    public synchronized void indexDefect(HistoricalDefect defect) {
        List<TextChunker.Chunk> chunks = textChunker.chunkText(defect.getDefectId(), defect.getDescription(), "DESCRIPTION");
        chunks.addAll(textChunker.chunkText(defect.getDefectId(), defect.getStackTrace(), "STACK_TRACE"));
        chunks.addAll(textChunker.chunkText(defect.getDefectId(), defect.getPatchDiff(), "PATCH_DIFF"));
        chunks.addAll(textChunker.chunkText(defect.getDefectId(), defect.getRootCause(), "ROOT_CAUSE"));

        vectorStore.indexDefect(defect, chunks, embeddingGenerator);
    }

    public List<VectorStore.SearchResult> searchSimilar(String query, int topK, double minThreshold) {
        double[] queryVector = embeddingGenerator.generateEmbedding(query);
        return vectorStore.search(queryVector, topK, minThreshold);
    }

    public List<VectorStore.SearchResult> searchSimilar(BugReport report, LogAnalysisResult logResult, int topK) {
        StringBuilder query = new StringBuilder();
        if (report.getTitle() != null) query.append(report.getTitle()).append(" ");
        if (report.getDescription() != null) query.append(report.getDescription()).append(" ");
        if (report.getAffectedModuleHint() != null) query.append(report.getAffectedModuleHint()).append(" ");

        if (logResult != null) {
            if (logResult.getExceptionType() != null) query.append(logResult.getExceptionType()).append(" ");
            if (logResult.getErrorMessage() != null) query.append(logResult.getErrorMessage()).append(" ");
            if (logResult.getFailurePoint() != null) {
                query.append(logResult.getFailurePoint().getClassName()).append(" ");
                query.append(logResult.getFailurePoint().getMethodName()).append(" ");
                query.append(logResult.getFailurePoint().getFileName()).append(" ");
            }
        }

        double[] queryVector = embeddingGenerator.generateEmbedding(query.toString());
        return vectorStore.search(queryVector, topK, 0.10);
    }

    public Collection<HistoricalDefect> getAllDefects() { return vectorStore.getAllDefects(); }
    public HistoricalDefect getDefectById(String id) { return vectorStore.getDefectById(id); }
    public int getDefectCount() { return vectorStore.getDefectCount(); }
    public int getChunkCount() { return vectorStore.getChunkCount(); }
}