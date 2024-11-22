package GUI;

import Core.Node;
import Messaging.FileBlockAnswerMessage;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GUIDownloadStats {

    private Map<String, Integer> nodesNBlocks; // Node stats: Node and number of downloads
    private Long totalTimeInSeconds; // Total time in seconds
    private GUI gui;
    private String hash;

    // Constructor
    public GUIDownloadStats(GUI gui, String hash, long totalTimeInSeconds) {
        this.gui = gui;
        this.hash = hash;
        this.totalTimeInSeconds = totalTimeInSeconds;
        this.nodesNBlocks = new HashMap<>();
        load();
    }

    private void load() {
        Map<String, ArrayList<FileBlockAnswerMessage>> downloadStatsMap = gui
            .getNode()
            .getDownloadManager()
            .getDownloadProcess(hash);

        for (String key : downloadStatsMap.keySet()) {
            nodesNBlocks.put(key, downloadStatsMap.get(key).size());
        }
    }

    // Converts seconds to a readable time format
    private String formatTime(long totalSeconds) {
        long hours = TimeUnit.SECONDS.toHours(totalSeconds);
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Displays the GUI
    public void open() {
        // Convert time to human-readable format
        String readableTime = formatTime(totalTimeInSeconds);

        // Create frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(
            new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS)
        );

        JLabel downloadFinished = new JLabel("Download finished");
        downloadFinished.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(downloadFinished);
        // Add details for each node
        for (Map.Entry<String, Integer> entry : nodesNBlocks.entrySet()) {
            String node = entry.getKey();
            int downloads = entry.getValue();
            JLabel nodeLabel = new JLabel(
                String.format(
                    "NodeAddress [address=%s]: %d downloads",
                    node,
                    downloads
                )
            );
            nodeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            frame.add(nodeLabel);
        }

        // Add time label
        JLabel timeLabel = new JLabel("Time to Download: " + readableTime);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(timeLabel);

        // Display the frame
        frame.setVisible(true);
    }
}
