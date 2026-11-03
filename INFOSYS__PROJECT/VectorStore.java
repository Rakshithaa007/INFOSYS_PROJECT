package com.bugdiagnosis.knowledgebase;

import com.bugdiagnosis.model.HistoricalDefect;

import java.util.*;

public class VectorStore {

    public static class IndexedChunk {
        private final TextChunker.Chunk chunk;
        private final double[] vector;
        private final HistoricalDefect defectRef;

        public IndexedChunk(TextChunker.Chunk chunk, double[] vector, HistoricalDefect defectRef) {
            this.chunk = chunk;
            this.vector = vector;
            this.defectRef = defectRef;
        }

        public TextChunker.Chunk getChunk() { return chunk; }
        public double[] getVector() { return vector; }
        public HistoricalDefect getDefectRef() { return defectRef; }
    }

    public static class SearchResult {
        private final HistoricalDefect defect;
        private final double similarityScore;
        private final String matchedChunkSnippet;

        public SearchResult(HistoricalDefect defect, double similarityScore, String matchedChunkSnippet) {
            this.defect = defect;
            this.similarityScore = similarityScore;
            this.matchedChunkSnippet = matchedChunkSnippet;
        }

        public HistoricalDefect getDefect() { return defect; }
        public double getSimilarityScore() { return similarityScore; }
        public String getMatchedChunkSnippet() { return matchedChunkSnippet; }
    }

    private final List<IndexedChunk> indexedChunks = new ArrayList<>();
    private final Map<String, HistoricalDefect> defectRegistry = new LinkedHashMap<>();

    public synchronized void indexDefect(HistoricalDefect defect, List<TextChunker.Chunk> chunks, EmbeddingGenerator generator) {
        defectRegistry.put(defect.getDefectId(), defect);

        double[] docVector = generator.generateEmbedding(defect.getFullCorpusText());
        defect.setEmbedding(docVector);

        for (TextChunker.Chunk chunk : chunks) {
            double[] chunkVector = generator.generateEmbedding(chunk.getText());
            indexedChunks.add(new IndexedChunk(chunk, chunkVector, defect));
        }
    }

    public List<SearchResult> search(double[] queryVector, int topK, double minThreshold) {
        Map<String, Double> defectScores = new HashMap<>();
        Map<String, String> bestSnippets = new HashMap<>();

        for (IndexedChunk ic : indexedChunks) {
            double score = EmbeddingGenerator.cosineSimilarity(queryVector, ic.getVector());
            String defectId = ic.getDefectRef().getDefectId();

            if (score > defectScores.getOrDefault(defectId, 0.0)) {
                defectScores.put(defectId, score);
                String snippet = ic.getChunk().getText();
                if (snippet.length() > 160) snippet = snippet.substring(0, 157) + "...";
                bestSnippets.put(defectId, snippet);
            }
        }

        for (HistoricalDefect defect : defectRegistry.values()) {
            if (defect.getEmbedding() != null) {
                double docScore = EmbeddingGenerator.cosineSimilarity(queryVector, defect.getEmbedding());
                double existing = defectScores.getOrDefault(defect.getDefectId(), 0.0);
                defectScores.put(defect.getDefectId(), Math.max(existing, docScore * 0.95));
            }
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(defectScores.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<SearchResult> results = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sorted) {
            if (results.size() >= topK) break;
            if (entry.getValue() >= minThreshold) {
                HistoricalDefect defect = defectRegistry.get(entry.getKey());
                defect.setSimilarityScore(Math.round(entry.getValue() * 1000.0) / 1000.0);
                results.add(new SearchResult(defect, defect.getSimilarityScore(), bestSnippets.getOrDefault(entry.getKey(), "")));
            }
        }

        return results;
    }

    public Collection<HistoricalDefect> getAllDefects() { return defectRegistry.values(); }
    public HistoricalDefect getDefectById(String id) { return defectRegistry.get(id); }
    public int getChunkCount() { return indexedChunks.size(); }
    public int getDefectCount() { return defectRegistry.size(); }
}