package de.dfki.vsm.editor.project;

import static de.dfki.vsm.Preferences.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Comment;
import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.event.ClearCodeEditorEvent;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.editor.event.WorkSpaceSelectedEvent;
import de.dfki.vsm.editor.util.GridManager;
import de.dfki.vsm.editor.util.SceneFlowLayoutManager;
import de.dfki.vsm.editor.util.grid.GridRectangle;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.CommentBadge;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Boundary;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.model.project.EditorProject;
import de.dfki.vsm.util.Pair;
import de.dfki.vsm.util.Triple;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;

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
  private final Set<Node> mNodeSet = new HashSet<>();
  //private final Set<Edge> mEdgeSet = new HashSet<>();
  private final Set<Comment> mCmtSet = new HashSet<>();

  // Snap to grid support
  private GridManager mGridManager = null;

  //
  private final Observable mObservable = new Observable();
  private final EventDispatcher mEventCaster = EventDispatcher.getInstance();

  // The parent SceneFlowEditor (TODO: remove)
  private final SceneFlowEditor mSceneFlowEditor;
  private final EditorProject mProject;

  /**
   *
   *
   */
  protected WorkSpace(SceneFlowEditor sceneFlowEditor, EditorProject project) {
    mSceneFlowEditor = sceneFlowEditor;
    mProject = project;
    mGridManager = new GridManager(this, getSuperNode());

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

    // rebuild node position
    mGridManager.update();

    /*
    // TODO: THIS SEEMS FISHY. IF A NODE HAS A LOCATION, WHY SHOULD IT BE
    // RELOCATED AT REFRESH?
    for (Node node : mNodeSet) {
      Point p = mGridManager.getNodeLocation(node.getLocation());

      node.resetLocation(p);
      node.getDataNode().setPosition(new Position(p.x, p.y));
    }
    */

    revalidate();
    repaint(100);
  }

  /**
   *
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

  public GridRectangle[][] getTransitionArea() {
    return mGridManager.getmTransitionArea();
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
    for (Node node : mNodeSet) {
      if (node.getDataNode().getId().equals(id)) {
        return node;
      }
    }

    return null;
  }

  public Set<Node> getNodes() {
    return mNodeSet;
  }

  private class EdgeIterator implements Iterator<Edge> {

    Iterator<Node> nodeIt = mNodeSet.iterator();
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
        return new EdgeIterator(getNodes());
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
    mGridManager.update();
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

  private void showNewSuperNode() {
    clearCurrentWorkspace();
    mEventCaster.convey(new ElementSelectedEvent(null));
    mGridManager.update();
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


  //reset components on works space
  private void showCurrentWorkSpace() {
    // Show the nodes and supernodes on the workspace.
    // Show the edges on the workspace.
    // Show the variables on workspace.
    showNodesOnWorkSpace();
    for (CommentBadge n : getSuperNode().getCommentList()){
      add(new Comment(this, n));
    }
    showEdgesOnWorkSpace();
    revalidate();
    repaint(100);
  }

  /** Add views for all the (sub)node models in this workspace's SuperNode */
  private void showNodesOnWorkSpace() {
    for (BasicNode n : getSuperNode().getNodes()) {
      // TODO: FISHY FISHY FISHY: SNAP GRID ON LOAD?
      Point p = mGridManager.getNodeLocation(
          new Point(n.getPosition().getXPos(), n.getPosition().getYPos()));

      n.setPosition(new Position(p.x, p.y));
      addToWorkSpace(new Node(this, n));
    }
  }

  /** Add views for all edges between nodes in this workspace */
  private void showEdgesOnWorkSpace() {
    for (Node sourceNode : mNodeSet) {
      BasicNode n = sourceNode.getDataNode();
      for (AbstractEdge e : n.getEdgeList()) {
        Node targetNode = getNode(e.getTargetUnid());
        if (targetNode != null) {
          add(new Edge(this, e, sourceNode, targetNode));
        }
      }
    }
  }

  protected void launchWorkSpaceSelectedEvent() {
    WorkSpaceSelectedEvent ev = new WorkSpaceSelectedEvent(this);
    mEventCaster.convey(ev);
  }

  protected void launchElementSelectedEvent(Object n) {
    ElementSelectedEvent ev =
        new ElementSelectedEvent(n == null ? getSuperNode() : n);
    mEventCaster.convey(ev);
  }

  /** Move node by dragging */
  protected void dragNode(Node node, MouseEvent event, Point moveVec) {
    dragNodes(new HashSet<Node>(){{add(node);}}, event, moveVec);
  }

  /**
   *  Drag a set of nodes to a new location
   */
  protected void dragNodes(Set<Node> nodes, MouseEvent event, Point moveVec) {
    boolean validDragging = true;

    for (Node node : nodes) {
      Point nodePos = node.getLocation();

      if (((nodePos.x + moveVec.x) <= 0) || ((nodePos.y + moveVec.y) <= 0)) {

        // stop dragging, if upper and left border would be passed!
        validDragging = false;
      }
    }

    if (validDragging) {
      for (Node node : nodes) {
        Point nodeLoc = node.getLocation();

        mGridManager.freeGridPosition(nodeLoc);
        node.translate(moveVec);
        if ((event.getModifiersEx() == 1024)) {
          node.mDragged = true;
        }

        // sWorkSpaceDrawArea = getSize();
        revalidate();
        repaint(100);
      }
    }
  }

  // TODO: NodesDraggedAction: location change for set of nodes
  protected void dragNodesFinished(Set<Node> nodes, MouseEvent event) {
    for (Node node : nodes) {
      Point p = node.getLocation();

      // check location of each node
      if (node.mDragged) {
        node.resetLocation(mGridManager.getNodeLocation(p));
      }

      // update workspace area - if dragged beyond current borders
      // sWorkSpaceDrawArea = getSize();
      node.mouseReleased(event);
    }
    refresh();
    revalidate();
    repaint(100);
    // mGridManager.normalizeGridWeight();
  }

  /**
   */
  @Override
  public void paintComponent(Graphics g) {
    // mLogger.message("Drawing Workspace");
    Graphics2D g2d = (Graphics2D) g;

    super.paintComponent(g);
    mGridManager.drawGrid(g2d);

    Color indicator = Color.WHITE;
    switch (getSuperNode().getFlavour()) {
      case CNODE: indicator = sCEDGE_COLOR; break;
      case PNODE: indicator = sPEDGE_COLOR; break;
      case FNODE: indicator = sFEDGE_COLOR; break;
      case INODE: indicator = sIEDGE_COLOR; break;
      case TNODE: indicator = sTEDGE_COLOR; break;
    }

    g2d.setColor(indicator);
    g2d.setStroke(new BasicStroke(3.0f));
    g2d.drawRect(1, 1, getSize().width - 3, getSize().height - 4);
    //scrollRectToVisible(new Rectangle(mLastMousePosition));
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

  /** For a given set of nodes that are subnodes of the same SuperNode, compute
   *  all edge views that emerge from a node outside the set and end in a node
   *  inside the set. nodes must be a subset of the current mNodeSet.
   *
   *  Only returns edges, no change in model or view
   */
  private Collection<Edge> computeIncomingEdges(Collection<Node> nodes) {
    List<Edge> result = new ArrayList<>();
    for(Edge e : getEdges()) {
      if (!nodes.contains(e.getSourceNode()) &&
          nodes.contains(e.getTargetNode())) {
        result.add(e);
      }
    }
    return result;
  }

  /** For a given set of nodes that are subnodes of the same SuperNode, compute
   *  all edge views that emerge from a node inside the set and end in a node
   *  inside the set
   *
   *  Only returns edges, no change in model or view
   */
  private List<Edge> computeInnerEdges(Collection<Node> nodes) {
    List<Edge> result = new ArrayList<>();
    for(Iterator<Edge> it = new EdgeIterator(nodes); it.hasNext();) {
      Edge e = it.next();
      if (nodes.contains(e.getSourceNode()) &&
          nodes.contains(e.getTargetNode())) {
        result.add(e);
      }
    }
    return result;
  }

  /** Removes view edge from workspace, no change in model */
  private void removeFromWorkSpace(Edge e) {
    // Free the docking points on source and target node
    e.disconnect();
    // remove from Panel
    super.remove(e);
    mObservable.deleteObserver(e);
  }

  /** Add edge to workspace, no change in model */
  private void addToWorkSpace(Edge e) {
    // Connect the docking points on source and target node
    e.connect();
    // add to from Panel
    super.add(e);
    mObservable.addObserver(e);
  }

  /** Removes node view from workspace, no change in model */
  private void removeFromWorkSpace(Node n) {
    mNodeSet.remove(n);
    mGridManager.freeGridPosition(n.getLocation());
    // remove from Panel
    super.remove(n);
    super.remove(n.getCmdBadge());
    mObservable.deleteObserver(n);
  }

  /** Add node view to workspace, no change in model */
  private void addToWorkSpace(Node n) {
    mNodeSet.add(n);
    //mGridManager.getNodeLocation(n.getLocation()); // ?
    // add to Panel
    super.add(n);
    super.add(n.getCmdBadge());
    mObservable.addObserver(n);
  }

  /** Remove comment view from workspace, AND from model */
  private void removeFromWorkSpace(Comment c) {
    mCmtSet.remove(c);
    super.remove(c);
    getSuperNode().removeComment(c.getData());
    mObservable.deleteObserver(c);
  }

  /** Add a new comment to the workspace and the model */
  private void addToWorkSpace(Comment c) {
    getSuperNode().addComment(c.getData());
    mCmtSet.add(c);
    super.add(c);
    mObservable.addObserver(c);
  }


  /** Remove the nodes in the given collection and all outgoing edges from these
   *  nodes from the view as well as the model. The model edges stay unchanged
   *  in the removed nodes.
   *
   *  This is only legal if none of the selected nodes is a start node, and
   *  no edges are pointing into the node set from the outside. To achieve
   *  this in the general case, call removeIncomingEdges(nodes) first.
   */
  private List<Edge> removeDisconnectedNodes(Iterable<Node> nodes) {
    // A list of edges starting at a node in nodes
    List<Edge> emergingEdges = new ArrayList<>();
    SuperNode current = getSuperNode();
    for (Node vn : nodes) {
      // remove nodes from the view
      removeFromWorkSpace(vn);
      // remove nodes from model
      // destructively changes the current SuperNode, which must be UNDOne
      current.removeNode(vn.getDataNode());
      // remove edges from the view
      for(Edge e : vn.getConnectedEdges()) {
        emergingEdges.add(e);
      }
      for (Edge e: emergingEdges) {
        removeFromWorkSpace(e);
      }
    }
    return emergingEdges;
  }

  /** Helper function, adjusting the positions of node views and models
   *  such that the center of the covered area is at the given position.
   */
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
    // should move the edges, too
    for (Node n : nodes) {
      n.translate(new Point(translateX, translateY));
      //n.translate does that
      //n.getDataNode().translate(translateX, translateY);
    }
  }

  // ######################################################################
  // actions for edges
  // ######################################################################

  /** Add the given nodes and edges of a disconnected subgraph of nodes as is
   *  to the view and model. This is used if re-inserted after a cut operation,
   *  or as part of an undo. The nodes and edges are just added, without further
   *  modifications.
   *
   *  TODO: The nodes should be selected after the paste.
   */
  public void pasteNodesAndEdges(Collection<Node> nodes, Collection<Edge> edges) {
    for (Node vn : nodes) {
      BasicNode n = vn.getDataNode();
      getSuperNode().addNode(n);
      addToWorkSpace(vn);
    }
    for (Edge ev: edges) {
      addToWorkSpace(ev);
    }
  }

  /** Complete the edge model, and create a new edge view from the given data */
  public Edge createEdge(AbstractEdge edge, Node source, Node target) {
    edge.setSourceNode(source.getDataNode());
    edge.setSourceUnid(source.getDataNode().getId());
    edge.setTargetNode(target.getDataNode());
    edge.setTargetUnid(target.getDataNode().getId());
    return new Edge(this, edge, source, target);
  }

  /** Remove a set of edges, view AND model */
  public Collection<Edge> removeEdges(Collection<Edge> edges) {
    // remove these from the view AND the model (their source node), and store
    // the views for an UNDO
    for (Edge ve: edges) {
      // remove edge from view
      removeFromWorkSpace(ve);
      // remove edge from model
      AbstractEdge e = ve.getDataEdge();
      // this destructively changes the source node of e, which must be
      // UNDOne
      e.getSourceNode().removeEdge(e);
    }
    return edges;
  }

  /** Add a set of edge views. Prerequisite: the node views and models the
   *  edges and their respective models exist, and were not modified in a way
   *  which interferes with, e.g., the docking points, or the positions.
   */
  public Collection<Edge> addEdges(Collection<Edge> edges) {
    // add these to the view AND the model (their source node), and store
    // the views for an UNDO
    for (Edge ve: edges) {
      // remove edge from view
      addToWorkSpace(ve);
      // remove edge from model
      AbstractEdge e = ve.getDataEdge();
      // this destructively changes the source node of e, which must be
      // UNDOne
      e.getSourceNode().addEdge(e);
    }
    return edges;
  }

  // ######################################################################
  // actions for nodes
  // ######################################################################

  /** Create a new node model and view, at the given location */
  public Node createNode(Point point, Node.Type type) {
    point = mGridManager.getNodeLocation(point);
    Position p = new Position(point.x, point.y);
    BasicNode model;
    if (type == Node.Type.BasicNode) {
      model = new BasicNode(mSceneFlowEditor.getIDManager(), p, getSuperNode());
    } else {
      model = new SuperNode(mSceneFlowEditor.getIDManager(), p, getSuperNode());
    }
    return new Node(this, model);
  }

  /** Add node n, which was just created. Avoid copy on add.
   * @param n the new node to add
   */
  public void addNewNode(Node n) {
    // upon creation, n has got the right position already
    addToWorkSpace(n);
  }

  /** Change type of node: BasicNode <-> SuperNode
   *
   * Super to Basic is only allowed if there are no inner nodes in the SuperNode
   */
  public void changeType(Node node) {
    Collection<Edge> incoming =
        computeIncomingEdges(new ArrayList<Node>(){{add(node);}});
    if (! node.changeType(mSceneFlowEditor.getIDManager(), incoming)) {
      // complain: operation not legal
      setMessageLabelText("SuperNode contains Nodes: Type change not possible");
    }
  }

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
  public Collection<Node> pasteNodesFromClipboard(Point mousePosition) {
    List<Node> nodes = mClipboard.getNodes();
    List<Edge> edges = computeInnerEdges(nodes);
    if (mClipboard.needsCopy(this)) {
      Pair<Collection<Node>, List<Edge>> toAdd =
          Node.copyGraph(this, getSuperNode(), nodes, edges);
      if (mousePosition != null) {
        // snap to grid: currently not.
        //mousePosition = mGridManager.getClosestGridPoint(mousePosition);
        translateNodeViews(toAdd.getFirst(), mousePosition);
      }
      pasteNodesAndEdges(toAdd.getFirst(), toAdd.getSecond());
      return toAdd.getFirst();
    }
    // just add nodes and edges to the view and model as is: same positions,
    // etc.
    pasteNodesAndEdges(nodes, edges);
    // now the clipboard must be set to copy: the nodes are used.
    mClipboard.forceCopy();
    return nodes;
  }

  /** Remove the nodes in the given collection.
   *  This is only legal if none of the selected nodes is a start node!
   *
   *  This must first collect all edges pointing into the node set from the
   *  outside and save them for an undo, remove them from the view and the
   *  model, and then remove the nodes and all outgoing edges from these nodes
   *  from the view and model graph.
   *
   *  If it's a cut operation, not a delete, put the nodes in the set into the
   *  clipboard.
   */
  public Triple<Collection<Edge>, Collection<Node>, Collection<Edge>>
  removeNodes(boolean isCutOperation, Collection<Node> nodes) {

    // Remove all edges that start at a node outside the given set of
    // nodes, and end inside this set
    Collection<Edge> incomingEdges = computeIncomingEdges(nodes);
    removeEdges(incomingEdges);
    List<Edge> emergingEdges = removeDisconnectedNodes(nodes);
    if (isCutOperation) {
      List<Edge> internalEdges = emergingEdges.stream()
          .filter((e) -> (nodes.contains(e.getTargetNode())))
          .collect(Collectors.toList());
      mClipboard.set(this, nodes, internalEdges);
    }
    return new Triple<>(incomingEdges, nodes, emergingEdges);
  }

  /** Copy nodes in the selected nodes set (mSelectedNodes) to the clipboard for
   *  the Copy operation (lazy copy).
   *
   *  TODO: rename once finished
   */
  public void copyNodesNew(Collection<Node> nodes) {
    mClipboard.setToCopy(this, nodes, Collections.emptyList());
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
  public void addCommentNew(Comment comment) {
    addToWorkSpace(comment);
  }

  /** Remove comment from WorkSpace and Model, for RemoveCommentAction */
  public void removeCommentNew(Comment comment) {
    removeFromWorkSpace(comment);
  }

}

  /* Assumes that the node and edge views can be perfectly reconstructed from
   * the models, and only models are handled internally

  /** Helper function, adjusting the positions of node and edge models
   *  such that the center of the covered area is at the given position.
   *
  private void translateNodes(Collection<BasicNode> nodes, Point p) {
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
    for (BasicNode n : nodes) {
      for (AbstractEdge e : n.getEdgeList()) {
        e.translate(translateX, translateY);
      }
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
