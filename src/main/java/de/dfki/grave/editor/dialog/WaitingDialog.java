package de.dfki.grave.editor.dialog;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Created by alvaro on 5/28/16.
 */
public class WaitingDialog extends JDialog implements Observer {

  private JLabel messageLabel = new JLabel("Loading...");
  private JPanel messagePane = new JPanel();
  private String messageText;

  public WaitingDialog() {
    super();
    messageText = "";
    initComponents();
  }

  public WaitingDialog(String text) {
    super();
    messageText = text;
    messageLabel.setText(text);
    initComponents();
  }

  public void setMessageText(String text) {
    messageText = text;
    messageLabel.setText(text);
  }

  private void initComponents() {
    messagePane.add(messageLabel);
    add(messagePane);
    setTitle("Info");
    pack();
    setLocationRelativeTo(null);
  }

  @Override
  public void update(Observable o, Object arg) {

  }
}
