package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import Core.Node;
import FileSearch.FileSearchResult;

public class GUI {

    private JFrame frame;
    private JList<FileSearchResult> fileList;
    private DefaultListModel<FileSearchResult> listModel;
    private ArrayList<FileSearchResult> localFiles = new ArrayList<>();
    private ArrayList<FileSearchResult> allFiles;
    private Node node;
    private boolean SHOW = true;
    private boolean isOpen = false;
    private String lastSearchKeyword = "";

    public GUI(int nodeId) {
        this.node = new Node(nodeId, this);

        createGUI();
    }


    public GUI(int nodeId, boolean show) {
        this.node = new Node(nodeId, this);

        createGUI();
        this.SHOW = show;
    }

    private void createGUI() {
        frame = new JFrame(
            "Port NodeAddress [ address " +
            node.getAddress().getHostAddress() +
            ":" +
            node.getPort() +
            " ]"
        );
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();
        frame.pack();
    }

    /*
     * Opens the GUI
     * If the GUI is already open, it does nothing
     * Otherwise, it starts the server and opens the GUI
     * The GUI is opened only if the SHOW variable is true
     */
    public void open() {
        if (this.isOpen) return;
        this.isOpen = true;

        new Thread(() -> {
            try {
                node.startServing();
            } catch (Exception e) {
                System.err.println("Failed to start server: " + e.getMessage());
                frame.dispose();
            }
        }).start();


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);

        frame.setVisible(SHOW);
    }

    private void addFrameContent() {
        frame.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel("Text to search:");
        JTextField searchTextField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchPanel.add(searchLabel);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);

        frame.add(searchPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

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
                    List<List<FileSearchResult>> filesToDownload =
                        new ArrayList<>();
                    for (FileSearchResult option : selectedOptions) {
                        List<FileSearchResult> searchResultOfDifferentNodes =
                            new ArrayList<FileSearchResult>();
                        for (FileSearchResult searchResult : allFiles) {
                            if (option.equals(searchResult)) {
                                searchResultOfDifferentNodes.add(searchResult);
                            }
                        }
                        filesToDownload.add(searchResultOfDifferentNodes);
                    }
                    
                    node.downloadFiles(filesToDownload);
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
                        node.getAddressAndPortFormated() +
                        "Search request sent for keyword: [" +
                        searchText +
                        "]"
                    );
                    lastSearchKeyword = searchText;
                }
            }
        );
    }

    /*
     * Simulates a download button click
     * The download button is simulated by sending a download request to all the nodes
     * Its used to test the GUI without actually downloading files
     */
    public void simulateDownloadButton(List<FileSearchResult> options) {
        List<List<FileSearchResult>> filesToDownload = new ArrayList<>();
        for (FileSearchResult option : options) {
            List<FileSearchResult> searchResultOfDifferentNodes = new ArrayList<
                FileSearchResult
            >();
            for (FileSearchResult searchResult : allFiles) {
                if (option.equals(searchResult)) {
                    searchResultOfDifferentNodes.add(searchResult);
                }
            }
            filesToDownload.add(searchResultOfDifferentNodes);
        }
        node.downloadFiles(filesToDownload);
    }

    /*
     * Reloads the list model
     * The list model is reloaded by sending a new word search message request 
     * with the previous searched word to all the nodes
     */
    public synchronized void reloadListModel() {
        System.out.println(
            node.getAddressAndPortFormated() + "Reloading GUI list"
        );
        listModel.clear();
        allFiles.clear();
        node.broadcastWordSearchMessageRequest(lastSearchKeyword);
    }

    /*
     * Loads the list model with the given list of files
     * The list model is used to display the files in the GUI
     */
    public synchronized void loadListModel(FileSearchResult[] list) {
        if (list == null || list.length == 0) return;
        File[] files = node.getFolder().listFiles();
        if (files != null) {
            for (File file : files) {
                FileSearchResult fileSearchResult = new FileSearchResult(file, node);
                allFiles.add(fileSearchResult);
                if(!localFiles.contains(fileSearchResult)) {
                    localFiles.add(fileSearchResult);
                };
            }
        }
        
        ArrayList<FileSearchResult> aux = new ArrayList<>();

        for (FileSearchResult searchResult : list) {
            if (!allFiles.contains(searchResult)) {
                aux.add(searchResult);
            }
            allFiles.add(searchResult);
        }


        SwingUtilities.invokeLater(() -> {
            for (FileSearchResult searchResult : aux) {
                searchResult.setDisplayNumber(countOccurrencesInAllFiles(searchResult));
                listModel.addElement(searchResult);
            }
        });
    }

    public ArrayList<FileSearchResult> getListModel() {
        ArrayList<FileSearchResult> list = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            list.add(listModel.getElementAt(i));
        }
        return list;
    }

    public void showDownloadStats(int hash, long duration) {
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

    public boolean getSHOW() {
        return SHOW;
    }

    /*
     * Counts the number of occurrences of a file in the list of all files
     * The count is used to display the number of neighboor nodes that contain the file
     */
    private synchronized int countOccurrencesInAllFiles(FileSearchResult searchResult) {
        int count = 0;
        for (FileSearchResult file : allFiles) {
            if (file.equals(searchResult)) {
                count++;
            }
        }
        return count;
    }
}
