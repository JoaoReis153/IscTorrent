package Services;

import Core.Node;
import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DownloadTasksManager extends Thread {

    private Node node;
    private FileSearchResult example;
    private List<FileSearchResult> requests;
    private ExecutorService threadPool;
    private CountDownLatch latch;
    private List<FileBlockRequestMessage> requestList; 
    private List<FileBlockAnswerMessage> answerList;
    private Map<String, Integer> numberOfDownloadsForPeer;
    private ArrayList<SubNode> peersWithFile;


    public DownloadTasksManager(Node node, List<FileSearchResult> requests) {
        this.node = node;
        this.requests = requests;
        this.example = requests.getFirst();
        
        System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" +  "Download task manager created for file " + example.getHash());
        this.answerList = new ArrayList<>();
        this.numberOfDownloadsForPeer = new HashMap<>();
        this.requestList = FileBlockRequestMessage.createBlockList(
            example.getHash(),
            example.getFileSize()
            );
            this.peersWithFile =  getNodesWithFile();
        this.threadPool = Executors.newFixedThreadPool(peersWithFile.size());
        System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" + " "+ requestList.size() + " blocks to process");
    }
    
    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            processDownload();
            long duration = System.currentTimeMillis() - start;
            node.getGUI().showDownloadStats(example.getHash(), duration);
            System.out.println( node.getAddressAndPortFormated() + "[taskmanager]" + "Download finished for file " + example.getHash() + " at a rate of " + (example.getFileSize() / duration) + " bytes/s");
            node.removeDownloadProcess(example.getHash());
            node.getGUI().reloadListModel();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(node.getAddressAndPortFormated() + "[taskmanager]" + "Error in DownloadAssistant: " + e);
        }
    }
    
    private void processDownload() {
        
        
        latch = new CountDownLatch(requestList.size());
        for (int i=0; i<peersWithFile.size(); i++) {
            
            DownloadAssistant assistant = new DownloadAssistant(
                this,
                latch,
                peersWithFile.get(i),
                i
            );
            threadPool.submit(assistant);
            System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" + "Submitted " + (i + 1) + "º assistant");
        }

        try {

            latch.await();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        System.out.println(node.getAddressAndPortFormated() + "[taskmanager]" + "All assistants finished");

        assembleAndWriteFile(example.getFileName(), answerList);
    }

    public void addNumberOfDownloadsForPeer(String peer, int number) {
        if(numberOfDownloadsForPeer.containsKey(peer)) {
            numberOfDownloadsForPeer.put(peer, numberOfDownloadsForPeer.get(peer) + number);
        } else {
            numberOfDownloadsForPeer.put(peer, number);
        }
    }

    public List<FileBlockRequestMessage> getDownloadRequestList() {
        return requestList;
    }

    public boolean finished() {
        return requestList.isEmpty();
    }

    public synchronized FileBlockRequestMessage getDownloadRequest() throws InterruptedException {
        
        while(requestList.isEmpty()) wait();
            
        FileBlockRequestMessage request = requestList.removeFirst();  
        return request;

    }
    
    public Map<String, Integer> getDownloadProcess() {
        return numberOfDownloadsForPeer;
    }

    public synchronized void addDownloadAnswer(
        FileBlockAnswerMessage answer
    ) {
        if(!answerList.contains(answer)) {
            answerList.add(answer);
            latch.countDown();
            notifyAll();
        } 
    }

    

    public synchronized FileBlockAnswerMessage getRespectiveAnswerMessage(
        FileBlockRequestMessage request
    ) {
        if(answerList.isEmpty()) return null;
        for (FileBlockAnswerMessage answer : answerList) {

            if (answer.getBlockRequest().equals(request)) {
                return answer;
            }}    
        return null;
        
        
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
    }

    private String buildFilePath(String fileName) {
        return (
            getNode().getFolder().getAbsolutePath() + "/" + fileName
        );
    }
}
