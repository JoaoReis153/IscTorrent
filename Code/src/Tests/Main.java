package Tests;

import java.io.File;
import java.io.IOException;
import java.util.*;

import Core.FileBlockRequestMessage;
import Core.Node;
import GUI.GUI;

public class Main {
	public static void main(String[] args) {

		// Parse the nodeId from the command-line argument
		int test = 1;
		if (test == 1) {

			// Create a Node object with the input nodeId
			Node no1 = new Node(1);
			GUI gui1 = new GUI(no1);
			gui1.open();

			Node nooo = new Node(2);
			GUI gui2 = new GUI(nooo);
			gui2.open();

		} else if (test == 2) {

			Node no1 = new Node(4);
			Node no2 = new Node(5);
			
			// Iniciar o servidor do no2 em uma nova thread
			new Thread(() -> {
				try {
					no2.startServing();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

			// Dar um pequeno atraso para garantir que o servidor de no2 esteja escutando
			// antes da conexão
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Tentar conectar no1 ao no2
			
			try {
				no1.connectToNode("127.0.0.1", 8085);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try {
				Thread.sleep(500);
				no1.sendWordSearchMessageRequest("batata");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		} else if (test == 3) {

			// to see the content of file given in the ^(argument)
			/*
			 * GUI window = new GUI("192.168.1.1", 80 , "Code/dl1"); window.open();
			 */
			
			  String workfolder = "./dl1";
			  
			  File folder = new File(workfolder);
			  
			  File[] files = folder.listFiles(); 
			  for (File file : files) { 
				  String fileName = file.getName(); 
				  System.out.println("----------" + fileName + " (size:" + file.length() + ")----------");
			  
			  // a divisao é dividida em blocos de 10240 bytes (enunciado) e depois o ultimo 
			  // bloco pelo restante 
			  // testar com ficheiros de tamanho diferente porque com continhas ja vai lá :] 
			  long fileSize = file.length(); 
			  int blockSize = 10240;
			  
			  List<FileBlockRequestMessage> blockList = FileBlockRequestMessage.createBlockList(fileName, fileSize, blockSize);
			  
			  for (FileBlockRequestMessage block : blockList) { 
				  System.out.println("Hash: " + block.getHash() + ", Offset: " + block.getOffset() + ", Length: " + block.getLength()); 
				  } 
			  }
			 

		}

	}
}
