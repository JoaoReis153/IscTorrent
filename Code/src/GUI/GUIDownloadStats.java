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
    private Long durationInMiliseconds; // Total time in seconds
    private GUI gui;
    private String hash;

    // Constructor
    public GUIDownloadStats(GUI gui, String hash, long durationInMiliseconds) {
        this.gui = gui;
        this.hash = hash;
        this.durationInMiliseconds = durationInMiliseconds;
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
    // Converts seconds and milliseconds to a readable time format
    private String formatTime(long totalMillis) {
        if (totalMillis < 1000) {
            // If less than 1 second, show milliseconds
            return String.format("%d ms", totalMillis);
        } else if (totalMillis < 60000) {
            // If between 1 second and 1 minute, show seconds and milliseconds
            long seconds = totalMillis / 1000;
            long millis = totalMillis % 1000;
            return String.format("%d.%03d seconds", seconds, millis);
        } else {
            // If greater than 1 minute, show minutes, seconds, and milliseconds
            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
            long seconds = (totalMillis / 1000) % 60;
            long millis = totalMillis % 1000;
            return String.format("%d:%02d.%03d", minutes, seconds, millis);
        }
    }

    // Displays the GUI
    public void open() {
        // Convert time to human-readable format
        String readableTime = formatTime(durationInMiliseconds);

        // Create frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        frame.setVisible(GUI.getSHOW());
    }
}
