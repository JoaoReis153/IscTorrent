package Services;

import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.util.concurrent.CountDownLatch;

public class DownloadAssistant extends Thread {

    private final DownloadTasksManager taskManager;
    private SubNode peerToRequestBlock;
    private boolean timedOut = false;

    public DownloadAssistant(
        DownloadTasksManager taskManager,
        CountDownLatch latch,
        SubNode peerToRequestBlock,
        int ID
    ) {
        this.taskManager = taskManager;
        this.peerToRequestBlock = peerToRequestBlock;
    }

    @Override
    public void run() {
        while (!taskManager.finished() && !timedOut) {
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

        if (!waitForAnswer(request, 100000)) {
            taskManager.addDownloadRequest(request);
            taskManager.stopRunning();
            timedOut = true;
        }
    }

    private boolean waitForAnswer(
        FileBlockRequestMessage request,
        long timeoutMs
    ) {
        long endTime = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < endTime) {
            try {
                FileBlockAnswerMessage answer =
                    taskManager.getRespectiveAnswerMessage(request);
                if (answer != null) {
                    return true; // Answer received
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.out.println("Thread interrupted");
                return false; // Handle interruption as a failure
            }
        }
        return false; // Timeout occurred
    }
}
