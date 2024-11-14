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

	// Construtor
	public Node(int nodeId , GUI gui) {

		this.gui = gui;

		// Validacao do ID do node
		if (nodeId < 0) {
			System.err.println("ID do node inválido");
			System.exit(1);
		}
		this.port += nodeId;

		// Criar a pasta de trabalho se não existir
		String workfolder = "Code/dl" + nodeId;
		this.folder = new File(workfolder);
		if (!this.folder.exists()) {
			boolean created = this.folder.mkdirs();
			if (!created) {
				System.out.println("Não foi possível criar a pasta dl" + nodeId);
				System.exit(1);
			}
		}

		// Obter o endereco do dispositivo
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

	// Iniciar o servidor
	public void startServing() {

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {

				Socket clientSocket = serverSocket.accept();
				System.out.println("Started serving");
				SubNode clientHandler = new SubNode(this, clientSocket , gui);
				clientHandler.start();
				peers.add(clientHandler);

			}
		} catch (IOException e) {
			System.err.println("Failed to start server: " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Awaiting connection...");
	}

	public void broadcastWordSearchMessageRequest(String keyword) {

		for (SubNode peer : peers) {

			peer.sendWordSearchMessageRequest(keyword);

		}
	}

	public void connectToNode(String nomeEndereco, int targetPort) {
		Socket clientSocket = null;
		InetAddress targetEndereco = null;

		// Validacao do endereco
		try {
			targetEndereco = InetAddress.getByName(nomeEndereco);

			// Validacao do endereco
			if (targetEndereco == null) {
				System.out.println("Issues connecting with node::NodeAddress [address=" + nomeEndereco + " port="
						+ targetPort + "] - Invalid address");
				return;
			}
		} catch (IOException e) {
			System.out.println("Wasn't able to connect to the address");
		}

		if (targetPort <= 8080 || targetPort >= 65535) {
			System.out.println("Issues connecting with node::NodeAddress [address=" + nomeEndereco + " port="
					+ targetPort + "] - Cannot connect to ports below or equal to 8080.");
			return;
		}

		// Tentativa de conexao
		try {

			if (targetEndereco.equals(this.endereco) && targetPort == this.port) {
				System.out.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port="
						+ targetPort + "] - Can't connect to itself");
				return;
			}

			clientSocket = new Socket(targetEndereco, targetPort);
			SubNode handler = new SubNode(this, clientSocket , gui);
			handler.start();
			peers.add(handler);

			Thread.sleep(100);

			handler.sendNewConnectionRequest(endereco, targetPort);

		} catch (IOException | InterruptedException e) {
			System.err.println("Issues connecting with node::NodeAddress [address=" + targetEndereco + " port="
					+ targetPort + "] - " + e);
			clientSocket = null;
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
