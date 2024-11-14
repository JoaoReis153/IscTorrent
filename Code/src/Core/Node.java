package Core;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.SwingUtilities;

import FileSearch.FileSearchResult;
import FileSearch.WordSearchMessage;
import GUI.GUI;
import Messaging.Connection;
import Messaging.NewConnectionRequest;
import Services.ClientHandler;

public class Node {

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

		this.port += nodeId;
		String workfolder = "Code/dl" + nodeId;
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

	public void startServing() {

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			this.serverSocket = serverSocket;
			while (true) {

				Socket socket = serverSocket.accept();
				Connection connection = new Connection(socket, port);
				peers.add(connection);
				new ClientHandler(connection, this).start();

			}
		} catch (IOException e) {
			System.err.println("Failed to start server: " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Awaiting connection...");
	}

	public void connectToNode(String nomeEndereco, int targetPort) {

		Socket targetSocket = null;
		Connection connection = null;
		InetAddress targetEndereco = null;

		// Validacao do endereco
		try {
			targetEndereco = InetAddress.getByName(nomeEndereco);
		} catch ( IOException e ) {
			System.out.println("Wasn't able to connect to the address");
		}

		// Validacao do endereco
		if (targetEndereco == null || !targetEndereco.equals(this.endereco)) {
			System.out.println("Issues connecting with node::NodeAddress [address=" + nomeEndereco + " port=" + targetPort + "] - Invalid address");
			return;
		}

		
		if (this.port > 8080 && targetPort <= 8080) {
			System.out.println("Issues connecting with node::NodeAddress [address=" + nomeEndereco + " port=" + targetPort + "] - Cannot connect to ports below or equal to 8080.");
			return;
		}

		// Tentativa de conexao
	    try {
	        
	        if (targetEndereco.equals(this.endereco) && targetPort == this.port) {
	            System.out.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "] - Can't connect to itself");
	            return;
	        }
	        
	        connection = new Connection(targetEndereco, port, targetPort);

	        targetSocket = connection.getSocket();
	        
	        
	        ObjectOutputStream out = connection.getOutputStream();

			if (out == null) {
				System.out.println("Outputstream / Inputstream null [invalid port: " + targetPort + "]");
				return;
			}

	        NewConnectionRequest request = new NewConnectionRequest(endereco, targetPort);
	        out.writeObject(request);
	        out.flush();
	        
	        System.out.println("Added new node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "]");

	    } catch (IOException e) {
	        System.err.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "] - " + e);
	        targetSocket = null;
	    }

	    // Adicionar a conexao
	    if (targetSocket != null && connection != null) {
	        peers.add(connection);
	    } else {
	        System.err.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port=" + targetPort + "] - Null Socket or Connection");
	    }
	}
	

	public void sendWordSearchMessageRequest(String keyword) {
		WordSearchMessage searchPackage = new WordSearchMessage(keyword, endereco, port);
		try {
			for(Connection peer : peers) {
				
				ObjectOutputStream objectOut = peer.getOutputStream();
				
				 if (objectOut != null) {
		                System.out.println("WordSearchMessage object sent to " + endereco + "::" + port);
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
	
	public Set<Connection> getPeers() {
		return peers;
	}
}

