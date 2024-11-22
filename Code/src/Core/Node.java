package Core;

import FileSearch.FileSearchResult;
import GUI.GUI;
import Services.DownloadTasksManager;
import Services.SubNode;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {

    private static final String WORK_FOLDER = "Code/dl";
    private static final int BASE_PORT = 8080;
    private static final int MAX_PORT = 65535;

    private final int nodeId;
    private final int port;
    private final InetAddress address;
    private final File folder;
    private final Set<SubNode> peers;
    private final DownloadTasksManager downloadManager;
    private final GUI gui;

    // Constructor
    public Node(int nodeId, GUI gui) throws IllegalArgumentException {
        this.nodeId = nodeId;
        this.port = BASE_PORT + nodeId;
        this.gui = gui;
        this.peers = new HashSet<>();
        this.downloadManager = new DownloadTasksManager(this, 5);

        validatePort();
        this.folder = createWorkingDirectory();
        this.address = initializeAddress();
    }

    private void validatePort() {
        if (Utils.isValidPort(port)) {
            throw new IllegalArgumentException(
                "Invalid node ID. Input a valid number (0-41070)"
            );
        }
    }

    private File createWorkingDirectory() {
        File folder = new File(WORK_FOLDER + nodeId);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException(
                "Failed to create directory: dl" + nodeId
            );
        }
        return folder;
    }

    private InetAddress initializeAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to get the device's address", e);
        }
    }

    // Getter for the working folder
    public File getFolder() {
        return folder;
    }

    // Start server to accept connections
    public void startServing() {
        System.out.println("Awaiting connection...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();

                SubNode clientHandler = new SubNode(
                    this,
                    downloadManager,
                    clientSocket,
                    gui,
                    true
                );
                clientHandler.start();

                peers.add(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }

    // Broadcast a word search message to all peers
    public void broadcastWordSearchMessageRequest(String keyword) {
        for (SubNode peer : peers) {
            peer.sendWordSearchMessageRequest(keyword);
        }
    }

    public void downloadFile(List<FileSearchResult> searchResults) {
        for (FileSearchResult f : searchResults) System.out.println(
            "Request file: " + f
        );

        downloadManager.addDownloadRequest(searchResults);
    }

    // Connect to another node
    public void connectToNode(String nomeEndereco, int targetPort) {
        Socket clientSocket = null;
        InetAddress targetEndereco = null;
        // Validate address
        try {
            targetEndereco = InetAddress.getByName(nomeEndereco);
            if (targetEndereco == null) {
                System.out.println(
                    "Failed to connect to node: Invalid address"
                );
                return;
            }
        } catch (IOException e) {
            System.out.println("Unable to resolve address: " + nomeEndereco);
        }
        // Validate port
        if (targetPort <= 8080 || targetPort >= 65535) {
            System.out.println("Failed to connect: Invalid port range");
            return;
        }

        // Attempt connection
        if (targetEndereco.equals(this.address) && targetPort == this.port) {
            System.out.println("Failed to connect: Cannot connect to itself");
            return;
        }

        for (SubNode peer : peers) {
            if (
                peer.getSocket().getInetAddress().equals(targetEndereco) &&
                peer.getSocket().getPort() == targetPort
            ) {
                System.out.println(
                    "Failed to connect: That connection already exists"
                );
                return;
            }

            if (peer.getOriginalBeforeOSchangePort() == targetPort) {
                System.out.println(
                    "Failed to connect: That connection alread exists"
                );
                return;
            }
        }

        try {
            clientSocket = new Socket(targetEndereco, targetPort);
            SubNode handler = new SubNode(
                this,
                downloadManager,
                clientSocket,
                gui,
                true
            );
            handler.start();

            peers.add(handler);

            Thread.sleep(100);

            handler.sendNewConnectionRequest(address, port);
        } catch (NoRouteToHostException e) {
            System.err.println("Failed to connect: Target is unreachable");
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to connect: " + e);
        }
    }

    public boolean hasFileWithHash(String hash) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (Utils.generateSHA256(file.getAbsolutePath()).equals(hash)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void listSubNodes() {
        for (SubNode peer : peers) {
            System.out.println(peer);
        }
    }

    public GUI getGUI() {
        return gui;
    }

    public int getId() {
        return nodeId;
    }

    // String representation of the node
    @Override
    public String toString() {
        return "Node " + address + " " + port;
    }

    // Getter for node's address
    public InetAddress getAddress() {
        return address;
    }

    // Getter for node's IP as a string
    public String getEnderecoIP() {
        String enderecoString = address.toString();
        return enderecoString.substring(enderecoString.indexOf("/") + 1);
    }

    // Getter and setter for port
    public int getPort() {
        return port;
    }

    public DownloadTasksManager getDownloadManager() {
        return downloadManager;
    }

    public Set<SubNode> getPeers() {
        return peers;
    }
}
