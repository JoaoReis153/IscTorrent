package Services;

import java.io.*;
import java.net.*;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;
import Messaging.Connection;
import Messaging.FileBlockRequestMessage;
import Messaging.NewConnectionRequest;

public class ClientHandler extends Thread {
		
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private Node node;
    
    public ClientHandler(Connection connection, Node node) {
        this.socket = connection.getSocket();
        this.in = connection.getInputStream();
        this.out = connection.getOutputStream();
        this.node = node;
    }
    
    

    @Override
    public void run() {
        try {
            Object obj;
            while ((obj = in.readObject()) != null) {
                //System.out.println("Received: " + obj);
                if (obj instanceof NewConnectionRequest) {
                    System.out.println("Received a connection request from " + this.socket.getInetAddress() + "::" + this);
                } else if (obj instanceof WordSearchMessage) {
                    System.out.println("Received a WordSearchMessage object with content: (" + ((WordSearchMessage) obj).getKeyword() + ")");
                     if (node.getFolder().exists() && node.getFolder().isDirectory()) {
                            File[] files = node.getFolder().listFiles();
                            if (files != null) {
                                for (File file : files) {
                                    String h1 = Utils.generateSHA256(file.getAbsolutePath());
                                    FileSearchResult response = new FileSearchResult((WordSearchMessage) obj, file.getName(), h1, file.length(), node.getEnderecoIP() ,node.getPort());
                                    this.out.writeObject(response);
                                    this.out.flush();
                                    System.out.println(file.getName());
                                }
                            }
                     } 
                } else if (obj instanceof FileSearchResult) {
                    System.out.println("Look at what I jst received: ");
                    System.out.println(obj);
                    
                } else if (obj instanceof FileBlockRequestMessage) {
                    FileBlockRequestMessage block = (FileBlockRequestMessage) obj;
                    System.out.println("Received block: " + block.getHash());
                    
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error handling client: " + e);
        } finally {
            // Close resources
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
