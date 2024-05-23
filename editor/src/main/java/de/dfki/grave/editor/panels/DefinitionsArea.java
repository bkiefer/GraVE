package de.dfki.grave.editor.panels;

import static de.dfki.grave.app.Preferences.*;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.grave.editor.ObserverDocument;
import de.dfki.grave.editor.action.EditContentAction;
import de.dfki.grave.editor.event.CodeEditedEvent;
import de.dfki.grave.model.SuperNode;
import de.dfki.grave.util.evt.EventDispatcher;


/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public class DefinitionsArea extends RSyntaxTextArea //implements ProjectElement
{

  /** This MouseListener guarantees that we can select components behind this
   *  disabled code area, and handles click events at the code area in case
   *  it's not selected
   * @author kiefer
   *
   */
  private class MyMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent ev) {
      if (ev.getButton() == MouseEvent.BUTTON1) {
          setSelected();
      }
    }
  }


  private class MyFocusListener implements FocusListener {

    @Override
    public void focusGained(FocusEvent e) {
      setSelected();
    }

    @Override
    public void focusLost(FocusEvent e) {
      setDeselected();
    }

  }

  // The node to which the badge is connected: must be a SuperNode

  protected ObserverDocument mDocument = null;

  private final UndoableEditListener mUndoListener;

  /** A syntax-aware area for entering code or numerical values
   * @param compo the component this code area belongs to
   */
  public DefinitionsArea(Font font) {
    setCodeFoldingEnabled(true);
    this.setLineWrap(true);
    this.setWrapStyleWord(true);
    setVisible(true);
    setBackground(sINACTIVE_CODE_COLOR);
    //this.setMaximumSize(new Dimension(maxWidth, maxHeight));

    // Get rid of annoying yellow line
    setHighlighter(null);
    setHighlightCurrentLine(false);
    setHighlightSecondaryLanguages(false);

    addMouseListener(new MyMouseListener());
    addFocusListener(new MyFocusListener());
    setFont(font);
    setLayout(new BorderLayout());
    getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "enter");
    getActionMap().put("enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) { okAction(); }});
    getInputMap().put(KeyStroke.getKeyStroke("ctrl ESCAPE"), "escape");
    getActionMap().put("escape", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) { cancelAction(); }});
    mUndoListener = new UndoableEditListener() {
      @Override
      public void undoableEditHappened(UndoableEditEvent e) {
        getEditor().getUndoManager().addTextEdit(e.getEdit());
      }
    };
    setEnabled(false);
  }

  public void setSuperNode(SuperNode s) {
    mDocument = new ObserverDocument(s.getDefinitionsHolder());
    this.setDocument(mDocument);
    mDocument.addUndoableEditListener(mUndoListener);
    setEnabled(true);
  }

  public boolean emptyDocument() {
    return mDocument == null || mDocument.toString().isBlank();
  }

  public void okAction() {
    EventDispatcher.getInstance().convey(new CodeEditedEvent(null, false));
  }

  public void cancelAction() {
    mDocument.discardChanges();
    EventDispatcher.getInstance().convey(new CodeEditedEvent(null, false));
  }

  /** remove undo listener and document */
  public void clear() {
    this.getDocument().removeUndoableEditListener(mUndoListener);
    this.setDocument(new RSyntaxDocument(""));
  }

  public void setSelected() {
    setBackground(sACTIVE_CODE_COLOR);
    // deselect the other code area
    EventDispatcher.getInstance().convey(new CodeEditedEvent(null, false));
  }

  public synchronized void setDeselected() {
    setBackground(sINACTIVE_CODE_COLOR);
    // Maybe this should be done in the OK action
    if (mDocument.contentChanged())
      new EditContentAction(getEditor(), mDocument).run();
  }

  private ProjectEditor getEditor() {
    Container p = this;
    while (! (p instanceof ProjectEditor)) {
      p = p.getParent();
    }
    return (ProjectEditor)p;
  }
}
