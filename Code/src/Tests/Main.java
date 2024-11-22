package Tests;

import Core.Node;
import Core.Utils;
import GUI.GUI;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int test = 2; // Change this to select the test case to run

        if (test == 1) {
            // GUI Initialization Test
            int argument = Integer.parseInt(args[0]);
            GUI gui = new GUI(argument);
        } else if (test == 2) {
            GUI gui1 = new GUI(1);
            GUI gui2 = new GUI(2);
            GUI gui3 = new GUI(3);

            Node firstNode = gui1.getNode();
            Node secondNode = gui2.getNode();
            Node thirdNode = gui3.getNode();

            try {
                System.out.println("--------------------------------------");
                Thread.sleep(500);
                secondNode.connectToNode(
                    InetAddress.getLocalHost().getHostAddress(),
                    8081
                );
                thirdNode.connectToNode(
                    InetAddress.getLocalHost().getHostAddress(),
                    8082
                );
                firstNode.broadcastWordSearchMessageRequest("");
                Thread.sleep(500);
                System.out.println(gui1.getListModel());
            } catch (InterruptedException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}
