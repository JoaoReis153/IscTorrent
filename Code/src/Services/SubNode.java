package Services;

import java.io.*;
import java.net.*;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;
import GUI.GUI;
import Messaging.FileBlockRequestMessage;
import Messaging.NewConnectionRequest;

public class SubNode extends Thread {

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Socket clientSocket;
	private Node node;
	private GUI gui;

	// Constructor
	public SubNode(Node node, Socket clientSocket, GUI gui) {
		this.clientSocket = clientSocket;
		this.node = node;
		this.gui = gui;
	}

	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
			Object obj;
			while ((obj = in.readObject()) != null) {
				// Handle New Connection Request
				if (obj instanceof NewConnectionRequest) {
					System.out.println("Received a connection request");

				// Handle Word Search Message
				} else if (obj instanceof WordSearchMessage) {
					System.out.println("Received WordSearchMessage with content: ("
							+ ((WordSearchMessage) obj).getKeyword() + ")");
					if (node.getFolder().exists() && node.getFolder().isDirectory()) {
						sendFileSearchResultList((WordSearchMessage) obj);
					}

				// Handle File Search Result List
				} else if (obj instanceof FileSearchResult[]) {
					FileSearchResult[] searchResultList = (FileSearchResult[]) obj;
					gui.loadListModel(searchResultList);

				// Handle File Block Request
				} else if (obj instanceof FileBlockRequestMessage) {
					System.out.println("Received FileBlockRequestMessage");
					System.out.println(obj.toString());
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error handling client: " + e);
		} finally {
			// Close resources
			try {
				if (in != null) in.close();
				if (out != null) out.close();
				if (clientSocket != null) clientSocket.close();
			} catch (IOException e) {
				System.out.println("Error closing resources: " + e.getMessage());
			}
		}
	}

	// Send Word Search Request to peer
	public void sendWordSearchMessageRequest(String keyword) {
		WordSearchMessage searchPackage = new WordSearchMessage(keyword);
		if (out != null) {
			try {
				System.out.println("Sent WordSearchMessageRequest with keyword: " + keyword);
				out.writeObject(searchPackage);
				out.flush();
			} catch (IOException e) {
				System.out.println("[ERROR::WordSearchMessageRequest]");
				e.printStackTrace();
			}
		}
	}

	// Send File Search Result List
	public void sendFileSearchResultList(WordSearchMessage obj) {
		File[] files = node.getFolder().listFiles();
		if (files != null) {
			String keyword = obj.getKeyword().toLowerCase();
			int keywordCount = 0;

			// Count matching files
			for (File file : files) {
				if (file.getName().toLowerCase().contains(keyword)) {
					keywordCount++;
				}
			}

			if (keywordCount == 0) return;

			FileSearchResult[] results = new FileSearchResult[keywordCount];
			int counter = 0;

			// Create FileSearchResult objects
			for (File file : files) {
				String hash = Utils.generateSHA256(file.getAbsolutePath());
				if (file.getName().toLowerCase().contains(keyword)) {
					results[counter++] = new FileSearchResult(obj, file.getName(), hash, file.length(),
							node.getEnderecoIP(), node.getPort());
				}
			}

			// Send results
			try {
				this.out.writeObject(results);
				this.out.flush();
			} catch (IOException e) {
				System.err.println("Error sending FileSearchResults");
			}
		}
	}

	// Send New Connection Request
	public void sendNewConnectionRequest(InetAddress endereco, int targetPort) {
		if (out == null) {
			System.out.println("OutputStream is null [invalid port: " + targetPort + "]");
			return;
		}

		NewConnectionRequest request = new NewConnectionRequest(endereco, targetPort);
		try {
			out.writeObject(request);
			out.flush();
		} catch (IOException e) {
			System.out.println("[ERROR::NewConnectionRequest]");
		}

		System.out.println("Added new node::NodeAddress [address=" + endereco + " port=" + target
