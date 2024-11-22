package Services;

import Core.Node;
import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DownloadTasksManager {

    private List<DownloadAssistant> assistants = new LinkedList<>();
    private final int DEFAULT_NUMBER_THREADS = 5;
    private Map<String, ArrayList<FileBlockAnswerMessage>> downloadMap;
    private List<List<FileSearchResult>> downloadRequests;
    private Node node;
    private ThreadPoolExecutor threadPool;

    public DownloadTasksManager(Node node, int numThreads) {
        this.node = node;
        this.downloadMap = new HashMap<
            String,
            ArrayList<FileBlockAnswerMessage>
        >();
        this.downloadRequests = new LinkedList<>();
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            numThreads
        );
        for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) threadPool.execute(
            new DownloadAssistant(this)
        );
    }

    public synchronized ArrayList<FileBlockAnswerMessage> getDownloadProcess(
        String hash
    ) {
        ArrayList<FileBlockAnswerMessage> answers = downloadMap.get(hash);
        return answers;
    }

    public synchronized void addDownloadProcess(
        String hash,
        FileBlockAnswerMessage answer
    ) {
        ArrayList<FileBlockAnswerMessage> answers = downloadMap.get(hash);
        if (answers == null) {
            answers = new ArrayList<FileBlockAnswerMessage>();
            downloadMap.put(hash, answers);
        }
        answers.add(answer);
    }

    public synchronized void addDownloadRequest(
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
