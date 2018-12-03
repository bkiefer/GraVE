package de.dfki.vsm.editor;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public class CmdBadge extends RSyntaxTextArea {

  //
  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // The node to which the badge is connected
  private final Node mNode;
  private final EditorConfig mEditorConfig;

  // TODO: put preferences into external yml
  private final Font mFont;
  private final int maxWidth = 800;
  private final int maxHeight = 300;

  private Color boxActiveColour = new Color(255, 255, 255, 100);

  /**
   */
  public CmdBadge(Node node, EditorConfig cfg) {
    super(30, 40);
    setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
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
        setBackground(boxActiveColour);
        mDispatcher.convey(new ElementSelectedEvent(mNode));
      }

      public void focusLost(FocusEvent e) {
        setBackground(new Color(175, 175, 175, 100));
        endEditMode();
      }
    });
    getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        mNode.getDataNode().setCmd(getText());
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        removeUpdate(e);
      }
      @Override
      public void changedUpdate(DocumentEvent arg0) {
        removeUpdate(arg0);
      }
    });
    mNode = node;
    mEditorConfig = cfg;
    mFont = new Font("Monospaced",
            Font.ITALIC,
            mEditorConfig.sWORKSPACEFONTSIZE);
    setFont(mFont);
    setLayout(new BorderLayout());
    update();
  }

  /*
   * Resets badge to its default visual behavior
   */
  public synchronized void endEditMode() {
    mNode.getDataNode().setCmd(getText());
    mDispatcher.convey(new ProjectChangedEvent(this));
    mDispatcher.convey(new ElementSelectedEvent(mNode));
    update();
  }

  public void translate(Point vector) {
    Point p = getLocation();
    p.translate(vector.x, vector.y);
    setLocation(p);
  }

  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  private void update() {
    String content = mNode.getDataNode().getCmd();
    if (content == null) return;
    // Sets visibility of the component to true only if there is something to display
    setVisible(!content.isEmpty());
    super.setText(content);
    if (!content.isEmpty()) {
      computeAndSetNewSize();
    } else {
      setSize(new Dimension(getWidth(), getHeight()));
    }
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
    setLocation(mNode.getLocation().x + (mNode.getWidth() - getSize().width)/2,
        mNode.getLocation().y + mNode.getHeight());
  }

  public Node getNode() {
    return mNode;
  }

  @Override
  public void setText(String text) {
    mNode.getDataNode().setCmd(text);
    update();
  }

  @Override
  public int hashCode() {
    return getText().hashCode();
  }
}
