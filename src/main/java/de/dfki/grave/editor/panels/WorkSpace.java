package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.action.MoveNodesAction;
import de.dfki.grave.editor.event.ClearCodeEditorEvent;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.editor.event.WorkSpaceSelectedEvent;
import de.dfki.grave.model.flow.*;
import de.dfki.grave.model.flow.geom.Boundary;
import de.dfki.grave.model.flow.geom.Position;
import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.util.Pair;
import de.dfki.grave.util.Triple;
import de.dfki.grave.util.evt.EventDispatcher;
import de.dfki.grave.util.evt.EventListener;

/**
 *
 * This is the View of the currently edited SuperNode, containing views for all
 * the SuperNodes contained nodes and the edges between them, and their
 * corresponding text badges.
 *
 * This class is meant to be used *only* with WorkSpacePanel, which provides
 * the "interactive" functionality: Mouse Movements/Menus/Dragging/Keyboard
 * while this class provides the "functionality" on the View elements:
 * Adding / Removing / Moving
 */
@SuppressWarnings("serial")
public abstract class WorkSpace extends JPanel implements EventListener {

  // The clipboard
  protected final ClipBoard mClipboard = ClipBoard.getInstance();

  // Elements to draw
  protected final Map<BasicNode, Node> mNodeSet = new IdentityHashMap<>();
  private final Set<Comment> mCmtSet = new HashSet<>();
  private final Map<AbstractEdge, Edge> mEdges = new IdentityHashMap<>();

  private boolean snapToGrid = true;

  // Snap to grid support
  private GridManager mGridManager = null;

  /** Node positions when dragging starts */
  private Map<Node, Point> mNodeStartPositions = null;

  //
  private final Observable mObservable = new Observable();
  private final EventDispatcher mEventCaster = EventDispatcher.getInstance();

  // The parent SceneFlowEditor (TODO: remove)
  private final SceneFlowEditor mSceneFlowEditor;
  private final EditorProject mProject;

  private final SceneFlow mSceneFlow;
  private final IDManager mIDManager; // manages new IDs for the SceneFlow

  public float mZoomFactor = 1.0f;

  /**
   *
   *
   */
  protected WorkSpace(SceneFlowEditor sceneFlowEditor, EditorProject project) {
    mSceneFlowEditor = sceneFlowEditor;
    mProject = project;
    mSceneFlow = mProject.getSceneFlow();
    mSceneFlowEditor.addActiveSuperNode(mSceneFlow);
    mIDManager = new IDManager(mSceneFlow);
    mGridManager = new GridManager(this, getSuperNode());
    mZoomFactor = getEditorConfig().sZOOM_FACTOR;

    // init layout
    setLayout(new SceneFlowLayoutManager());
    setBorder(BorderFactory.createEmptyBorder());

    // Add the element editor to the event multicaster
    mEventCaster.register(this);
    //show all elements
    showCurrentWorkSpace();
  }

  /** Inhibit mouse actions for a while. Must be implemented by subclass */
  protected abstract void ignoreMouseInput();

  //
  public void refresh() {
    mObservable.update(null);
    revalidate();
    repaint(100);
  }

  public void refreshAll() {
    clearCurrentWorkspace();
    showCurrentWorkSpace();
  }

  /**
   *
   */
  @Override
  public void update(Object event) {
    checkChangesOnWorkspace();
  }

  // TODO: Move that up to to the editor
  protected void checkChangesOnWorkspace() {
    //mLogger.message("Checking changes on workspace");
    // checkHash
    if (EditorInstance.getInstance().getSelectedProjectEditor() != null) {
      if (EditorInstance.getInstance().getSelectedProjectEditor().getEditorProject() != null) {
        if (mProject.hasChanged()) {
          //int selectecTabIndex = EditorInstance.getInstance().getProjectEditors().getSelectedIndex();
          EditorInstance.getInstance().setTabNameModified();
          //mLogger.message("Changes on workspace detected");
        }
      }
    }
  }

  /**
   *
   */
  protected void launchProjectChangedEvent() {
    if (mProject.hasChanged()) {
      ProjectChangedEvent ev = new ProjectChangedEvent(this);
      mEventCaster.convey(ev);
    }
  }

  /** Return the SuperNode this WorkSpace currently displays */
  protected SuperNode getSuperNode() {
    return mSceneFlowEditor.getActiveSuperNode();
  }

  /** Show a status message on the editor */
  public void setMessageLabelText(String s) {
    mSceneFlowEditor.setMessageLabelText(s);
  }

  public void clearClipBoard() {
    mClipboard.clear();
  }

  public EditorConfig getEditorConfig() {
    return mProject.getEditorConfig();
  }

  public SceneFlowEditor getSceneFlowEditor() {
    return mSceneFlowEditor;
  }

  public GridManager getGridManager() {
    return mGridManager;
  }

  private Node getNode(String id) {
    for (Node node : mNodeSet.values()) {
      if (node.getDataNode().getId().equals(id)) {
        return node;
      }
    }

    return null;
  }

  public Collection<Node> getNodes() {
    return mNodeSet.values();
  }

  public void zoomOut() {
    if (mZoomFactor > 0.5) mZoomFactor -= .1;
    getEditorConfig().sZOOM_FACTOR = mZoomFactor;
    //saveEditorConfig(); // TODO: activate
    refreshAll();
  }

  public void zoomIn() {
    if (mZoomFactor < 3.0) mZoomFactor += .1;
    getEditorConfig().sZOOM_FACTOR = mZoomFactor;
    //saveEditorConfig(); // TODO: activate
    refreshAll();
  }

  public int zoom(int val) {
    return (int)(val * mZoomFactor);
  }

  public int unzoom(int val) {
    return (int)(val / mZoomFactor);
  }

  public Point zoom(Point val) {
    return new Point((int)(val.x * mZoomFactor),
        (int)(val.y * mZoomFactor));
  }

  public Point unzoom(Point val) {
    return new Point((int)(val.x / mZoomFactor),
        (int)(val.y / mZoomFactor));
  }

  public Point zoom(Position val) {
    return new Point((int)(val.getXPos() * mZoomFactor),
        (int)(val.getYPos() * mZoomFactor));
  }

  private class EdgeIterator implements Iterator<Edge> {

    Iterator<Node> nodeIt = mNodeSet.values().iterator();
    Node current;
    Iterator<Edge> edgeIt;

    EdgeIterator(Iterable<Node> nodes) {
      nodeIt = nodes.iterator();
      current = nodeIt.hasNext() ? nodeIt.next() : null;
      edgeIt = current == null ? null : current.getConnectedEdges().iterator();
    }

    @Override
    public boolean hasNext() {
      while (edgeIt != null && ! edgeIt.hasNext()) {
        current = nodeIt.hasNext() ? nodeIt.next() : null;
        edgeIt = current == null ? null : current.getConnectedEdges().iterator();
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

  protected Set<Comment> getComments() {
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
    mEventCaster.convey(new ClearCodeEditorEvent(this));
    repaint(100);
    // TODO: Refresh here!
  }

  /** */
  public void cleanup() {
    // TODO: proper cleanup
    clearClipBoard();
    clearCurrentWorkspace();
  }

  /** Try to get all edges as straight as possible */
  public void straightenAllEdges() {
    for (Edge edge : getEdges()) {
      edge.straightenEdge();
    }
    repaint(100);
  }

  /** Try to find nice paths for all edges */
  public void normalizeAllEdges() {
    for (Edge edge : getEdges()) {
      edge.rebuildEdgeNicely();
    }
    repaint(100);
  }

  public void straightenEdge(AbstractEdge e) {
    mEdges.get(e).straightenEdge();
  }

  public void rebuildEdgeNicely(AbstractEdge e) {
    mEdges.get(e).straightenEdge();
  }

  public void normalizeEdge(AbstractEdge e) {
    mEdges.get(e).straightenEdge();
  }

  private void showNewSuperNode() {
    clearCurrentWorkspace();
    mEventCaster.convey(new ElementSelectedEvent(null));
    showCurrentWorkSpace();
  }

  /** Jump into the SuperNode node (currently present on the WorkSpace) */
  public void increaseWorkSpaceLevel(Node node) {
    // Reset mouse interaction
    ignoreMouseInput();
    SuperNode superNode = (SuperNode) node.getDataNode();
    mSceneFlowEditor.addActiveSuperNode(superNode);
    showNewSuperNode();
  }

  /** Pop out to the specified SuperNode */
  public void selectNewWorkSpaceLevel(SuperNode supernode) {
    if (getSuperNode().equals(supernode)) return;
    SuperNode parent = mSceneFlowEditor.removeActiveSuperNode();
    while (parent != null && parent != supernode) {
      parent = mSceneFlowEditor.removeActiveSuperNode();
    }
    showNewSuperNode();
 }

  /** Pop out one level, if possible */
  public void decreaseWorkSpaceLevel() {
    SuperNode parent = mSceneFlowEditor.removeActiveSuperNode();
    if (parent == null) return;
    showNewSuperNode();
  }

  // ######################################################################
  // Turn the current SuperNode model into view objects
  // ######################################################################

  //reset components on works space
  protected void showCurrentWorkSpace() {
    // Show the nodes and supernodes on the workspace.
    // Show the edges on the workspace.
    // Show the variables on workspace.
    showNodesOnWorkSpace();
    for (CommentBadge n : getSuperNode().getCommentList()){
      addToWorkSpace(new Comment(this, n));
    }
    showEdgesOnWorkSpace();
    revalidate();
    repaint(100);
  }

  /** Add views for all the (sub)node models in this workspace's SuperNode */
  private void showNodesOnWorkSpace() {
    for (BasicNode n : getSuperNode().getNodes()) {
      addNode(n);
    }
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

 protected void launchWorkSpaceSelectedEvent() {
    WorkSpaceSelectedEvent ev = new WorkSpaceSelectedEvent(this);
    mEventCaster.convey(ev);
  }

  protected void launchElementSelectedEvent(Object n) {
    ElementSelectedEvent ev =
        new ElementSelectedEvent(n == null ? getSuperNode() : n);
    mEventCaster.convey(ev);
  }


  public void moveTo(BasicNode n, Point loc) {
    Node vn = mNodeSet.get(n);
    if (snapToGrid) {
      mGridManager.releaseGridPosition(vn.getLocation());
    }
    vn.moveTo(loc);
  }

  /** Change this edge in some way, all Points in model coordinates */
  public void modifyEdge(AbstractEdge edge,
      BasicNode[] nodes, int[] docks, Point[] ctrls) {
    Edge e = mEdges.get(edge);
    e.modifyEdge(mNodeSet.get(nodes[0]), mNodeSet.get(nodes[1]),
        nodes, docks, ctrls);
  }

  /**
   *  Drag a set of nodes to a new location, mouse not released
   */
  protected boolean dragNodes(Set<Node> nodes, Point moveVec) {
    if (mNodeStartPositions == null) {
      mNodeStartPositions = new IdentityHashMap<>();
      for (Node n : nodes) {
        Point nodeLoc = n.getLocation();
        if (snapToGrid)
          mGridManager.releaseGridPosition(nodeLoc);
        mNodeStartPositions.put(n, nodeLoc);
      }
    }
    for (Node node : nodes) {
      Point nodePos = node.getLocation();
      if (((nodePos.x + moveVec.x) <= 0) || ((nodePos.y + moveVec.y) <= 0)) {
        // stop dragging, if upper and left border would be passed!
        return false;
      }
    }

    for (Node node : nodes) {
      node.translate(moveVec);
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

    Map<BasicNode, Point> newPositions =
        new IdentityHashMap<>(mNodeStartPositions.size());
    for (Node node : mNodeStartPositions.keySet()) {
      Point p = node.getLocation();
      // check location of each node
      if (snapToGrid)
        p = mGridManager.getNodeLocation(p);

      newPositions.put(node.getDataNode(), unzoom(p));
      node.mouseReleased(event);
    }
    Map<BasicNode, Point> oldPositions =
        new IdentityHashMap<>(mNodeStartPositions.size());
    for (Map.Entry<Node, Point> entry : mNodeStartPositions.entrySet()) {
      oldPositions.put(entry.getKey().getDataNode(), unzoom(entry.getValue()));
    }
    new MoveNodesAction(this, oldPositions, newPositions).run();
    mNodeStartPositions = null;
    // mGridManager.normalizeGridWeight();
  }


  /** If there's a node in the set under p, return it, otherwise null */
  public Node findNodeAtPoint(Iterable<Node> nodes, Point p) {
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

  /**
   */
  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    super.paintComponent(g);
    mGridManager.drawGrid(g2d, this.getVisibleRect());

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
    mObservable.deleteObserver(e);
  }

  /** Add edge to workspace, no change in model */
  private void addToWorkSpace(Edge e) {
    // Connect the docking points on source and target node
    e.connect();
    mEdges.put(e.getDataEdge(), e);
    // add to Panel
    super.add(e);
    mObservable.addObserver(e);
  }

  /** Removes node view from workspace, no change in model */
  private void removeFromWorkSpace(Node n) {
    mNodeSet.remove(n.getDataNode());
    if (snapToGrid)
      mGridManager.releaseGridPosition(n.getLocation());
    // remove from Panel
    super.remove(n);
    super.remove(n.getCmdBadge());
    mObservable.deleteObserver(n);
  }

  /** Add node view to workspace, no change in model */
  private void addToWorkSpace(Node n) {
    mNodeSet.put(n.getDataNode(), n);
    // add to Panel
    super.add(n);
    super.add(n.getCmdBadge());
    mObservable.addObserver(n);
  }

  /** Remove comment view from workspace, no change in model */
  private void removeFromWorkSpace(Comment c) {
    mCmtSet.remove(c);
    super.remove(c);
    mObservable.deleteObserver(c);
  }

  /** Add a new comment to the workspace, no change in the model */
  private void addToWorkSpace(Comment c) {
    mCmtSet.add(c);
    super.add(c);
    mObservable.addObserver(c);
  }


  /** Helper function, adjusting the positions of node views and models
   *  such that the center of the covered area is at the given position.
   *
  private void translateNodeViews(Collection<Node> nodes, Point p) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE,
        maxX = 0, maxY = 0;
    // compute the covered area
    for (Node n : nodes) {
      int x = n.getLocation().x;
      int y = n.getLocation().y;
      minX = Math.min(minX, x); minY = Math.min(minY, y);
      maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
    }
    // translate such that the center of the area is on p
    int translateX = Math.max(p.x - (maxX + minX) / 2, -minX);
    int translateY = Math.max(p.y - (maxY + minY) / 2, -minY);
    // add half the node size
    Node node = nodes.iterator().next();
    translateX += node.getWidth() / 2;
    translateY += node.getHeight() / 2;
    // should move the edges, too
    for (Node n : nodes) {
      Point loc = n.getLocation();
      loc.translate(translateX, translateY);
      if (snapToGrid) {
        loc = mGridManager.getNodeLocation(loc);
      }
      n.moveTo(loc);
      //n.translate does that
      //n.getDataNode().translate(translateX, translateY);
    }
  }*/

  // ######################################################################
  // actions for edges
  // ######################################################################

  /** Add the given nodes and edges of a disconnected subgraph of nodes as is
   *  to the view and model. This is used if re-inserted after a cut operation,
   *  or as part of an undo. The nodes and edges are just added, without further
   *  modifications.
   */
  public void pasteNodesAndEdges(Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges) {
    for (BasicNode n : nodes) {
      getSuperNode().addNode(n);
      addNode(n);
      // add edge views for internal edges
      for (AbstractEdge e: n.getEdgeList()) {
        if (nodes.contains(e.getTargetNode())) {
          addEdgeView(e);
        }
      }
    }
    // Add edges emerging from the set of `nodes', which also requires
    // reclaiming reconnecting to target nodes and reclaiming the dock
    addEdges(edges);
  }

  /** Complete the edge model, and create a new edge view from the given data.
   *  THIS METHOD IS ONLY TO BE CALLED TO CREATE A COMPLETELY NEW EDGE VIA THE
   *  GUI
   */
  public AbstractEdge createEdge(AbstractEdge edge, BasicNode source, BasicNode target) {
    Node sourceView = mNodeSet.get(source);
    Node targetView = mNodeSet.get(target);
    edge.connect(source, target);
    edge.straightenEdge(sourceView.getWidth());
    Edge ve = new Edge(this, edge, sourceView, targetView);
    addToWorkSpace(ve);
    return edge;
  }

  /** Remove an edge, view AND model */
  public void removeEdge(AbstractEdge e) {
    Edge edge = mEdges.get(e);
    // remove it from the view AND the model (their source node)
    // remove edge from view
    removeFromWorkSpace(edge);
    // this destructively changes the source node of e, which must be
    // UNDOne
    e.getSourceNode().removeEdge(e);
  }

  /** Add an edge view for the given edge, requires that the corresponding node
   *  views already exist.
   */
  public void addEdgeView(AbstractEdge e) {
    // add edge to view
    Edge ve = new Edge(this, e, mNodeSet.get(e.getSourceNode())
        , mNodeSet.get(e.getTargetNode()));
    addToWorkSpace(ve);
  }

  /** Add an edge, view AND model, requires that the corresponding node views
   *  already exist.
   */
  public void addEdge(AbstractEdge e) {
    // add edge to view
    addEdgeView(e);
    // add edge to model
    // this destructively changes the source node of e, which must be
    // UNDOne
    e.getSourceNode().addEdge(e);
  }

  /** Add a set of edge views. Prerequisite: the node views and models the
   *  edges and their respective models exist, and were not modified in a way
   *  which interferes with, e.g., the docking points, or the positions.
   */
  public void addEdges(Collection<AbstractEdge> edges) {
    // add these to the view AND the model (their source node), and store
    // the views for an UNDO
    for (AbstractEdge e: edges) {
      addEdge(e);
    }
  }

  // ######################################################################
  // actions for nodes
  // ######################################################################

  /** Add a NEW node, create the view for the newly created node prototype */
  public void addNode(BasicNode n) {
    addToWorkSpace(new Node(this, n));
  }

  /** Create a new node model and view from a model *PROTOTYPE*, at the given
   *  location, and add it to the workspace
   */
  public BasicNode createNode(Point point, BasicNode model) {
    if (snapToGrid)
      point = mGridManager.getNodeLocation(point);
    Position p = new Position(unzoom(point.x), unzoom(point.y));
    return model.createNode(mIDManager, p, getSuperNode());
  }

  private Node getSourceNode(Edge e) {
    return mNodeSet.get(e.getDataEdge().getSourceNode());
  }

  private Node getTargetNode(Edge e) {
    return mNodeSet.get(e.getDataEdge().getTargetNode());
  }

  /** For a given set of nodes that are subnodes of the same SuperNode, compute
   *  all edge views that emerge from a node outside the set and end in a node
   *  inside the set. nodes must be a subset of the current mNodeSet.
   *
   *  Only returns edges, no change in model or view
   */
  private Collection<Edge> computeIncomingEdges(Collection<Node> nodes) {
    List<Edge> result = new ArrayList<>();
    // easier now
    for (Edge e : mEdges.values()) {
      if (nodes.contains(getTargetNode(e)) && ! nodes.contains(getSourceNode(e)))
        result.add(e);
    }
    return result;
  }

  /** Change type of node: BasicNode <-> SuperNode
   *
   * Super to Basic is only allowed if there are no inner nodes in the SuperNode
   */
  public BasicNode changeType(BasicNode n, BasicNode changeTo) {
    Node node = mNodeSet.get(n);
    BasicNode result = null;
    Collection<Edge> incoming =
        computeIncomingEdges(new ArrayList<Node>(){{add(node);}});
    if ((result = node.changeType(mIDManager, edgeModels(incoming) , changeTo))
        == null) {
      // complain: operation not legal
      setMessageLabelText("SuperNode contains Nodes: Type change not possible");
    }
    return result;
  }

  /** This copies some subset of node and edge views and their underlying
   *  models. One basic assumption is that there are no "dangling" edges which
   *  either start or end at a node outside the given node set.
   *
   *  The copied views will be added to the given WorkSpace, and all copied
   *  node models will be subnodes of the given SuperNode.
   *
  public Pair<Collection<Node>, List<Edge>> copyGraph(
      List<Node> nodeViews, List<Edge> edgeViews) {
    SuperNode newParent = getSuperNode();
    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    Map<Node, Node> origView2copy = new IdentityHashMap<>();
    for (Node nodeView : nodeViews) {
      BasicNode n = nodeView.getDataNode();
      BasicNode cpy = n.deepCopy(mIDManager, newParent);
      orig2copy.put(n, cpy);
      // now create a new Node as view for the copy of n
      Node newNode = new Node(this, cpy);
      origView2copy.put(nodeView, newNode);
    }

    List<Edge> newEdges = new ArrayList<>();
    for (Edge edgeView : edgeViews) {
      AbstractEdge e = edgeView.getDataEdge().deepCopy(orig2copy);
      // now create a new Edge as view for the copy of e
      Edge newEdge = new Edge(this, e,
          origView2copy.get(edgeView.getSourceNode()),
          origView2copy.get(edgeView.getTargetNode()));
      newEdges.add(newEdge);
    }
    return new Pair<Collection<Node>, List<Edge>>(origView2copy.values(), newEdges);
  }*/

  /** This copies some subset of node and edge views and their underlying
   *  models. One basic assumption is that there are no "dangling" edges which
   *  either start or end at a node outside the given node set.
   *
   *  The copied views will be added to the given WorkSpace, and all copied
   *  node models will be subnodes of the given SuperNode.
   *
  public Pair<Collection<BasicNode>, List<AbstractEdge>> copyGraphModel(
      List<BasicNode> nodeViews, List<AbstractEdge> edgeViews) {
    SuperNode newParent = getSuperNode();
    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    for (BasicNode n : nodeViews) {
      BasicNode cpy = n.deepCopy(mIDManager, newParent);
      orig2copy.put(n, cpy);
    }

    List<AbstractEdge> newEdges = new ArrayList<>();
    for (AbstractEdge edge: edgeViews) {
      AbstractEdge e = edge.deepCopy(orig2copy);
      newEdges.add(e);
    }
    return new Pair<Collection<BasicNode>, List<AbstractEdge>>(
        orig2copy.values(), newEdges);
  }*/

  /** paste nodes from the clipboard
   *  This operates in two modes: if not in copy mode, the nodes are just
   *  added. Otherwise:
   *
   *  Do a deep copy of the model and view nodes and edges in the set, assuming
   *  there are no "dangling" edges, and add them to this workspace. Then,
   *  adjust the positions of the new node views such that the center of the
   *  paste area is at the given position
   *
   *  About the placement of the new nodes: They should keep their relative
   *  positions for paste after copy, but at the location of the mouse. For
   *  undo, they retain their old positions, but what after cut? Treat it like
   *  undo, or copy? I favour undo, since the other can be achieved by dragging.
   */
  public Collection<BasicNode> pasteNodesFromClipboard(Point mousePosition) {
    List<BasicNode> nodes = mClipboard.getNodes();
    List<AbstractEdge> edges = mClipboard.getEdges();
    if (mClipboard.needsCopy(this)) {
      Pair<Collection<BasicNode>, List<AbstractEdge>> toAdd =
          getSuperNode().copyGraphModel(mIDManager, nodes, edges);
      if (mousePosition != null) {
        // snap to grid: currently not.
        //mousePosition = mGridManager.getClosestGridPoint(mousePosition);
        translateNodes(toAdd.getFirst(), mousePosition);
      }
      pasteNodesAndEdges(toAdd.getFirst(), toAdd.getSecond());
      return toAdd.getFirst();
    }
    // just add nodes and edges to the view and model as is: same positions,
    // etc.
    pasteNodesAndEdges(nodes, edges);
    // now the clipboard must be set to copy: the nodes are used.
    mClipboard.forceCopy();
    refresh();
    return nodes;
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

  /** Remove the nodes in the given collection.
   *  This is only legal if none of the selected nodes is a start node!
   *
   *  This collects three types of edges: internal, incoming, and outgoing
   *  edges, which is defined relative to the set of nodes given as input.
   *  Incoming edges have to be removed and saved for undo, the rest disappears
   *  anyway when the nodes are removed from the graph
   *
   *  If it's a cut operation, not a delete, put the nodes in the set into the
   *  clipboard.
   *  @return a triple incoming edges, nodes, emerging edges
   */
  public Triple<Collection<AbstractEdge>, Collection<BasicNode>, Collection<AbstractEdge>>
  removeNodes(boolean isCutOperation, Collection<BasicNode> nodes) {
    // Edges pointin.getDataNodeg from the set to the outside
    List<Edge> emergingEdges = new ArrayList<>();
    // Edges between nodes in the set
    List<Edge> internalEdges = new ArrayList<>();
    // Edges pointing from the outside into the set
    List<Edge> incomingEdges = new ArrayList<>();
    SuperNode current = getSuperNode();
    for (BasicNode n : nodes) {
      Node vn = mNodeSet.get(n);
      // remove nodes from model
      // destructively changes the current SuperNode, which must be UNDOne
      current.removeNode(n);
      // remove edges from the view
      for(Edge e : vn.getConnectedEdges()) {
        if (! nodes.contains(getSourceNode(e).getDataNode())) {
          // incoming edge
          incomingEdges.add(e);
          // remove edge from model
          AbstractEdge edge = e.getDataEdge();
          // this destructively changes the source node of edge, which must be
          // UNDOne
          edge.getSourceNode().removeEdge(edge);
        } else {
          if (nodes.contains(mNodeSet.get(e.getDataEdge().getTargetNode()).getDataNode())) {
            internalEdges.add(e);
          } else {
            emergingEdges.add(e);
          }
        }
      }
    }
    // remove nodes from the view
    for (BasicNode n: nodes) removeFromWorkSpace(mNodeSet.get(n));
    // to avoid ConcurrentModification
    for (Edge e: emergingEdges) removeFromWorkSpace(e);
    for (Edge e: internalEdges) removeFromWorkSpace(e);
    for (Edge e: incomingEdges) removeFromWorkSpace(e);

    if (isCutOperation) {
      mClipboard.set(this, nodes, edgeModels(internalEdges));
    }
    return new Triple<>(
        edgeModels(incomingEdges), nodes, edgeModels(emergingEdges));
  }

  /** Copy nodes in the selected nodes set (mSelectedNodes) to the clipboard for
   *  the Copy operation (lazy copy).
   */
  public void copyNodes(Collection<BasicNode> nodes) {
    mClipboard.setToCopy(this, nodes, Node.computeInnerEdges(nodes));
  }

  // ######################################################################
  // actions for comments
  // ######################################################################

  /** Create a totally new comment, for the CreateCommentAction */
  public Comment createComment(Point coordinate) {
    CommentBadge badge = new CommentBadge();
    badge.setBoundary(new Boundary(coordinate.x, coordinate.y, 100, 100));
    Comment newComment = new Comment(this, badge);
    return newComment;
  }

  /** Add a new comment */
  public void addComment(Comment comment) {
    getSuperNode().addComment(comment.getData());
    addToWorkSpace(comment);
  }

  /** Remove comment from WorkSpace and Model, for RemoveCommentAction */
  public void removeComment(Comment comment) {
    removeFromWorkSpace(comment);
    getSuperNode().removeComment(comment.getData());
  }

  /* Assumes that the node and edge views can be perfectly reconstructed from
   * the models, and only models are handled internally
   */

  /** Helper function, adjusting the positions of node and edge models
   *  such that the center of the covered area is at the given position.
   */
  private static void translateNodes(Collection<BasicNode> nodes, Point p) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE,
        maxX = 0, maxY = 0;
    // compute the covered area
    for (BasicNode n : nodes) {
      int x = n.getPosition().getXPos();
      int y = n.getPosition().getYPos();
      minX = Math.min(minX, x); minY = Math.min(minY, y);
      maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
    }
    // translate such that the center of the area is on p
    int translateX = Math.max(p.x - (maxX + minX) / 2, -minX);
    int translateY = Math.max(p.y - (maxY + minY) / 2, -minY);
    // should move the edges, too
    for (BasicNode n : nodes) {
      n.translate(translateX, translateY);
      // now translate all outgoing edges
      //n.translate does that
      //n.getDataNode().translate(translateX, translateY);
    }
    /*
    for (BasicNode n : nodes) {
      for (AbstractEdge e : n.getEdgeList()) {
        e.translate(translateX, translateY);
      }
    }
    */
  }

}

  /** paste nodes from the clipboard
   *  This operates in two modes: if not in copy mode, the nodes are just
   *  added. Otherwise:
   *
   *  Do a deep copy of the model nodes and edges in the set, assuming
   *  there are no "dangling" edges, and add them to this workspace. Then,
   *  adjust the positions of the new node views such that the center of the
   *  paste area is at the given position
   *
   *  About the placement of the new nodes: They should keep their relative
   *  positions for paste after copy, but at the location of the mouse. For
   *  undo, they retain their old positions, but what after cut? Treat it like
   *  undo, or copy? I favour undo, since the other can be achieved by dragging.
   *
  public void pasteNodesFromClipboard0(Point mousePosition) {
    List<Node> nodes = mClipboard.getNodes();
    List<Edge> edges = computeInnerEdges(nodes);
    if (mClipboard.needsCopy(this)) {
      Collection<BasicNode> toAdd = Node.copyGraphModel(
          getSceneFlowEditor().getIDManager(), getSuperNode(), nodes, edges);
      if (mousePosition != null) {
        // snap to grid: currently not.
        //mousePosition = mGridManager.getClosestGridPoint(mousePosition);
        translateNodes(toAdd, mousePosition);
      }

      //pasteNodes(toAdd.getFirst(), toAdd.getSecond());
    } else {
      // just add nodes and edges to the view and model as is: same positions,
      // etc.
      pasteNodes(nodes, edges);
      // now the clipboard must be set to copy: the nodes are used.
      mClipboard.forceCopy();
    }
  }*/
