package Tests;

import java.io.File;
import java.io.IOException;
import java.util.*;

import Core.FileBlockRequestMessage;
import Core.Node;
import GUI.GUI;

public class Main {
	public static void main(String[] args) {

		// to see the content of file given in the ^(argument)
		/*
		 * GUI window = new GUI("192.168.1.1", 80 , "Code/dl1"); window.open();
		 */
		/*
		 * String workfolder = "./dl1";
		 * 
		 * File folder = new File(workfolder);
		 * 
		 * File[] files = folder.listFiles(); for (File file : files) { String fileName
		 * = file.getName(); System.out.println("----------" + fileName + " (size:" +
		 * file.length() + ")----------");
		 * 
		 * // a divisao é dividida em blocos de 10240 bytes (enunciado) e depois o
		 * ultimo // bloco pelo restante // testar com ficheiros de tamanho diferente
		 * porque com continhas ja vai lá :] long fileSize = file.length(); int
		 * blockSize = 10240;
		 * 
		 * List<FileBlockRequestMessage> blockList =
		 * FileBlockRequestMessage.createBlockList(fileName, fileSize, blockSize);
		 * 
		 * for (FileBlockRequestMessage block : blockList) { System.out.println("Hash: "
		 * + block.getHash() + ", Offset: " + block.getOffset() + ", Length: " +
		 * block.getLength()); } }
		 */

		if (args.length != 1) {
			System.out.println("Usage: java Main <nodeId>");
			System.exit(1);
		}

		// Parse the nodeId from the command-line argument
		int nodeId = Integer.parseInt(args[0]);

		// Create a Node object with the input nodeId
		Node node = new Node(nodeId);
		 
		GUI gui = new GUI(node);
		gui.open();

		/*
		Node no1 = new Node(1); 
		Node no2 = new Node(2);
		System.out.println("No1 end: " + no1.getEndereco() + " , " + no1.getPort());
		System.out.println("No2 end: " + no2.getEndereco() + " , " + no2.getPort());

		// Iniciar o servidor do no2 em uma nova thread
		new Thread(() -> { 
		    try {
		        no2.startServing(); 
		    } catch (IOException e) { 
		        e.printStackTrace(); 
		    }
		}).start();

		// Dar um pequeno atraso para garantir que o servidor de no2 esteja escutando antes da conexão
		try { 
		    Thread.sleep(500); 
		} catch (InterruptedException e) { 
		    // TODO Auto-generated catch block 
		    e.printStackTrace(); 
		}

		// Tentar conectar no1 ao no2 
		try { 
		    no1.connectToNode("127.0.0.1", 8082); 
		} catch (IOException e) { 
		    e.printStackTrace(); 
		}

		 */
	
	}
}
