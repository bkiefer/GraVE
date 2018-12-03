package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.dfki.vsm.editor.action.*;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.editor.util.DockingManager;
import de.dfki.vsm.editor.util.IDManager;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.Pair;
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
  private boolean isBasic;
  private BasicNode mDataNode;

  private CmdBadge mCmdBadge;

  //
  // TODO: move away
  private final WorkSpace mWorkSpace;

  // The name which will be displayed on the node
  private String mDisplayName;

  // The color of the node
  // TODO: eventually move computation of color to paint component
  private Color mColor;

  private boolean mIsActive;

  private EditorConfig getEditorConfig() { return mWorkSpace.getEditorConfig(); }

  /** This copies some subset of node and edge views and their underlying
   *  models. One basic assumption is that there are no "dangling" edges which
   *  either start or end at a node outside the given node set.
   *
   *  The copied views will be added to the given WorkSpace, and all copied
   *  node models will be subnodes of the given SuperNode.
   */
  public static Pair<Collection<Node>, List<Edge>> copyGraph(WorkSpace workSpace,
      SuperNode newParent, List<Node> nodeViews, List<Edge> edgeViews) {
    IDManager mgr = workSpace.getSceneFlowEditor().getIDManager();

    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    Map<Node, Node> origView2copy = new IdentityHashMap<>();
    for (Node nodeView : nodeViews) {
      BasicNode n = nodeView.getDataNode();
      BasicNode cpy = n.deepCopy(mgr, newParent);
      orig2copy.put(n, cpy);
      // now create a new Node as view for the copy of n
      Node newNode = new Node(workSpace, cpy);
      origView2copy.put(nodeView, newNode);
    }

    List<Edge> newEdges = new ArrayList<>();
    for (Edge edgeView : edgeViews) {
      AbstractEdge e = edgeView.getDataEdge().deepCopy(orig2copy);
      // now create a new Edge as view for the copy of e
      // TODO: TRANSLATE TO CURRENT MOUSE POSITION
      Edge newEdge = new Edge(workSpace, e,
          origView2copy.get(edgeView.getSourceNode()),
          origView2copy.get(edgeView.getTargetNode()));
      newEdges.add(newEdge);
    }
    return new Pair<Collection<Node>, List<Edge>>(origView2copy.values(), newEdges);
  }

  /** This copies some subset of node and edge views and their underlying
   *  models. One basic assumption is that there are no "dangling" edges which
   *  either start or end at a node outside the given node set.
   *
   *  The copied views will be added to the given WorkSpace, and all copied
   *  node models will be subnodes of the given SuperNode.
   */
  public static Collection<BasicNode> copyGraphModel(IDManager mgr,
      SuperNode newParent, List<Node> nodeViews, List<Edge> edgeViews) {


    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    for (Node nodeView : nodeViews) {
      BasicNode n = nodeView.getDataNode();
      orig2copy.put(n, n.deepCopy(mgr, newParent));
    }

    for (Edge edgeView : edgeViews) {
      // connects the new edges in the copied model nodes
      edgeView.getDataEdge().deepCopy(orig2copy);
    }
    return orig2copy.values();
  }
  public enum Type {
    BasicNode, SuperNode
  }

  /**
   *  Create new Node view from the node model
   */
  public Node(WorkSpace workSpace, BasicNode dataNode) {
    mWorkSpace = workSpace;
    mDataNode = dataNode;
    // setToolTipText(mDataNode.getId());
    // the former overrides any MouseListener!!!

    isBasic = !(mDataNode instanceof SuperNode);

    // Init docking manager
    mDockingManager = new DockingManager(this);

    // Set initial position
    Point pos = new Point(mDataNode.getPosition().getXPos(),
            mDataNode.getPosition().getYPos());

    setBounds(pos.x, pos.y, getEditorConfig().sNODEWIDTH, getEditorConfig().sNODEHEIGHT);

    if (mDataNode.getParentNode().isStartNode(mDataNode)) {
      addStartSign();
    }

    // Create the command badge of the GUI-BasicNode, after setting Position!
    mCmdBadge = new CmdBadge(this, mWorkSpace.getEditorConfig());

    // update
    update();
  }

  public Type getType() {
    return isBasic ? Type.BasicNode : Type.SuperNode;
  }

  public WorkSpace getWorkSpace() {
    return mWorkSpace;
  }

  public BasicNode getDataNode() {
    return mDataNode;
  }

  public CmdBadge getCmdBadge() {
    return mCmdBadge;
  }

  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  public DockingManager getDockingManager() {
    return mDockingManager;
  }

  public boolean changeType(IDManager mgr, Collection<Edge> incoming) {
    BasicNode newNode;
    if (! isBasic) {
      SuperNode n = (SuperNode)getDataNode();
      if (n.getNodeSize() > 0) {
        // complain: this operation can not be done, SuperNode has subnodes
        return false;
      }
      newNode = new BasicNode(mgr, n);
    } else {
      newNode = new SuperNode(mgr, getDataNode());
    }
    for (Edge in : incoming) {
      AbstractEdge e = in.getDataEdge();
      e.setTargetNode(newNode);
      e.setTargetUnid(newNode.getId());
    }
    mDataNode = newNode;
    update();
    return true;
  }


  @Override
  public void update(Observable o, Object obj) {
    update();
  }

  public void setText(String text) {
    // this automatically sets the text in DataNode, too...
    mCmdBadge.setText(text);
  }

  private void setColor() {
    // Update the color of the node that has to be changed
    // if the type or the flavour of the node have changed
    mColor = (isBasic) ? sBASIC_NODE_COLOR : sSUPER_NODE_COLOR;

    // Set the flavour dependent color
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
    // node's size or the node's font size have changed
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
    Position g = new Position(newLocation.x, newLocation.y);
    mDataNode.setPosition(g);
    Point location = getLocation();

    for (Edge edge : mDockingManager.getConnectedEdges()) {
      edge.updateRelativeEdgeControlPointPos(this, newLocation.x - location.x, newLocation.y - location.y);
    }
    setLocation(newLocation);
    CmdBadge badge = getCmdBadge();
    if (badge != null) {
      badge.setLocation();
    }
  }

  public void translate(Point vector) {
    Point location = getLocation();
    location.translate(vector.x, vector.y);
    // also translate all edge points
    resetLocation(location);
  }

  // TODO: move to workspace
  public void removeStartSign() {
    if (mStartSign != null) {
      mDockingManager.releaseDockPointForStartSign();
      mWorkSpace.remove(mStartSign);
      mStartSign = null;
    }
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

  /** Tells the node that an edge connects and that node is sourcenode */
  public Point connectAsSource(Edge edge, Point point) {
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

  /** Tells the node that an edge connects */
  public Point connectAsTarget(Edge e, Point p) {
    // get location of node
    Point loc = getLocation();
    // get relative (to the current node) coordinates;
    p.setLocation(p.x - loc.x, p.y - loc.y);
    Point dp = (e.getSourceNode() != this)
        ? mDockingManager.getNearestDockPoint(e, p)
        : mDockingManager.getNearestSecondDockPoint(e, p);
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

  /**
   * Returns the center of a node. The location is in the top left corner.
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

    /**
   *
   * TODO: ADD "CREATE XEDGE" FOR ALL LEGAL EDGES STARTING AT THIS NODE
   */
  public void showContextMenu(MouseEvent evt, Node node) {
    JPopupMenu pop = new JPopupMenu();
    JMenuItem item = null;
    SuperNode current = mDataNode.getParentNode();
    EditorAction action;

    item = new JMenuItem(
        current.isStartNode(node.getDataNode()) ? "Unset Start" : "Set Start");
    action = new ToggleStartNodeAction(mWorkSpace, node);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    pop.add(new JSeparator());

    if (!(node.getDataNode() instanceof SuperNode)) {
      item = new JMenuItem("To Supernode");
      action = new ChangeNodeTypeAction(mWorkSpace, node);
      item.addActionListener(action.getActionListener());
      pop.add(item);

      pop.add(new JSeparator());
    }

    // TODO: MAYBE INVERT: IF NO CMD, ADD ONE
    if (node.getDataNode().getCmd() != null) {
      item = new JMenuItem("Edit Command");
      action = new EditCommandAction(mWorkSpace, node.getCmdBadge());
      item.addActionListener(action.getActionListener());
      pop.add(item);

      pop.add(new JSeparator());
    }

    item = new JMenuItem("Copy");
    action = new CopyNodesAction(mWorkSpace, node);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    item = new JMenuItem("Cut");
    action = new RemoveNodesAction(mWorkSpace, node, true);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    pop.add(new JSeparator());

    item = new JMenuItem("Delete");
    action = new RemoveNodesAction(mWorkSpace, node, false);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    pop.show(this, node.getX() + node.getWidth(), node.getY());
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

    // enter supernode, if it has been double clicked
    // TODO: move to workspace
    if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
      if (! isBasic) {
        mWorkSpace.increaseWorkSpaceLevel(this);
      }
    }

    // show contect menu
    // TODO: move to workspace
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      showContextMenu(event, this);
    }

    // !!!!!!!!!!!!!!!!!!!!
    // System.err.println("Sending node selected event");
    mEventMulticaster.convey(new ElementSelectedEvent(this));
  }

  public void mousePressed(MouseEvent event) {
    mouseClicked(event);
    /*
    mPressed = true;
    mSelected = true;

    //Point loc = getLocation();
    //Point clickLoc = event.getPoint();

    // mLastMousePosition =                new Point(clickLoc);
    // save click location relavitvely to node postion
    // mClickPosition.setLocation(clickLoc.x - loc.x, clickLoc.y - loc.y);
    repaint(100);


    // show contect menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      showContextMenu(event, this);
    }*/
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
    if (!isBasic) {
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

    } else {
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
    g2d.setColor(Color.WHITE);

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
