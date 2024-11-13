package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Core.Node;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

public class GUI {
    private JFrame frame;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private ArrayList<String> allFiles;
    private File folder; // pasta de trabalho quando se inicia o programa
    private Node node;

    // Construtor da classe GUI onde recebe o endereço , porta e a pasta de trabalho
    public GUI(Node node) {
    	this.node = node;
        this.folder = node.getFolder();
        frame = new JFrame("Port NodeAddress [ address " + node.getEnderecoIP() + ":" + node.getPort() + " ]");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();
        frame.pack();

    }

    // Torna a janela visível
    public void open() {
    	new Thread(() -> {
			node.startServing();
		}).start();
    	
        frame.setVisible(true);
    }

    // Adiciona o conteúdo da janela
    private void addFrameContent() {
        frame.setLayout(new BorderLayout());

        // Painel de pesquisa
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Alinhamento à esquerda

        JLabel searchLabel = new JLabel("Texto a procurar:");
        JTextField searchTextField = new JTextField(20);
        JButton searchButton = new JButton("Procurar");

        searchPanel.add(searchLabel);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);

        frame.add(searchPanel, BorderLayout.NORTH);

        // Painel inferior
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Área esquerda
        JPanel leftArea = new JPanel();
        leftArea.setPreferredSize(new Dimension(300, 150));
        leftArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(fileList);
        leftArea.setLayout(new BorderLayout());
        leftArea.add(scrollPane, BorderLayout.CENTER);

        // Área direita
        JPanel rightButtonsPanel = new JPanel();
        rightButtonsPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton downloadButton = new JButton("Descarregar");
        JButton connectButton = new JButton("Ligar a Nó");

        downloadButton.setPreferredSize(new Dimension(150, 75));
        connectButton.setPreferredSize(new Dimension(150, 75));

        rightButtonsPanel.add(downloadButton);
        rightButtonsPanel.add(connectButton);

        bottomPanel.add(leftArea, BorderLayout.CENTER);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.CENTER);

        // Eventos dos botões
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiNode newNodeWindow = new GuiNode(node);
                newNodeWindow.open();
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<String> selectedFiles = fileList.getSelectedValuesList();
                // Download logic
            }
        });

        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchTextField.getText().toLowerCase();
                node.sendWordSearchMessageRequest(searchText);
                filterFileList(searchText);
            }
        });


        allFiles = new ArrayList<>();
        //loadFilesFromWorkFolder();
        
    }

    /*
     // Função para carregar os ficheiros da pasta de trabalho
    private void loadFilesFromWorkFolder() {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) { // Verifica se é um ficheiro (não uma pasta)
                        String fileName = file.getName();
                        allFiles.add(fileName);  // Adiciona à lista de todos os ficheiros
                        listModel.addElement(fileName);  // Adiciona à lista exibida na GUI
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Pasta de trabalho não encontrada: " + folder.toString(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    */

    
    private void filterFileList(String searchText) {
        listModel.clear();
        for (String file : allFiles) {
            if (file.toLowerCase().contains(searchText)) {
                listModel.addElement(file);
            }
        }
    }
    


}
