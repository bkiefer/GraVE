package de.dfki.grave.editor;

import static de.dfki.grave.editor.panels.WorkSpacePanel.addItem;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import javax.swing.*;
import javax.swing.border.Border;

import de.dfki.grave.Preferences;
import de.dfki.grave.editor.action.MoveCommentAction;
import de.dfki.grave.editor.action.RemoveCommentAction;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.Boundary;
import de.dfki.grave.model.flow.CommentBadge;

import java.util.Observer;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Comment extends JTextArea
    implements DocumentContainer, Observer {

  private ObserverDocument mDocument;
  private CommentBadge mDataComment;
  protected WorkSpace mWorkSpace;

  // position
  // private Point mClickPosition = new Point(0, 0);

  // for drag: move or resize
  private Rectangle mCommentStartBounds = null;

  // edit
  private boolean mEditMode = false;

  // interaction flags
  private boolean mResizing;

  public Comment() {
    setDocument(mDocument = new ObserverDocument(""));
  }

  public Comment(WorkSpace workSpace, CommentBadge dataComment) {
    super();
    mWorkSpace = workSpace;
    mDataComment = dataComment;
    setDocument(mDocument = new ObserverDocument(dataComment));

    // font setup
    setFont(mWorkSpace.getEditorConfig().sCOMMENT_FONT.getFont());

    Color borderColor = Preferences.sCOMMENT_BADGE_COLOR.darker();
    Border border = BorderFactory.createMatteBorder(3, 3, 3, 3, borderColor);
    setBorder(border);

    // size setup
    update();

    setVisible(true);
    setLineWrap(true);
    setWrapStyleWord(true);

    // setLayout(new BorderLayout());

    setBackground(Preferences.sCOMMENT_BADGE_COLOR);

    // TODO: add DocumentListener to resize after text changes?

    // add our own listener so we can drag this thing around
    /*
    for (MouseMotionListener m : getMouseMotionListeners())
      removeMouseMotionListener(m);
    for (MouseListener m : getMouseListeners())
      removeMouseListener(m);
     */
    MyMouseListener myDrag = new MyMouseListener();
    addMouseMotionListener(myDrag);
    addMouseListener(myDrag);
    
    setEnabled(false);
    addFocusListener(new FocusListener() {

      @Override
      public void focusGained(FocusEvent e) {
      }

      @Override
      public void focusLost(FocusEvent e) {
        setDeselected();
      }
    });
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

  /*
   * Returns true if mouse in the lower right area, which stands for the resizng
   * area. ------ | | | | | -| | | | ------
   *
   */
  private boolean isResizingAreaSelected(Point p) {
    Rectangle r = getBounds();
    // Since added to workspace, now in local coordinates
    if ((r.width - p.x < 15) && (r.height - p.y < 15)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Show the context menu for a comment
   */
  public void showContextMenu(MouseEvent evt, Comment comment) {
    JPopupMenu pop = new JPopupMenu();
    addItem(pop, "Delete", new RemoveCommentAction(mWorkSpace, comment));
    Rectangle r = comment.getBounds();
    pop.show(this, r.width, evt.getY());
  }

  public void setSelected() {

  };

  /** Resets the comment to its default visual behavior */
  public void setDeselected() {
    if (mEditMode) {
      // TODO: write Text back to model with an undoable action.
      mEditMode = false;
      mDocument.updateModel();
    }

    mResizing = false;
    update();
    setEnabled(false);
  }

  private class MyMouseListener implements MouseListener, MouseMotionListener {
    Point mPressedLocation = null;

    @Override
    public void mouseClicked(MouseEvent e) {
      if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
        mEditMode = true;
        Comment.this.setEditable(true);
        Comment.this.setEnabled(true);
        Comment.this.requestFocus();
      }

      // show context menu
      if ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1)) {
        showContextMenu(e, Comment.this);
      }
    }

    @Override
    public void mousePressed(MouseEvent me) {
      mPressedLocation = me.getPoint();
      // DEBUG System.out.println("mouse pressed");

      // Now make sure the WorkSpacePanel knows about the mouse press event
      Component child = me.getComponent();
      Component parent = child.getParent();

      // transform the mouse coordinate to be relative to the parent component:
      int deltax = child.getX() + me.getX();
      int deltay = child.getY() + me.getY();

      // build new mouse event:
      MouseEvent parentMouseEvent = new MouseEvent(parent,
          MouseEvent.MOUSE_PRESSED, me.getWhen(), me.getModifiersEx(), deltax,
          deltay, me.getClickCount(), false);
      // dispatch it to the parent component
      parent.dispatchEvent(parentMouseEvent);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (mCommentStartBounds != null) {
        new MoveCommentAction(mWorkSpace, Comment.this, mCommentStartBounds)
            .run();
        mCommentStartBounds = null;
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
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

      if (Preferences.DEBUG_MOUSE_LOCATIONS) {
        mWorkSpace.setMessageLabelText(String.format("(%d, %d)/(%d, %d)",
            mPressedLocation.x, mPressedLocation.y, dx, dy));
      }
      
      if (mResizing) {
        // Change the size of a comment with the mouse
        Dimension d = getSize();
        d.width = Math.max(mWorkSpace.toViewXPos(50), d.width + dx);
        d.height = Math.max(mWorkSpace.toViewYPos(50), d.height + dy);
        setSize(d);
        mPressedLocation.x += dx;
        mPressedLocation.y += dy;
      } else {
        // Change the location of a comment with the mouse
        Point currPos = getLocation();
        dx += currPos.x;
        dy += currPos.y;
        if (dx >= 0 && dy >= 0) {
          setLocation(dx, dy);
        }
      }
      // update model boundary
      setBoundary(getBounds());
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }
  }
}
