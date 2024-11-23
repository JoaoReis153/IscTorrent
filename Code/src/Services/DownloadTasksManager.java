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
    private List<List<FileSearchResult>> downloadRequestsOnWait;
    private List<FileSearchResult> requestsBeingProcessed;
    private Node node;
    private ThreadPoolExecutor threadPool;

    public DownloadTasksManager(Node node, int numThreads) {
        this.node = node;
        this.downloadMap = new HashMap<
            Integer,
            HashMap<String, ArrayList<FileBlockAnswerMessage>>
        >();
        this.downloadRequestsOnWait = new LinkedList<>();
        this.requestsBeingProcessed = new LinkedList<>();
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
            numThreads
        );
        for (int i = 0; i < DEFAULT_NUMBER_THREADS; i++) threadPool.execute(
            new DownloadAssistant(this, i)
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

    public int getDownloadProcessSize(int hash) {
        HashMap<String, ArrayList<FileBlockAnswerMessage>> answers =
            downloadMap.get(hash);

        if (answers == null) return 0;

        int total = 0;

        for (ArrayList<FileBlockAnswerMessage> answerBlock : answers.values()) {
            total += answerBlock.size();
        }

        return total;
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
        if (searchResults.isEmpty()) return;
        FileSearchResult request = searchResults.get(0);
        if (requestsBeingProcessed.contains(request)) {
            System.out.println(
                node.getAddressAndPortFormated() +
                "Already processing the doownload of: " +
                request
            );
            return;
        }

        requestsBeingProcessed.add(request);
        downloadRequestsOnWait.add(searchResults);

        System.out.println(
            node.getAddressAndPortFormated() +
            "Download waiting for assistant: " +
            downloadRequestsOnWait.size()
        );
        notifyAll();
    }

    public void removeDownloadBeingProcessed(FileSearchResult request) {
        requestsBeingProcessed.remove(request);
    }

    public List<DownloadAssistant> getAssistants() {
        return assistants;
    }

    public int getDEFAULT_NUMBER_THREADS() {
        return DEFAULT_NUMBER_THREADS;
    }

    public synchronized List<FileSearchResult> getDownloadRequest() {
        try {
            while (downloadRequestsOnWait.isEmpty()) wait();
        } catch (InterruptedException e) {}
        return downloadRequestsOnWait.removeFirst();
    }

    public Node getNode() {
        return node;
    }

    public ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
}
