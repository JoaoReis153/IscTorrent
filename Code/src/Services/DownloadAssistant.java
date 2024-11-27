package Services;

import FileSearch.FileSearchResult;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
            FileBlockRequestMessage request = taskManager.getDownloadRequest();
            if (request != null) {
                peerToRequestBlock.sendFileBlockMessageRequest(request);
                while (getRespectiveAnswerMessage(request) == null) {}
            }
        }
    }

    private FileBlockAnswerMessage getRespectiveAnswerMessage(
        FileBlockRequestMessage request
    ) {
        List<FileBlockAnswerMessage> answerList = taskManager.getAnswerList();
        for (FileBlockAnswerMessage answer : answerList) {
            if (answer.getBlockRequest().equals(request)) {
                return answer;
            }
        }
        return null;
    }
}
