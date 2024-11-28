package Services;

import Core.Node;
import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadTasksManager extends Thread {

    private final int DEFAULT_NUMBER_THREADS = 5;

    private Node node;
    private FileSearchResult example;
    private List<FileSearchResult> requests;
    private ExecutorService threadPool;
    private CountDownLatch latch;
    private List<FileBlockRequestMessage> requestList;
    private List<FileBlockAnswerMessage> answerList;

    public DownloadTasksManager(Node node, List<FileSearchResult> requests) {
        this.node = node;
        this.requests = requests;
        this.example = requests.getFirst();
        System.out.println("Download task manager created for file " + example.getHash());
        this.requestList = FileBlockRequestMessage.createBlockList(
            example.getHash(),
            example.getFileSize()
            );
            this.threadPool = Executors.newFixedThreadPool(DEFAULT_NUMBER_THREADS);
    }

    @Override
    public void run() {
        while (true) {
            try {
                processDownload();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in DownloadAssistant: " + e);
            }
        }
    }

    private void processDownload() {
        
        for (SubNode peer : getNodesWithFile()) {
            
            DownloadAssistant assistant = new DownloadAssistant(
                this,
                latch,
                peer,
                1
            );
            threadPool.submit(assistant);
        }
    }

    public synchronized boolean finished() {
        return requestList.isEmpty();
    }

    public synchronized FileBlockRequestMessage getDownloadRequest() {
        return requestList.removeFirst();
    }

    public synchronized void addDownloadAnswer(
        InetAddress address,
        int port,
        FileBlockAnswerMessage answer
    ) {
        if (requestList.contains(answer.getBlockRequest())) {
            System.out.println(
                node.getAddressAndPortFormated() +
                "[" +
                this +
                "]" +
                "Received answer for block request: " +
                answer.getBlockRequest()
            );
            requestList.remove(answer.getBlockRequest());
            answerList.add(answer);
        }
    }

    private ArrayList<SubNode> getNodesWithFile() {
        ArrayList<SubNode> nodesWithFile = new ArrayList<>();
        for (FileSearchResult request : requests) {
            for (SubNode peer : node.getPeers()) {
                if (
                    peer.hasConnectionWith(
                        request.getAddress(),
                        request.getPort()
                    )
                ) {
                    nodesWithFile.add(peer);
                }
            }
        }
        return nodesWithFile;
    }

    public List<FileBlockAnswerMessage> getAnswerList() {
        return answerList;
    }

    public Node getNode() {
        return node;
    }
}
