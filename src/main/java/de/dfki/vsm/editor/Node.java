package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;
import static de.dfki.vsm.editor.project.WorkSpacePanel.addItem;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import de.dfki.vsm.editor.action.*;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.editor.project.WorkSpacePanel;
import de.dfki.vsm.editor.util.IDManager;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.ChainedIterator;
import de.dfki.vsm.util.Pair;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public final class Node extends EditorComponent {

  // interaction flags
  private boolean mSelected = false;
  public boolean mPressed = false;
  public boolean mDragged = false;

  private final EventDispatcher mEventMulticaster = EventDispatcher.getInstance();
  private boolean isBasic;
  private BasicNode mDataNode;

  /** The list of outgoing edge views, representing the edges of the model */
  private Set<Edge> mOutEdges = new HashSet<>();
  private Set<Edge> mInEdges = new HashSet<>();

  private CmdBadge mCmdBadge;
  private Document nodeCodeDocument;

  //
  // TODO: move away
  private final WorkSpace mWorkSpace;

  // The name which will be displayed on the node
  private String mDisplayName;

  // The color of the node
  // TODO: eventually move computation of color to paint component
  private Color mColor;

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

  /**
   *  Create new Node view from the (complete) node model, when reading from
   *  file, or creating a new node without edges
   */
  public Node(WorkSpace workSpace, BasicNode dataNode) {
    mWorkSpace = workSpace;
    mDataNode = dataNode;
    //setToolTipText(mDataNode.getId());
    // the former overrides any MouseListener!!!

    isBasic = !(mDataNode instanceof SuperNode);

    // Set initial position
    Point pos = new Point(mDataNode.getPosition().getXPos(),
            mDataNode.getPosition().getYPos());

    setBounds(pos.x, pos.y, getEditorConfig().sNODEWIDTH, getEditorConfig().sNODEHEIGHT);

    nodeCodeDocument = new ObserverDocument();
    try {
      nodeCodeDocument.insertString(0, mDataNode.getCmd(), null);
    } catch (BadLocationException ex) {
      Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
    }
    // Create the command badge of the GUI-BasicNode, after setting Position!
    mCmdBadge = new CmdBadge(this, mWorkSpace.getEditorConfig(), nodeCodeDocument);

    // update
    update();
  }

  public boolean isBasic() {
    return isBasic;
  }

  public Document getCodeDocument() {
    return nodeCodeDocument;
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

  public void setSelected() {
    mPressed = false;
    mSelected = true;
    repaint(100);
    mEventMulticaster.convey(new ElementSelectedEvent(this));
  }

  /** Resets the node to its default visual behavior */
  public void setDeselected() {
    mSelected = false;
    mPressed = false;
    mDragged = false;
    repaint(100);
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
      e.setTarget(newNode, e.getTargetDock());
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
    for (Edge edge : getConnectedEdges()) {
      edge.updateEdgeGraphics();
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

  /** Only necessary since we have no model->view map, and the model only has
   *  outgoing edges.
   */
  public void connectSource(Edge edge) {
    mOutEdges.add(edge);
  }
  /** Only necessary since we have no model->view map, and the model only has
   *  outgoing edges.
   */
  public void connectTarget(Edge edge) {
    mInEdges.add(edge);
  }

  /** Only necessary since we have no model->view map, and the model only has
   *  outgoing edges.
   */
  public void disconnectSource(Edge e) {
    mOutEdges.remove(e);
  }

  /** Only necessary since we have no model->view map, and the model only has
   *  outgoing edges.
   */
  public void disconnectTarget(Edge e) {
    mInEdges.remove(e);
  }


  /**
   * Returns the center of a node. The location is in the top left corner.
   */
  public Point getCenterPoint() {
    return mDataNode.getCenter();
  }

  public Iterable<Edge> getConnectedEdges() {
    return new Iterable<Edge>() {
      @Override
      public Iterator<Edge> iterator() {
        return new ChainedIterator<Edge>(
            mOutEdges.iterator(), mInEdges.iterator());
      };
    };
  }

  // **********************************************************************
  // Dock point functions
  // **********************************************************************

  public int getNearestFreeDock(Point p) {
    return mDataNode.getNearestFreeDock(p);
  }

  public Point getDockPoint(int which) {
    return mDataNode.getDockPoint(which, getWidth());
  }

  /*
   *

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
   */

  public boolean isEdgeAllowed(AbstractEdge e) {
    return mDataNode.canAddEdge(e);
  }

  /**
   *
   * TODO: ADD "CREATE XEDGE" FOR ALL LEGAL EDGES STARTING AT THIS NODE
   */
  public void showContextMenu(WorkSpacePanel mWorkSpace) {
    JPopupMenu pop = new JPopupMenu();
    SuperNode curr = mDataNode.getParentNode();

    addItem(pop, curr.isStartNode(getDataNode()) ? "Unset Start" : "Set Start",
        new ToggleStartNodeAction(mWorkSpace, this));
    pop.add(new JSeparator());

    if (!(getDataNode() instanceof SuperNode)) {
      addItem(pop, "To Supernode", new ChangeNodeTypeAction(mWorkSpace, this));
      pop.add(new JSeparator());
    }

    // TODO: MAYBE INVERT: IF NO CMD, ADD ONE
    if (getDataNode().getCmd() != null) {
      addItem(pop, "Edit Command", new EditCommandAction(mWorkSpace, getCmdBadge()));
      pop.add(new JSeparator());
    }

    addItem(pop, "Copy", new CopyNodesAction(mWorkSpace, this));
    addItem(pop, "Cut", new RemoveNodesAction(mWorkSpace, this, true));
    pop.add(new JSeparator());
    addItem(pop, "Delete", new RemoveNodesAction(mWorkSpace, this, false));
    pop.show(this, getWidth(), 0);
  }

  public void mouseClicked(MouseEvent event) {
    setSelected();
  }

  public void mousePressed(MouseEvent event) {
    mPressed = true;
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

    int nw = nodeWidth - borderOffset * 2;
    int nh = nodeHeight - borderOffset * 2;
    int lu = borderOffset + 1; // left or upper
    // Draw the node as a supernode
    if (!isBasic) {
      if (mDataNode.isStartNode()) {
        int rght = nw + 4; // right
        int low = nh + 4;  // lower
        int gap = (int)(low / 4.5) / 2; // we need /2 for the computation
        // points: center, upper gap pt, nw, ne, se, sw, lower gap pt, center
        int [] xp = {
            rght / 2, lu,            lu, rght, rght, lu, lu,             rght / 2
        };
        int [] yp = {
            low / 2,  low / 2 - gap, lu, lu,   low,  low, low / 2 + gap, low / 2
        };
        g2d.fillPolygon(xp, yp, xp.length);
        g2d.setColor(sSTART_SIGN_COLOR);
        // points: center, upper gap pt, lower gap pt, center
        int [] sxp = { rght / 2, lu,                lu,                rght / 2 };
        int [] syp = { low / 2,  low / 2 - gap - 1, low / 2 + gap + 1, low / 2 };
        g2d.fillPolygon(sxp, syp, sxp.length);
      } else {
        g2d.fillRect(borderOffset + 1, borderOffset + 1, nw - 1, nh - 1);
      }

      if (mSelected) {
        g2d.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER, 10, dashPattern, 0));
        g2d.setColor(sSTART_SIGN_COLOR);
        g2d.drawRect(borderOffset, borderOffset, nw, nh);
      } else if (mDataNode.isEndNode()) {
        g2d.setStroke(new BasicStroke(borderSize));
        g2d.setColor(mColor.darker());
        g2d.drawRect(lu, lu, nw - 2, nh - 2);
      }
    } else {
      if (mDataNode.isStartNode()) {
        int angle = 28;
        g2d.fillArc(lu, lu, nw - 1, nh - 1, - 180 + angle/2, 360 - angle);
        g2d.setColor(sSTART_SIGN_COLOR);
        g2d.fillArc(lu, lu, nw - 1, nh - 1, - 180 + angle/2, - angle);
      } else {
        g2d.fillOval(lu, lu, nw - 1, nh - 1);
      }

      if (mSelected) {
        g2d.setStroke(new BasicStroke(borderSize, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_MITER, 2, dashPattern, 0));
        g2d.setColor(sSTART_SIGN_COLOR);
        g2d.drawOval(borderOffset, borderOffset, nw, nh);
      } else if (mDataNode.isEndNode()) {
        g2d.setStroke(new BasicStroke(borderSize));
        g2d.setColor(mColor.darker());
        g2d.drawOval(lu, lu, nw - 2, nh - 2);
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
