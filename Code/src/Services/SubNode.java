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

	public SubNode(Node node, Socket clientSocket , GUI gui) {
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

				if (obj instanceof NewConnectionRequest) {
					System.out.println("Received a connection request");
			
				} else if (obj instanceof WordSearchMessage) {
					System.out.println("Received a WordSearchMessage object with content: ("
							+ ((WordSearchMessage) obj).getKeyword() + ")");
					
							
					if (node.getFolder().exists() && node.getFolder().isDirectory()) {
						sendFileSearchResultList((WordSearchMessage) obj);
					}
					
				} else if (obj instanceof FileSearchResult[]) {

					FileSearchResult[] searchResultList = (FileSearchResult[]) obj;

					gui.loadListModel(searchResultList);

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
				System.out.println("Sent a WordSearchMessageRequest with keyword: " + keyword);
				out.writeObject(searchPackage);
				out.flush();
			} catch (IOException e) {
				System.out.println("[ERROR::WordSearchMessageRequest]");
				e.printStackTrace();
			}
		}
	}

	public void sendFileSearchResultList(WordSearchMessage obj) {
		File[] files = node.getFolder().listFiles();
		if (files != null) {
			String keyword = obj.getKeyword().toLowerCase();
			int keywordCount = 0;
			
			for (File file : files) {
				String fileName = file.getName().toLowerCase();
				if(fileName.contains(keyword)) {
					keywordCount++;
				}
			}
			
			if (keywordCount == 0) return;
			
			FileSearchResult[] f = new FileSearchResult[keywordCount];
			int counter = 0;
			
			for (File file : files) {
				String h1 = Utils.generateSHA256(file.getAbsolutePath());
				String fileName = file.getName().toLowerCase();
				if(fileName.contains(keyword)) {									
					FileSearchResult response = new FileSearchResult( obj, file.getName(), h1, file.length(), node.getEnderecoIP(), node.getPort());
					f[counter++] = response;
				}
			}
			
			try {
				this.out.writeObject(f);
				this.out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("There was a problem sending the list of FileSearchResults");
				//e.printStackTrace()
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
			//e.printStackTrace()
		}

		System.out.println("Added new node::NodeAddress [address=" + endereco + " port=" + targetPort + "]");

	}

}
