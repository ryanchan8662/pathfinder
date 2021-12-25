import java.awt.event.*;

import javax.swing.JTextField;

public class ButtonEvent implements ActionListener {

    String text;
    JTextField textBox;

    public ButtonEvent (String textInput, JTextField inputBox) {
        this.textBox = inputBox;
        this.text = textInput;
    }

    public void actionPerformed (ActionEvent e) {  

    }
}
