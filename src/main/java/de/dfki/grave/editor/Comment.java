package de.dfki.grave.editor;

import static de.dfki.grave.editor.panels.WorkSpacePanel.addItem;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import javax.swing.*;

import de.dfki.grave.editor.action.MoveCommentAction;
import de.dfki.grave.editor.action.RemoveCommentAction;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.CommentBadge;
import de.dfki.grave.model.flow.geom.Boundary;
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

  private Font mFont = null;

  // position
  private Point mClickPosition = new Point(0, 0);

  // for drag: move or resize
  private Rectangle mCommentStartBounds = null;

  // edit
  private boolean mEditMode = false;

  // interaction flags
  public boolean mPressed;
  public boolean mDragged;
  public boolean mResizing;

  public Comment() {
    setDocument(mDocument = new ObserverDocument(""));
  }

  public Comment(WorkSpace workSpace, CommentBadge dataComment) {
    super();
    mWorkSpace = workSpace;
    mDataComment = dataComment;
    setDocument(mDocument = new ObserverDocument(dataComment));

    // font setup
    mFont = new Font("SansSerif", Font.ITALIC, /* (mWorkSpace != null) ? */
            mWorkSpace.getEditorConfig().sWORKSPACEFONTSIZE /* : sBUILDING_BLOCK_FONT_SIZE */);

    Boundary b = mDataComment.getBoundary();

    // size setup
    setBounds(b.getXPos(), b.getYPos(), b.getWidth(), b.getHeight());

    setFont(mFont);
    setVisible(true);
    setLineWrap(true);
    setWrapStyleWord(true);

    setLayout(new BorderLayout());
    setBackground(new Color(200, 200, 200, 200));

    // TODO: add DocumentListener to resize after text changes?

    // add our own listener so we can drag this thing around
    for (MouseMotionListener m : getMouseMotionListeners())
      removeMouseMotionListener(m);
    MyMouseMotionListener myDrag = new MyMouseMotionListener();
    addMouseMotionListener(myDrag);
    // not necessary to delete other MouseListeners
    addMouseListener(myDrag);
  }

  @Override
  public void update(Observable o, Object o1) {
    update();
  }

  public void update() {
    repaint(100);
  }

  public ObserverDocument getDoc() {
    return mDocument;
  }

  public String getDescription() {
    return toString();
  }

  public CommentBadge getData() {
    return mDataComment;
  }

  private void setBoundary(Rectangle r) {
    mDataComment.setBoundary(
        new Boundary(mWorkSpace.toModelXPos(r.x), mWorkSpace.toModelXPos(r.y),
            mWorkSpace.toModelXPos(r.width), mWorkSpace.toModelXPos(r.height)));
  }

  /** For undo/redo */
  public void moveOrResize(Rectangle r) {
    setBounds(r);
    setBoundary(r);
  }

  public boolean containsPoint(Point p) {
    return getBounds().contains(p.x, p.y);
  }

  /*
     * Returns true if mouse in the lower right area, which stands for the resizng area.
     *  ------
     * |      |
     * |      |
     * |     -|
     * |    | |
     *  ------
     *
   */
  private boolean isResizingAreaSelected(Point p) {
    Rectangle r = getBounds();
    // TODO: ADAPT FOR ZOOM
    if (((r.x + r.width) - p.x < 15) && ((r.y + r.height) - p.y < 15)) {
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


  private class MyMouseMotionListener implements MouseListener,
          MouseMotionListener {
    Point mPressedLocation = null;

    @Override
    public void mouseClicked(MouseEvent e) {
      mPressed = false;

      Point loc = getLocation();
      Point clickLoc = e.getPoint();

      // save click location relavitvely to node postion
      mClickPosition.setLocation(clickLoc.x - loc.x, clickLoc.y - loc.y);

      if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
        mEditMode = true;
      }

      // show context menu
      if ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1)) {
        showContextMenu(e, Comment.this);
      }

      revalidate();
      repaint(100);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      mPressedLocation = e.getPoint();
      // DEBUG System.out.println("mouse pressed");
      mPressed = true;

      Point loc = getLocation();
      Point clickLoc = e.getPoint();

      // save click location relavitvely to node postion
      mClickPosition.setLocation(clickLoc.x - loc.x, clickLoc.y - loc.y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (mCommentStartBounds != null) {
        new MoveCommentAction(mWorkSpace, Comment.this, mCommentStartBounds).run();
        mCommentStartBounds = null;
      }

      mPressed = false;
      mDragged = false;
      repaint(100);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      Point currentMousePosition = e.getPoint();
      if (mCommentStartBounds == null) {
        mCommentStartBounds = getBounds();
        // if not dragged, but once resized, leave it by resized and vice versa, leave it by dragged
        mResizing = isResizingAreaSelected(currentMousePosition);
      }

      // compute movement trajectory vectors
      int dx = currentMousePosition.x - mPressedLocation.x;
      int dy = currentMousePosition.y - mPressedLocation.x;

      if (mResizing) {
        // Change the size of a comment with the mouse
        Dimension d = getSize();
        d.width = Math.max(50, d.width + dx);
        d.height = Math.max(50, d.height + dy);
        setSize(d);
      } else {
        // Change the location of a comment with the mouse
        Point currPos = getLocation();
        dx += currPos.x; dy += currPos.y;
        if (dx >= 0 && dy >= 0) {
          setLocation(dx, dy);
        }
      }
      // update model boundary
      setBoundary(getBounds());
      if ((e.getModifiersEx() == 1024)) {
        mDragged = true;
      }
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

  public void setSelected(){};

  /** Resets the comment to its default visual behavior */
  public void setDeselected() {
    if (mEditMode) {
    }

    mPressed = false;
    mDragged = false;
    mResizing = false;
    update();
  }

}
