package Core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Connection {

	private int port;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	Connection(InetAddress address, int port) throws IOException {
		socket = new Socket(address, port);
		this.port = port;
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());		
		this.inputStream = new ObjectInputStream(socket.getInputStream());
	}
	
	Connection(Socket socket) throws IOException {
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
		return inputStream;
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
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