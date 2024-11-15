package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import Core.Node;
import FileSearch.FileSearchResult;

public class GUI {
    private JFrame frame;
    private JList<String> fileList;
    public DefaultListModel<String> listModel;
    private ArrayList<String> allFiles;
    private Node node;

    // Constructor of the GUI class where it receives the address, port, and work
    // folder
    public GUI(int nodeId) {
        this.node = new Node(nodeId, this);
        frame = new JFrame("Port NodeAddress [ address " + node.getEnderecoIP() + ":" + node.getPort() + " ]");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();
        frame.pack();

        System.out.println("GUI initialized for Node with ID: " + nodeId);
    }

    // Makes the window visible
    public void open() {
        new Thread(() -> {
            node.startServing();
        }).start();

        frame.setVisible(true);

        System.out.println("GUI is now visible.");
    }

    // Adds the content to the window
    private void addFrameContent() {
        frame.setLayout(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Align to the left

        JLabel searchLabel = new JLabel("Text to search:");
        JTextField searchTextField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchPanel.add(searchLabel);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);

        frame.add(searchPanel, BorderLayout.NORTH);

        // Bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Left area
        JPanel leftArea = new JPanel();
        leftArea.setPreferredSize(new Dimension(300, 150));
        leftArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(fileList);
        leftArea.setLayout(new BorderLayout());
        leftArea.add(scrollPane, BorderLayout.CENTER);

        // Right area with buttons
        JPanel rightButtonsPanel = new JPanel();
        rightButtonsPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton downloadButton = new JButton("Download");
        JButton connectButton = new JButton("Connect to");

        downloadButton.setPreferredSize(new Dimension(150, 75));
        connectButton.setPreferredSize(new Dimension(150, 75));

        rightButtonsPanel.add(downloadButton);
        rightButtonsPanel.add(connectButton);

        bottomPanel.add(leftArea, BorderLayout.CENTER);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.CENTER);

        // Button event listeners
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiNode newNodeWindow = new GuiNode(node);
                newNodeWindow.open();
                System.out.println("New Node connection window opened.");
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<String> selectedFiles = fileList.getSelectedValuesList();
                // Download logic
                System.out.println("Download initiated for selected files: " + selectedFiles);
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchTextField.getText().toLowerCase();
                listModel.clear();
                node.broadcastWordSearchMessageRequest(searchText);
                System.out.println("Search request sent for keyword: " + searchText);
            }
        });

        allFiles = new ArrayList<>();
    }

    public void loadListModel(FileSearchResult[] list) {
        if (list == null || list.length == 0) {
            System.out.println("No search results found to load.");
            return;
        }
        for (FileSearchResult searchResult : list) {
            listModel.addElement(searchResult.getFileName());
        }
        System.out.println("Loaded search results into the file list.");
    }

    public Node getNode() {
        return node;
    }
}
