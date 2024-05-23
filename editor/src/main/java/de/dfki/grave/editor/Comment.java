package de.dfki.grave.editor;

import static de.dfki.grave.editor.panels.WorkSpace.addItem;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import de.dfki.grave.app.Preferences;
import de.dfki.grave.editor.action.EditContentAction;
import de.dfki.grave.editor.action.MoveCommentAction;
import de.dfki.grave.editor.action.RemoveCommentsAction;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.Boundary;
import de.dfki.grave.model.flow.CommentBadge;
import de.dfki.grave.util.evt.EventDispatcher;

/** A class for Comment Text Bubbles
 *  .----.
 *  |    |
 *  |   o|
 *  `----'
 *   |/
 * @author kiefer
 *
 */
@SuppressWarnings("serial")
public class Comment extends JPanel implements DocumentContainer, Observer {

  private JTextArea mTextField;

  private ObserverDocument mDocument;
  private CommentBadge mDataComment;
  private WorkSpace mWorkSpace;

  // in edit, or not
  private boolean mEditMode = false;

  private final static Color TRANSPARENT =  new Color(0,0,0,0);
  private final Color activeColor, inactiveColor;

  /** Constructor for prototype in the "Create item" area */
  public Comment() {
    activeColor = inactiveColor = null;
    mTextField = new JTextArea();
    mTextField.setDocument(mDocument = new ObserverDocument(""));
  }

  public Comment(WorkSpace workSpace, CommentBadge dataComment) {
    super();
    setLayout(new BorderLayout());
    mWorkSpace = workSpace;
    mDataComment = dataComment;
    mTextField = new JTextArea();
    add(mTextField, BorderLayout.CENTER);
    mTextField.setDocument(mDocument = new ObserverDocument(dataComment));
    mDocument.addUndoableEditListener(
        new UndoableEditListener() {
          @Override
          public void undoableEditHappened(UndoableEditEvent e) {
            getEditor().getUndoManager().addTextEdit(e.getEdit());
          }
        });
    // font setup
    setFont(mWorkSpace.getEditorConfig().sCOMMENT_FONT.getFont());

    inactiveColor = Preferences.sCOMMENT_BADGE_COLOR;
    // active color only sets the alpha channel to opaque
    activeColor = new Color(inactiveColor.getRed(), inactiveColor.getGreen(),
        inactiveColor.getBlue(), 255);
    mTextField.setDisabledTextColor(Color.gray);
    Border border = new TextBubbleBorder(Color.gray, 3, 7, 9, Color.black, 10);
    setBorder(border);
    setBackground(TRANSPARENT); // transparent background
    // size setup
    update();

    setVisible(true);
    mTextField.setLineWrap(true);
    mTextField.setWrapStyleWord(true);

    TextAreaMouseListener textDrag = new TextAreaMouseListener();
    mTextField.addMouseMotionListener(textDrag);
    mTextField.addMouseListener(textDrag);

    MyMouseListener myDrag = new MyMouseListener();
    addMouseMotionListener(myDrag);
    addMouseListener(myDrag);

    update();
    mTextField.setBackground(inactiveColor);
    mTextField.setEditable(false);
    setEnabled(false);
  }

  @Override
  public void update(Observable o, Object o1) {
    update();
  }

  public void update() {
    Boundary b = mDataComment.getBoundary();
    setBounds(mWorkSpace.toViewRectangle(b));
  }

  @Override
  public ObserverDocument getDoc() {
    return mDocument;
  }

  public CommentBadge getData() {
    return mDataComment;
  }

  /** Set model boundary, r is in view coordinates */
  private void setBoundary(Rectangle r) {
    mDataComment.setBoundary(mWorkSpace.toModelBoundary(r));
  }

  /** For undo/redo, r must be in view coordinates */
  public void moveOrResize(Rectangle r) {
    setBounds(r);
    setBoundary(r);
  }

  public boolean containsPoint(Point p) {
    return getBounds().contains(p.x, p.y);
  }

  /** Returns true if mouse in the lower right (resizing) area */
  private boolean isResizingAreaSelected(Point p) {
    Rectangle r = getBounds();
    // Since added to workspace, now in local coordinates
    if ((r.width - p.x < 10) && (r.height - p.y < 20)) {
      return true;
    } else {
      return false;
    }
  }

  /** Show the context menu for a comment  */
  public void showContextMenu(MouseEvent evt, Comment comment) {
    JPopupMenu pop = new JPopupMenu();
    addItem(pop, "Delete",
        new RemoveCommentsAction(getEditor(), comment.getData()));
    Rectangle r = comment.getBounds();
    pop.show(this, r.width, evt.getY());
  }

  public void setSelected() {
    if (! isEnabled()) {
      mTextField.setBackground(activeColor);
      getEditor().getUndoManager().startTextMode();
      mEditMode = true;
      mTextField.setEditable(true);
      setEnabled(true);
      EventDispatcher.getInstance().convey(new ElementSelectedEvent(this));
      requestFocus();
    }
  };

  /** Resets the comment to its default visual behavior */
  public void setDeselected() {
    if (isEnabled()) {
      if (mEditMode) {
        getEditor().getUndoManager().endTextMode();
        mEditMode = false;
        if (mDocument.contentChanged())
          new EditContentAction(getEditor(), mDocument).run();
      }
      update();
      mTextField.setBackground(inactiveColor);
      mTextField.setEditable(false);
      setEnabled(false);
      EventDispatcher.getInstance().convey(new ElementSelectedEvent(null));
    }
  }

  public static void passEventToParent(MouseEvent me, int what) {
    // Now make sure the WorkSpacePanel knows about the mouse press event
    Component child = me.getComponent();
    Component parent = child.getParent();

    // transform the mouse coordinate to be relative to the parent component:
    int deltax = child.getX() + me.getX();
    int deltay = child.getY() + me.getY();

    // build new mouse event:
    MouseEvent parentMouseEvent = new MouseEvent(parent, what, me.getWhen(),
        me.getModifiersEx(), deltax, deltay, me.getClickCount(),
        me.isPopupTrigger(), me.getButton());
    // dispatch it to the parent component
    parent.dispatchEvent(parentMouseEvent);
  }

  private class MyMouseListener extends MouseAdapter {

    // interaction flags: move or resize?
    private boolean mResizing;

    // for drag: move or resize
    private Rectangle mCommentStartBounds = null;

    // To compute movement delta
    Point mPressedLocation = null;



    @Override
    /** Pass on the mouse press event if this component is not enabled */
    public void mouseClicked(MouseEvent me) {
      // show context menu
      if ((me.getButton() == MouseEvent.BUTTON3)
          && (me.getClickCount() == 1)) {
        showContextMenu(me, Comment.this);
        return;
      }
      if (Comment.this.isEnabled()) return;
      mPressedLocation = me.getPoint();
      // parent (WorkSpacePanel) will handle selection/deselection
      passEventToParent(me, MouseEvent.MOUSE_CLICKED);
    }

    @Override
    /** Pass on the mouse press event if this component is not enabled */
    public void mousePressed(MouseEvent me) {
      if (Comment.this.isEnabled()) return;
      mPressedLocation = me.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (mCommentStartBounds != null) {
        new MoveCommentAction(getEditor(), Comment.this.getData(),
            mWorkSpace.toModelBoundary(mCommentStartBounds))
            .run();
        mCommentStartBounds = null;
        mResizing = false;
        setBackground(TRANSPARENT);
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      // If selected, leave it to the text area
      if (Comment.this.isEnabled()) return;
      Point currentMousePosition = e.getPoint();
      if (mCommentStartBounds == null) {
        mCommentStartBounds = getBounds();
        // if not dragged, but once resized, leave it by resized and vice versa,
        // leave it by dragged
        mResizing = isResizingAreaSelected(currentMousePosition);
      }

      // compute movement trajectory vectors
      int dx = currentMousePosition.x - mPressedLocation.x;
      int dy = currentMousePosition.y - mPressedLocation.y;

      if (mResizing) {
        setBackground(Color.white);
        // Change the size of a comment with the mouse
        Dimension d = getSize();
        d.width = Math.max(mWorkSpace.toViewXPos(50), d.width + dx);
        d.height = Math.max(mWorkSpace.toViewYPos(50), d.height + dy);
        setSize(d);
        mPressedLocation.translate(dx, dy);
      } else {
        // Change the location of a comment with the mouse
        Point currPos = getLocation();
        currPos.translate(dx, dy);
        if (currPos.x >= 0 && currPos.y >= 0) {
          setLocation(currPos);
        }
      }
      // update model boundary
      setBoundary(getBounds());
    }
  }


  private class TextAreaMouseListener extends MouseAdapter {

    @Override
    /** Pass on the mouse press event if this component is not enabled */
    public void mouseClicked(MouseEvent me) {
      if (! Comment.this.isEnabled())
        // parent (WorkSpacePanel) will handle selection/deselection
        passEventToParent(me, MouseEvent.MOUSE_CLICKED);
    }

    @Override
    /** Pass on the mouse press event if this component is not enabled */
    public void mousePressed(MouseEvent me) {
      if (! Comment.this.isEnabled())
        passEventToParent(me, MouseEvent.MOUSE_PRESSED);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (! Comment.this.isEnabled())
        passEventToParent(e, MouseEvent.MOUSE_RELEASED);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (! Comment.this.isEnabled())
        passEventToParent(e, MouseEvent.MOUSE_DRAGGED);
    }
  }

  private ProjectEditor getEditor() {
    Container p = this;
    while (! (p instanceof ProjectEditor)) {
      p = p.getParent();
    }
    return (ProjectEditor)p;
  }
}
