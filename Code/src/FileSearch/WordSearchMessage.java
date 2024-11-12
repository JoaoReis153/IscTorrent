package FileSearch;

import java.io.Serializable;
import java.net.InetAddress;

import Core.Node;

public class WordSearchMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String keyword;
	private InetAddress endereco;
	private int port;

	public WordSearchMessage(String keyword, InetAddress endereco, int port) {
		this.keyword = keyword;
		this.endereco = endereco;
		this.port = port;
	}

	public String getKeyword() {
		return keyword;
	}

	public InetAddress getEndereco() {
		return endereco;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "WordSearchMessage [keyword=" + keyword + ", endereco=" + endereco + ", port=" + port + "]";
	}

}
