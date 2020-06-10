package de.dfki.grave.editor.panels;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.grave.editor.EditorComponent;

@SuppressWarnings("serial")
public class CodeEditPanel extends JPanel {
  private RSyntaxTextArea textArea;
  private EditorComponent editedObject;

  // make this jpane, with scrollpane, and textarea
  // TODO: THE CODE MUST BE ADAPTED WHEN CHANGED IN THE CONFIG
  public CodeEditPanel(Font font) { 
    super(new BorderLayout());
    textArea = new RSyntaxTextArea();
    textArea.setCodeFoldingEnabled(true);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setVisible(true);
    textArea.setFont(font);
    //textArea.setBackground(Color.white);
    
    // Get rid of annoying yellow line
    textArea.setHighlightCurrentLine(false);
    textArea.setHighlightSecondaryLanguages(false);
    textArea.setEnabled(true);
    JScrollPane s = new JScrollPane(textArea);
    add(s, BorderLayout.CENTER);
  }

  public void setEditedObject(EditorComponent n) {
    if (editedObject != null) {
      editedObject.checkDocumentChange();
    }
    editedObject = null;
    if (n == null || n.getDoc() == null) {
      textArea.setDocument(new RSyntaxDocument(""));
      setEnabled(false);
      return;
    }
    setEnabled(true);
    editedObject = n;
    textArea.setDocument(n.getDoc());
    revalidate();
    repaint();
  }

  public void updateBorders(int x, int y, int w, int h) {
    //setBounds(x, y, w, h);
    //s.setBounds(x, y, w, h);
    textArea.setBounds(x, y, w, h);
  }

}