package Messaging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Connection {

	private int targetPort;
	private int originPort;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	public Connection(InetAddress targetAddress, int originport, int targetport) {
		this.originPort = originport;
		this.targetPort = targetport;

		try {
			socket = new Socket(targetAddress, targetport);
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			
		} catch (SocketTimeoutException e) {
			System.err.println("Connection timed out while connecting to " + targetAddress + "::" + targetPort);
			close(); 

		} catch (IOException e) {
			System.err.println("I/O error occurred while connecting to " + targetAddress + "::" + targetPort + " - " + e.getMessage());
			close();
		}
	}

	public Connection(Socket socket, int originPort) {
		this.socket = socket;
		this.originPort = originPort;
		try {
			this.outputStream = new ObjectOutputStream(socket.getOutputStream());
			this.inputStream = new ObjectInputStream(socket.getInputStream());
			
		} catch (IOException e) {
			System.err.println("Error creating the object streams for the connection");
		}
	}

	// getters
	public int getOriginPort() {
		return originPort;
	}
	
	public int getTargetPort() {
		return targetPort;
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
		return "Connection [Ports(from/to):" + originPort + "::" + targetPort + ", Address(From/To):" + socket.getLocalAddress().toString() + "::" + socket.getInetAddress().toString() + "]";
	}

}
