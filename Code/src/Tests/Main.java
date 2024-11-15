package Tests;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import Core.Node;
import GUI.GUI;
import Messaging.FileBlockRequestMessage;
import Services.SubNode;

public class Main {
    public static void main(String[] args) {

        int test = 6;  // Change this to select the test case to run

        if (test == 1) {
            // Basic Node Initialization Test
            GUI gui = new GUI(1);
            Node node = new Node(1, gui);
            System.out.println("Node created: " + node);
        } else if (test == 2) {
            // Node Connection Test
            GUI gui1 = new GUI(1);
            Node node1 = new Node(1, gui1);
            GUI gui2 = new GUI(2);
            Node node2 = new Node(2, gui2);

            new Thread(node2::startServing).start();

            try {
                Thread.sleep(500);
                node1.connectToNode("localhost", node2.getPort());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (test == 3) {
            // File Block Creation Test
            String workfolder = "./dl1";
            File folder = new File(workfolder);

            if (folder.exists()) {
                File[] files = folder.listFiles();
                for (File file : files) {
                    long fileSize = file.length();
                    int blockSize = 10240;

                    List<FileBlockRequestMessage> blockList = FileBlockRequestMessage.createBlockList(file.getName(), fileSize, blockSize);
                    for (FileBlockRequestMessage block : blockList) {
                        System.out.println("Hash: " + block.getHash() + ", Offset: " + block.getOffset() + ", Length: " + block.getLength());
                    }
                }
            }
        } else if (test == 4) {
            // GUI Initialization Test
            int argument = Integer.parseInt(args[0]);
            GUI gui = new GUI(argument);
            gui.open();
        } else if (test == 5) {
            // Broadcast Word Search Test
            GUI gui1 = new GUI(1);
            Node node1 = new Node(1, gui1);
            Node node2 = new Node(2, gui1);

            new Thread(node1::startServing).start();
            new Thread(node2::startServing).start();

            try {
                Thread.sleep(500);
                node1.connectToNode("localhost", node2.getPort());
                node1.broadcastWordSearchMessageRequest("exampleKeyword");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (test == 6) {
            // New Connection Request Test
            GUI gui = new GUI(1);
            Node node = new Node(1, gui);
            
            GUI gu = new GUI(2);
            Node nod = new Node(2, gui);

            try {
                InetAddress address = InetAddress.getByName("localhost");
                node.connectToNode("127.0.0.1", 8082);
                SubNode subNode = new SubNode(node, new Socket(address, 8081) , gui);
                subNode.sendNewConnectionRequest(address, 8081);
            } catch (IOException e) {
                System.out.println("There was an error in test 6");
            }
        }
    }
}
