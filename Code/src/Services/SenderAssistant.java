package Services;

import Core.Node;
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
    return new FileBlockAnswerMessage(node.getAddress().getHostAddress(),node.getPort(), node.getId(), request);
  }

}
