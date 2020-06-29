package de.dfki.grave.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.grave.editor.event.CodeEditedEvent;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.util.evt.EventDispatcher;


/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public class CodeArea extends RSyntaxTextArea implements ProjectElement {
  
  /** This MouseListener guarantees that we can select components behind this
   *  disabled code area, and handles click events at the code area in case
   *  it's not selected
   * @author kiefer
   *
   */
  private class MyMouseListener extends MouseAdapter {

    // this will be called when mouse is pressed on the component
    public void mousePressed(MouseEvent me) { 
      if (! CodeArea.this.isEnabled()) {
        Component child = me.getComponent();
        Component parent = child.getParent();
        
        //transform the mouse coordinate to be relative to the parent component:
        int deltax = child.getX() + me.getX();
        int deltay = child.getY() + me.getY();
        
        //build new mouse event:
        MouseEvent parentMouseEvent =
            new MouseEvent(parent, MouseEvent.MOUSE_PRESSED, 
                me.getWhen(), me.getModifiersEx(), deltax, deltay,
                me.getClickCount(), false); 
        //dispatch it to the parent component
        parent.dispatchEvent( parentMouseEvent);
      }
    }
    
    public void mouseClicked(MouseEvent ev) {
      if (! CodeArea.this.isEnabled() && ev.getButton() == MouseEvent.BUTTON1)
        if ((ev.getClickCount() == 1 && mComponent.isSelected())
            || (ev.getClickCount() == 2)) {
          tryToSelect();
        }
    }
  }
  
  // The node to which the badge is connected
  protected final EditorComponent mComponent;
  
  private final UndoableEditListener mUndoListener;

  // TODO: put into preferences
  private final int maxWidth = 800;
  private final int maxHeight = 300;

  // TODO: color to preferences
  private Color activeColour = new Color(200, 200, 200, 255);
  private Color inactiveColour = new Color(175, 175, 175, 100);
  
  /** A syntax-aware area for entering code or numerical values
   * @param compo the component this code area belongs to 
   */
  protected CodeArea(EditorComponent compo, Font font, Color col) {
    setCodeFoldingEnabled(true);
    this.setLineWrap(true);
    this.setWrapStyleWord(true);
    setVisible(true);
    setBackground(inactiveColour);
    this.setMaximumSize(new Dimension(maxWidth, maxHeight));

    // Get rid of annoying yellow line
    setHighlighter(null);
    setHighlightCurrentLine(false);
    setHighlightSecondaryLanguages(false);

    addMouseListener(new MyMouseListener());
    mComponent = compo;
    setFont(font);
    if (col != null) {
      setBorder(BorderFactory.createLineBorder(col));
    }
    setLayout(new BorderLayout());
    ObserverDocument d = mComponent.getDoc();
    this.setDocument(d);
    d.addDocumentListener(new DocumentListener(){
      @Override
      public void insertUpdate(DocumentEvent e) { update(); }
      @Override
      public void removeUpdate(DocumentEvent e) { insertUpdate(e); }
      @Override
      public void changedUpdate(DocumentEvent e) { insertUpdate(e); }
    });
    getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "enter");
    getActionMap().put("enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) { okAction(); }});
    getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
    getActionMap().put("escape", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) { cancelAction(); }});
    d.addUndoableEditListener(
        mUndoListener = new UndoableEditListener() {
          public void undoableEditHappened(UndoableEditEvent e) {
            getEditor().getUndoManager().addTextEdit(e.getEdit());
          }
        });
    update();
    setEnabled(false);
  }

  public EditorComponent getEditorComponent() {
    return mComponent;
  }

  public void okAction() {
    EventDispatcher.getInstance().convey(new CodeEditedEvent(this, false));
  }
  
  public void cancelAction() {
    mComponent.discardDocumentChange();
    EventDispatcher.getInstance().convey(new CodeEditedEvent(this, false));
  }
  
  /** remove undo listener and document */
  public void clear() {
    this.getDocument().removeUndoableEditListener(mUndoListener);
    this.setDocument(new RSyntaxDocument(""));
  }
  
  public void tryToSelect() {
    EventDispatcher.getInstance().convey(new CodeEditedEvent(this, true));
  }
  
  public void setSelected() {
    if (! mComponent.isSelected())
      mComponent.setSelected();
    setBackground(activeColour);
    setEnabled(true);
  }

  public synchronized void setDeselected() {
    setBackground(inactiveColour);
    // Maybe this should be done in the OK action
    mComponent.checkDocumentChange();
    update();
    setEnabled(false);
  }

  public void update() {
    String text = getText();
    if (text.trim().isEmpty()) {
      setVisible(false);
      return;
    }
    setVisible(true);
    int lines = getLineCount();
    setRows(lines);
    int newHeight = lines * this.getLineHeight();
    newHeight = newHeight > maxHeight? maxHeight : newHeight;
    
    FontMetrics fm = getFontMetrics(getFont());
    Optional<Integer> longestLine = Arrays.asList(text.split("\n")).stream()
        .map(s -> fm.stringWidth(s)).max(Comparator.naturalOrder());
    // No idea why i have to add 2 here
    int newWidth = longestLine.isPresent() ? longestLine.get() + 2: 0;
    
    setSize(new Dimension(newWidth, newHeight));
    setLocation();
  }

  void setLocation() {
    setLocation(mComponent.getCodeAreaLocation(getSize()));
  }    
  
  public ProjectEditor getEditor() {
    Container p = this;
    while (! (p instanceof ProjectEditor)) {
      p = p.getParent();
    }
    return (ProjectEditor)p;
  }
}
