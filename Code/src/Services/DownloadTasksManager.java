package Services;

import Core.Node;
import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadTasksManager extends Thread {

    private final int DEFAULT_NUMBER_THREADS = 5;

    private Node node;
    private FileSearchResult example;
    private List<FileSearchResult> requests;
    private ExecutorService threadPool;
    private CountDownLatch latch;
    private List<FileBlockRequestMessage> requestList; 
    private List<FileBlockAnswerMessage> answerList;

    private Lock lock = new ReentrantLock();
    private Condition condition  = lock.newCondition();




    public DownloadTasksManager(Node node, List<FileSearchResult> requests) {
        this.node = node;
        this.requests = requests;
        this.example = requests.getFirst();
        
        System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" +  "Download task manager created for file " + example.getHash());
        this.answerList = new ArrayList<>();

        this.requestList = FileBlockRequestMessage.createBlockList(
            example.getHash(),
            example.getFileSize()
            );
        System.out.println("Created " + requestList.size() + " blocks");
        this.threadPool = Executors.newFixedThreadPool(DEFAULT_NUMBER_THREADS);
    }

    @Override
    public void run() {
            try {
                processDownload();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(node.getAddressAndPortFormated() + "[taskmanager]" + "Error in DownloadAssistant: " + e);
            }
    }

    private void processDownload() {
        ArrayList<SubNode> peers = getNodesWithFile();
        latch = new CountDownLatch(requestList.size());
        for (int i=0; i<peers.size(); i++) {
            
            DownloadAssistant assistant = new DownloadAssistant(
                this,
                latch,
                peers.get(i),
                i
            );
            threadPool.submit(assistant);
            System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" + "Submitted " + (i + 1) + "º assistant");
        }

        try {
            System.out.println("Got here");
            latch.await();
            System.out.println("Passed the latch");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" + "All assistants finished");

        assembleAndWriteFile(example.getFileName(), answerList);
    }

    public List<FileBlockRequestMessage> getDownloadRequestList() {
        return requestList;
    }

    public boolean finished() {
        return requestList.isEmpty();
    }

    public FileBlockRequestMessage getDownloadRequest() {
        lock.lock();
        try {
            FileBlockRequestMessage request = !requestList.isEmpty() ? requestList.removeFirst() : null; 
            return request;
        } finally {
            lock.unlock();
        }

    }


    public void addDownloadAnswer(
        FileBlockAnswerMessage answer
    ) {
        lock.lock();
        try {
            if(!answerList.contains(answer)) {
                answerList.add(answer);
                latch.countDown();
                condition.signalAll();
            } 
        } finally {
            lock.unlock();
        }
    
    }

    

    public FileBlockAnswerMessage getRespectiveAnswerMessage(
        FileBlockRequestMessage request
    ) {
        lock.lock();
        try {
            if(answerList.isEmpty()) return null;
            for (FileBlockAnswerMessage answer : answerList) {
    
                if (answer.getBlockRequest().equals(request)) {
                    return answer;
                }}    
            return null;
        } finally {
            lock.unlock();
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


    
    private void assembleAndWriteFile(
        String fileName,
        List<FileBlockAnswerMessage> receivedBlockMap
    )  {
        TreeMap<Long, byte[]> fileParts = collectFileParts(receivedBlockMap);
        if (fileParts.isEmpty()) return;
        String filePath = buildFilePath(fileName);
        writeFileToDisc(filePath, fileParts);
        verifyFileCreation(filePath);
    }

    private TreeMap<Long, byte[]> collectFileParts(
        List<FileBlockAnswerMessage> receivedBlockMap
    ) {
        TreeMap<Long, byte[]> fileParts = new TreeMap<>();


        for (FileBlockAnswerMessage block : receivedBlockMap) {
            if (block.getData() == null) {
                System.err.println(
                    "Warning: Block data is null for offset: " +
                    block.getOffset()
                );
                return new TreeMap<>();
            }
            fileParts.put(block.getOffset(), block.getData());
        }

        return fileParts;
    }


      private void writeFileToDisc(
        String filePath,
        TreeMap<Long, byte[]> fileParts
    )  {
        byte[] combinedData = combineFileParts(fileParts);

        try {
            Files.write(Paths.get(filePath), combinedData);
        } catch (IOException e) {
            System.out.println("Error writing file: " + filePath);
            e.printStackTrace();
        }
    }

    private byte[] combineFileParts(TreeMap<Long, byte[]> fileParts) {
        int totalSize = 0;
        for (byte[] bytes : fileParts.values()) {
            totalSize += bytes.length;
        }

        byte[] combinedData = new byte[totalSize];
        int position = 0;

        for (byte[] part : fileParts.values()) {
            System.arraycopy(part, 0, combinedData, position, part.length);
            position += part.length;
        }

        return combinedData;
    }

    private void verifyFileCreation(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            System.err.println("Error: File was not created at: " + filePath);
            return;
        }
        System.out.println(
            getNode().getAddressAndPortFormated() +
            "[taskmanager]"  +
            "Downloaded: " +
            filePath
        );
    }

    private String buildFilePath(String fileName) {
        return (
            getNode().getFolder().getAbsolutePath() + "/" + fileName
        );
    }
}
