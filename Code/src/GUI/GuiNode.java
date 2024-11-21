package GUI;

import Core.Node;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class GUINode {

    private JFrame frame;
    private Node node;
    private GUI gui;

    public GUINode(GUI gui) {
        this.gui = gui;
        this.node = gui.getNode();
        frame = new JFrame("Add Node");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // Close only this window
        addFrameContent();
        frame.pack(); // Adjust the window size
    }

    public void open() {
        frame.setVisible(this.gui.getSHOW());
    }

    private void addFrameContent() {
        // Using FlowLayout to organize components in a line
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Left-aligned with spacing

        // Label and text field for Address
        JLabel addressLabel = new JLabel("Address:");
        JTextField addressField = new JTextField(15); // Text field for address
        addressField.setText(node.getEnderecoIP());
        frame.add(addressLabel);
        frame.add(addressField);

        // Label and text field for Port
        JLabel portLabel = new JLabel("Port:");
        JTextField portField = new JTextField(5); // Text field for port
        frame.add(portLabel);
        frame.add(portField);

        // Cancel and OK buttons
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");

        // Adding functionality to the buttons
        cancelButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose(); // Close the window when Cancel is clicked
                }
            }
        );

        okButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Logic to handle the address and port
                    String address = addressField.getText();
                    String portText = portField.getText();
                    try {
                        int port = Integer.parseInt(portText);
                        JOptionPane.showMessageDialog(
                            frame,
                            "Address: " + address + "\nPort: " + port
                        );
                        node.connectToNode(address, port);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Invalid port!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        );

        frame.add(cancelButton);
        frame.add(okButton);
    }
}
