package de.dfki.grave.editor.panels;

import static de.dfki.grave.app.Preferences.*;

import java.awt.BorderLayout;
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
        mEditedObject.okAction();      // save changes
      }
    });
    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape_CANCEL");
    mTextArea.getActionMap().put("escape_CANCEL", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditedObject.cancelAction();  // revert all changes
      }
    });
  }

  public void setEditedObject(CodeArea n) {
    assert n != null;
    mEditedObject = n;
    mTextArea.setDocument(n.getDocument());
    setEnabled();
    revalidate();
    repaint();
  }

  private void setEnabled() {
    mTextArea.setBackground(sACTIVE_CODE_COLOR);
    mTextArea.setEnabled(true);
  }

  public void setDisabled() {
    mEditedObject = null;
    mTextArea.setDocument(new RSyntaxDocument(""));
    mTextArea.setBackground(sINACTIVE_CODE_COLOR);
    mTextArea.setEnabled(false);
  }

  public CodeArea getActiveArea() {
    return mEditedObject;
  }

  public void updateBorders(int x, int y, int w, int h) {
    mTextArea.setBounds(x, y, w, h);
  }

}