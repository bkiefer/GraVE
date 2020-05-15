package de.dfki.grave.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.editor.panels.UndoRedoProvider;
import de.dfki.grave.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public class CodeArea extends RSyntaxTextArea
  implements Selectable, DocumentContainer {

  //
  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // The node to which the badge is connected
  private final EditorComponent mComponent;
  private ObserverDocument mDocument;

  // TODO: put preferences into external yml
  private final Font mFont;
  private final int maxWidth = 800;
  private final int maxHeight = 300;

  private Color boxActiveColour = new Color(255, 255, 255, 100);

  /** A syntax-aware area for entering code or numerical values
   * @param compo the component this code area belongs to 
   */
  public CodeArea(EditorComponent compo, ObserverDocument d, Font font,
      Color borderColor) {
    super(30, 40);
    setCodeFoldingEnabled(true);
    this.setLineWrap(true);
    this.setWrapStyleWord(true);
    setVisible(true);
    setBackground(new Color(175, 175, 175, 100));
    this.setMaximumSize(new Dimension(maxWidth, maxHeight));

    // Get rid of annoying yellow line
    setHighlighter(null);
    setHighlightCurrentLine(false);
    setHighlightSecondaryLanguages(false);

    addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        setSelected();
      }

      public void focusLost(FocusEvent e) {
        setDeselected();
      }
    });
    mComponent = compo;
    mFont = font;
    setFont(mFont);
    setLayout(new BorderLayout());
    if (borderColor != null) 
      setBorder(BorderFactory.createLineBorder(borderColor));
    this.setDocument(mDocument = d);
    d.addDocumentListener(new DocumentListener(){
      @Override
      public void insertUpdate(DocumentEvent e) { computeAndSetNewSize(); }
      @Override
      public void removeUpdate(DocumentEvent e) { insertUpdate(e); }
      @Override
      public void changedUpdate(DocumentEvent e) { insertUpdate(e); }
    });
    mDocument.addUndoableEditListener(
        new UndoableEditListener() {
          public void undoableEditHappened(UndoableEditEvent e) {
            UndoRedoProvider.addEdit(e.getEdit());
          }
        });
    computeAndSetNewSize();
  }

  @Override
  public void setSelected() {
    setBackground(boxActiveColour);
    mDispatcher.convey(new ElementSelectedEvent(mComponent));
  }

  @Override
  public synchronized void setDeselected() {
    setBackground(new Color(175, 175, 175, 100));
    mDocument.updateModel();
    mDispatcher.convey(new ProjectChangedEvent(this));
    mDispatcher.convey(new ElementSelectedEvent(mComponent));
    computeAndSetNewSize();
  }

  public void translate(Point vector) {
    Point p = getLocation();
    p.translate(vector.x, vector.y);
    setLocation(p);
  }

  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  private void computeAndSetNewSize() {
    int lines = (int) getText().chars().filter(x -> x == '\n').count() + 1;
    String longestLine = Arrays.asList(getText().split("\n")).stream()
            .max(Comparator.comparingInt(String::length)).get();
    FontMetrics fm = getFontMetrics(getFont());
    int newHeight = fm.getHeight() * lines;
    // font is monospaced, so this always works
    int newWidth = fm.stringWidth("p") * longestLine.length();
    newWidth = newWidth > maxWidth? maxWidth : newWidth;
    newHeight = newHeight > maxHeight? maxHeight : newHeight;
    setSize(new Dimension(newWidth, newHeight));
    setLocation();
  }

  public void setLocation() {
    setLocation(mComponent.getLocation().x + (mComponent.getWidth() - getSize().width)/2,
        mComponent.getLocation().y + mComponent.getHeight());
  }

  public ObserverDocument getDoc() {
    return mDocument;
  }

  /* Should not be called!
  @Override
  public void setText(String text) {
    mNode.getDataNode().setCmd(text);
    update();
  }*/

  @Override
  public int hashCode() {
    return getText().hashCode();
  }

}
