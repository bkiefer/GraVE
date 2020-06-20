package de.dfki.grave.editor;

import static de.dfki.grave.editor.panels.WorkSpacePanel.addItem;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import de.dfki.grave.Preferences;
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
public class Comment extends JTextArea 
implements DocumentContainer, Observer, ProjectElement {

  private ObserverDocument mDocument;
  private CommentBadge mDataComment;
  private WorkSpace mWorkSpace;

  // in edit, or not
  private boolean mEditMode = false;
  
  private final Color activeColor, inactiveColor;

  /** Constructor for prototype in the "Create item" area */
  public Comment() {
    activeColor = inactiveColor = null;
    setDocument(mDocument = new ObserverDocument(""));
  }

  public Comment(WorkSpace workSpace, CommentBadge dataComment) {
    super();
    mWorkSpace = workSpace;
    mDataComment = dataComment;
    setDocument(mDocument = new ObserverDocument(dataComment));
    mDocument.addUndoableEditListener(
        new UndoableEditListener() {
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
    setDisabledTextColor(Color.gray);
    Border border = new TextBubbleBorder(Color.gray,3,7,9);
    setBorder(border);

    // size setup
    update();

    setVisible(true);
    setLineWrap(true);
    setWrapStyleWord(true);

    MyMouseListener myDrag = new MyMouseListener();
    addMouseMotionListener(myDrag);
    addMouseListener(myDrag);
    
    setDeselected();
  }

  @Override
  public void update(Observable o, Object o1) {
    update();
  }

  public void update() {
    Boundary b = mDataComment.getBoundary();
    setBounds(mWorkSpace.toViewRectangle(b));
  }

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
    if ((r.width - p.x < 15) && (r.height - p.y < 15)) {
      return true;
    } else {
      return false;
    }
  }

  /** Show the context menu for a comment  */
  public void showContextMenu(MouseEvent evt, Comment comment) {
    JPopupMenu pop = new JPopupMenu();
    addItem(pop, "Delete", 
        new RemoveCommentsAction(mWorkSpace.getEditor(), comment.getData()));
    Rectangle r = comment.getBounds();
    pop.show(this, r.width, evt.getY());
  }

  public void setSelected() {
    setBackground(activeColor);
    getEditor().getUndoManager().startTextMode();
    mEditMode = true;
    setEditable(true);
    setEnabled(true);
    EventDispatcher.getInstance().convey(new ElementSelectedEvent(this));
    requestFocus();
  };

  /** Resets the comment to its default visual behavior */
  public void setDeselected() {
    if (mEditMode) {
      getEditor().getUndoManager().endTextMode();
      mEditMode = false;
      if (mDocument.contentChanged())
        new EditContentAction(mWorkSpace.getEditor(), mDocument).run();
    }
    update();
    setBackground(inactiveColor);
    setEditable(false);
    setEnabled(false);
  }

  private class MyMouseListener extends MouseAdapter {
    
    // interaction flags: move or resize?
    private boolean mResizing;
  
    // for drag: move or resize
    private Rectangle mCommentStartBounds = null;
    
    // To compute movement delta
    Point mPressedLocation = null;
    
    private void passEventToParent(MouseEvent me, int what) {
      // Now make sure the WorkSpacePanel knows about the mouse press event
      Component child = me.getComponent();
      Component parent = child.getParent();

      // transform the mouse coordinate to be relative to the parent component:
      int deltax = child.getX() + me.getX();
      int deltay = child.getY() + me.getY();

      // build new mouse event:
      MouseEvent parentMouseEvent = new MouseEvent(parent, what, me.getWhen(),
          me.getModifiersEx(), deltax, deltay, me.getClickCount(), false);
      // dispatch it to the parent component
      parent.dispatchEvent(parentMouseEvent);
    }
    
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
        new MoveCommentAction(mWorkSpace.getEditor(), Comment.this.getData(),
            mWorkSpace.toModelBoundary(mCommentStartBounds))
            .run();
        mCommentStartBounds = null;
        mResizing = false;
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
    
  public ProjectEditor getEditor() {
    Container p = this;
    while (! (p instanceof ProjectEditor)) {
      p = p.getParent();
    }
    return (ProjectEditor)p;
  }
}
