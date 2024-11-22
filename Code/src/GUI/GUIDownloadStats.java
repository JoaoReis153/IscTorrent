package GUI;

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

    private Map<String, Integer> nodesNBlocks;
    private Long durationInMiliseconds;
    private GUI gui;
    private int hash;

    public GUIDownloadStats(GUI gui, int hash, long durationInMiliseconds) {
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

    private String formatTime(long totalMillis) {
        if (totalMillis < 1000) {
            return String.format("%d ms", totalMillis);
        } else if (totalMillis < 60000) {
            long seconds = totalMillis / 1000;
            long millis = totalMillis % 1000;
            return String.format("%d.%03d seconds", seconds, millis);
        } else {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis);
            long seconds = (totalMillis / 1000) % 60;
            long millis = totalMillis % 1000;
            return String.format("%d:%02d.%03d", minutes, seconds, millis);
        }
    }

    public void open() {
        String readableTime = formatTime(durationInMiliseconds);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(
            new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS)
        );

        JLabel downloadFinished = new JLabel("Download finished");
        downloadFinished.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(downloadFinished);

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

        JLabel timeLabel = new JLabel("Time to Download: " + readableTime);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        frame.add(timeLabel);

        frame.setVisible(GUI.getSHOW());
    }
}
