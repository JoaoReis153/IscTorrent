package Core;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

import GUI.GUI;
import Services.SubNode;

public class Node {

	private InetAddress endereco;
	private final File folder;
	private CopyOnWriteArrayList<SubNode> peers = new CopyOnWriteArrayList<>();
	private GUI gui;

	private int port = 8080;

	// Constructor
	public Node(int nodeId, GUI gui) {

		this.gui = gui;

		// Validate Node ID
		if (nodeId < 0) {
			System.err.println("Invalid node ID");
			System.exit(1);
		}
		this.port += nodeId;

		// Create working directory if it doesn't exist
		String workfolder = "Code/dl" + nodeId;
		this.folder = new File(workfolder);
		if (!this.folder.exists()) {
			boolean created = this.folder.mkdirs();
			if (!created) {
				System.out.println("Failed to create directory: dl" + nodeId);
				System.exit(1);
			}
		}

		// Get device address
		try {
			endereco = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Unable to get the device's address: \n" + e);
			System.exit(1);
		}
	}

	// Getter for the working folder
	public File getFolder() {
		return folder;
	}

	// Start server to accept connections
	public void startServing() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connection established");
				SubNode clientHandler = new SubNode(this, clientSocket, gui);
				clientHandler.start();
				peers.add(clientHandler);
			}
		} catch (IOException e) {
			System.err.println("Failed to start server: " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Awaiting connection...");
	}

	// Broadcast a word search message to all peers
	public void broadcastWordSearchMessageRequest(String keyword) {
		for (SubNode peer : peers) {
			peer.sendWordSearchMessageRequest(keyword);
		}
	}

	// Connect to another node
	public void connectToNode(String nomeEndereco, int targetPort) {
		Socket clientSocket = null;
		InetAddress targetEndereco = null;

		// Validate address
		try {
			targetEndereco = InetAddress.getByName(nomeEndereco);
			if (targetEndereco == null) {
				System.out.println("Failed to connect to node::NodeAddress [address=" + nomeEndereco + " port="
						+ targetPort + "] - Invalid address");
				return;
			}
		} catch (IOException e) {
			System.out.println("Unable to resolve address: " + nomeEndereco);
		}

		// Validate port
		if (targetPort <= 8080 || targetPort >= 65535) {
			System.out.println("Failed to connect to node::NodeAddress [address=" + nomeEndereco + " port="
					+ targetPort + "] - Invalid port range");
			return;
		}

		// Attempt connection
		try {
			if (targetEndereco.equals(this.endereco) && targetPort == this.port) {
				System.out.println("Failed to connect to node::NodeAddress [address=" + targetEndereco + " port="
						+ targetPort + "] - Cannot connect to itself");
				return;
			}

			clientSocket = new Socket(targetEndereco, targetPort);
			SubNode handler = new SubNode(this, clientSocket, gui);
			handler.start();
			peers.add(handler);

			Thread.sleep(100);

			handler.sendNewConnectionRequest(endereco, targetPort);

		} catch (IOException | InterruptedException e) {
			System.err.println("Failed to connect to node::NodeAddress [address=" + targetEndereco + " port="
					+ targetPort + "] - " + e);
		}
	}

	// String representation of the node
	@Override
	public String toString() {
		return "Node [endereco=" + endereco + ", port=" + port + "]";
	}

	// Getter for node's address
	public InetAddress getEndereco() {
		return endereco;
	}

	// Getter for node's IP as a string
	public String getEnderecoIP() {
		String enderecoString = endereco.toString();
		return enderecoString.substring(enderecoString.indexOf("/") + 1);
	}

	// Getter and setter for port
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
