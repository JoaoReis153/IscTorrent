package Tests;

import java.io.File;
import java.util.*;

import Core.FileBlockRequestMessage;
import GUI.GUI;

public class Main {
    public static void main(String[] args) {

        //to see the content of file given in the ^(argument)
        /*
        GUI window = new GUI("192.168.1.1", 80 , "Code/dl1");
        window.open();
        */
        
        // a divisao é dividida em blocos de 10240 bytes (enunciado) e depois o ultimo bloco pelo restante
        // TODO testar com ficheiros de tamanho diferente porque com continhas ja vai lá :]
        String fileHash = "testestestsetse";
        long fileSize = 24875;
        int blockSize = 10240;

        List<FileBlockRequestMessage> blockList = FileBlockRequestMessage.createBlockList(fileHash, fileSize, blockSize);

        for (FileBlockRequestMessage block : blockList) {
            System.out.println("Hash: " + block.getHash() + ", Offset: " + block.getOffset() + ", Length: " + block.getLength());
        }
    }
    
}
 