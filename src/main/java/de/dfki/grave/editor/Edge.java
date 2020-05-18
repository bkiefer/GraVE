package de.dfki.grave.editor;

import static de.dfki.grave.Preferences.*;
import static de.dfki.grave.editor.panels.WorkSpacePanel.addItem;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.swing.JPopupMenu;

import de.dfki.grave.editor.action.MoveEdgeCtrlAction;
import de.dfki.grave.editor.action.MoveEdgeEndPointAction;
import de.dfki.grave.editor.action.NormalizeEdgeAction;
import de.dfki.grave.editor.action.RemoveEdgeAction;
import de.dfki.grave.editor.action.ShortestEdgeAction;
import de.dfki.grave.editor.action.StraightenEdgeAction;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.*;


/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Edge extends EditorComponent {
  /** MIN POSITION OF THE CONTROLPOINTS OF THE EDGE */

  // Reference to data model edges and nodes
  private AbstractEdge mDataEdge = null;

  // The two graphical nodes to which this edge is connected
  private Node mSourceNode = null;
  private Node mTargetNode = null;
  private EdgeArrow mArrow = null;

  //
  // other stuff
  private static class Props {
    public Props(String n, String d, Color c) {
      mName = n; mDescription = d; mColor = c;
    }
    String mName;
    @SuppressWarnings("unused")
    String mDescription;
    Color mColor;
  }

  private static final Map<Class<? extends AbstractEdge>, Props> edgeProperties =
      new HashMap<Class<? extends AbstractEdge>, Props>() {{
        put(EpsilonEdge.class,
            new Props("Epsilon", "Unconditioned edge", sEEDGE_COLOR));
        put(ForkingEdge.class,
            new Props("Fork", "Fork edge", sFEDGE_COLOR));
        put(TimeoutEdge.class,
            new Props("Timeout", "Edge with a time condition", sTEDGE_COLOR));
        put(GuardedEdge.class,
            new Props("Conditional", "Edge with a logical condition", sCEDGE_COLOR));
        put(RandomEdge.class,
            new Props("Probability", "Edge with a probabilistic condition", sPEDGE_COLOR));
        put(InterruptEdge.class,
            new Props("Interruptive", "Edge with a logical condition that interrupts supernodes", sIEDGE_COLOR));
      }};

  private Color color() {
    if (mDataEdge == null) return null;
    return edgeProperties.get(mDataEdge.getClass()).mColor;
  }

  private String name() {
    if (mDataEdge == null) return null;
    return edgeProperties.get(mDataEdge.getClass()).mName;
  }

  /** This constructor assumes that the edge model has a complete edge model,
   *  with reasonable values for the docks and control points, which is connected
   *  to the correct node models.
   *
   *  So either they come from file input, or they have to be set properly when
   *  a new edge is created.
   */
  public Edge(WorkSpace ws, AbstractEdge edge, Node sourceNode, Node targetNode) {
    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    mDataEdge = edge;
    mWorkSpace = ws;
    mSourceNode = sourceNode;
    mTargetNode = targetNode;

    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    mArrow = new EdgeArrow();
    update();
    initCodeArea(mDataEdge.getContent() != null ? mDataEdge : null, color());
    setDeselected();
    setVisible(true);
  }

  @Override
  public void update(Observable o, Object obj) {
    update();
  }

  public AbstractEdge getDataEdge() {
    return mDataEdge;
  }

  @Override
  public String getName() {
    return name() + "(" + mSourceNode.getDataNode().getId() + "->" + mTargetNode.getDataNode().getId() + ")";
  }

  public String getDescription() {
    return mDataEdge != null ? mDataEdge.getContent() : null;
  }

  Point getCodeAreaLocation(Dimension r) {
    Point2D p = getCurveCenter();
    // center around middle of curve
    int x = (int) Math.round(p.getX() - r.width / 2.0);
    int y = (int) Math.round(p.getY() - r.height / 2.0);
    return new Point(x, y);
  }
  
  // **********************************************************************
  // All methods use view coordinates
  // **********************************************************************

  /** Absolute position of edge start (view coordinates) */
  private Point getStart() {
    return mSourceNode.getDockPoint(mDataEdge.getSourceDock());
  }

  /** Absolute position of edge end (view coordinates)*/
  private Point getEnd() {
    return mTargetNode.getDockPoint(mDataEdge.getTargetDock());
  }

  /** Absolute position of edge start control point (view coordinates) */
  private Point getStartCtrl() {
    return Geom.add(getStart(), mWorkSpace.toViewPoint(mDataEdge.getSourceCtrlPoint()));
  }

  /** Absolute position of edge end control point (view coordinates) */
  private Point getEndCtrl() {
    return Geom.add(getEnd(), mWorkSpace.toViewPoint(mDataEdge.getTargetCtrlPoint()));
  }

  /** is p (in view coordinates) on the curve or the code area of this edge? */
  public boolean containsPoint(Point p) {
    if (mArrow.curveContainsPoint(p)) return true;
    // also look at the text box, if any.
    CodeArea c = getCodeArea();
    if (c != null) {
      return c.getBounds().contains(p);
    }
    return false;
  }

  /** is p (in view coordinates) on the curve, the code area, or the control points
   *  of this edge? */
  public boolean containsPointSelected(Point p) {
    return containsPoint(p) || mArrow.controlContainsPoint(p);
  }
  
  public boolean outOfBounds() {
    Point ctrl1 = getStartCtrl();
    Point ctrl2 = getEndCtrl();
    // return ctrl1.x < 0 || ctrl1.y < 0 || ctrl2.x < 0 || ctrl2.y < 0;
    // what do we consider as out of bounds?
    // If the control point is covered by the node
    return mSourceNode.contains(ctrl1) || mTargetNode.contains(ctrl2);
  }

  public boolean isIntersectByRectangle(double x1, double x2, double y1, double y2) {
    return mArrow.isIntersectByRectangle(x1, x2, y1, y2);
  }

  public void updateEdgeGraphics() {
    mArrow.computeCurve(getStart(), getStartCtrl(), getEndCtrl(), getEnd());
    computeBounds();
  }

  private void update() {
    // Adapt font size
    if (getEditorConfig().sWORKSPACEFONTSIZE != getFont().getSize())
      getFont().deriveFont(getEditorConfig().sWORKSPACEFONTSIZE);

    updateEdgeGraphics();
  }

  /*
   * Take input value of mValueEditor and set it as value of the edge
   * EDGE MODIFICATION  
   *
  void updateFromTextEditor() {
    if (mCodeArea == null) return;
    String input = mCodeArea.getText();
    if (mDataEdge != null) {
      try {
        mDataEdge.setContent(input);
      }
      catch (NumberFormatException ex) {
        mWorkSpace.getSceneFlowEditor().setMessageLabelText(
            "Not a number: " + input);
      }
      catch (Exception ex) {
        mWorkSpace.getSceneFlowEditor().setMessageLabelText(
            "Something wrong here: " + input);
      }
    }
    EditorInstance.getInstance().refresh();
  }*/

  public void setSelected() {
    mArrow.showControlPoints();
    super.setSelected();
  }
  
  public void setDeselected() {
    mArrow.deselectMCs();
    super.setDeselected();
  }

  public boolean isInEditMode() {
    return mSelected;
  }
  
  /**
   */
  private void showContextMenu(MouseEvent evt, Edge edge) {
    JPopupMenu pop = new JPopupMenu();
    AbstractEdge model = edge.getDataEdge();
    //addItem(pop, "Modify", new ModifyEdgeAction(edge, this));
    addItem(pop, "Delete", new RemoveEdgeAction(mWorkSpace, model));
    addItem(pop, "Shortest Path", new ShortestEdgeAction(mWorkSpace, model));
    addItem(pop, "Straighten", new StraightenEdgeAction(mWorkSpace, model));
    addItem(pop, "Smart Path", new NormalizeEdgeAction(mWorkSpace, model));
    pop.show(mWorkSpace, evt.getX(), evt.getY());
  }

  /** Return true if one of the end or control points is selected */
  public boolean controlPointSelected() {
    return mArrow.mSelected >= EdgeArrow.S && mArrow.mSelected <= EdgeArrow.E;
  }

  @Override
  public void mouseClicked(MouseEvent event) {
    // show context menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      showContextMenu(event, this);
    }
    repaint(100);
  }

  /**
   * Handles the mouse pressed event
   */
  @Override
  public void mousePressed(MouseEvent e) {
    if (mSelected) {
      mArrow.edgeSelected(e.getPoint());
      // revalidate();
      repaint(100);
    }
  }

  private void deflectSource(Node newNode) {
    // change the SOURCE view and model to reflect edge change
    // disconnect view
    mSourceNode.disconnectSource(this);
    // new source view
    mSourceNode = newNode;
    // new source view
    mSourceNode.connectSource(this);
  }

  private void deflectTarget(Node newNode) {
    // change the TARGET view and model to reflect edge change
    // disconnect view
    mTargetNode.disconnectTarget(this);
    // new target view
    mTargetNode = newNode;
    // new target view
    mTargetNode.connectTarget(this);
  }

  /** Change this edge in some way
   *  EDGE MODIFICATION
   */
  public void modifyEdge(Node newStart, Node newEnd,
      BasicNode[] nodes, int[] docks, Position[] controls) {
    getDataEdge().modifyEdge(nodes, docks, controls);
    deflectSource(newStart);
    deflectTarget(newEnd);
  }

  /** Disconnect this edge view from the node view it is connected to
   * EDGE MODIFICATION
   */
  public void disconnect() {
    mSourceNode.disconnectSource(this);
    mTargetNode.disconnectTarget(this);
  }

  /** Connect this edge view to the node view it should be connected to
   * EDGE MODIFICATION
   */
  public void connect() {
    mSourceNode.connectSource(this);
    mTargetNode.connectTarget(this);
  }

  /** Make edge as straight as possible
   *  EDGE MODIFICATION
   */
  public void straightenEdge() {
    mDataEdge.straightenEdge(mSourceNode.getWidth());
    updateEdgeGraphics();
  }

  /** Try to give this edge a better shape (TODO: define! implement!)
   *  EDGE MODIFICATION
   */
  public void rebuildEdgeNicely() {
    // disconnectEdge
    straightenEdge();
  }

  private boolean canDeflect(Node curr, Node old) {
    return (curr != null &&
        (curr == old || curr.getDataNode().canAddEdge(mDataEdge)));
  }

  /** This takes all the responsibility for edge changes using the mouse, except
   *  creation of a new edge:
   *  - Assigning a new dock to the start point
   *  - Modification of a control point
   *  - Assigning a new dock, possibly at a new target node, to the end point
   *
   *  TODO: Here, any change that has happened during drag should be registered in an
   *  undoable way
   */
  @Override
  public void mouseReleased(java.awt.event.MouseEvent e) {
    Point p = e.getPoint();
    switch (mArrow.mSelected) {
    case EdgeArrow.S: {
      Node newNode = mWorkSpace.findNodeAtPoint(p);
      if (canDeflect(newNode, mSourceNode)) {
        int dock = newNode.getNearestFreeDock(p);
        new MoveEdgeEndPointAction(mWorkSpace, getDataEdge(), true, dock,
            newNode.getDataNode()).run();
      } else {
        updateEdgeGraphics(); // put arrow back into old position
      }
      break;
    }
    case EdgeArrow.E: {
      Node newNode = mWorkSpace.findNodeAtPoint(p);
      if (canDeflect(newNode, mTargetNode)) {
        int dock = newNode.getNearestFreeDock(p);
        new MoveEdgeEndPointAction(mWorkSpace, getDataEdge(), false, dock,
            newNode.getDataNode()).run();
      } else {
        updateEdgeGraphics(); // put arrow back into old position
      }
      break;
    }
    case EdgeArrow.C1: {
      // compute vector from dock point to p (relative ctrls)
      Point dock = mSourceNode.getDockPoint(mDataEdge.getSourceDock());
      p.translate(-dock.x, -dock.y);
      // All actions use Positions (model coordinates)
      new MoveEdgeCtrlAction(mWorkSpace, getDataEdge(), true, toModelPos(p)).run();
      break;
    }
    case EdgeArrow.C2: {
      Point dock = mTargetNode.getDockPoint(mDataEdge.getTargetDock());
      p.translate(-dock.x, -dock.y);
      // All actions use Positions (model coordinates)
      new MoveEdgeCtrlAction(mWorkSpace, getDataEdge(), false, toModelPos(p)).run();
      break;
    }
    }
  }

  public void mouseDragged(java.awt.event.MouseEvent e) {
    Point p = e.getPoint();
    // do not allow x and y values below 10
    if (p.x - 10 < 0)
      p.x = 10;
    if (p.y - 10 < 0)
      p.y = 10;

    mArrow.mouseDragged(this, p);
    computeBounds();
    repaint(100);
  }

  Point2D getCurveCenter() {
    return new Point2D.Double(mArrow.mLeftCurve.x2, mArrow.mLeftCurve.y2);
  }
  
  private Rectangle computeTextBoxBounds() {
    CodeArea c = getCodeArea();
    if (c == null) return null;
    c.setDeselected();
    Dimension r = c.getSize();
    
    // center around middle of curve
    int x = (int) Math.round(mArrow.mLeftCurve.x2 - r.width/2);
    int y = (int) Math.round(mArrow.mLeftCurve.y2 - r.height/2);
    c.setLocation(x, y);
    return c.getBounds();
  }

  private void computeBounds() {
    // set bounds of edge
    Rectangle bounds = mArrow.computeBounds();
    // add the size of the text box
    Rectangle textBox = computeTextBoxBounds();
    if (textBox != null)
      bounds.add(textBox);
    // set the components bounds (everything in view coordinates already)
    setBounds(bounds);
  }

  public void paintComponent(Graphics g) {
    float lineWidth = mSourceNode.getWidth() / 30.0f;
    Graphics2D graphics = (Graphics2D) g;

    graphics.setColor(color());
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER));

    // NOTE: This is the magic command that makes all drawing relative
    // to the position of this component, and removes the "absolute" position
    // part
    graphics.translate(-getLocation().x, -getLocation().y);
    // draw the arrow
    mArrow.paintArrow(graphics, lineWidth, color());
  }
}
