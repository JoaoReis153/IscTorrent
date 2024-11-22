package GUI;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class GUI {

    private JFrame frame;
    private JList<FileSearchResult> fileList;
    private DefaultListModel<FileSearchResult> listModel;
    private ArrayList<FileSearchResult> allFiles;
    private Node node;
    private static boolean SHOW = false;

    // Constructor of the GUI class where it receives the address, port, and work
    // folder
    public GUI(int nodeId) {
        this.node = new Node(nodeId, this);
        frame = new JFrame(
            "Port NodeAddress [ address " +
            node.getEnderecoIP() +
            ":" +
            node.getPort() +
            " ]"
        );
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();
        frame.pack();
        open();
    }

    // Makes the window visible
    public void open() {
        new Thread(() -> {
            node.startServing();
        }).start();
        frame.setVisible(SHOW);
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

        allFiles = new ArrayList<>();
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(fileList);
        leftArea.setLayout(new BorderLayout());
        leftArea.add(scrollPane, BorderLayout.CENTER);

        // Right area with buttons
        JPanel rightButtonsPanel = new JPanel();
        rightButtonsPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton downloadButton = new JButton("Download");
        JButton connectButton = new JButton("Connect to Node");

        downloadButton.setPreferredSize(new Dimension(150, 75));
        connectButton.setPreferredSize(new Dimension(150, 75));

        rightButtonsPanel.add(downloadButton);
        rightButtonsPanel.add(connectButton);

        bottomPanel.add(leftArea, BorderLayout.CENTER);
        bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.CENTER);

        // Button event listeners
        connectButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GUINode newNodeWindow = new GUINode(GUI.this);
                    newNodeWindow.open();
                }
            }
        );

        downloadButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<FileSearchResult> selectedOptions =
                        fileList.getSelectedValuesList();
                    for (FileSearchResult option : selectedOptions) {
                        List<FileSearchResult> searchResultOfDifferentNodes =
                            new ArrayList<FileSearchResult>();
                        for (FileSearchResult searchResult : allFiles) {
                            if (option.equals(searchResult)) {
                                searchResultOfDifferentNodes.add(searchResult);
                            }
                        }
                        node.downloadFile(searchResultOfDifferentNodes);
                    }
                }
            }
        );

        searchButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listModel.clear();
                    allFiles.clear();
                    String searchText = searchTextField.getText().toLowerCase();
                    node.broadcastWordSearchMessageRequest(searchText);
                    System.out.println(
                        "Search request sent for keyword: " + searchText
                    );
                }
            }
        );
    }

    public void simulateSelectedOptions(List<FileSearchResult> options) {
        for (FileSearchResult option : options) {
            List<FileSearchResult> searchResultOfDifferentNodes = new ArrayList<
                FileSearchResult
            >();
            for (FileSearchResult searchResult : allFiles) {
                if (option.equals(searchResult)) {
                    searchResultOfDifferentNodes.add(searchResult);
                }
            }
            node.downloadFile(searchResultOfDifferentNodes);
        }
    }

    public synchronized void loadListModel(FileSearchResult[] list) {
        if (list == null || list.length == 0) return;
        File[] files = node.getFolder().listFiles();
        if (files != null) {
            // Create FileSearchResult objects
            for (File file : files) {
                String hash = Utils.generateSHA256(file.getAbsolutePath());

                allFiles.add(
                    new FileSearchResult(
                        null,
                        file.getName(),
                        hash,
                        file.length(),
                        node.getEnderecoIP(),
                        node.getPort()
                    )
                );
            }
        }
        SwingUtilities.invokeLater(() -> {
            int added = 0;
            for (FileSearchResult searchResult : list) {
                if (!allFiles.contains(searchResult)) {
                    added++;
                    listModel.addElement(searchResult);
                }
                allFiles.add(searchResult);
            }
            System.out.println(
                "Loaded " + added + " search results into the file list."
            );
        });
    }

    public ArrayList<FileSearchResult> getListModel() {
        ArrayList<FileSearchResult> list = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            list.add(listModel.getElementAt(i));
        }
        return list;
    }

    public void showDownloadStats(String hash, long duration) {
        GUIDownloadStats downloadStats = new GUIDownloadStats(
            GUI.this,
            hash,
            duration
        );
        downloadStats.open();
    }

    public Node getNode() {
        return node;
    }

    public static boolean getSHOW() {
        return SHOW;
    }
}
