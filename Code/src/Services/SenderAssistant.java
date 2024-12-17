package Services;

import Core.Node;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.File;

public class SenderAssistant extends Thread {

    private final Node node;

    public SenderAssistant(Node node) {
        this.node = node;
    }

    public void run() {
        while (true) {
            try {
                processBlockRequest();
            } catch (InterruptedException e) {
                System.out.println(node.getAddressAndPortFormated() + " Error in SenderAssistant");
                e.printStackTrace();
            }
        }
    }


    /*
     * Processes a block request message.
     * 
     * It takes a block request message from the blocksToProcess list in  the node
     * and sends a block answer message to the peer that sent the request.
     */
    public void processBlockRequest() throws InterruptedException {
        FileBlockRequestMessage request;

        request = this.node.getBlockRequest();
        if(request == null) return;
        FileBlockAnswerMessage answer = fillRequest(request);
        if (answer == null) return;
        if (request.getSenderAddress() == null) {
            System.out.println(node.getAddressAndPortFormated() + " SenderAssistant: Null address");
            return;
        } else if (request.getSenderPort() == 0) {
            System.out.println(node.getAddressAndPortFormated() + " SenderAssistant: Null port");
            return;
        }

        node
            .getPeerToSend(request.getSenderAddress(), request.getSenderPort())
            .sendFileBlockAnswer(answer);
    }


    /*
     * Fills a block request message with the file block requested
     * 
     * It takes a block request message and searches for the file with the same hash as the request
     * 
     * If the file block is found, it returns a block answer message with the
     * file block requested.
     * 
     * If the file block is not found, it returns null.
     */ 
    public FileBlockAnswerMessage fillRequest(FileBlockRequestMessage request) {            
        int hash = request.getHash();
        File file = findFileByHash(hash);
        if (file == null) return null;
        return new FileBlockAnswerMessage(
            node.getAddress().getHostAddress(),
            node.getPort(),
            node.getId(),
            request,
            file
        );
    }

    /*
     * Finds a file with the same hash as the one requested
     * 
     * It takes a hash and searches for the file with the same hash as the hash
     * 
     * If the file is found, it returns the file
     * 
     * If the file is not found, it returns null.
     */ 
    private File findFileByHash(int hash) {
        File folder = new File(Node.WORK_FOLDER + node.getId() + "/");
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException(
                "Invalid directory path: " + folder.getPath()
            );
        }

        File[] files = folder.listFiles();
        if (files == null) {
            throw new IllegalArgumentException(
                "Unable to list files in directory"
            );
        }

        for (File file : files) {
            if (file.isFile()) {
                if (node.getHash(file.getAbsolutePath()) == hash) {
                    return file;
                }
            }
        }
        return null;
    }
}
