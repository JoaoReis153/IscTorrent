package Core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Connection {

	private int targetport;
	private int originport;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	Connection(InetAddress address, int originport, int targetport) {
		this.originport = originport;
		this.targetport = targetport;
		try {

			socket = new Socket(address, targetport);
			
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			
		} catch (SocketTimeoutException e) {
			System.err.println("Connection timed out while connecting to " + address + "::" + port);
			close(); 
		} catch (IOException e) {
			System.err.println("I/O error occurred while connecting to " + address + "::" + port + " - " + e.getMessage());
			close();
		}
	}

	Connection(Socket socket) {
		this.socket = socket;
		this.port = socket.getPort();
		try {
			
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.inputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("Error creating the object streams for the connection");
		}
	}

	public int getPort() {
		return port;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectInputStream getInputStream() {
		if(inputStream == null) 
			System.out.println("The input stream is null");
		
		return inputStream;
	}

	public ObjectOutputStream getOutputStream() {
		if(outputStream == null) 
			System.out.println("The output stream is null");
		
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
		return "Connection [Ports(from/to):" + originport + "::" + targetport + ", Address(From/To):" + socket.getLocalAddress().toString() + "::" + socket.getInetAddress().toString() + "]";
	}

}
