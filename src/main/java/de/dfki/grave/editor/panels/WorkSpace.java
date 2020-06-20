package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.sCEDGE_COLOR;
import static de.dfki.grave.Preferences.sFEDGE_COLOR;
import static de.dfki.grave.Preferences.sIEDGE_COLOR;
import static de.dfki.grave.Preferences.sPEDGE_COLOR;
import static de.dfki.grave.Preferences.sTEDGE_COLOR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.CodeArea;
import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.ProjectElement;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.model.flow.*;
import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.util.evt.EventDispatcher;

/**
 *
 * This is the View of the currently edited SuperNode, containing views for all
 * the SuperNode's contained nodes and the edges between them, and their
 * corresponding text badges, as well as comments.
 *
 * This class is meant to be used *only* with WorkSpacePanel, which provides
 * the "interactive" functionality: Mouse Movements/Menus/Dragging/Keyboard
 * while this class provides the "functionality" on the View elements:
 * Adding / Removing / Moving
 */
@SuppressWarnings("serial")
public abstract class WorkSpace extends JPanel implements ProjectElement {
  private static final Logger logger = LoggerFactory.getLogger(WorkSpace.class);

  // Elements to draw
  protected final Map<BasicNode, Node> mNodeSet = new IdentityHashMap<>();
  private final Map<CommentBadge, Comment> mCmtSet = new IdentityHashMap<>();
  private final Map<AbstractEdge, Edge> mEdges = new IdentityHashMap<>();

  // Snap to grid support: TODO: MOVE TO MODEL
  private GridManager mGridManager = null;

  /** Node positions when dragging starts */
  private Map<Node, Point> mNodeStartPositions = null;

  //
  private final Observable mObservable = new Observable();

  // The project editor
  private final ProjectEditor mEditor;
  private final EditorProject mProject;

  private float mZoomFactor = 1.0f;
  
  // to suspend mouse input when the workspace changes drastically
  private boolean mIgnoreMouseInput = false;

  /**
   *
   *
   */
  protected WorkSpace(ProjectEditor editor) {
    mEditor = editor;
    mProject = editor.getEditorProject();
    mGridManager = new GridManager(this);
    mZoomFactor = getEditorConfig().sZOOM_FACTOR;

    // init layout
    setLayout(new SceneFlowLayoutManager());
    setBorder(BorderFactory.createEmptyBorder());

    // show all elements
    showCurrentWorkSpace();
  }

  /** Inhibit mouse actions for a while. */
  protected void ignoreMouseInput(boolean ignore) {
    mIgnoreMouseInput = ignore;
  }

  /** Return true if mouse actions are suspended. */
  protected boolean shouldIgnoreMouseInput() {
    return mIgnoreMouseInput;
  }
  
  //
  public void refresh() {
    mObservable.update(null);
    repaint(100);
    revalidate();
  }

  public void refreshAll() {
    mZoomFactor = getEditorConfig().sZOOM_FACTOR;
    clearCurrentWorkspace();
    showCurrentWorkSpace();
  }

  public void updateAll() {
    for (Node n : mNodeSet.values()) {
      n.update(null,  null);
    }
    for (Edge e: mEdges.values()) {
      e.update(null,  null);
    }
    for (Comment c: mCmtSet.values()) {
      c.update();
    }
    revalidate();
    repaint(100);    
  }

  /** Return the SuperNode this WorkSpace currently displays */
  protected SuperNode getSuperNode() {
    return mEditor.getActiveSuperNode();
  }

  /** Show a status message on the editor */
  protected void setMessageLabelText(String s) {
    mEditor.setMessageLabelText(s);
  }

  public EditorConfig getEditorConfig() {
    return mProject.getEditorConfig();
  }

  public ProjectEditor getEditor() {
    return mEditor;
  }
  
  /* ######################################################################
   * Zoom Methods, followed by Coordinate transformations
   * ###################################################################### */
 
  /* The split in x and y for the next four methods is currently not necessary,
   * for possible future extensions, where x and y might behave differently
   */
  
  /** Convert from model x position to x view position */
  public int toViewXPos(int modx) {
    return (int)(modx * mZoomFactor);
  }
  
  /** Convert from model y position to y view position */
  public int toViewYPos(int mody) {
    return (int)(mody * mZoomFactor);
  }

  /** Convert from view x position to x model position */
  public int toModelXPos(int viewx) {
    return (int)(viewx / mZoomFactor);
  }

  /** Convert from view y position to y model position */
  public int toModelYPos(int viewy) {
    return (int)(viewy / mZoomFactor);
  }

  /** Convert from view to model coordinates */
  public Position toModelPos(Point val) {
    return new Position(toModelXPos(val.x), toModelYPos(val.y));
  }

  /** Convert from model to view coordinates */
  public Point toViewPoint(Position val) {
    return new Point(toViewXPos(val.getXPos()), toViewYPos(val.getYPos()));
  }
  
  /** Convert from view to model coordinates */
  public Boundary toModelBoundary(Rectangle r) {
    int x = toModelXPos(r.x);
    int y = toModelYPos(r.y);
    int width = toModelXPos(r.x + r.width) - x;
    int height = toModelYPos(r.y + r.height) - y;
    return new Boundary(x, y, width, height);
  }
  
  /** Convert from model to view coordinates */
  public Rectangle toViewRectangle(Boundary r) {
    int x = toViewXPos(r.getXPos());
    int y = toViewYPos(r.getYPos());
    int width = toViewXPos(r.getXPos() + r.getWidth()) - x;
    int height = toViewYPos(r.getYPos() + r.getHeight()) - y;
    return new Rectangle(x, y, width, height);
  }
  
  /* ######################################################################
   * Node/Edge/Comment access methods
   * ###################################################################### */
  
  private Node getNode(String id) {
    for (Node node : mNodeSet.values()) {
      if (node.getDataNode().getId().equals(id)) {
        return node;
      }
    }

    return null;
  }

  private class EdgeIterator implements Iterator<Edge> {

    Iterator<Node> nodeIt = mNodeSet.values().iterator();
    Node current;
    Iterator<Edge> edgeIt;

    EdgeIterator(Iterable<Node> nodes) {
      nodeIt = nodes.iterator();
      current = nodeIt.hasNext() ? nodeIt.next() : null;
      //edgeIt = current == null ? null : current.getConnectedEdges().iterator();
      edgeIt = current == null ? null : current.getOutgoingEdges().iterator();
    }

    @Override
    public boolean hasNext() {
      while (edgeIt != null && ! edgeIt.hasNext()) {
        current = nodeIt.hasNext() ? nodeIt.next() : null;
        //edgeIt = current == null ? null : current.getConnectedEdges().iterator();
        edgeIt = current == null ? null : current.getOutgoingEdges().iterator();
      }
      return edgeIt != null && edgeIt.hasNext();
    }

    @Override
    public Edge next() {
      return edgeIt.next();
    }
  }

  public Iterable<Edge> getEdges() {
    return new Iterable<Edge>(){
      @Override
      public Iterator<Edge> iterator() {
        return new EdgeIterator(mNodeSet.values());
      }
    };
  }

  protected Map<CommentBadge, Comment> getComments() {
    return mCmtSet;
  }


  /** Clear all data structures of this WorkSpace */
  protected void clearCurrentWorkspace() {
    mObservable.deleteObservers();

    // Clear the list of currently shown nodes and edges and
    // remove all components from the workspace.
    mNodeSet.clear();
    mCmtSet.clear();
    removeAll();

    // Create a new Gridmanager for the workspace
    mGridManager.clear();
    revalidate();
    repaint(100);
    // TODO: Refresh here!
  }

  /** */
  public void cleanup() {
    // TODO: proper cleanup
    getEditor().clearClipBoard();
    clearCurrentWorkspace();
  }

  public void updateView(AbstractEdge e) {
    if (mEdges.containsKey(e)) mEdges.get(e).update();
  }
  
  public void showNewSuperNode() {
    clearCurrentWorkspace();
    showCurrentWorkSpace();
  }

  // ######################################################################
  // Turn the current SuperNode model into view objects
  // ######################################################################

  //reset components on works space
  protected void showCurrentWorkSpace() {
    // Show the nodes and supernodes on the workspace.
    // Show the edges on the workspace.
    // Show the variables on workspace.
    for (BasicNode n : getSuperNode().getNodes()) {
      addNodeView(n);
    }
    for (CommentBadge n : getSuperNode().getCommentList()){
      addToWorkSpace(new Comment(this, n));
    }
    showEdgesOnWorkSpace();
    revalidate();
    repaint(100);
  }

  /** Add views for all edges between nodes in this workspace */
  private void showEdgesOnWorkSpace() {
    for (Node sourceNode : mNodeSet.values()) {
      for (AbstractEdge e : sourceNode.getDataNode().getEdgeList()) {
        Node targetNode = getNode(e.getTargetUnid());
        if (targetNode != null) {
          addToWorkSpace(new Edge(this, e, sourceNode, targetNode));
        }
      }
    }
  }

  // ######################################################################
  // END OF: Turn the current SuperNode model into view objects
  // ######################################################################

  protected void launchElementSelectedEvent(Object n) {
    ElementSelectedEvent ev =
        new ElementSelectedEvent(n == null ? getSuperNode() : n);
    EventDispatcher.getInstance().convey(ev);
  }
  

  /** Helper function for dragNodes */
  private Map<Node, Point> computeNewPositions(Point moveVec) {
    Map<Node, Point> newPositions =
        new IdentityHashMap<>(mNodeStartPositions.size());
    for (Map.Entry<Node, Point> e : mNodeStartPositions.entrySet()) {
      Node node = e.getKey();
      Point newPos = Geom.add(mNodeStartPositions.get(node), moveVec);
      if (newPos.x < 0 || newPos.y < 0) {
        // stop dragging, if upper and left border would be passed!
        return null;
      }
      newPositions.put(node, newPos);
    };
    return newPositions;
  }

  /**
   *  Drag a set of nodes to a new location, mouse not released.
   *
   *  moveVec is in view coordinates
   */
  protected boolean dragNodes(Set<Node> nodes, Point moveVec) {
    if (mNodeStartPositions == null) {
      mNodeStartPositions = new IdentityHashMap<>();
      for (Node n : nodes) {
        if (getEditorConfig().sSNAPTOGRID)
          mGridManager.releaseGridPosition(n.getDataNode());
        mNodeStartPositions.put(n, n.getLocation());
      }
    }
    // new positions in view coordinates
    Map<Node, Point> newPositions = computeNewPositions(moveVec);
    if (newPositions == null) return false;

    for (Map.Entry<Node, Point> e : newPositions.entrySet()) {
      e.getKey().moveTo(toModelPos(e.getValue()));
    }
    return true;
  }

  /** Move a set of nodes to a new position
   * @param nodeSet the set of nodes with their original locations
   * @param event   contains the mouse release position
   */
  protected void dragNodesFinished(MouseEvent event) {
    if (mNodeStartPositions == null)
      return;

    Map<BasicNode, Position> newPositions =
        new IdentityHashMap<>(mNodeStartPositions.size());
    for (Node node : mNodeStartPositions.keySet()) {
      Position pos = toModelPos(node.getLocation());
      if (getEditorConfig().sSNAPTOGRID)
        pos = mGridManager.getNodeLocation(pos);

      newPositions.put(node.getDataNode(), pos);
      node.mouseReleased(event);
    }
    Map<BasicNode, Position> oldPositions =
        new IdentityHashMap<>(mNodeStartPositions.size());
    for (Map.Entry<Node, Point> entry : mNodeStartPositions.entrySet()) {
      oldPositions.put(entry.getKey().getDataNode(), 
          toModelPos(entry.getValue()));
    }
    new MoveNodesAction(getEditor(), oldPositions, newPositions).run();
    mNodeStartPositions = null;
    // mGridManager.normalizeGridWeight();
  }


  /** If there's a node in the set under p, return it, otherwise null */
  private Node findNodeAtPoint(Iterable<Node> nodes, Point p) {
    for (Node node : nodes) {
      if (node.containsPoint(p.x, p.y)) {
        return node;
      }
    }
    return null;
  }

  /** If there's a node on this workspace under p, return it, otherwise null */
  public Node findNodeAtPoint(Point p) {
    return findNodeAtPoint(mNodeSet.values(), p);
  }
  
  /** Where are the grid points (crosses):
   * (3 * offset + col * gridWidth, 3 * offsets + row * gridHeight)
   * where gridWidth = nodeWidth * config.sGRID_SCALE * config.sZOOM_FACTOR
   * and offset = gridWidth / 4
   * Currently, we assume a square grid, and take
   * nodeWidth = nodeHeight = max(nodeWidth, nodeHeight)
   */
  private void drawGrid(Graphics2D g2d, Rectangle visibleRect) {
    g2d.setStroke(new BasicStroke(1.0f));
    g2d.setColor(Color.GRAY.brighter());
    float gridWidth = mGridManager.gridWidth() * mZoomFactor;

    // compute row and col of the first and last grid point
    int offs = getEditorConfig().sNODEWIDTH / 2;
    Point offset = toViewPoint(new Position(offs, offs));
    int col = (int)(visibleRect.x / gridWidth);
    int lastCol = (int)((visibleRect.x + visibleRect.width) / gridWidth) + 1;
    int lastRow = (int)((visibleRect.y + visibleRect.height) / gridWidth) + 1;
    for (int x = (int)(col * gridWidth) ; col <= lastCol; ++col, x += gridWidth) {
      int row = (int)(visibleRect.y / gridWidth);
      for (int y = (int)(row * gridWidth); row <= lastRow; ++row, y += gridWidth) {
        // TODO: this is for debugging only, wasteful, and should go
        if (mGridManager.positionOccupiedBy(
            toModelPos(new Point(x, y))) != null) {
          g2d.setColor(Color.RED); // for debugging! Should never be visible.
        } else {
          g2d.setColor(Color.GRAY.brighter());
        }
        int xx = (int)(x + offset.x); 
        int yy = (int)(y + offset.y);
        // draw small cross
        int width = (int)(gridWidth / 20);
        g2d.drawLine(xx - width, yy, xx + width, yy);
        g2d.drawLine(xx, yy - width, xx, yy + width);
      }
    }
  }
  
  /**
   */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    super.paintComponent(g);
    if (getEditorConfig().sSHOWGRID)
      drawGrid(g2d, this.getVisibleRect());

    // draw colored border all around the workspace to indicate supernode type
    Color indicator;
    switch (getSuperNode().getFlavour()) {
      case CNODE: indicator = sCEDGE_COLOR; break;
      case PNODE: indicator = sPEDGE_COLOR; break;
      case FNODE: indicator = sFEDGE_COLOR; break;
      case INODE: indicator = sIEDGE_COLOR; break;
      case TNODE: indicator = sTEDGE_COLOR; break;
      default: indicator = Color.WHITE;
    }

    g2d.setColor(indicator);
    g2d.setStroke(new BasicStroke(3.0f));
    g2d.drawRect(1, 1, getSize().width - 3, getSize().height - 4);
  }


  /** Add our own update method */
  public class Observable extends java.util.Observable {

    public void update(Object obj) {
      setChanged();
      notifyObservers(obj);
    }
  }

  // ######################################################################
  // Helper functions for undoable actions for nodes and edges
  // ######################################################################

  /** Removes view edge from workspace, no change in model */
  private void removeFromWorkSpace(Edge e) {
    // Free the docking points on source and target node
    e.disconnect();
    mEdges.remove(e.getDataEdge());
    // remove from Panel
    super.remove(e);
    CodeArea c = e.getCodeArea();
    if (c != null) {
      c.clear();
      super.remove(c);
    }
    mObservable.deleteObserver(e);
  }

  /** Add edge to workspace, no change in model */
  private void addToWorkSpace(Edge e) {
    // Connect the docking points on source and target node
    e.connect();
    mEdges.put(e.getDataEdge(), e);
    // add to Panel
    super.add(e);
    CodeArea c = e.getCodeArea();
    if (c != null)
      super.add(c);
    mObservable.addObserver(e);
  }

  /** Removes node view from workspace, no change in model */
  private void removeFromWorkSpace(Node n) {
    mNodeSet.remove(n.getDataNode());
    if (getEditorConfig().sSNAPTOGRID)
      mGridManager.releaseGridPosition(n.getDataNode());
    // remove from Panel
    super.remove(n);
    CodeArea c = n.getCodeArea();
    if (c != null) {
      c.clear();
      super.remove(c);
    }
    mObservable.deleteObserver(n);
  }

  /** Add node view to workspace, no change in model */
  private void addToWorkSpace(Node n) {
    if (getEditorConfig().sSNAPTOGRID)
      mGridManager.occupyGridPosition(n.getDataNode());
    mNodeSet.put(n.getDataNode(), n);
    // add to Panel
    super.add(n);
    CodeArea c = n.getCodeArea();
    if (c != null)
      super.add(c);
    mObservable.addObserver(n);
  }

  /** Remove comment view from workspace, no change in model */
  private void removeFromWorkSpace(Comment c) {
    mCmtSet.remove(c.getData());
    super.remove(c);
    mObservable.deleteObserver(c);
  }

  /** Add a new comment to the workspace, no change in the model */
  private void addToWorkSpace(Comment c) {
    mCmtSet.put(c.getData(), c);
    super.add(c);
    mObservable.addObserver(c);
  }

  // ######################################################################
  // actions for edges
  // ######################################################################

  /** Add the given node and edge models of a disconnected subgraph of nodes
   *  to the view. This is used if re-inserted after a cut operation,
   *  or as part of an undo.
   */
  public void addNodes(Collection<BasicNode> nodes) {
    // first add all nodes, so the views exist when adding edges
    for (BasicNode n : nodes) {
      //getSuperNode().addNode(n);
      addNodeView(n);
    }
    // add edge views for internal edges
    for (BasicNode n : nodes) {
      for (AbstractEdge e: n.getEdgeList()) {
        addEdge(e);
      }
    }
  }

  /** Remove the edge view for this model, if any */
  public void removeEdge(AbstractEdge e) {
    Edge edge = mEdges.get(e);
    if (edge != null)
      removeFromWorkSpace(edge);
  }

  /** Add a view for this edge, requires that the corresponding node views 
   *  already exist.
   */
  public void addEdge(AbstractEdge e) {
    Edge ve = new Edge(this, e, mNodeSet.get(e.getSourceNode())
        , mNodeSet.get(e.getTargetNode()));
    addToWorkSpace(ve);
  }

  /** Add a set of edge views for the given models
   * 
   *  Prerequisite: the node views and models the edges and their respective
   *     models exist, and were not modified in a way which interferes with, 
   *     e.g., the docking points, or the positions.
   */
  public void addEdges(Collection<AbstractEdge> edges) {
    for (AbstractEdge e: edges) {
      addEdge(e);
    }
  }

  // ######################################################################
  // actions for nodes
  // ######################################################################

  /** Add a NEW node, create the view for the newly created node prototype */
  public void addNodeView(BasicNode n) {
    addToWorkSpace(new Node(this, n));
  }

  /** Create a new node model and view from a model *PROTOTYPE*, at the given
   *  location, and add it to the workspace
   */
  public BasicNode createNode(Point point, BasicNode model) {
    Position p = toModelPos(point);
    if (getEditorConfig().sSNAPTOGRID) {
      p = mGridManager.getNodeLocation(p);
    }
    return model.createNode(p, getSuperNode());
  }

  /** Change type of node: BasicNode <-> SuperNode
   *
   * Super to Basic is only allowed if there are no inner nodes in the SuperNode
   */
  public void changeType(BasicNode n, BasicNode changeTo) {
    Node node = mNodeSet.get(n);
    // removing the old node and adding a new changed one would require to
    // re-create the edges too, which is not necessary
    node.setModel(changeTo);
    mNodeSet.put(node.getDataNode(), node);
  }

  protected List<AbstractEdge> edgeModels(Collection<Edge> l) {
    List<AbstractEdge> result = new ArrayList<>(l.size());
    for (Edge e: l) result.add(e.getDataEdge());
    return result;
  }

  protected List<BasicNode> nodeModels(Collection<Node> l) {
    List<BasicNode> result = new ArrayList<>(l.size());
    for (Node n: l) result.add(n.getDataNode());
    return result;
  }
  
  /** Remove the node and edge views that belong to the models from the view 
   */
  @SuppressWarnings("unchecked")
  public void removeNodes(Collection<BasicNode> nodes, Object[] edgeLists) {
    // Edges pointing from the set to the outside
    Collection<AbstractEdge> emergingEdges = (Collection<AbstractEdge>)edgeLists[0];
    // Edges between nodes in the set
    Collection<AbstractEdge> internalEdges = (Collection<AbstractEdge>)edgeLists[1];
    // Edges pointing from the outside into the set
    Collection<AbstractEdge> incomingEdges = (Collection<AbstractEdge>)edgeLists[2];

    // remove nodes from the view
    for (BasicNode n: nodes) removeFromWorkSpace(mNodeSet.get(n));
    // to avoid ConcurrentModification
    for (AbstractEdge e: emergingEdges) removeFromWorkSpace(mEdges.get(e));
    for (AbstractEdge e: internalEdges) removeFromWorkSpace(mEdges.get(e));
    for (AbstractEdge e: incomingEdges) removeFromWorkSpace(mEdges.get(e));

  }

  // ######################################################################
  // actions for comments
  // ######################################################################

  /** Add a new comment */
  public void addComment(CommentBadge comment) {
    if (comment.getParentNode() == this.getSuperNode())
      addToWorkSpace(new Comment(this, comment));
  }

  /** Remove comment from WorkSpace and Model, for RemoveCommentAction */
  public void removeComment(CommentBadge comment) {
    if (mCmtSet.containsKey(comment))
      removeFromWorkSpace(mCmtSet.get(comment));
  }
}
