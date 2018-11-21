package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.*;

import javax.swing.JComponent;

import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.project.EditorConfig;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.editor.util.DockingManager;
import de.dfki.vsm.model.flow.*;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public final class Node extends JComponent implements Observer {

  // ToDO: move to workspace - just have a link here
  private StartSign mStartSign = null;
  private StartSign mAltStartSign = null;

  // private Point mStartSignPosition;
  //
  private DockingManager mDockingManager = null;

  // interaction flags
  public boolean mSelected = false;
  public boolean mPressed = false;
  public boolean mDragged = false;

  private final EventDispatcher mEventMulticaster = EventDispatcher.getInstance();
  private Type mType;
  private BasicNode mDataNode;

  //
  // TODO: move away
  private final WorkSpacePanel mWorkSpace;

  // The name which will be displayed on the node
  private String mDisplayName;

  // The color of the node
  // TODO: eventually move computation of color to paint component
  private Color mColor;

  private boolean mIsActive;

  private EditorConfig getEditorConfig() { return mWorkSpace.getEditorConfig(); }

  public enum Type {
    BasicNode, SuperNode
  }

  /**
   *
   */
  public Node(WorkSpacePanel workSpace, BasicNode dataNode) {
    mWorkSpace = workSpace;
    mDataNode = dataNode;
    // setToolTipText(mDataNode.getId());
    // the former overrides any MouseListener!!!

    if (mDataNode instanceof SuperNode) {
      mType = Type.SuperNode;
    } else {
      mType = Type.BasicNode;
    }

    // Init docking manager
    mDockingManager = new DockingManager(this);

    // Set initial position
    Point pos = new Point(mDataNode.getPosition().getXPos(),
            mDataNode.getPosition().getYPos());

    setBounds(pos.x, pos.y, getEditorConfig().sNODEWIDTH, getEditorConfig().sNODEHEIGHT);

    // Set the initial start sign
    HashMap<String, BasicNode> startNodeMap
            = mWorkSpace.getSceneFlowManager().getCurrentActiveSuperNode().getStartNodeMap();

    if (startNodeMap.containsKey(mDataNode.getId())) {
      addStartSign();
    }
    if (mDataNode.isHistoryNode()) {
      addAltStartSign();
    }

    // update
    update();
  }

  public Type getType() {
    return mType;
  }

  public WorkSpacePanel getWorkSpace() {
    return mWorkSpace;
  }

  public BasicNode getDataNode() {
    return mDataNode;
  }

  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  public DockingManager getDockingManager() {
    return mDockingManager;
  }

  @Override
  public void update(Observable o, Object obj) {
    update();
  }

  private void setColor() {
    // Update the color of the node that has to be changed
    // if the type or the flavour of the node have changed
    if (mType == Type.SuperNode)
      mColor = sSUPER_NODE_COLOR;
    else
      mColor = sBASIC_NODE_COLOR;

    // Set the history node color
    if (mDataNode.isHistoryNode()) {
      mColor = sHISTORY_NODE_COLOR;
    }

    // Set the flavour dependend color
    switch (mDataNode.getFlavour()) {
      case ENODE: mColor = sEEDGE_COLOR; break;
      case FNODE: mColor = sFEDGE_COLOR; break;
      case TNODE: mColor = sTEDGE_COLOR; break;
      case PNODE: mColor = sPEDGE_COLOR; break;
      case CNODE: mColor = sCEDGE_COLOR; break;
      case INODE: mColor = sIEDGE_COLOR; break;
      case NONE: break;
    }
  }

  private void update() {

    // reset location
    // Recompute the node's docking positions.
    // Free all docking points that have to be
    // recomputed if the node's size has changed
    mDockingManager.update();

    if (mStartSign != null) {
      mStartSign.update();
    }

    if (mAltStartSign != null) {
      mAltStartSign.update();
    }

    // Update the font and the font metrics that have to be
    // recomputed if the node's font size has changed
    // TODO: Move attributes to preferences and make editable
    Map<TextAttribute, Object> map = new Hashtable<>();

    map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    map.put(TextAttribute.FAMILY, Font.SANS_SERIF);
    map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_DEMIBOLD);
    map.put(TextAttribute.SIZE, getEditorConfig().sWORKSPACEFONTSIZE);

    // Derive the font from the attribute map
    Font font = Font.getFont(map);
    // Derive the node's font metrics from the font
    FontMetrics fontMetrics = getFontMetrics(font);
    // Set the node's font to the updated font
    setFont(font);

    //TODO!!!
    // Update the display name that has to be changed if the
    // node's size or the node's font size have chaged
    String prefix = "";
    if (fontMetrics.stringWidth(mDataNode.getName()) > getWidth() - 10) {
      for (char c : mDataNode.getName().toCharArray()) {
        if (fontMetrics.stringWidth(prefix + c + "...") < getWidth() - 10) {
          prefix += c;
        } else {
          break;
        }
      }
      mDisplayName = mDataNode.getName();//prefix + "...";
    } else {
      mDisplayName = mDataNode.getName();
    }

    // Set the flavour dependend color
    setColor();

    // Update the bounds if the node's size has changed
    // BK: WHAT? IF THE SIZE HAS CHANGED, RESET TO EDITORCONFIG?
    //setBounds(getX(), getY(), getEditorConfig().sNODEWIDTH, getEditorConfig().sNODEHEIGHT);
  }

  @Override
  public String getName() {
    return mDisplayName;
  }

  public void resetLocation(Point newLocation) {
    Point location = getLocation();

    for (Edge edge : mDockingManager.getConnectedEdges()) {
      edge.mEg.updateRealtiveEdgeControlPointPos(this, newLocation.x - location.x, newLocation.y - location.y);
    }

    setLocation(newLocation);
  }

  public void updateLocation(Point vector) {
    Point location = getLocation();

    for (Edge edge : mDockingManager.getConnectedEdges()) {
      edge.mEg.updateRealtiveEdgeControlPointPos(this, vector.x, vector.y);
    }

    setLocation(location.x + vector.x, location.y + vector.y);
    updateDataModel();
  }

  // TODO - move to controler class - sceneflowManager!
  private void updateDataModel() {
//      mDataNode.getGraphics().setPosition(getLocation().x, getLocation().y);
    Position g = new Position(getLocation().x, getLocation().y);

    mDataNode.setPosition(g);
  }

  // TODO: move to workspace
  public void removeStartSign() {
    if (mStartSign != null) {
      mDockingManager.releaseDockPointForStartSign();
      mWorkSpace.remove(mStartSign);
      mStartSign = null;
    }

//      if (mAltStartSign != null) {
//        mDockingManager.releaseDockPointForStartSign();
//        mWorkSpace.remove(mAltStartSign);
//        mAltStartSign = null;
//      }
  }

  // TODO: move to workspace
  public void addStartSign() {
    mStartSign = new StartSign(this);
    mWorkSpace.add(mStartSign);
  }

  public void addAltStartSign() {
    mAltStartSign = new StartSign(this, true, Color.LIGHT_GRAY);
    mWorkSpace.add(mAltStartSign);
  }

  // Tells the node that an edge connects and that node is sourcenode
  public Point connectEdgeAtSourceNode(Edge edge, Point point) {
    // get location of node
    Point loc = getLocation();

    // get relative (to the current node) coordinates;
    point.setLocation(point.x - loc.x, point.y - loc.y);

    Point dp = mDockingManager.getNearestDockPoint(edge, point);

    // make position absolute to underlying canvas
    dp.setLocation(dp.x + loc.x, dp.y + loc.y);

    // set working type and color
    setColor();
    return dp;
  }

  // Tells the node that an edge connects
  public Point connectEdgetAtTargetNode(Edge e, Point p) {
    // get location of node
    Point loc = getLocation();
    // get relative (to the current node) coordinates;
    p.setLocation(p.x - loc.x, p.y - loc.y);
    Point dp = mDockingManager.getNearestDockPoint(e, p);
    // make position absolute to underlying canvas
    dp.setLocation(dp.x + loc.x, dp.y + loc.y);
    return dp;
  }

  public Point connectSelfPointingEdge(Edge e, Point p) {
    Point loc = getLocation();
    // get relative (to the current node) coordinates;
    p.setLocation(p.x - loc.x, p.y - loc.y);
    Point dp = mDockingManager.getNearestSecondDockPoint(e, p);
    // make position absolute to underlying canvas
    dp.setLocation(dp.x + loc.x, dp.y + loc.y);
    return dp;
  }

  public Point disconnectEdge(Edge e) {
    Point relPos = mDockingManager.freeDockPoint(e);
    Point pos = getLocation();
    Point absLoc;
    if (relPos != null) {
      absLoc = new Point(relPos.x + pos.x, relPos.y + pos.y);
    } else {
      absLoc = new Point(pos.x, pos.y);
    }
    return absLoc;
  }

  public Point disconnectSelfPointingEdge(Edge e) {
    Point relPos = mDockingManager.freeSecondDockPoint(e);
    Point pos = getLocation();
    Point absLoc = new Point(relPos.x + pos.x, relPos.y + pos.y);
    return absLoc;
  }

  /*
     * Returns the center of a node
   */
  public Point getCenterPoint() {
    Point loc = getLocation();
    Point c = new Point();
    c.setLocation(loc.x + getWidth() / 2, loc.y + getHeight() / 2);
    return c;
  }

  public Set<Edge> getConnectedEdges() {
    return mDockingManager.getConnectedEdges();
  }

  public Point getEdgeDockPoint(Edge e) {
    Point loc = getLocation();
    Point dp = mDockingManager.getDockPoint(e);
    // make position absolute to underlying canvas
    if (dp != null) {
      dp.setLocation(dp.x + loc.x, dp.y + loc.y);
    } else {
      if (this.mDataNode.isEndNode()) {
        return (new Point(loc.x, loc.y + getHeight() / 2));
      } else {
        return (new Point(loc.x + getWidth(), loc.y + getHeight() / 2));
      }
    }
    return dp;
  }

  public Point getSelfPointingEdgeDockPoint(Edge e) {
    Point loc = getLocation();
    Point dp = mDockingManager.getSecondDockPoint(e);
    // make position absolute to underlying canvas
    if (dp != null) {
      dp.setLocation(dp.x + loc.x, dp.y + loc.y);
    } else {
      if (this.mDataNode.isEndNode()) {
        return (new Point(loc.x, loc.y + getHeight() / 2));
      } else {
        return (new Point(loc.x + getWidth(), loc.y + getHeight() / 2));
      }
    }
    return dp;
  }

  public ArrayList<Point> getEdgeStartPoints() {
    ArrayList<Point> fDP = mDockingManager.getFreeDockPoints();
    ArrayList<Point> points = new ArrayList<>();
    Point loc = getLocation();
    for (Point p : fDP) {
      // make position absolute to underlying canvas
      points.add(new Point(p.x + loc.x, p.y + loc.y));
    }
    return points;
  }

  public boolean isEdgeAllowed(AbstractEdge e) {
    return mDataNode.canAddEdge(e);
  }

  /*
     * Resets the node to its default visual behavior
   */
  public void setDeselected() {
    mSelected = false;
    mPressed = false;
    mDragged = false;
    repaint(100);
  }

  public void mouseClicked(MouseEvent event) {
    mPressed = false;
    mSelected = true;
    //Point loc = getLocation();
    //Point clickLoc = event.getPoint();

    // mLastMousePosition = new Point(clickLoc);
    // save click location relavitvely to node postion
    // mClickPosition.setLocation(clickLoc.x - loc.x, clickLoc.y - loc.y);
    repaint(100);

//      enter supernode, if it has been double clicked
    // TODO: move to workspace
    if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
      if (mType == Type.SuperNode) {
        mWorkSpace.increaseWorkSpaceLevel(this);
      }
    }

    // show contect menu
    // TODO: move to workspace
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      mWorkSpace.showContextMenu(event, this);
    }

//      ////////!!!!!!!!!!!!!!!!!!!!
    // System.err.println("Sending node selected event");
    mEventMulticaster.convey(new NodeSelectedEvent(this, this.getDataNode()));
  }

  public void mousePressed(MouseEvent event) {
    mPressed = true;
    mSelected = true;

    //Point loc = getLocation();
    //Point clickLoc = event.getPoint();

    // mLastMousePosition =                new Point(clickLoc);
    // save click location relavitvely to node postion
    // mClickPosition.setLocation(clickLoc.x - loc.x, clickLoc.y - loc.y);
    repaint(100);

//      enter supernode, if it has been double clicked
    // TODO: move to workspace
    if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
      if (mType == Type.SuperNode) {
        mWorkSpace.increaseWorkSpaceLevel(this);
      }
    }

    // show contect menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      mWorkSpace.showContextMenu(event, this);
    }
  }

  public void mouseReleased(MouseEvent e) {
    mPressed = false;
    mDragged = false;
    repaint(100);
  }

  //
  @Override
  public void paintComponent(final Graphics graphics) {
    super.paintComponent(graphics);

    final Graphics2D g2d = (Graphics2D) graphics;

    int nodeWidth = getWidth();
    int nodeHeight = getHeight();

    // TODO move to update
    // Compute the font metrics and the correction offsets
    final FontMetrics fontMetrics = getFontMetrics(getFont());
    final int hOffset = (fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
    final int wIdOffset = fontMetrics.stringWidth("[" + mDataNode.getId() + "]") / 2;
    //final int wNameOffset = fontMetrics.stringWidth(mDisplayName) / 2;

    // Compute the border which is relative to a nodes size.
    // It is used for visualising an end nodes and node selection
    final float borderSize = Math.max(1.0f, nodeWidth / 25.0f);
    final int borderOffset = Math.round(borderSize);
    final float[] dashPattern = {borderSize * 0.5f, borderSize * 1.25f};

    // Enable antialiasing
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Set the color to gray while pressed
    if (mPressed) {
      g2d.setColor(Color.GRAY);
    } else {
      g2d.setColor(mColor);
    }

    // Draw the node as a supernode
    if (mType == Type.SuperNode) {
      g2d.fillRect(borderOffset + 1, borderOffset + 1, nodeWidth - borderOffset * 2 - 1,
              nodeHeight - borderOffset * 2 - 1);

      if (mSelected) {
        g2d.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        g2d.setColor(sSTART_SIGN_COLOR);
        g2d.drawRect(borderOffset, borderOffset, nodeWidth - borderOffset * 2,
                nodeHeight - borderOffset * 2);
      } else if (this.mDataNode.isEndNode()) {
        g2d.setStroke(new BasicStroke(borderSize));
        g2d.setColor(mColor.darker());
        g2d.drawRect(borderOffset + 1, borderOffset + 1, nodeWidth - borderOffset * 2 - 2,
                nodeHeight - borderOffset * 2 - 2);
      }

      // Draw visualization highlights
      if (mIsActive) {
        g2d.setColor(new Color(246, 0, 0, 100));
        g2d.fillRect(1, 1, nodeWidth - 1, nodeHeight - 1);
      }

    } else if (mType == Type.BasicNode) {
      g2d.fillOval(borderOffset + 1, borderOffset + 1, nodeWidth - borderOffset * 2 - 1,
              nodeHeight - borderOffset * 2 - 1);

      if (mSelected) {
        g2d.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 2,
                dashPattern, 0));

        // TODO: warum andrs als bei supernode?
        g2d.setColor(sSTART_SIGN_COLOR);
        g2d.drawOval(borderOffset, borderOffset, nodeWidth - borderOffset * 2,
                nodeHeight - borderOffset * 2);
      } else if (this.mDataNode.isEndNode()) {
        g2d.setStroke(new BasicStroke(borderSize));
        g2d.setColor(mColor.darker());
        g2d.drawOval(borderOffset + 1, borderOffset + 1, nodeWidth - borderOffset * 2 - 2,
                nodeHeight - borderOffset * 2 - 2);
      }

      // draw activity cue
      if (mIsActive) {
        g2d.setColor(new Color(246, 0, 0, 100));
        g2d.fillOval(1, 1, nodeWidth - 1, nodeHeight - 1);
      }
    }

    // Draw the node's display name
    if (mDataNode.isHistoryNode()) {
      g2d.setColor(Color.BLACK);
    } else {
      g2d.setColor(Color.WHITE);
    }

    if (!mDisplayName.isEmpty()) {
      final String[] lines = mDisplayName.split(";");
      final int lOffset = getEditorConfig().sSHOWIDSOFNODES
              ? lines.length : (lines.length - 1);
      for (int i = 0; i < lines.length; i++) {
        g2d.drawString(lines[i],
                nodeWidth / 2 - fontMetrics.stringWidth(lines[i]) / 2, // The x position
                nodeHeight / 2 + hOffset
                - lOffset * fontMetrics.getHeight() / 2
                + i * fontMetrics.getHeight()
        );
      }

      //g2d.drawString(mDisplayName, nodeWidth / 2 - wNameOffset,
      //        (nodeHeight + 2) / 2 + hOffset);
      // Draw the node's identifier string
      if (getEditorConfig().sSHOWIDSOFNODES) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("[" + mDataNode.getId() + "]",
                nodeWidth / 2 - wIdOffset, // The x position
                nodeHeight / 2 + hOffset
                - lOffset * fontMetrics.getHeight() / 2
                + lines.length * fontMetrics.getHeight());
      }
    }
  }
}
