package Services;

import Core.Node;
import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class DownloadAssistant extends Thread {

    private DownloadTasksManager taskManager;

    public DownloadAssistant(DownloadTasksManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<FileSearchResult> request =
                    taskManager.getDownloadRequest();
                if (request != null && !request.isEmpty()) {
                    ArrayList<SubNode> peersWithFile = new ArrayList<SubNode>();

                    for (SubNode peer : taskManager.getNode().getPeers()) {
                        String peerAddressPort =
                            peer.getSocket().getInetAddress().getHostAddress() +
                            "::" +
                            peer.getSocket().getPort();
                        for (FileSearchResult sr : request) {
                            String searchResultAddressPort =
                                sr.getAddress() + "::" + sr.getPort();
                            if (
                                peerAddressPort.equals(searchResultAddressPort)
                            ) {
                                peersWithFile.add(peer);
                                continue;
                            }
                        }
                    }

                    long startTime = System.currentTimeMillis();

                    FileSearchResult first = request.get(0);

                    String hash = first.getHash();

                    List<FileBlockRequestMessage> blockList =
                        FileBlockRequestMessage.createBlockList(
                            hash,
                            first.getFileSize()
                        );

                    CountDownLatch latch = new CountDownLatch(blockList.size());

                    int blockListSize = blockList.size();
                    while (blockList.size() > 0) {
                        FileBlockRequestMessage block = blockList.remove(0);
                        for (SubNode peer : taskManager.getNode().getPeers()) {
                            peer.setBlockAnswerLatch(latch);
                            peer.sendFileBlockRequestMessageRequest(block);
                        }
                    }
                    latch.await();

                    while (
                        taskManager.getDownloadProcess(hash).size() <
                        blockList.size()
                    ) {}

                    System.out.println(
                        "Received all blocks: " +
                                taskManager.getDownloadProcess(hash).size() ==
                            null
                            ? 0
                            : taskManager.getDownloadProcess(hash).size() +
                            " of " +
                            blockListSize
                    );

                    assembleAndWriteFile(
                        first.getFileName(),
                        taskManager.getDownloadProcess(hash)
                    );

                    long duration = System.currentTimeMillis() - startTime;

                    taskManager
                        .getNode()
                        .getGUI()
                        .showDownloadStats(hash, duration);
                }
            } catch (Exception e) {
                e.printStackTrace(); // This will print the full stack trace
                System.out.println("Error in DownloadAssistant: " + e);
            }
        }
    }

    private void assembleAndWriteFile(
        String fileName,
        Map<String, ArrayList<FileBlockAnswerMessage>> receivedBlockMap
    ) throws IOException {
        // Use a TreeMap to keep blocks ordered by offset
        TreeMap<Long, byte[]> fileParts = new TreeMap<>();
        List<FileBlockAnswerMessage> receivedBlocks = new ArrayList<
            FileBlockAnswerMessage
        >();
        for (List<FileBlockAnswerMessage> blocks : receivedBlockMap.values()) {
            receivedBlocks.addAll(blocks);
        }
        // Populate the TreeMap with blocks
        for (FileBlockAnswerMessage block : receivedBlocks) {
            if (block.getData() == null) {
                System.err.println(
                    "Warning: Block data is null for offset: " +
                    block.getOffset()
                );
                System.out.println("----------");
                System.err.println(block);
                return;
            }
            fileParts.put(block.getOffset(), block.getData());
        }

        String filePath =
            taskManager.getNode().getFolder().getAbsolutePath() +
            "/" +
            fileName;
        // Write the blocks to the file in the correct order
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            for (Map.Entry<Long, byte[]> entry : fileParts.entrySet()) {
                fileOut.write(entry.getValue());
            }
        }

        System.out.println("File written successfully to: " + filePath);
    }
}
