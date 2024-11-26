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

    public static final String WORK_FOLDER = "Code/dl";
    private static final int BASE_PORT = 8080;
    private static final int MAX_PORT = 65535;
    private static final boolean DEBUG = true;

    private final int nodeId;
    private final int port;
    private final InetAddress address;
    private final File folder;
    private final Set<SubNode> peers;
    private final DownloadTasksManager downloadManager;
    private final GUI gui;

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
        if (!Utils.isValidID(this.nodeId)) {
            throw new IllegalArgumentException(
                "Invalid node ID. Input a valid number (1-41070)"
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

    public void startServing() {
        System.out.println(
            getAddressAndPortFormated() + "Awaiting connection..."
        );
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                SubNode clientHandler = new SubNode(
                    this,
                    downloadManager,
                    clientSocket,
                    true
                );
                clientHandler.start();
                peers.add(clientHandler);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to start server: " + e.getMessage()
            );
        }
    }

    public void connectToNode(String targetAddress, int targetPort) {
        InetAddress targetInetAddress = resolveAddress(targetAddress);
        if (!isValidConnection(targetInetAddress, targetPort)) {
            return;
        }

        establishConnection(targetInetAddress, targetPort);
    }

    private InetAddress resolveAddress(String address) {
        try {
            InetAddress resolved = InetAddress.getByName(address);
            if (resolved == null) {
                throw new IllegalArgumentException("Invalid address");
            }
            return resolved;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                "Unable to resolve address: " + address
            );
        }
    }

    private boolean isValidConnection(
        InetAddress targetAddress,
        int targetPort
    ) {
        if (targetPort <= BASE_PORT || targetPort >= MAX_PORT) {
            System.out.println(
                getAddressAndPortFormated() +
                "Failed to connect: Invalid port range"
            );
            return false;
        }

        if (targetAddress.equals(this.address) && targetPort == this.port) {
            System.out.println(
                getAddressAndPortFormated() +
                "Failed to connect: Cannot connect to itself"
            );
            return false;
        }

        if (isAlreadyConnected(targetAddress, targetPort)) {
            System.out.println(
                getAddressAndPortFormated() +
                "Failed to connect: Connection already exists"
            );
            return false;
        }

        return true;
    }

    private boolean isAlreadyConnected(
        InetAddress targetAddress,
        int targetPort
    ) {
        for (SubNode peer : peers) {
            if (
                (peer.getSocket().getInetAddress().equals(targetAddress) &&
                    peer.getSocket().getPort() == targetPort) ||
                peer.getOriginalBeforeOSchangePort() == targetPort
            ) {
                return true;
            }
        }
        return false;
    }

    private void establishConnection(
        InetAddress targetAddress,
        int targetPort
    ) {
        try (Socket clientSocket = new Socket(targetAddress, targetPort)) {
            SubNode handler = new SubNode(
                this,
                downloadManager,
                clientSocket,
                true
            );
            handler.start();
            peers.add(handler);

            Thread.sleep(100);
            handler.sendNewConnectionRequest(address, port);
        } catch (IOException e) {
            System.err.println(
                "Failed to establish connection: " + e.getMessage()
            );
        } catch (InterruptedException e) {
            System.err.println(
                "Failed to establish connection: " + e.getMessage()
            );
        }
    }

    public void broadcastWordSearchMessageRequest(String keyword) {
        for (SubNode peer : peers) peer.sendWordSearchMessageRequest(keyword);
    }

    public void createDownloadRequest(List<FileSearchResult> searchResults) {
        for (FileSearchResult searchResult : searchResults) {
            System.out.println(
                getAddressAndPortFormated() + "Request file: " + searchResult
            );
        }

        downloadManager.addDownloadRequest(searchResults);
    }

    public boolean hasFileWithHash(int hash) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();

        for (File file : files) {
            if (Utils.calculateFileHash(file.getAbsolutePath()) == hash) {
                return true;
            }
        }

        return false;
    }

    public void removePeer(SubNode peer) {
        peers.remove(peer);
        int port = Utils.isValidPort(peer.getSocket().getPort())
            ? peer.getSocket().getPort()
            : peer.getOriginalBeforeOSchangePort();
        System.out.println(
            getAddressAndPortFormated() +
            "Removed connection with: " +
            peer.getSocket().getInetAddress().getHostAddress() +
            "::" +
            port
        );
    }

    public String getEnderecoIP() {
        return address.getHostAddress();
    }

    public File getFolder() {
        return folder;
    }

    public GUI getGUI() {
        return gui;
    }

    public int getId() {
        return nodeId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public DownloadTasksManager getDownloadManager() {
        return downloadManager;
    }

    public Set<SubNode> getPeers() {
        return peers;
    }

    public String getAddressAndPort() {
        return address.getHostAddress() + ":" + port;
    }

    public String getAddressAndPortFormated() {
        if (DEBUG == false) return "";
        return "[" + address.getHostAddress() + ":" + port + "]";
    }

    @Override
    public String toString() {
        return "Node " + address + " " + port;
    }
}
