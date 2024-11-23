package Tests;

import Core.Utils;
import FileSearch.FileSearchResult;
import GUI.GUI;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int test = 0;

        if (test == 0) {
            if (args.length == 0) {
                System.out.println(
                    "Usage: Please provide at least one ID as arguments."
                );
                return;
            }

            System.out.println("Number of nodes to create: " + args.length);

            ArrayList<GUI> guiList = new ArrayList<GUI>();
            for (String arg : args) {
                try {
                    int id = Integer.parseInt(arg);
                    GUI gui = new GUI(id);
                    guiList.add(gui);
                    if (id != 1) {
                        gui
                            .getNode()
                            .connectToNode(
                                InetAddress.getLocalHost().getHostAddress(),
                                8081
                            );
                    }
                    gui.open();
                } catch (NumberFormatException e) {
                    System.err.println(
                        "Invalid ID: " +
                        arg +
                        ". Please provide numeric values."
                    );
                } catch (IllegalArgumentException e) {
                    System.err.println(
                        "Failed to create node: " + e.getMessage()
                    );

                    continue;
                } catch (Exception e) {
                    System.err.println(
                        "An error occurred while initializing the GUI for ID: " +
                        arg
                    );
                    e.printStackTrace();
                }
            }
            guiList
                .getFirst()
                .getNode()
                .broadcastWordSearchMessageRequest("video");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<FileSearchResult> list = new ArrayList<>();
            list.add(guiList.getFirst().getListModel().getFirst());

            guiList.getFirst().simulateDownloadButton(list);
        }
    }
}
