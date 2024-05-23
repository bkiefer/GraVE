package de.dfki.grave.editor.panels;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.dfki.grave.model.SuperNode;

@SuppressWarnings("serial")
public class DefinitionsPanel extends JPanel {
  private DefinitionsArea mTextArea;

  public DefinitionsPanel(Font font) {
    super(new BorderLayout());
    mTextArea = new DefinitionsArea(font);
    JScrollPane s = new JScrollPane(mTextArea);
    add(s, BorderLayout.CENTER);
  }

  public void setSuperNode(SuperNode s) {
    mTextArea.setSuperNode(s);
  }

  public void setDeselected() {
    mTextArea.setDeselected();
  }
}
