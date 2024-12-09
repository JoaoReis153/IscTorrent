package Services;

import java.io.File;

import Core.Node;
import Core.Utils;
import Messaging.FileBlockAnswerMessage;
import Messaging.FileBlockRequestMessage;

public class SenderAssistant extends Thread {
  
  private final Node node;

  public SenderAssistant(Node node) {
    this.node = node;
  }


  public void run() {
    while (true) {
      try {
        processBlockRequest();
      } catch (InterruptedException e) {
        System.out.println("Error in SenderAssistant");
        e.printStackTrace();
      }
    }
  }

  public void processBlockRequest() throws InterruptedException {
    FileBlockRequestMessage request;

    request = this.node.getBlockRequest();
    FileBlockAnswerMessage answer = fillRequest(request);
    if(answer == null) return;
    if(request.getSenderAddress() == null) {
      System.out.println("SenderAssistant: Null address");
      return;
    } else if (request.getSenderPort() == 0) {
      System.out.println("SenderAssistant: Null port");
      return;
    }
    
    node.getPeerToSend(request.getSenderAddress(), request.getSenderPort()).sendFileBlockAnswer(answer);
  }


  public FileBlockAnswerMessage fillRequest(FileBlockRequestMessage request) {
    File file = findFileByHash(request.getHash());
    if(file == null) return null;
    return new FileBlockAnswerMessage(node.getAddress().getHostAddress(),node.getPort(), node.getId(), request, file);
  }


  private File findFileByHash(int hash) {
      File folder = new File(Node.WORK_FOLDER + node.getId() + "/");
      if (!folder.isDirectory()) {
          throw new IllegalArgumentException(
              "Invalid directory path: " + folder.getPath()
          );
      }

      File[] files = folder.listFiles();
      if (files == null) {
          throw new IllegalArgumentException(
              "Unable to list files in directory"
          );
      }

      for (File file : files) {
          if (file.isFile()) {
              if (Utils.calculateFileHash(file.getAbsolutePath()) == hash) {
                  return file;
              }
          }
      }
      return null;
  }
}
