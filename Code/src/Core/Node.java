package Core;

import java.io.*;
import java.net.*;
import java.util.*;

import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;

public class Node {

	public static class DealWithClient extends Thread {
		private ObjectInputStream in;

		private ObjectOutputStream out;

		private Socket socket;

		private Node node;
		
		DealWithClient(Connection connection, Node node) throws IOException {
			this.socket = connection.getSocket();
			this.in = connection.getInputStream();
			this.out = connection.getOutputStream();
			this.node = node;
		}

		@Override
		public void run() {
			try {
				Object obj;
				while ((obj = in.readObject()) != null) {
					
					if (obj instanceof NewConnectionRequest) {
						System.out.println("Received a connection request");
						
					} else if (obj instanceof WordSearchMessage) {
						System.out.println("Received a WordSearchMessage object from content: (" + ((WordSearchMessage) obj).getKeyword() + ")");
					} else if (obj instanceof FileSearchResult) {
						
					} else if (obj instanceof FileBlockRequestMessage) {
						FileBlockRequestMessage block = (FileBlockRequestMessage) obj;
						System.out.println("Received block: " + block.getHash());
						
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Error handling client: " + e);
			} finally {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
					if (socket != null)
						socket.close();
				} catch (IOException e) {
					System.out.println("Error closing resources: " + e.getMessage());
				}
			}
		}
	}

	private Socket clientSocket;
	private final int nodeId;
	private InetAddress endereco;
	private final File folder;
	private ServerSocket serverSocket;
	private Set<Connection> peers = new HashSet<>(); 

	private int port = 8080;

	// Construtor
	public Node(int nodeId) {
		if (nodeId < 0) {
			System.err.println("ID do node inválido");
			System.exit(1);
		}
		this.nodeId = nodeId;
		this.port += nodeId;
		String workfolder = "./dl" + nodeId;
		this.folder = new File(workfolder);

		if (!this.folder.exists()) {
			boolean created = this.folder.mkdirs();
			if (!created) {
				System.out.println("Não foi possível criar a pasta dl" + nodeId);

				System.exit(1);
			}
		}

		try {
			endereco = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			System.out.println("Não foi possível obter o endereço deste dispositivo: \n" + e);
			System.exit(1);
		}

	}

	public File getFolder() {
		return folder;
	}

	public void startServing() throws IOException {
		System.out.println("Awaiting connection...");
		this.serverSocket = new ServerSocket(port);
		try {
			while (true) {

				Socket socket = serverSocket.accept();
				Connection connection = new Connection(socket);
				peers.add(connection);
				new DealWithClient(connection, this).start();

			}
		} finally {
			serverSocket.close();
		}
	}

	public void connectToNode(String nomeEndereco, int targetPort) throws IOException {
		InetAddress targetEndereco = InetAddress.getByName(nomeEndereco);
		Socket targetSocket = null;
		try {

			if (targetEndereco .equals(this.endereco) && targetPort == this.port) {
				System.out.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "]");
				return;
			}

			
			Connection connection = new Connection(endereco, targetPort);
			targetSocket = connection.getSocket();
			ObjectOutputStream out = connection.getOutputStream();
			NewConnectionRequest request = new NewConnectionRequest(connection, endereco, targetPort);
			out.writeObject(request);
			out.flush();
			System.out.println("Added new node::NodeAddress [address=" + targetEndereco  + " port=" + targetPort + "]");

		} catch (IOException e) {
			System.out.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "] - "
					+ e.getMessage());
			targetSocket  = null;
		}

		Connection connection = new Connection(endereco, targetPort);
		
		if (targetSocket  != null) {
			peers.add(connection);
		} else {
			System.out.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "] - Null Socket");
		}
	}
	
	public void sendWordSearchMessageRequest(String keyword) {
		WordSearchMessage searchPackage = new WordSearchMessage(keyword, endereco, port);
		try {
			for(Connection peer : peers) {
				
				ObjectOutputStream objectOut = peer.getOutputStream();
				
				 if (objectOut != null) {
		                System.out.println("Sent a search package");
		                objectOut.writeObject(searchPackage);
		                objectOut.flush();
		            }
			}

		} catch (IOException e) {
			System.out.println("Error sending object: " + e.getMessage());
		}
	}

	@Override
	public String toString() {
		return "Node [endereco=" + endereco + ", port=" + port + "]";
	}

	public InetAddress getEndereco() {
		return endereco;
	}

	public String getEnderecoIP() {
		String enderecoString = endereco.toString();
		String result = enderecoString.substring(enderecoString.indexOf("/") + 1);
		return result;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}

