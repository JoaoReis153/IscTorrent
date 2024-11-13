package Core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Connection {

	private int port;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	Connection(InetAddress address, int port) {
		this.port = port;
		try {

			socket = new Socket(address, port);
			
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			
		} catch (SocketTimeoutException e) {
			System.err.println("Connection timed out while connecting to " + address + "::" + port);
			close(); 
		} catch (IOException e) {
			System.err.println("I/O error occurred while connecting to " + address + "::" + port + " - " + e.getMessage());
			close();
			return;
		}
	}

	Connection(Socket socket) {
		this.socket = socket;
		this.port = socket.getPort();
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		this.inputStream = new ObjectInputStream(socket.getInputStream());
	}

	public int getPort() {
		return port;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectInputStream getInputStream() {
		if(inputStream == null || outputStream == null) {
			return null;
		}
		return inputStream;
	}

	public ObjectOutputStream getOutputStream() {
		if(inputStream == null || outputStream == null) {
			return null;
		}
		return outputStream;
	}

	public void close() {
		try {
			if (inputStream != null)
				inputStream.close();
			if (outputStream != null)
				outputStream.close();
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			System.err.println("Error while closing resources: " + e.getMessage());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Connection that = (Connection) o;
		return socket.equals(that.socket);
	}

	@Override
	public String toString() {
		return "Connection [port=" + port + ", socket=" + socket + ", inputStream=" + inputStream + ", outputStream="
				+ outputStream + "]";
	}

}
