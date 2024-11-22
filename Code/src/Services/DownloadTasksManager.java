package Services;

import Core.Node;
import FileSearch.FileSearchResult;
import Messaging.FileBlockRequestMessage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DownloadTasksManager {

    private List<DownloadAssistant> assistants = new LinkedList<>();
    private final int DEFAULT_NUMBER_THREADS = 5;
    private List<List<FileSearchResult>> downloadRequests = new LinkedList<>();
    private Node node;
    private ThreadPoolExecutor threadPool;

    public DownloadTasksManager(Node node, int numThreads) {
        this.node = node;
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            numThreads
        );
        for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) threadPool.execute(
            new DownloadAssistant(this)
        );
    }

    public synchronized void downloadFile(
        List<FileSearchResult> searchResults
    ) {
        downloadRequests.add(searchResults);
    }

    public List<DownloadAssistant> getAssistants() {
        return assistants;
    }

    public int getDEFAULT_NUMBER_THREADS() {
        return DEFAULT_NUMBER_THREADS;
    }

    public synchronized List<FileSearchResult> getDownloadRequest() {
        if (downloadRequests.isEmpty()) return null;
        return downloadRequests.removeFirst();
    }

    public Node getNode() {
        return node;
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    public synchronized void stopServer() {}
}
