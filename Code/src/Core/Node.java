package Core;

import java.io.*;
import java.net.*;
import java.util.*;

public class Node {

	private class NewConnectionRequest {
		private int port;

		NewConnectionRequest(int port) {
			this.port = port;
		}

		int getPort() {
			return port;
		}

	}

	public static class DealWithClient extends Thread {
		private ObjectInputStream in;

		private PrintWriter out;

		private Socket socket;

		DealWithClient(Socket socket) throws IOException {
			this.socket = socket;

			in = new ObjectInputStream(socket.getInputStream()); // Change to ObjectInputStream
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		}

		@Override
		public void run() {
			try {
				Object obj;
				while ((obj = in.readObject()) != null) {
					if (obj instanceof String) {
						String str = (String) obj;
						if (str.equals("FIM")) {
							break;
						}
						System.out.println("Eco: " + str);
						out.println(str);
					}
					// Handle other types of objects (e.g., FileBlockRequestMessage)
					else if (obj instanceof FileBlockRequestMessage) {
						FileBlockRequestMessage block = (FileBlockRequestMessage) obj;
						System.out.println("Received block: " + block.getHash());
						// You can add logic to handle the FileBlockRequestMessage
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Error handling client: " + e.getMessage());
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

	private BufferedReader in;
	private PrintWriter out;
	private Socket clientSocket;
	private final int nodeId;
	private InetAddress endereco;
	private Set<InetAddress> peers;
	private final File folder;
	private ServerSocket serverSocket;
	private int port = 8080;

	// Construtor
	public Node(int nodeId) {
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

		this.peers = new HashSet<>();

	}

	public File getFolder() {
		return folder;
	}

	public void startServing() throws IOException {
		this.serverSocket = new ServerSocket(port);
		try {
			while (true) {

				Socket socket = serverSocket.accept();

				new DealWithClient(socket).start();

			}
		} finally {
			serverSocket.close();
		}
	}

	public void connectToNode(String nomeEndereco, int targetPort) throws IOException {
		try {
			InetAddress endereco = InetAddress.getByName(nomeEndereco);
			System.out.println("Endereco:" + endereco + " - Port: " + targetPort);

			if (endereco.equals(this.endereco) && targetPort == this.port) {
				System.out.println("Não foi possível estabelecer a ligação");
				return;
			}
			
			clientSocket = new Socket(endereco, targetPort);
			System.out.println("Ligado ao " + endereco + " na porta " + targetPort);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
		} catch (IOException e) {
			System.out.println(
					"Problema na conexão com o " + nomeEndereco + " na porta " + targetPort + ": " + e.getMessage());
			clientSocket = null;
		}
		
		if (clientSocket != null) {
			System.out.println("Client Socket: " + clientSocket);
			peers.add(endereco);
		} else {
			System.out.println("Houve um problema na conexão. Client Socket está a null.");
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
