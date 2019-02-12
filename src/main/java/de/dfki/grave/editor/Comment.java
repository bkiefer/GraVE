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
import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.util.ios.ResourceLoader;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Comment extends EditorComponent
implements MouseListener, MouseMotionListener {

  private JEditorPane mTextEditor = null;
  private JLabel mTextLabel = null;

  // font
  private Font mFont = null;

  // position
  private Point mClickPosition = new Point(0, 0);

  // for drag: move or resize
  private Point mLastMousePos = null;
  private Rectangle mCommentStartBounds = null;

  // edit
  private boolean mEditMode = false;

  // image
  private Image mResizeMarker;
  private AlphaComposite mAC;
  private AlphaComposite mACFull;
  private CommentBadge mDataComment;

  // interaction flags
  public boolean mPressed;
  public boolean mDragged;
  public boolean mResizing;

  public Comment() {
    mDataComment = null;
  }

  public Comment(WorkSpace workSpace, CommentBadge dataComment) {
    mAC = AlphaComposite.getInstance(AlphaComposite.XOR, 0.15f);
    mACFull = AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f);
    mWorkSpace = workSpace;
    mDataComment = dataComment;

    // resize marker
    mResizeMarker = ResourceLoader.loadImage("img/new/resize.png");

    // font setup
    mFont = new Font("SansSerif", Font.ITALIC, /* (mWorkSpace != null) ? */
            getEditorConfig().sWORKSPACEFONTSIZE /* : sBUILDING_BLOCK_FONT_SIZE */);

    Boundary b = mDataComment.getBoundary();

    // size setup
    setBounds(zoom(b.getXPos()), zoom(b.getYPos()),
        zoom(b.getWidth()), zoom(b.getHeight()));
    mTextLabel = new JLabel();
    mTextLabel.setOpaque(false);
    mTextLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    mTextLabel.setVerticalAlignment(SwingConstants.TOP);
    mTextLabel.setFont(mFont);

    // mTextLabel.setForeground(new Color(147, 130, 52, 127));
    mTextLabel.setForeground(new Color(75, 75, 75, 127));
    mTextEditor = new JEditorPane();
    //mTextEditor.setContentType();
    mTextEditor.setOpaque(false);
    mTextEditor.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    // now use the same font than the label!
    // String bodyRule = "body { font-family: " + mFont.getFamily() + "; " + "font-size: " + mFont.getSize() + "pt; }";
    //((HTMLDocument) mTextEditor.getDocument()).getStyleSheet().addRule(bodyRule);
    setLayout(new BorderLayout());

    // first put it in the editor, then back in the label
    mTextEditor.setText(mDataComment.getHTMLText());
    mTextLabel.setText(formatLabelText(mTextEditor.getText()));
    add(mTextLabel, BorderLayout.CENTER);
  }

  private String formatLabelText(String text) {
    //In order to the JLabel accept break lines
    text = text.replaceAll("(\r\n|\n)", "<br />");
    String displayText = "<html>" + text + "</html>";
    return displayText;
  }

  @Override
  public void update(Observable o, Object obj) {
    update();
  }

  public void update() {

    mFont = new Font("SansSerif", Font.ITALIC,
        getEditorConfig().sWORKSPACEFONTSIZE);
    mTextLabel.setFont(mFont);
    mTextEditor.setFont(mFont);

    /* Do we want this?
    String bodyRule = "body { font-family: " + mFont.getFamily()
       + "; " + "font-size: " + mFont.getSize() + "pt; }";
    ((HTMLDocument) mTextEditor.getDocument()).getStyleSheet().addRule(bodyRule);
    */
    mDataComment.setHTMLText(mTextEditor.getText());
    mTextEditor.setText(mDataComment.getHTMLText());
    mTextLabel.setText(formatLabelText(mTextEditor.getText()));
    repaint(100);
  }

  public String getDescription() {
    return toString();
  }

  public CommentBadge getData() {
    return mDataComment;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

//      mFont = new Font("SansSerif", Font.PLAIN, /*(mWorkSpace != null) ?*/ sWORKSPACEFONTSIZE /*: sBUILDING_BLOCK_FONT_SIZE*/);
//           mTextLabel.setFont(mFont);
//             mTextLabel.setText(mTextEditor.getText());
//    S tring bodyRule = "body { font-family: " + mFont.getFamily() + "; " + "font-size: " + mFont.getSize() + "pt; }";
//    ( (HTMLDocument) mTextEditor.getDocument()).getStyleSheet().addRule(bodyRule);
    Graphics2D graphics = (Graphics2D) g;

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Rectangle r = getBounds();

    // graphics.setColor(new Color(227, 206, 29, 127));
    graphics.setColor(new Color(200, 200, 200, 200));

    if (mEditMode) {
      graphics.setStroke(new BasicStroke(2.0f));
      graphics.drawRoundRect(0, 0, r.width - 1, r.height - 1, 15, 15);
    } else {
      graphics.fillRoundRect(0, 0, r.width, r.height, 15, 15);
      graphics.setComposite(mAC);
      graphics.drawImage(mResizeMarker, r.width - 13, r.height - 13, this);
      graphics.setComposite(mACFull);
    }
  }

  private void setBoundary(Rectangle r) {
    mDataComment.setBoundary(
        new Boundary(unzoom(r.x), unzoom(r.y), unzoom(r.width), unzoom(r.height)));
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
    pop.show(this, evt.getX() - r.x, evt.getY() - r.y);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    mPressed = false;

    Point loc = getLocation();
    Point clickLoc = e.getPoint();

    // save click location relavitvely to node postion
    mClickPosition.setLocation(clickLoc.x - loc.x, clickLoc.y - loc.y);

    if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
      String text = mTextEditor.getText();

      mTextEditor.setText(text);
      remove(mTextLabel);
      add(mTextEditor, BorderLayout.CENTER);
      mEditMode = true;
    }

    // show context menu
    if ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1)) {
      showContextMenu(e, this);
    }

    revalidate();
    repaint(100);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    mLastMousePos = e.getPoint();
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
      new MoveCommentAction(mWorkSpace, this, mCommentStartBounds).run();
      mCommentStartBounds = null;
    }

    // DEBUG System.out.println("mouse released");
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
    int dx = currentMousePosition.x - mLastMousePos.x;
    int dy = currentMousePosition.y - mLastMousePos.y;

    mLastMousePos = new Point(currentMousePosition.x, currentMousePosition.y);

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
  public void mouseMoved(MouseEvent e) {
  }

  public void setSelected(){};

  /** Resets the comment to its default visual behavior */
  public void setDeselected() {

    // DEBUG System.out.println("Comment Deselected!");
    if (mEditMode) {
      String htmlText = mTextEditor.getText();

      System.out.println(htmlText);
      mTextLabel.setText(formatLabelText(htmlText));
      remove(mTextEditor);
      add(mTextLabel, BorderLayout.CENTER);
      mEditMode = false;

      // store text in TEXT node
      mDataComment.setHTMLText(htmlText);
    }

    mPressed = false;
    mDragged = false;
    mResizing = false;
    update();
  }

}
