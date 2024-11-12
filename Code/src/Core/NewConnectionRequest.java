package Core;

import java.io.Serializable;
import java.net.InetAddress;

public class NewConnectionRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private InetAddress clientAddress;
	private int clientPort;

	NewConnectionRequest(InetAddress clientAddress, int clientPort) {
		this.clientAddress = clientAddress;
		this.clientPort = clientPort;
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public int getClientPort() {
		return clientPort;
	}

}