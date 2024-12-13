package Tests;

import Core.Node;
import GUI.GUI;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        /*
        Test:
        0 - Create nodes
        1 - Create a network of nodes and connect them
        2 - Create a network of nodes, connect them and search in the first node
        3 - Create a network of nodes, connect them and search in all nodes
        4 - Create a network of nodes, connect them and download all files in the last node.
        5 - Create a network of nodes, connect them and download all files in every node
        */

        int test = 1; // Set this to the test you want to run

        if (test == 0) {
            // Test 0: Create nodes
            System.out.println("Test 0: Create nodes");

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
                    gui.open();
                    System.out.println(
                        "Node with ID " + id + " created and GUI opened."
                    );
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
                } catch (Exception e) {
                    System.err.println(
                        "An error occurred while creating the node with ID: " +
                        arg
                    );
                    e.printStackTrace();
                }
            }
        }

        if (test == 1) {
            // Test 1: Create a network of nodes and connect them
            System.out.println(
                "Test 1: Create a network of nodes and connect them"
            );

            ArrayList<GUI> guiList = initializeNodes(args, true);

            if (guiList.isEmpty()) {
                System.out.println("No nodes created. Exiting test.");
                return;
            }

            // Establish connections between all nodes
            connectNodes(guiList);
        }

        if (test == 2) {
            // Test 2: Create a network of nodes, connect them, and search messages
            System.out.println(
                "Test 2: Create a network of nodes, connect them, and search in the first node"
            );

            ArrayList<GUI> guiList = initializeNodes(args, true);

            if (guiList.isEmpty()) {
                System.out.println("No nodes created. Exiting test.");
                return;
            }

            connectNodes(guiList);

            ArrayList<GUI> secondGuiList = new ArrayList<>();
            secondGuiList.add(guiList.get(0));
            broadcastSearchMessage(secondGuiList, "");
        }

        if (test == 3) {
            // Test 2: Create a network of nodes, connect them, and search messages
            System.out.println(
                "Test 3: Create a network of nodes, connect them, and search in all nodes"
            );

            ArrayList<GUI> guiList = initializeNodes(args, true);

            if (guiList.isEmpty()) {
                System.out.println("No nodes created. Exiting test.");
                return;
            }

            connectNodes(guiList);

            broadcastSearchMessage(guiList, "");
        }

        if (test == 4) {
            // Test 4: Create a network of nodes, connect them, and download files in the last node
            System.out.println(
                "Test 4: Create a network of nodes, connect them, and download files"
            );

            ArrayList<GUI> guiList = initializeNodes(args, true);

            if (guiList.isEmpty()) {
                System.out.println("No nodes created. Exiting test.");
                return;
            }

            connectNodes(guiList);

            broadcastSearchMessage(guiList, "");

            // Simulate a download operation
            if (!guiList.isEmpty()) {
                try {
                    GUI lastGui = guiList.get(guiList.size() - 1);
                    lastGui.simulateDownloadButton(lastGui.getListModel());
                    System.out.println(
                        "Download simulated on Node " +
                        lastGui.getNode().getId()
                    );
                } catch (Exception e) {
                    System.err.println("Failed to simulate download.");
                    e.printStackTrace();
                }
            }
        }

        if (test == 5) {
            // Test 3: Create a network of nodes, connect them, and download files
            System.out.println(
                "Test 5: Create a network of nodes, connect them, and download files"
            );

            ArrayList<GUI> guiList = initializeNodes(args, true);

            if (guiList.isEmpty()) {
                System.out.println("No nodes created. Exiting test.");
                return;
            }

            connectNodes(guiList);

            broadcastSearchMessage(guiList, "");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Simulate a download operation
            if (!guiList.isEmpty()) {
                try {
                    for (GUI gui : guiList) {
                        gui.simulateDownloadButton(gui.getListModel());
                        System.out.println(
                            "Download simulated on Node " +
                            gui.getNode().getId()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Failed to simulate download.");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Initializes nodes based on the provided arguments.
     *
     * @param args Command-line arguments containing node IDs
     * @return List of GUI objects for the created nodes
     */
    private static ArrayList<GUI> initializeNodes(String[] args, boolean show) {
        ArrayList<GUI> guiList = new ArrayList<>();

        if (args.length == 0) {
            System.out.println(
                "Usage: Please provide at least one ID as arguments."
            );
            return guiList;
        }

        for (String arg : args) {
            try {
                int id = Integer.parseInt(arg);
                GUI gui = new GUI(id, show);
                guiList.add(gui);
                gui.open();
            } catch (NumberFormatException e) {
                System.err.println(
                    "Invalid ID: " + arg + ". Please provide numeric values."
                );
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to create node: " + e.getMessage());
            } catch (Exception e) {
                System.err.println(
                    "An error occurred while initializing the node with ID: " +
                    arg
                );
                e.printStackTrace();
            }
        }

        return guiList;
    }

    /**
     * Connects all nodes in the provided list to each other.
     *
     * @param guiList List of GUI objects representing the nodes
     */
    private static void connectNodes(ArrayList<GUI> guiList) {
        for (GUI gui : guiList) {
            Node currentNode = gui.getNode();
            for (GUI otherGui : guiList) {
                if (currentNode != otherGui.getNode()) {
                    try {
                        currentNode.connectToNode(
                            otherGui.getNode().getAddress().getHostAddress(),
                            otherGui.getNode().getPort()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Broadcasts a search message from in the nodes provided in the list.
     *
     * @param guiList List of GUI objects representing the nodes
     */

    private static void broadcastSearchMessage(
        ArrayList<GUI> guiList,
        String broadcastString
    ) {
        for (GUI gui : guiList) {
            gui.getNode().broadcastWordSearchMessageRequest(broadcastString);
        }
    }
}
