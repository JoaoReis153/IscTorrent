package Services;

import java.io.*;
import java.net.*;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;
import Messaging.FileBlockRequestMessage;
import Messaging.NewConnectionRequest;

public class SubNode extends Thread {

	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Socket clientSocket;
	private Node node;

	public SubNode(Node node, Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.node = node;
	}

	@Override
	public void run() {
		try {
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
			Object obj;
			while ((obj = in.readObject()) != null) {

				if (obj instanceof NewConnectionRequest) {
					System.out.println("Received a connection request");

				} else if (obj instanceof WordSearchMessage) {
					System.out.println("Received a WordSearchMessage object with content: ("
							+ ((WordSearchMessage) obj).getKeyword() + ")");
					if (node.getFolder().exists() && node.getFolder().isDirectory()) {
						File[] files = node.getFolder().listFiles();
						if (files != null) {
							for (File file : files) {
								String h1 = Utils.generateSHA256(file.getAbsolutePath());
								FileSearchResult response = new FileSearchResult((WordSearchMessage) obj,
										file.getName(), h1, file.length(), node.getEnderecoIP(), node.getPort());
								this.out.writeObject(response);
								this.out.flush();
								System.out.println(file.getName());
							}
						}
					}
				} else if (obj instanceof FileSearchResult) {
					System.out.println("Received FileSearchResult");
					System.out.println(obj.toString());
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
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				if (clientSocket != null)
					clientSocket.close();
			} catch (IOException e) {
				System.out.println("Error closing resources: " + e.getMessage());
			}
		}
	}

	public void sendWordSearchMessageRequest(String keyword) {
		WordSearchMessage searchPackage = new WordSearchMessage(keyword);
		if (out != null) {
			try {
				System.out.println("Sent a WordSearchMessageRequest");
				out.writeObject(searchPackage);
				out.flush();
			} catch (IOException e) {
				System.out.println("[ERROR::WordSearchMessageRequest]");
				e.printStackTrace();
			}
		}
	}

	public void sendNewConnectionRequest(InetAddress endereco, int targetPort) {
		if (out == null) {
			System.out.println("Outputstream / Inputstream null [invalid port: " + targetPort + "]");
			return;
		}

		NewConnectionRequest request = new NewConnectionRequest(endereco, targetPort);
		try {
			out.writeObject(request);
			out.flush();
		} catch (IOException e) {
			System.out.println("[ERROR::NewConnectionRequest]");
			e.printStackTrace();
		}

		System.out.println("Added new node::NodeAddress [address=" + endereco + " port=" + targetPort + "]");

	}

}
