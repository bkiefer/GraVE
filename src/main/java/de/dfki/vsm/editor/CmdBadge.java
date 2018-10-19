package de.dfki.vsm.editor;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observer;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CmdBadge extends RSyntaxTextArea implements EventListener, Observer {

  //
  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // The node to which the badge is connected
  private final Node mNode;
  private final EditorConfig mEditorConfig;

  private final Font mFont;

  /**
   */
  public CmdBadge(Node node) {
    super(30, 40);
    setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    setCodeFoldingEnabled(true);
    setVisible(true);
    setBackground(new Color(235, 235, 235, 0));
    setOpaque(false);
    addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        setBackground(new Color(0, 0, 0, 100));
        setForeground(new Color(255, 255, 255, 100));
        setOpaque(true);
      }

      public void focusLost(FocusEvent e) {
        setBackground(new Color(175, 175, 175, 0));
        setForeground(new Color(0, 0, 0, 100));
        setOpaque(false);
        endEditMode();
      }
    });

    mNode = node;
    mEditorConfig = mNode.getWorkSpace().getEditorConfig();
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
    mNode.getDataNode().getCmd().setContent(getText());
    mDispatcher.convey(new ProjectChangedEvent(this));
    mDispatcher.convey(new NodeSelectedEvent(this, mNode.getDataNode()));
    update();
  }

  public void updateLocation(Point vector) {
    Point location = getLocation();
    setLocation(location.x + vector.x, location.y + vector.y);
  }

  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  @Override
  public void update(java.util.Observable obs, Object obj) {
    update();
  }

  private void update() {
    String content = mNode.getDataNode().getCmd().getContent();

    // Sets visibility of the component to true only if there is something to display
    setVisible(!content.isEmpty());
    setText(content);

    if (!content.isEmpty()) {
      Dimension dimension = new Dimension(200, getLineCount() * getLineHeight());
      setSize(dimension);
      setLocation(mNode.getLocation().x + (mEditorConfig.sNODEWIDTH / 2)
              - (dimension.width / 2),
              mNode.getLocation().y + mEditorConfig.sNODEHEIGHT);
    } else {
      setSize(new Dimension(getWidth(), getHeight()));
    }
  }

  public Node getNode() {
    return mNode;
  }

  @Override
  public void update(EventObject event) {
    System.out.println("Event happened!!");
  }
}
