package Tests;

import Core.Utils;
import GUI.GUI;
import java.net.InetAddress;

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

            for (String arg : args) {
                try {
                    int id = Integer.parseInt(arg);
                    GUI gui = new GUI(id);
                    if (id != 1) {
                        gui
                            .getNode()
                            .connectToNode(
                                InetAddress.getLocalHost().getHostAddress(),
                                8081
                            );
                    }
                    gui.getNode().broadcastWordSearchMessageRequest("");
                    gui.open();
                    System.out.println("GUI opened for ID: " + id);
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
        }
        if (test == 1) {
            String file = "./dl1/doc1.txt";

            int a = Utils.calculateFileHash(file);

            System.out.println(a);
        }
    }
}
