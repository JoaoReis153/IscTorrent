package GUI;

import Core.Node;
import Core.Utils;
import FileSearch.FileSearchResult;
import Services.SubNode;
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
    private static boolean SHOW = true;
    private boolean isOpen = false;

    public GUI(int nodeId) throws IllegalArgumentException {
        this.node = new Node(nodeId, this);

        createGUI();
    }

    private void createGUI() {
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
    }

    public void open() {
        if (this.isOpen) return;
        this.isOpen = true;

        new Thread(() -> {
            node.startServing();
        }).start();
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
                    System.out.println(
                        node.getAddressAndPortFormated() +
                        "N of Peers: " +
                        node.getPeers().size()
                    );
                    for (SubNode peer : node.getPeers()) {
                        System.out.println(
                            node.getAddressAndPortFormated() +
                            "Socket: " +
                            peer.getSocket()
                        );
                    }
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
                }
            }
        );
    }

    public void simulateDownloadButton(List<FileSearchResult> options) {
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
            for (File file : files) {
                allFiles.add(new FileSearchResult(file, node));
            }
        }
        SwingUtilities.invokeLater(() -> {
            for (FileSearchResult searchResult : list) {
                if (!allFiles.contains(searchResult)) {
                    listModel.addElement(searchResult);
                }
                allFiles.add(searchResult);
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

    public static boolean getSHOW() {
        return SHOW;
    }
}
