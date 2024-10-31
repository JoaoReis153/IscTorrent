import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;

public class GUI {
    private JFrame frame;
    private JList<String> fileList;
    private DefaultListModel<String> listModel;
    private ArrayList<String> allFiles; 

    public GUI(String address, int porta) {
        frame = new JFrame("Port NodeAddress [ address " + address + ":" + porta + " ]");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addFrameContent();
        frame.pack();
    }

    public void open() {
        frame.setVisible(true);
    }

    private void addFrameContent() {
        frame.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel("Texto a procurar:");
        JTextField searchTextField = new JTextField(20);
        JButton searchButton = new JButton("Procurar");

        searchPanel.add(searchLabel);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);

        frame.add(searchPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JPanel leftArea = new JPanel();
        leftArea.setPreferredSize(new Dimension(300, 150));
        leftArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(fileList);
        leftArea.setLayout(new BorderLayout());
        leftArea.add(scrollPane, BorderLayout.CENTER);

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

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewNode newNodeWindow = new NewNode();
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
                filterFileList(searchText);
            }
        });


        allFiles = new ArrayList<>();
        addFilesToList(new String[]{"file1.txt", "file2.txt", "file3.jpg", "document.pdf", "presentation.ppt"});
    }

    private void addFilesToList(String[] files) {
        for (String file : files) {
            allFiles.add(file);
            listModel.addElement(file);
        }
    }

    private void filterFileList(String searchText) {
        listModel.clear();
        for (String file : allFiles) {
            if (file.toLowerCase().contains(searchText)) {
                listModel.addElement(file);
            }
        }
    }

    public static void main(String[] args) {
        GUI window = new GUI("192.168.1.1", 80);
        window.open();
    }
}
