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
    private Map<
        Integer,
        HashMap<String, ArrayList<FileBlockAnswerMessage>>
    > downloadMap;
    private List<List<FileSearchResult>> downloadRequests;
    private Node node;
    private ThreadPoolExecutor threadPool;

    public DownloadTasksManager(Node node, int numThreads) {
        this.node = node;
        this.downloadMap = new HashMap<
            Integer,
            HashMap<String, ArrayList<FileBlockAnswerMessage>>
        >();
        this.downloadRequests = new LinkedList<>();
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            numThreads
        );
        for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) threadPool.execute(
            new DownloadAssistant(this)
        );
    }

    public synchronized Map<
        String,
        ArrayList<FileBlockAnswerMessage>
    > getDownloadProcess(int hash) {
        HashMap<String, ArrayList<FileBlockAnswerMessage>> answers =
            downloadMap.get(hash);

        if (answers == null) return new HashMap<
            String,
            ArrayList<FileBlockAnswerMessage>
        >();
        return answers;
    }

    public synchronized void addDownloadProcess(
        int hash,
        String address,
        int port,
        FileBlockAnswerMessage answer
    ) {
        HashMap<String, ArrayList<FileBlockAnswerMessage>> fileMap =
            downloadMap.get(hash);
        if (fileMap == null) {
            fileMap = new HashMap<String, ArrayList<FileBlockAnswerMessage>>();
            downloadMap.put(hash, fileMap);
        }
        String key = address + "::" + port;
        ArrayList<FileBlockAnswerMessage> answers = fileMap.get(key);
        if (answers == null) {
            answers = new ArrayList<FileBlockAnswerMessage>();
            fileMap.put(key, answers);
        }
        answers.add(answer);
    }

    public synchronized void addDownloadRequest(
        List<FileSearchResult> searchResults
    ) {
        System.out.println(
            node.getAddressAndPortFormated() +
            "Adding Download Requests: " +
            searchResults.size()
        );
        downloadRequests.add(searchResults);
        System.out.println(
            node.getAddressAndPortFormated() +
            "Download Requests: " +
            downloadRequests.size()
        );
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
