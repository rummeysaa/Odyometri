        //Kodun çalışıp çalışmadığını test etmek için kullandığım ekstra bir döküman.

package audiometry;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private final SerialManager serialManager;
    private final JComboBox<String> portComboBox;
    private final JTextArea consoleArea;
    private final JButton connectButton;

    public MainApp() {
        serialManager = new SerialManager();

        setTitle("Audiometer Communication Test");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Active Ports:"));

        portComboBox = new JComboBox<>(serialManager.getAvailablePortNames());
        if (portComboBox.getItemCount() == 0) {
            portComboBox.addItem("No Port Found");
            portComboBox.setEnabled(false);
        }
        topPanel.add(portComboBox);

        connectButton = new JButton("Connect and Listen");
        topPanel.add(connectButton);
        add(topPanel, BorderLayout.NORTH);

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(Color.GREEN);
        consoleArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        add(new JScrollPane(consoleArea), BorderLayout.CENTER);

        connectButton.addActionListener(e -> {
            String selectedPort = (String) portComboBox.getSelectedItem();
            if (selectedPort != null && !selectedPort.equals("No Port Found")) {

                boolean isConnected = serialManager.connectAndTest(selectedPort, message ->
                        SwingUtilities.invokeLater(() -> consoleArea.append(message + "\n"))
                );

                if (isConnected) {
                    consoleArea.append(selectedPort + " opened successfully. Waiting for signal...\n");
                    connectButton.setEnabled(false);
                } else {
                    consoleArea.append("ERROR: " + selectedPort + " could not be opened.\n");
                }
            }
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}