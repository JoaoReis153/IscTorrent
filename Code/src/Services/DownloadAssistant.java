package Services;

import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.util.concurrent.CountDownLatch;

public class DownloadAssistant extends Thread {

    private final DownloadTasksManager taskManager;
    private CountDownLatch latch;
    private final int ID;
    private SubNode peerToRequestBlock;

    public DownloadAssistant(
        DownloadTasksManager taskManager,
        CountDownLatch latch,
        SubNode peerToRequestBlock,
        int ID
    ) {
        this.taskManager = taskManager;
        this.latch = latch;
        this.peerToRequestBlock = peerToRequestBlock;
        this.ID = ID;
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

        waitForAnswer(request);

    }

    private void waitForAnswer(FileBlockRequestMessage request) {
        while (true) {
            FileBlockAnswerMessage answer = taskManager.getRespectiveAnswerMessage(request);
            if (answer != null) 
                break; 

        }
    }

    
}
