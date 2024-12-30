package Tests;

import Core.Node;
import GUI.GUI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Test {

    ArrayList<Integer> inputs;
    ArrayList<GUI> guiList;
    int mode;

    public Test(HashSet<String> args, int mode) {
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
            case 1 -> test1();
            case 2 -> test2();
            case 3 -> test3();
            case 4 -> test4();
            case 5 -> test5();
            default -> System.out.println("Invalid mode.");
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
  

     public static void main(String[] args) {
        
        Scanner scanner = new Scanner(System.in);
        HashSet<String> nodeIds = new HashSet<>();

        // Read the node IDs from user input
        System.out.println("Enter the node IDs (separated by spaces): ");

        String line = scanner.nextLine().trim();
        
        // Split the line by spaces and add the IDs to the list
        String[] ids = line.split("\\s+");
        for (String id : ids) {
            if (!id.isEmpty()) {
                nodeIds.add(id);
            }
        }
    
        
        System.out.println("Node IDs entered: " + nodeIds);
        
        /*
        Test:
        0 - Create nodes
        1 - Create a network of nodes and connect them
        2 - Create a network of nodes, connect them and search in the first node
        3 - Create a network of nodes, connect them and search in all nodes
        4 - Create a network of nodes, connect them and download all files in the last node.
        5 - Create a network of nodes, connect them and download all files in every node
        */

        System.out.println("Choose the test mode:");
        System.out.println("1 - Create a network of nodes and connect them");
        System.out.println("2 - Create a network, connect nodes, and search in the first node");
        System.out.println("3 - Create a network, connect nodes, and search in all nodes");
        System.out.println("4 - Create a network, connect nodes, and download files in the last node");
        System.out.println("5 - Create a network, connect nodes, and download files in every node (Be careful with the temperature of the computer)");
        System.out.print("Enter the test number: ");
        
        // Read the test mode interactively from user input
        int mode = -1; // Default mode
        try {
            mode = Integer.parseInt(scanner.nextLine()); // Read and parse input
        } catch (NumberFormatException e) {
            System.err.println("Invalid input. Please enter a number between 0 and 5.");
            System.exit(1);
        }

        // Validate the chosen mode
        if (mode < 0 || mode > 5) {
            System.err.println("Invalid mode! Please choose a number between 0 and 5.");
            System.exit(1);
        }

        // Create the TestModes object with the chosen mode
        Test test = new Test(nodeIds, mode);

        // Run the selected test
        test.run();

        // Close the scanner
        scanner.close();
    }
}
