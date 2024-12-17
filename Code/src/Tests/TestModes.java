package Tests;

import java.util.ArrayList;

import Core.Node;
import GUI.GUI;

public class TestModes {

    ArrayList<Integer> inputs;
    ArrayList<GUI> guiList;
    int mode;

    public TestModes(String[] args, int mode) {
      //Take the inputs and trasnform them into ints
        this.inputs = new ArrayList<>();
        for (String arg : args) {
            try {
                int id = Integer.parseInt(arg);
                inputs.add(id);
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
            }
          }

        this.mode = mode;
    }

    public void run() {

        initializeNodes(true);

        if (guiList.isEmpty()) {
            System.out.println("No nodes created. Exiting test.");
            return;
        }

        switch (mode) {
            case 0:
                break;
            case 1:
                test1();
                break;
            case 2:
                test2();
                break;
            case 3:
                test3();
                break;
            case 4:
                test4();
                break;
            case 5:
                test5();
                break;
            default:
                System.out.println("Invalid mode.");
                break;
        }
    }

      public void test1() {
        // Test 1: Create a network of nodes and connect them
        System.out.println(
            "Test 1: Create a network of nodes and connect them"
        );

        // Establish connections between all nodes
        connectNodes();
      } 

      public void test2() {
        // Test 2: Create a network of nodes, connect them, and search messages
        System.out.println(
            "Test 2: Create a network of nodes, connect them, and search in the first node"
        );

        connectNodes();

        ArrayList<GUI> secondGuiList = new ArrayList<>();
        secondGuiList.add(guiList.get(0));
        broadcastSearchMessage(secondGuiList, "");
    }

      public void test3() {
        // Test 2: Create a network of nodes, connect them, and search messages
        System.out.println(
            "Test 3: Create a network of nodes, connect them, and search in all nodes"
        );

        connectNodes();

        broadcastSearchMessage(guiList, "");
    }

      public void test4() {
        // Test 4: Create a network of nodes, connect them, and download files in the last node
        System.out.println(
            "Test 4: Create a network of nodes, connect them, and download files"
        );

        connectNodes();

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

      public void test5() {
        // Test 3: Create a network of nodes, connect them, and download files
        System.out.println(
            "Test 5: Create a network of nodes, connect them, and download files"
        );

        connectNodes();

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


    /**
     * Initializes nodes based on the provided arguments.
     *
     * @param inputs Command-line arguments containing node IDs
     * @return List of GUI objects for the created nodes
     */
    private void initializeNodes(boolean show) {
        this.guiList = new ArrayList<>();

        if (inputs.size() == 0) {
            System.out.println(
                "Usage: Please provide at least one ID as arguments."
            );
            return;
        }

        for (int id : inputs) {
            try {
                GUI gui = new GUI(id, show);
                guiList.add(gui);
                gui.open();
            } catch (NumberFormatException e) {
                System.err.println(
                    "Invalid ID: " + id + ". Please provide numeric values."
                );
            } catch (IllegalArgumentException e) {
                System.err.println("Failed to create node: " + e.getMessage());
            } catch (Exception e) {
                System.err.println(
                    "An error occurred while initializing the node with ID: " +
                    id
                );
                e.printStackTrace();
            }
        }
    }

    /**
     * Connects all nodes in the provided list to each other.
     *
     * @param guiList List of GUI objects representing the nodes
     */ 
    private void connectNodes() {
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
