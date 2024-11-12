package Core;

import java.io.Serializable;
import java.net.InetAddress;

public class NewConnectionRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private InetAddress clientAddress;
	private int clientPort;
	private Connection connection;

	NewConnectionRequest(Connection connection, InetAddress clientAddress, int clientPort) {
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
		this.connection = connection;
	}
	
	public InetAddress getClientAddress() {
		return clientAddress;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public int getClientPort() {
		return clientPort;
	}
	
	
}