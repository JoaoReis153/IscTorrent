package Tests;

import java.util.Scanner;

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

        // Read the test mode interactively from user input
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose the test mode:");
        System.out.println("0 - Create nodes");
        System.out.println("1 - Create a network of nodes and connect them");
        System.out.println("2 - Create a network, connect nodes, and search in the first node");
        System.out.println("3 - Create a network, connect nodes, and search in all nodes");
        System.out.println("4 - Create a network, connect nodes, and download files in the last node");
        System.out.println("5 - Create a network, connect nodes, and download files in every node");
        System.out.print("Enter the test number: ");

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
        TestModes test = new TestModes(args, mode);

        // Run the selected test
        test.run();

        // Close the scanner
        scanner.close();
    }
}
