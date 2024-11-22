package Services;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;
import GUI.GUI;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import Messaging.NewConnectionRequest;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class SubNode extends Thread {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int originalBeforeOSchangePort;
    private Socket socket;
    private Node node;
    private GUI gui;
    private DownloadTasksManager downloadManager;
    private boolean userCreated;
    private boolean running = true;
    private CountDownLatch blockAnswerLatch;

    // Constructor
    public SubNode(
        Node node,
        DownloadTasksManager downloadManager,
        Socket socket,
        GUI gui,
        boolean userCreated
    ) {
        this.node = node;
        this.downloadManager = downloadManager;
        this.socket = socket;
        this.gui = gui;
        this.userCreated = userCreated;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            Object obj;
            while (running && (obj = in.readObject()) != null) {
                // Handle New Connection Request
                if (obj instanceof NewConnectionRequest) {
                    this.originalBeforeOSchangePort =
                        ((NewConnectionRequest) obj).getClientPort();
                    System.out.println(
                            "Added new node::NodeAddress [address=" +
                            socket.getInetAddress().getHostAddress() +
                            " port=" +
                            originalBeforeOSchangePort +
                            "]"
                        );
                    // Handle Word Search Message
                } else if (obj instanceof WordSearchMessage) {
                    System.out.println(
                        "Received WordSearchMessage with content: (" +
                        ((WordSearchMessage) obj).getKeyword() +
                        ")"
                    );
                    if (
                        node.getFolder().exists() &&
                        node.getFolder().isDirectory()
                    ) {
                        sendFileSearchResultList((WordSearchMessage) obj);
                    }
                    // Handle File Search Result List
                } else if (obj instanceof FileSearchResult[]) {
                    FileSearchResult[] searchResultList =
                        (FileSearchResult[]) obj;
                    if (gui == null) {
                        System.out.println("There was a problem with the GUI");
                        System.exit(1);
                    }
                    gui.loadListModel(searchResultList);
                    // Handle File Block Request
                } else if (obj instanceof FileBlockRequestMessage) {
                    System.out.println(
                        "Received FileBlockRequestMessage: " + obj
                    );

                    FileBlockRequestMessage request =
                        (FileBlockRequestMessage) obj;
                    FileBlockAnswerMessage answer = new FileBlockAnswerMessage(
                       node.getId(), request.getHash(),
                        request.getOffset(),
                        request.getLength()
                    );

                    out.writeObject(answer);
                    out.flush();
                } else if (obj instanceof FileBlockAnswerMessage) {
                    System.out.println(
                        "Received FileBlockAnswerMessage: " + obj
                    );
                    FileBlockAnswerMessage answer =
                        (FileBlockAnswerMessage) obj;
                    downloadManager.addDownloadProcess(
                        answer.getHash(),
                        answer
                    );
                    if (blockAnswerLatch != null) blockAnswerLatch.countDown();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    public void close() {
        running = false;
        closeResources();
        System.out.println(
            "Thread closed for SubNode at " +
            socket.getInetAddress() +
            ":" +
            socket.getPort()
        );
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }

    public void sendFileBlockRequestMessageRequest(
        FileBlockRequestMessage block
    ) {
        if (out != null && !socket.isClosed()) {
            try {
                System.out.println("Sent FileBlockRequestMessage: " + block);
                out.writeObject(block);
                out.flush();
            } catch (IOException e) {
                System.out.println("Error sending FileBlockRequestMessage");
                e.printStackTrace();
            }
        } else {
            System.out.println(
                "OutputStream is null [invalid port: " + socket.getPort() + "]"
            );
        }
    }

    public void setBlockAnswerLatch(CountDownLatch latch) {
        this.blockAnswerLatch = latch;
    }

    // Send Word Search Request to peer
    public void sendWordSearchMessageRequest(String keyword) {
        WordSearchMessage searchPackage = new WordSearchMessage(keyword);
        if (out != null && !socket.isClosed()) {
            try {
                System.out.println(
                    "Sent WordSearchMessageRequest with keyword: " + keyword
                );
                out.writeObject(searchPackage);
                out.flush();
            } catch (IOException e) {
                System.out.println("Error sending WordSearchMessageRequest");
                e.printStackTrace();
            }
        }
    }

    // Send File Search Result List
    public void sendFileSearchResultList(WordSearchMessage obj) {
        File[] files = node.getFolder().listFiles();
        if (files != null) {
            String keyword = obj.getKeyword().toLowerCase();
            int keywordCount = 0;

            // Count matching files
            for (File file : files) {
                if (file.getName().toLowerCase().contains(keyword)) {
                    keywordCount++;
                }
            }

            if (keywordCount == 0) return;

            FileSearchResult[] results = new FileSearchResult[keywordCount];
            int counter = 0;

            // Create FileSearchResult objects
            for (File file : files) {
                String hash = Utils.generateSHA256(file.getAbsolutePath());
                if (file.getName().toLowerCase().contains(keyword)) {
                    results[counter++] = new FileSearchResult(
                        obj,
                        file.getName(),
                        hash,
                        file.length(),
                        node.getEnderecoIP(),
                        node.getPort()
                    );
                }
            }

            // Send results
            try {
                this.out.writeObject(results);
                this.out.flush();
            } catch (IOException e) {
                System.out.println("Error sending FileSearchResults");
            }
        }
    }

    // Send New Connection Request
    public void sendNewConnectionRequest(InetAddress endereco, int thisPort) {
        if (out == null) {
            System.out.println(
                "OutputStream is null [invalid port: " + thisPort + "]"
            );
            return;
        }

        NewConnectionRequest request = new NewConnectionRequest(
            endereco,
            thisPort
        );
        try {
            out.writeObject(request);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending NewConnectionRequest");
        }

        System.out.println(
            "Added new node::NodeAddress [address=" +
            endereco.getHostAddress() +
            " port=" +
            socket.getPort() +
            "]"
        );
    }

    public int getOriginalBeforeOSchangePort() {
        return originalBeforeOSchangePort;
    }

    @Override
    public String toString() {
        return (
            "SubNode [originalBeforeOSchangePort=" +
            originalBeforeOSchangePort +
            ", socket=" +
            socket +
            ", node=" +
            node +
            ", gui=" +
            gui +
            ", userCreated=" +
            userCreated +
            ", running=" +
            running +
            "]"
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SubNode subNode = (SubNode) obj;
        return (
            userCreated &&
            this.socket.getPort() == subNode.socket.getPort() &&
            socket.getInetAddress().equals(subNode.getSocket().getInetAddress())
        );
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public int hashCode() {
        if (userCreated) return Objects.hash(
            socket.getInetAddress(),
            socket.getPort()
        );
        return super.hashCode();
    }
}
