package Services;

import FileSearch.FileSearchResult;
import Messaging.FileBlockRequestMessage;
import java.util.ArrayList;
import java.util.List;
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

                    FileSearchResult first = request.get(0);
                    List<FileBlockRequestMessage> blockList =
                        FileBlockRequestMessage.createBlockList(
                            first.getHash(),
                            first.getFileSize()
                        );

                    CountDownLatch latch = new CountDownLatch(blockList.size());

                    while (blockList.size() > 0) {
                        FileBlockRequestMessage block = blockList.remove(0);
                        for (SubNode peer : taskManager.getNode().getPeers()) {
                            peer.setBlockAnswerLatch(latch);
                            peer.sendFileBlockRequestMessageRequest(block);
                        }
                    }

                    latch.await();

                    System.out.println("Received all blocks");
                }
            } catch (Exception e) {
                e.printStackTrace(); // This will print the full stack trace
                System.out.println("Error in DownloadAssistant: " + e);
            }
        }
    }
}
