package Tests;

import Core.Node;
import FileSearch.FileSearchResult;
import GUI.GUI;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        int test = 2; // Change this to select the test case to run

        if (test == 1) {
            // GUI Initialization Test
            int argument = Integer.parseInt(args[0]);
            GUI gui = new GUI(argument);
        } else if (test == 2) {
            GUI gui1 = new GUI(1);
            GUI gui3 = new GUI(3);
            GUI gui2 = new GUI(2);

            Node firstNode = gui1.getNode();
            Node secondNode = gui2.getNode();
            Node thirdNode = gui3.getNode();

            try {
                Thread.sleep(500);
                secondNode.connectToNode(
                    InetAddress.getLocalHost().getHostAddress(),
                    8081
                );
                secondNode.connectToNode(
                    InetAddress.getLocalHost().getHostAddress(),
                    8083
                );
                secondNode.broadcastWordSearchMessageRequest("");
                Thread.sleep(500);
                ArrayList<FileSearchResult> GUI2listModel = gui2.getListModel();
                ArrayList<FileSearchResult> GUI2list = new ArrayList<
                    FileSearchResult
                >();
                GUI2list.add(GUI2listModel.getFirst());
                gui2.simulateSelectedOptions(GUI2list);
            } catch (InterruptedException | UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}
