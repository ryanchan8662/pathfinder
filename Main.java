import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class Main extends JPanel{

    JTextField fileNameField = new JTextField(30);
    String directory = System.getProperty("user.dir");

    public Main () {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = makeGbc(0, 0);
        JLabel label = new JLabel("Image directory: "); // textbox label
        label.setFont(new Font("SansSerif", Font.BOLD, 14)); // set font for label
        add(label, gbc); // add label
        JPanel panel = new JPanel(); // create main panel
        JButton blueButton = new JButton("Browse");
        blueButton.setBackground(new Color(40, 64, 128));
        blueButton.setForeground(new Color(255, 255, 255));
        blueButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        this.fileNameField.setFont(new Font("SansSerif", Font.PLAIN, 14)); // set textbox font
        this.fileNameField.setText(this.directory);
        this.fileNameField.addActionListener(new ButtonEvent(this.directory, this.fileNameField));
        panel.add(fileNameField); // add textbox
        panel.add(blueButton); // add browse button
        panel.setBorder(BorderFactory.createEtchedBorder());
        gbc = makeGbc(1, 0);
        add(panel, gbc);
    }

    private GridBagConstraints makeGbc (int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = x;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = (x == 0) ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private static void createAndShowUI () {
        JFrame frame = new JFrame("Pathfinder");
        frame.getContentPane().add(new Main());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void actionPerformed (ActionEvent e) {

    }

    public static void main (String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowUI();
            }
        });
    }
}