package Tests;

import GUI.GUI;
import java.net.InetAddress;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        int test = 3;
        if (test == 0) {
            System.out.println("Test 0");
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
            /*
            guiList.getFirst().getNode().broadcastWordSearchMessageRequest("");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            guiList
                .getFirst()
                .simulateDownloadButton(guiList.getFirst().getListModel());
            */
        } else if (test == 1) {
            System.out.println("Test 1");
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
        } else if (test == 2) {
            GUI gui6 = new GUI(6);
            GUI gui7 = new GUI(7);
            GUI gui8 = new GUI(8);
            gui7.open();
            gui8.open();
            gui6.open();

            //Connect 6 -> 7 and 7 -> 8
            try {
                gui6
                    .getNode()
                    .connectToNode(
                        InetAddress.getLocalHost().getHostAddress(),
                        8087
                    );
                gui7
                    .getNode()
                    .connectToNode(
                        InetAddress.getLocalHost().getHostAddress(),
                        8088
                    );
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            gui6.getNode().broadcastWordSearchMessageRequest("");
        }
        if (test == 3) {
            System.out.println("Test 3");
            if (args.length == 0) {
                System.out.println(
                    "Usage: Please provide at least one ID as arguments."
                );
                return;
            }

            System.out.println("Number of nodes to create: " + args.length);

            ArrayList<GUI> guiList = new ArrayList<GUI>();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                try {
                    int id = Integer.parseInt(arg);
                    GUI gui = new GUI(id);
                    for (int j = 0; j < guiList.size(); j++) {
                        GUI g = guiList.get(j);

                        gui
                            .getNode()
                            .connectToNode(
                                InetAddress.getLocalHost().getHostAddress(),
                                g.getNode().getPort()
                            );
                    }
                    guiList.add(gui);
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
            for (GUI gui : guiList) {
                gui.getNode().broadcastWordSearchMessageRequest("");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (GUI gui : guiList) {
                gui.simulateDownloadButton(gui.getListModel());
            }
        }
    }
}
