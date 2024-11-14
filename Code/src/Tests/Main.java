package Tests;

import java.io.File;
import java.io.IOException;
import java.util.*;

import Core.Node;
import GUI.GUI;
import Messaging.FileBlockRequestMessage;

public class Main {
	public static void main(String[] args) {

		// Parse the nodeId from the command-line argument
		int test = 4;
		if (test == 1) {



		} else if (test == 2) {
			

			// Iniciar o servidor do no2 em uma nova thread
	


			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Tentar conectar no1 ao no2
			
			
			
			
			try {
				Thread.sleep(1000);
				
			
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		} else if (test == 3) {

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
			 

		} else if (test == 4) {
			int argument = Integer.parseInt(args[0]);
			GUI gui1 = new GUI(argument);
			gui1.open();

		}

	}
}