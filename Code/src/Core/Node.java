package Core;

import FileSearch.FileSearchResult;
import GUI.GUI;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import Services.DownloadTasksManager;
import Services.SenderAssistant;
import Services.SubNode;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {

    public static final String WORK_FOLDER = "Code/dl";
    private static final int BASE_PORT = 8080;
    private static final int MAX_PORT = 65535;

    private final int nodeId;
    private final int port;
    private final InetAddress address;
    private final File folder;
    private final Set<SubNode> peers;
    private final HashMap<Integer, DownloadTasksManager> downloadManagers;
    private final GUI gui;
    private ArrayList<FileBlockRequestMessage> blocksToProcess;
    private ExecutorService senders;
    private final int numberOfSenders = 5;
    private HashMap<String, Integer> hashes;

    private final ExecutorService downloadTaskManagersThreadPool =
        Executors.newFixedThreadPool(10);

    public Node(int nodeId, GUI gui) {
        this.hashes = new HashMap<>();
        this.nodeId = nodeId;
        this.port = BASE_PORT + nodeId;
        this.gui = gui;
        this.peers = new HashSet<>();
        this.downloadManagers = new HashMap<>();
        this.blocksToProcess = new ArrayList<>();
        validatePort();
        initializeSenders(numberOfSenders);
        this.folder = createWorkingDirectory();
        this.address = initializeAddress();
        loadHashes();
    }

    public SubNode getPeerToSend(String address, int port) {
        for (SubNode peer : peers) {
            if (
                peer.getDestinationAddress().equals(address) &&
                peer.getDestinationPort() == port
            ) {
                return peer;
            }
        }
        return null;
    }

    private void initializeSenders(int n) {
        if (n <= 0) return;

        this.senders = Executors.newFixedThreadPool(n);
        for (int i = 0; i < n; i++) {
            this.senders.execute(new SenderAssistant(this));
        }
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
            throw new RuntimeException( getAddressAndPortFormated() +
                "Failed to create directory: dl" + nodeId
            );
        }
        return folder;
    }

    private InetAddress initializeAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException( getAddressAndPortFormated() +" Unable to get the device's address", e);
        }
    }

    public void startServing() {
        System.out.println(
            getAddressAndPortFormated() + "Awaiting connection..."
        );
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                SubNode clientHandler = new SubNode(this, clientSocket, true);
                clientHandler.start();
                peers.add(clientHandler);
            }
        } catch (IOException e) {
            throw new RuntimeException(  getAddressAndPortFormated() +
                "Failed to start server: " + e.getMessage()
            );
        }
    }

    public synchronized void addElementToBlocksToProcess(
        FileBlockRequestMessage request
    ) {
        blocksToProcess.add(request);
        notify();
    }

    public synchronized void removeElementFromBlocksToProcess(
        FileBlockRequestMessage request
    ) throws InterruptedException {
        if (blocksToProcess.isEmpty()) wait();
        blocksToProcess.remove(request);
    }

    public void connectToNode(String targetAddress, int targetPort) {
        try {
            InetAddress targetInetAddress = resolveAddress(targetAddress);
            if (!isValidConnection(targetInetAddress, targetPort)) {
                return;
            }

            establishConnection(targetInetAddress, targetPort);
        } catch (Exception e) {
            System.err.println("Error connecting to node: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            Socket clientSocket = new Socket(targetAddress, targetPort);
            SubNode handler = new SubNode(this, clientSocket, true);
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

    public void downloadFiles(List<List<FileSearchResult>> filesToDownload) {
        for (List<FileSearchResult> file : filesToDownload) {
            //Check if it's already downloading the file
            if (downloadManagers.containsKey(file.get(0).getHash())) continue;

            // Check if already has the file in the directory
            if (hasFileWithHash(file.get(0).getHash())) continue;
            FileSearchResult example = file.get(0);
            System.out.println(
                getAddressAndPortFormated() + "Request file: " + example
            );
            DownloadTasksManager downloadManager = new DownloadTasksManager(
                this,
                file
            );
            downloadTaskManagersThreadPool.execute(downloadManager);
            downloadManagers.put(example.getHash(), downloadManager);
        }
    }

    public void loadHashes() {
        if (folder == null || !folder.exists() || !folder.isDirectory()) return;

        File[] files = folder.listFiles();

        for (File file : files) {
            hashes.put(
                file.getAbsolutePath(),
                Utils.calculateFileHash(file.getAbsolutePath())
            );
        }
    }

    public int getHash(String filePath) {
        if (hashes.containsKey(filePath)) {
            return hashes.get(filePath);
        } else {
            return Utils.calculateFileHash(filePath);
        }
    }

    public boolean hasFileWithHash(int hash) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();

        for (File file : files) {
            if (hashes.get(file.getAbsolutePath()) == hash) {
                return true;
            }
        }

        return false;
    }

    public synchronized FileBlockRequestMessage getBlockRequest()
        throws InterruptedException {
        if (blocksToProcess.isEmpty()) wait();
        try {
            return blocksToProcess.remove(0);
        } catch (Exception e) {
        }
        return null;
    }

    public synchronized void addBlockRequest(FileBlockRequestMessage request) {
        blocksToProcess.add(request);
        notify();
    }

    public void removeDownloadProcess(int hash) {
        downloadManagers.remove(hash);
    }

    public Map<String, Integer> getDownloadProcess(int hash) {
        return downloadManagers.get(hash).getDownloadProcess();
    }

    public void addDownloadAnswer(
        int hash,
        InetAddress address,
        int port,
        FileBlockAnswerMessage answer
    ) {
        if (downloadManagers.get(hash) == null) return;
        downloadManagers.get(hash).addDownloadAnswer(answer);
        downloadManagers
            .get(hash)
            .addNumberOfDownloadsForPeer(
                address.getHostAddress() + ":" + port,
                1
            );
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

    public Set<SubNode> getPeers() {
        return peers;
    }

    public String getAddressAndPort() {
        return address.getHostAddress() + ":" + port;
    }

    public String getAddressAndPortFormated() {
        return "[" + address.getHostAddress() + ":" + port + "]";
    }

    @Override
    public String toString() {
        return "Node " + address + " " + port;
    }
}
