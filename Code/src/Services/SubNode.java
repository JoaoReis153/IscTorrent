package Services;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;
import Messaging.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class SubNode extends Thread {

    private final Node node;

    private final Socket socket;
    private final boolean userCreated;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int originalBeforeOSchangePort;
    private boolean running = true;
    private CountDownLatch blockAnswerLatch;

    public SubNode(Node node, Socket socket, boolean userCreated) {
        this.node = node;
        this.socket = socket;
        this.userCreated = userCreated;
    }

    @Override
    public void run() {
        initializeStreams();
        handleCommunication();
    }

    private void initializeStreams() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error initializing streams: " + e.getMessage());
            closeResources();
        }
    }

    private void handleCommunication() {
        try {
            Object obj;
            while (running && (obj = in.readObject()) != null) {
                handleIncomingMessage(obj);
            }
        } catch (StreamCorruptedException e) {
            System.err.println("Stream corrupted: " + e.getMessage());
            e.printStackTrace();
        } catch (EOFException e) {
            System.err.println("End of stream reached unexpectedly.");
        } catch (InvalidClassException e) {
            System.err.println("Invalid class: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(
                node.getAddressAndPortFormated() + "IO error: " + e.getMessage()
            );
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.err.println("Closed in 0");
            close();
        }
    }

    private void handleIncomingMessage(Object obj) {
        if (obj instanceof NewConnectionRequest) {
            handleNewConnectionRequest((NewConnectionRequest) obj);
        } else if (obj instanceof WordSearchMessage) {
            handleWordSearchMessage((WordSearchMessage) obj);
        } else if (obj instanceof FileSearchResult[]) {
            handleFileSearchResults((FileSearchResult[]) obj);
        } else if (obj instanceof FileBlockRequestMessage) {
            handleFileBlockRequest((FileBlockRequestMessage) obj);
        } else if (obj instanceof FileBlockAnswerMessage) {
            handleFileBlockAnswer((FileBlockAnswerMessage) obj);
        }
    }

    private void handleNewConnectionRequest(NewConnectionRequest request) {
        this.originalBeforeOSchangePort = request.getClientPort();
        logNewConnection();
    }

    private void handleWordSearchMessage(WordSearchMessage message) {
        System.out.println(
            node.getAddressAndPortFormated() +
            "Received WordSearchMessage with content: [" +
            message.getKeyword() +
            "]"
        );
        if (node.getFolder().exists() && node.getFolder().isDirectory()) {
            sendFileSearchResultList(message);
        }
    }

    private void handleFileSearchResults(FileSearchResult[] results) {
        if (node.getGUI() == null) {
            System.out.println(
                node.getAddressAndPortFormated() +
                "There was a problem with the GUI"
            );
            System.exit(1);
        }
        System.out.println(
            node.getAddressAndPortFormated() +
            "Received " +
            results.length +
            " search results"
        );
        node.getGUI().loadListModel(results);
    }

    private void handleFileBlockRequest(FileBlockRequestMessage request) {
        System.out.println(
            node.getAddressAndPortFormated() +
            "Received FileBlockRequestMessage: " +
            request
        );

        node.addBlockRequest(request);
    }

    private void handleFileBlockAnswer(FileBlockAnswerMessage answer) {
        System.out.println(
            node.getAddressAndPortFormated() + "Received " + answer
        );
        int port = Utils.isValidPort(socket.getPort())
            ? socket.getPort()
            : originalBeforeOSchangePort;

        node.addDownloadAnswer(
            answer.getHash(),
            socket.getInetAddress(),
            port,
            answer
        );
        if (blockAnswerLatch != null) blockAnswerLatch.countDown();
    }

    public void sendFileBlockRequest(FileBlockRequestMessage block) {
        block.setSenderAddress(node.getAddress().getHostAddress());
        block.setSenderPort(node.getPort());
        sendObject(block);
    }

    public void sendWordSearchMessageRequest(String keyword) {
        WordSearchMessage searchPackage = new WordSearchMessage(keyword);
        sendObject(searchPackage);
    }

    public void sendNewConnectionRequest(InetAddress endereco, int thisPort) {
        if (out == null) {
            System.out.println(
                node.getAddressAndPortFormated() +
                "OutputStream is null [invalid port: " +
                thisPort +
                "]"
            );
            return;
        }
        NewConnectionRequest request = new NewConnectionRequest(
            endereco,
            thisPort
        );
        sendObject(request);
        logNewConnection();
    }

    public synchronized void sendObject(Object message) {
        System.out.println(
            node.getAddressAndPortFormated() + "Sending message: " + message.toString()
        );
        if (out != null && !socket.isClosed()) {
            try {
                out.reset();
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        } else {
            System.out.println(
                node.getAddressAndPortFormated() +
                "Cannot send message because socket is closed"
            );
        }
    }

    public void close() {
        if (!running) return;

        running = false;

        closeResources();
        int port = Utils.isValidPort(socket.getPort())
            ? socket.getPort()
            : originalBeforeOSchangePort;
        System.out.println(
            node.getAddressAndPortFormated() +
            "Thread closed for SubNode at " +
            socket.getInetAddress().getHostAddress() +
            ":" +
            port
        );
        node.removePeer(this);
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println(
                node.getAddressAndPortFormated() +
                "Error closing resources: " +
                e.getMessage()
            );
        }
    }

    private List<String> getIgnoredFileNames() {
        List<String> ignoredFiles = new ArrayList<>();
        File gitignore = new File(
            this.node.getFolder().getParentFile().getParentFile(),
            ".gitignore"
        );

        if (gitignore.exists() && gitignore.isFile()) {
            try (
                BufferedReader br = new BufferedReader(
                    new FileReader(gitignore)
                )
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        ignoredFiles.add(line);
                    }
                }
            } catch (IOException e) {
                System.out.println(
                    node.getAddressAndPortFormated() +
                    "Error reading .gitignore: " +
                    e.getMessage()
                );
            }
        }
        return ignoredFiles;
    }

    public void setBlockAnswerLatch(CountDownLatch latch) {
        this.blockAnswerLatch = latch;
    }

    public int getOriginalBeforeOSchangePort() {
        return originalBeforeOSchangePort;
    }

    public Socket getSocket() {
        return socket;
    }

    private void logNewConnection() {
        int port = Utils.isValidPort(originalBeforeOSchangePort)
            ? originalBeforeOSchangePort
            : socket.getPort();
        System.out.println(
            node.getAddressAndPortFormated() +
            "Added new node::NodeAddress [address=" +
            socket.getInetAddress().getHostAddress() +
            " port=" +
            port +
            "]"
        );
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
            node.getGUI() +
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

    public boolean hasConnectionWith(InetAddress address, int port) {
        int thisSocketPort = Utils.isValidPort(socket.getPort())
            ? socket.getPort()
            : originalBeforeOSchangePort;

        return (
            this.socket.getLocalAddress()
                .getHostAddress()
                .equals(address.getHostAddress()) &&
            thisSocketPort == port
        );
    }

    @Override
    public int hashCode() {
        if (userCreated) return Objects.hash(
            socket.getInetAddress(),
            socket.getPort()
        );
        return super.hashCode();
    }

    private void sendFileSearchResultList(WordSearchMessage searchMessage) {
        File[] files = node.getFolder().listFiles();
        if (files == null) return;

        String keyword = searchMessage.getKeyword().toLowerCase();
        List<String> filesToIgnore = getIgnoredFileNames();

        int keywordCount = countMatchingFiles(files, keyword, filesToIgnore);
        if (keywordCount == 0) return;

        FileSearchResult[] results = createFileSearchResults(
            files,
            keyword,
            filesToIgnore,
            keywordCount,
            searchMessage
        );

        if (results.length == 0) return;
        else if (results.length == 1) System.out.println(
            node.getAddressAndPortFormated() +
            "Sent 1 file search result [" +
            results[0].getHash() +
            "]"
        );
        else System.out.println(
            node.getAddressAndPortFormated() +
            "Sent " +
            results.length +
            " files search result for keyword: [" +
            searchMessage.getKeyword() +
            "]"
        );

        sendObject(results);
    }

    private int countMatchingFiles(
        File[] files,
        String keyword,
        List<String> filesToIgnore
    ) {
        int count = 0;
        for (File file : files) {
            if (isFileMatch(file, keyword, filesToIgnore)) {
                count++;
            }
        }
        return count;
    }

    private boolean isFileMatch(
        File file,
        String keyword,
        List<String> filesToIgnore
    ) {
        return (
            file.getName().toLowerCase().contains(keyword) &&
            !filesToIgnore.contains(file.getName())
        );
    }

    private FileSearchResult[] createFileSearchResults(
        File[] files,
        String keyword,
        List<String> filesToIgnore,
        int resultCount,
        WordSearchMessage searchMessage
    ) {
        FileSearchResult[] results = new FileSearchResult[resultCount];
        int counter = 0;

        for (File file : files) {
            if (isFileMatch(file, keyword, filesToIgnore)) {
                int hash = Utils.calculateFileHash(file.getAbsolutePath());
                results[counter++] = new FileSearchResult(
                    searchMessage,
                    file.getName(),
                    hash,
                    file.length(),
                    node.getAddress(),
                    node.getPort()
                );
            }
        }
        return results;
    }

    public void sendFileBlockAnswer(FileBlockAnswerMessage answer) {
        


        sendObject(answer);
    }

    public String getDestinationAddress() {
        return socket.getInetAddress().getHostAddress();
    }
    
    public int getDestinationPort() {
        int port = Utils.isValidPort(socket.getPort())
            ? socket.getPort()
            : originalBeforeOSchangePort;
        return port;
    }
}
