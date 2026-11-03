package com.bugdiagnosis.knowledgebase;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    private final int chunkSizeWords;
    private final int overlapWords;

    public TextChunker() {
        this(120, 25);
    }

    public TextChunker(int chunkSizeWords, int overlapWords) {
        this.chunkSizeWords = chunkSizeWords;
        this.overlapWords = overlapWords;
    }

    public static class Chunk {
        private final String defectId;
        private final String text;
        private final int chunkIndex;
        private final String sectionType;

        public Chunk(String defectId, String text, int chunkIndex, String sectionType) {
            this.defectId = defectId;
            this.text = text;
            this.chunkIndex = chunkIndex;
            this.sectionType = sectionType;
        }

        public String getDefectId() { return defectId; }
        public String getText() { return text; }
        public int getChunkIndex() { return chunkIndex; }
        public String sectionType() { return sectionType; }
    }

    public List<Chunk> chunkText(String defectId, String text, String sectionType) {
        List<Chunk> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        String[] words = text.split("\\s+");
        if (words.length <= chunkSizeWords) {
            chunks.add(new Chunk(defectId, text.trim(), 0, sectionType));
            return chunks;
        }

        int start = 0;
        int index = 0;
        while (start < words.length) {
            int end = Math.min(start + chunkSizeWords, words.length);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(words[i]);
            }
            chunks.add(new Chunk(defectId, sb.toString(), index++, sectionType));
            if (end == words.length) break;
            start += (chunkSizeWords - overlapWords);
        }

        return chunks;
    }
}
