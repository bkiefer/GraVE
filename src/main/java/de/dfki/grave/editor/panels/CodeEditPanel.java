package de.dfki.grave.editor.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.grave.editor.CodeArea;

@SuppressWarnings("serial")
public class CodeEditPanel extends JPanel {
  private RSyntaxTextArea mTextArea;
  private CodeArea mEditedObject;

  // TODO: color to preferences
  private Color activeColour = new Color(200, 200, 200, 255);
  private Color inactiveColour = new Color(175, 175, 175, 100);
  
  public CodeEditPanel(Font font) { 
    super(new BorderLayout());
    mTextArea = new RSyntaxTextArea();
    mTextArea.setCodeFoldingEnabled(true);
    mTextArea.setLineWrap(true);
    mTextArea.setWrapStyleWord(true);
    mTextArea.setVisible(true);
    mTextArea.setFont(font);
    //textArea.setBackground(Color.white);
    
    // Get rid of annoying yellow line
    mTextArea.setHighlightCurrentLine(false);
    mTextArea.setHighlightSecondaryLanguages(false);
    setDisabled();
    JScrollPane s = new JScrollPane(mTextArea);
    add(s, BorderLayout.CENTER);
    
    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "enter_OK");
    mTextArea.getActionMap().put("enter_OK", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) { 
        mEditedObject.okAction(); // save changes
      }
    });
    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape_CANCEL");
    mTextArea.getActionMap().put("escape_CANCEL", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO: revert all changes
        mEditedObject.cancelAction();
      }
    });
  }

  public void setEditedObject(CodeArea n) {
    if (n == null || n.getEditorComponent().getDoc() == null) {
      setDisabled();
      return;
    }
    mEditedObject = n;
    mTextArea.setDocument(n.getEditorComponent().getDoc());
    setEnabled();
    revalidate();
    repaint();
  }
  
  /** */
  public CodeArea getEditedObject() {
    return mEditedObject;
  }
  
  private void setEnabled() {
    mTextArea.setBackground(activeColour);
    mTextArea.setEnabled(true);
  }
  
  public void setDisabled() {
    mEditedObject = null;
    mTextArea.setDocument(new RSyntaxDocument(""));
    mTextArea.setBackground(inactiveColour);
    mTextArea.setEnabled(false);
  }

  public CodeArea getActiveArea() {
    return mEditedObject;
  }
  
  public void updateBorders(int x, int y, int w, int h) {
    mTextArea.setBounds(x, y, w, h);
  }

}