package com.bugdiagnosis.knowledgebase;

import java.util.*;

public class EmbeddingGenerator {

    public static final int VECTOR_DIMENSION = 256;

    private static final Map<String, Double> KEYWORD_WEIGHTS = new HashMap<>();
    static {
        KEYWORD_WEIGHTS.put("nullpointerexception", 3.0);
        KEYWORD_WEIGHTS.put("outofmemoryerror", 3.5);
        KEYWORD_WEIGHTS.put("deadlock", 3.2);
        KEYWORD_WEIGHTS.put("concurrency", 2.8);
        KEYWORD_WEIGHTS.put("timeout", 2.6);
        KEYWORD_WEIGHTS.put("sqlexception", 3.0);
        KEYWORD_WEIGHTS.put("segmentation", 3.5);
        KEYWORD_WEIGHTS.put("typeerror", 3.0);
        KEYWORD_WEIGHTS.put("compiler", 2.2);
        KEYWORD_WEIGHTS.put("astparser", 2.5);
    }

    public double[] generateEmbedding(String text) {
        double[] vector = new double[VECTOR_DIMENSION];
        if (text == null || text.trim().isEmpty()) return vector;

        String normalized = text.toLowerCase().replaceAll("[^a-z0-9_$.\\-\\s]", " ");
        String[] tokens = normalized.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            double weight = KEYWORD_WEIGHTS.getOrDefault(token, 1.0);

            int hash1 = Math.abs(token.hashCode()) % VECTOR_DIMENSION;
            vector[hash1] += weight;

            if (token.length() >= 3) {
                for (int i = 0; i <= token.length() - 3; i++) {
                    String sub = token.substring(i, i + 3);
                    int subHash = Math.abs(sub.hashCode()) % VECTOR_DIMENSION;
                    vector[subHash] += 0.35 * weight;
                }
            }

            int hash2 = Math.abs(token.hashCode() * 31 + 17) % VECTOR_DIMENSION;
            vector[hash2] += (0.5 * weight);
        }

        double sumSq = 0.0;
        for (double v : vector) sumSq += (v * v);

        if (sumSq > 0.0) {
            double norm = Math.sqrt(sumSq);
            for (int i = 0; i < VECTOR_DIMENSION; i++) vector[i] /= norm;
        }

        return vector;
    }

    public static double cosineSimilarity(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) return 0.0;
        double dot = 0.0;
        for (int i = 0; i < v1.length; i++) dot += (v1[i] * v2[i]);
        return Math.max(0.0, Math.min(1.0, dot));
    }
}