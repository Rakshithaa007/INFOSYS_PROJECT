package com.bugdiagnosis.knowledgebase;

import com.bugdiagnosis.model.HistoricalDefect;
import com.bugdiagnosis.model.Priority;
import com.bugdiagnosis.model.Severity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeedDatasetLoader {

    public static List<HistoricalDefect> loadSeededDefects() {
        List<HistoricalDefect> list = new ArrayList<>();

        // 1. Apache Tomcat Deadlock
        list.add(new HistoricalDefect(
                "APACHE-TOMCAT-8812",
                "Apache",
                "Tomcat NioEndpoint acceptor thread deadlocks under high concurrent keep-alive load",
                "Under intense concurrent HTTP keep-alive connection bursts, NioEndpoint$Acceptor deadlocks acquiring the server socket channel lock while worker threads wait on poller event queue.",
                "java.lang.IllegalStateException: Poller event queue saturated\n\tat org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:782)\n\tat org.apache.tomcat.util.net.NioEndpoint$Acceptor.run(NioEndpoint.java:450)",
                "Tomcat Connector / NIO",
                Severity.CRITICAL,
                Priority.P1_CRITICAL,
                "Acceptor thread held the endpoint synchronization lock while attempting an offer on the bounded poller queue, resulting in lock inversion.",
                "Decouple poller queue submission with non-blocking offer and exponential backoff retry. Release endpoint lock before polling events.",
                "--- a/java/org/apache/tomcat/util/net/NioEndpoint.java\n+++ b/java/org/apache/tomcat/util/net/NioEndpoint.java\n@@ -450,4 +450,6 @@ public class NioEndpoint {\n-   synchronized (this) {\n-       poller.add(socketChannel);\n-   }\n+   if (!poller.add(socketChannel, 50, TimeUnit.MILLISECONDS)) {\n+       log.warn(\"Poller queue full, throttling backoff\");\n+   }",
                Arrays.asList("apache", "tomcat", "deadlock", "concurrency", "nio")
        ));

        // 2. Apache Hadoop OOM
        list.add(new HistoricalDefect(
                "APACHE-HADOOP-14205",
                "Apache",
                "HDFS NameNode throws OutOfMemoryError heap space during simultaneous block reports",
                "When hundreds of DataNodes report blocks after a cluster restart, NameNode FSNamesystem allocates excessive temporary block metadata in memory, exhausting JVM old generation heap.",
                "java.lang.OutOfMemoryError: Java heap space\n\tat java.base/java.util.Arrays.copyOf(Arrays.java:3745)\n\tat org.apache.hadoop.hdfs.server.blockmanagement.BlockManager.processReport(BlockManager.java:1890)",
                "HDFS Distributed Storage",
                Severity.CRITICAL,
                Priority.P1_CRITICAL,
                "BlockManager accumulated raw uncompressed RPC payload in single heap array instead of streaming block batches with memory backpressure.",
                "Introduce batched incremental block reporting and set maxBlockReportSize limit per RPC invocation.",
                "--- a/hadoop-hdfs/BlockManager.java\n+++ b/hadoop-hdfs/BlockManager.java\n@@ -1890,3 +1890,5 @@ public class BlockManager {\n-   BlockList list = parseFullReport(report);\n+   try (BlockReportStream stream = report.openStream()) {\n+       while (stream.hasNextChunk()) processBlocks(stream.nextChunk());\n+   }",
                Arrays.asList("apache", "hadoop", "hdfs", "outofmemoryerror", "oom")
        ));

        // 3. Mozilla Firefox DOM SIGSEGV Crash
        list.add(new HistoricalDefect(
                "MOZILLA-BUG-1489210",
                "Mozilla",
                "Firefox crashes with SIGSEGV in PresShell::Destroy on fast tab closure with ongoing WebGL context",
                "Closing a browser tab while a WebGL frame render loop is active triggers a use-after-free in nsPresContext because document observer dereferences destroyed PresShell.",
                "Crash [@ PresShell::Destroy]\n0  libxul.so!PresShell::Destroy [PresShell.cpp:920]\n1  libxul.so!DocumentViewerImpl::Destroy [DocumentViewerImpl.cpp:412]",
                "Gecko DOM & Rendering",
                Severity.CRITICAL,
                Priority.P1_CRITICAL,
                "WebGL canvas context failed to detach its frame tick callback before PresShell tear-down, dereferencing freed memory.",
                "Null-check PresShell pointer and explicitly cancel RAF callbacks in WebGLContext::Cleanup before destroying PresShell.",
                "--- a/layout/base/PresShell.cpp\n+++ b/layout/base/PresShell.cpp\n@@ -920,2 +920,5 @@ void PresShell::Destroy() {\n+   if (mDestroying) return;\n+   mDestroying = true;\n+   NotifyRenderingContextTeardown();\n-   delete mPresContext;\n",
                Arrays.asList("mozilla", "firefox", "sigsegv", "crash", "dom")
        ));

        // 4. Eclipse JDT ASTParser NPE
        list.add(new HistoricalDefect(
                "ECLIPSE-JDT-512039",
                "Eclipse",
                "ASTParser throws NullPointerException when parsing Java 17 sealed interfaces with wildcard generics",
                "Editing Java source files containing generic wildcard sealed permitted types causes Eclipse Java Language Server and JDT indexer to crash with NullPointerException.",
                "java.lang.NullPointerException: Cannot invoke \"binding.isInterface()\" because \"binding\" is null\n\tat org.eclipse.jdt.core.dom.ASTConverter.convertType(ASTConverter.java:3125)\n\tat org.eclipse.jdt.core.dom.ASTParser.createAST(ASTParser.java:820)",
                "Eclipse JDT / Compiler AST",
                Severity.HIGH,
                Priority.P2_HIGH,
                "ASTConverter assumed ReferenceBinding was always resolved before visiting permitted subclass list, which is false during incremental compilation.",
                "Add null-guard for binding resolution in ASTConverter.convertType and return fallback RecoveredTypeBinding.",
                "--- a/org.eclipse.jdt.core/dom/ASTConverter.java\n+++ b/org.eclipse.jdt.core/dom/ASTConverter.java\n@@ -3125,2 +3125,5 @@ public class ASTConverter {\n-   if (binding.isInterface()) {\n+   if (binding == null) return this.ast.newSimpleType(this.ast.newSimpleName(\"Unknown\"));\n+   if (binding.isInterface()) {\n",
                Arrays.asList("eclipse", "jdt", "astparser", "nullpointerexception", "npe")
        ));

        return list;
    }
}