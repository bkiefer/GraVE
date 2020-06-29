package de.dfki.grave.editor;

import static de.dfki.grave.Preferences.*;
import static de.dfki.grave.editor.panels.WorkSpacePanel.addItem;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import de.dfki.grave.editor.action.*;
import de.dfki.grave.editor.panels.*;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.Position;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.grave.util.ChainedIterator;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public final class Node extends EditorComponent {

  // interaction flags
  public boolean mPressed = false;

  private BasicNode mDataNode;

  /** The list of outgoing edge views, representing the edges of the model */
  private Set<Edge> mOutEdges = new HashSet<>();
  private Set<Edge> mInEdges = new HashSet<>();

  // The name which will be displayed on the node
  private String mDisplayName;

  // The color of the node
  private Color mColor;

  /**
   *  Create new Node view from the (complete) node model, when reading from
   *  file, or creating a new node without edges
   */
  public Node(WorkSpace workSpace, BasicNode dataNode) {
    mWorkSpace = workSpace;
    mDataNode = dataNode;
    initCodeArea(mDataNode, null);
    // update
    update();
  }

  public boolean isBasic() {
    return mDataNode.isBasic();
  }

  public BasicNode getDataNode() {
    return mDataNode;
  }
  
  protected Point getCodeAreaLocation(Dimension r) {
    return new Point(getLocation().x + (getWidth() - r.width)/2,
        getLocation().y + getHeight());
  }

  public void setSelected() {
    mPressed = false;
    super.setSelected();
  }

  /** Resets the node to its default visual behavior */
  public void setDeselected() {
    mPressed = false;
    super.setDeselected();
  }

  /** newNode is only non-null if it's an undo */
  public void setModel(BasicNode newNode) {
    mDataNode = newNode;
    update();
  }

  @Override
  public void update(Observable o, Object obj) {
    update();
  }

  private void setColor() {
    // Update the color of the node that has to be changed
    // if the type or the flavour of the node have changed
    mColor = (isBasic()) ? sBASIC_NODE_COLOR : sSUPER_NODE_COLOR;

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

  public void update() {
    setFont(getEditorConfig().sNODE_FONT.getFont());

    //setToolTipText(mDataNode.getId()); overrides any MouseListener!!!
    // Set initial position and size
    setViewBounds(mDataNode.getPosition().getXPos(),
        mDataNode.getPosition().getYPos(),
        getEditorConfig().sNODEWIDTH,
        getEditorConfig().sNODEHEIGHT);
    
    FontMetrics fontMetrics = getFontMetrics(getFont());
    
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
      // TODO: should this be reactivated?
      mDisplayName = mDataNode.getName();//prefix + "...";
    } else {
      mDisplayName = mDataNode.getName();
    }

    // Set the flavour dependent color
    setColor();
    super.update();
  }

  @Override
  public String getName() {
    return mDisplayName;
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

  /** Return an Iterable over all the edges */
  public Iterable<Edge> getConnectedEdges() {
    return new Iterable<Edge>() {
      @Override
      public Iterator<Edge> iterator() {
        return new ChainedIterator<Edge>(
            mOutEdges.iterator(), mInEdges.iterator());
      };
    };
  }
  
  /** Return an Iterable over all outgoing edges */
  public Iterable<Edge> getOutgoingEdges() {
    return mOutEdges;
  }

  public boolean isEdgeAllowed(AbstractEdge e) {
    return mDataNode.canAddEdge(e);
  }

  // **********************************************************************
  // Dock point functions
  // **********************************************************************

  /** Return the ID of a free dock point closest to p, which is in (zoomed)
   *  view coordinates. In addition, we have to account for the translation
   *  of the node center in the view.
   */
  public int getNearestFreeDock(Point p, boolean source) {
    p.translate(- getWidth()/2, - getHeight()/2);
    return mDataNode.getNearestFreeDock(toModelPos(p), source);
  }

  /** Return the dock point for the given ID, in (zoomed)
   *  workspace coordinates
   */
  public Point getDockPoint(int which) {
    // dp contains the zoom factor, it's in getWidth()
    Point2D dp = mDataNode.getDockPoint(which, getWidth());
    Point center = getCenterPoint();
    center.translate((int)dp.getX(), (int)dp.getY());
    return center;
  }

  // **********************************************************************
  // Other location functions
  // **********************************************************************

  /** Returns the center of a node, in (zoomed) workspace coordinates.
   */
  public Point getCenterPoint() {
    Point center = toViewPoint(mDataNode.getPosition());
    center.translate(getWidth() / 2, getHeight()/2);
    return center;
  }

  /** Given x, y in workspace coordinates, is the point contained in this
   *  node?
   */
  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  /** Move view _and_ model to new location, including CommandBadge
   *  @param newLocation a Point in *MODEL* coordinates.
   */
  public void moveTo(Position newLocation) {
    mDataNode.setPosition(newLocation);
    for (Edge edge : getConnectedEdges()) {
      edge.update();
    }
    setViewLocation(newLocation.getXPos(), newLocation.getYPos());
    CodeArea badge = getCodeArea();
    if (badge != null) {
      badge.setLocation();
    }
  }

  // **********************************************************************
  // Mouse, Menu, Paint functions
  // **********************************************************************

  /**
   * TODO: ADD "CREATE XEDGE" FOR ALL LEGAL EDGES STARTING AT THIS NODE
   */
  public void showContextMenu() {
    JPopupMenu pop = new JPopupMenu();

    if (! isBasic()) {
      SuperNode n = (SuperNode)getDataNode();
      if (n.getNodeSize() == 0) {
        addItem(pop, "To BasicNode", new ChangeNodeTypeAction(getEditor(), mDataNode));
        pop.add(new JSeparator());
      }
    } else {
      // Only BasicNodes can become start nodes
      if (! mDataNode.isStartNode()) {
        addItem(pop, "Set Start",
            new ToggleStartNodeAction(getEditor(), this.getDataNode()));
      }
      addItem(pop, "To Supernode", new ChangeNodeTypeAction(getEditor(), mDataNode));
    }
    addItem(pop, "Edit Code", (e) -> activateCodeArea());
    pop.add(new JSeparator());
    addItem(pop, "Copy", new CopyNodesAction(getEditor(), mDataNode));
    if (! mDataNode.isStartNode()) {
      addItem(pop, "Cut", new RemoveNodesAction(getEditor(), mDataNode, true));
      pop.add(new JSeparator());
      addItem(pop, "Delete", new RemoveNodesAction(getEditor(), mDataNode, false));
    }
    pop.show(this, getWidth(), 0);
  }

  public void mouseClicked(MouseEvent event) {
    // enter supernode, if it has been double clicked
    if (!isBasic()
        && event.getButton() == MouseEvent.BUTTON1
        && event.getClickCount() == 2) {
      getEditor().increaseWorkSpaceLevel(this);
    }
  }

  public void mousePressed(MouseEvent event) {
    mPressed = true;
  }

  public void mouseReleased(MouseEvent e) {
    mPressed = false;
    repaint(100);
  }

  //
  @Override
  public void paintComponent(final Graphics graphics) {
    super.paintComponent(graphics);

    final Graphics2D g2d = (Graphics2D) graphics;

    int nodeWidth = getWidth();
    int nodeHeight = getHeight();

    // Compute the font metrics and the correction offsets
    final FontMetrics fontMetrics = getFontMetrics(getFont());
    final int hOffset = (fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
    final int wIdOffset = fontMetrics.stringWidth("[" + mDataNode.getId() + "]") / 2;

    // Compute the border which is relative to a nodes size.
    // It is used for visualising an end nodes and node selection
    final float borderSize = Math.max(1.0f, nodeWidth / 25.0f);
    final int borderOffset = Math.round(borderSize);
    final float[] dashPattern = {borderSize * 0.5f, borderSize * 1.25f};

    // Enable antialiasing
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Set the color to gray while pressed
    g2d.setColor(mPressed ? Color.GRAY : mColor);

    int nw = nodeWidth - borderOffset * 2;
    int nh = nodeHeight - borderOffset * 2;
    int lu = borderOffset + 1; // left or upper
    // Draw the node as a supernode
    if (!isBasic()) {
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
