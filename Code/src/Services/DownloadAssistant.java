package Services;

import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.util.concurrent.CountDownLatch;

public class DownloadAssistant extends Thread {

    private final DownloadTasksManager taskManager;
    private SubNode peerToRequestBlock;

    public DownloadAssistant(
        DownloadTasksManager taskManager,
        CountDownLatch latch,
        SubNode peerToRequestBlock,
        int ID
    ) {
        this.taskManager = taskManager;
        this.peerToRequestBlock = peerToRequestBlock;
    }

    public void run() {
        while (!taskManager.finished()) {
            
            FileBlockRequestMessage request;
            try {
                request = taskManager.getDownloadRequest();
                if (request != null) {
                    handleRequest(request);
                }
            } catch (InterruptedException e) {
                System.out.println("Error in DownloadAssistant");
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(FileBlockRequestMessage request) {

        peerToRequestBlock.sendFileBlockRequest(request);

        if(!waitForAnswer(request, 300))
            taskManager.addDownloadRequest(request);
    }

    private boolean waitForAnswer(FileBlockRequestMessage request, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                FileBlockAnswerMessage answer = taskManager.getRespectiveAnswerMessage(request);
                if (answer != null) 
                    return true;
                if (System.currentTimeMillis() - start > timeoutMs) {
                    return false; // Timeout occurred
                }
                Thread.sleep(10); // Prevent tight looping
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    
}