package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

import Core.Node;

public class GuiNode {
    private JFrame frame;
    private Node node;
    public GuiNode(Node node) {
    	this.node = node;
        frame = new JFrame("Adicionar Nó");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // Fechar apenas esta janela

        addFrameContent();

        frame.pack(); // Ajusta o tamanho da janela
    }

    public void open() {
        frame.setVisible(true); // Torna a janela visível
    }

    private void addFrameContent() {
        // Usando FlowLayout para organizar os componentes em uma linha
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Alinhamento à esquerda com espaçamento

        // Rótulo e campo de texto para Endereço
        JLabel addressLabel = new JLabel("Endereço:");
        JTextField addressField = new JTextField(15); // Campo de texto para o endereço
        frame.add(addressLabel);
        frame.add(addressField);

        // Rótulo e campo de texto para Porta
        JLabel portLabel = new JLabel("Porta:");
        JTextField portField = new JTextField(5); // Campo de texto para a porta
        frame.add(portLabel);
        frame.add(portField);

        // Botões Cancelar e OK
        JButton cancelButton = new JButton("Cancelar");
        JButton okButton = new JButton("OK");

        // Adicionando funcionalidade aos botões
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Fecha a janela quando Cancelar é clicado
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Aqui você pode adicionar a lógica para o que fazer com o endereço e porta
                String address = addressField.getText();
                String portText = portField.getText();
                int port = Integer.parseInt(portText);
               
                JOptionPane.showMessageDialog(frame, "Endereço: " + address + "\nPorta: " + port);
                
                try {
					node.connectToNode(address, port);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });

        frame.add(cancelButton);
        frame.add(okButton);
    }

}