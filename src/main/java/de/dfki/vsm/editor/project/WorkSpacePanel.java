package de.dfki.vsm.editor.project;

import static de.dfki.vsm.Preferences.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.*;
import de.dfki.vsm.editor.action.*;
import de.dfki.vsm.editor.event.ClearCodeEditorEvent;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.editor.event.WorkSpaceSelectedEvent;
import de.dfki.vsm.editor.util.GridManager;
import de.dfki.vsm.editor.util.SceneFlowLayoutManager;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.CommentBadge;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.model.project.EditorProject;
import de.dfki.vsm.util.Pair;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 *
 * This is the View of the currently edited SuperNode, containing views for all
 * the SuperNodes contained nodes and the edges between them, and their
 * corresponding text badges.
 */
@SuppressWarnings("serial")
public final class WorkSpacePanel extends JPanel implements EventListener, MouseListener, MouseMotionListener {

  // The clipboard
  private final ClipBoard mClipboard = ClipBoard.getInstance();

  // Elements to draw
  private final Set<Node> mNodeSet = new HashSet<>();
  private final Set<Edge> mEdgeSet = new HashSet<>();
  private final Set<Comment> mCmtSet = new HashSet<>();

  // Flags for mouse interaction
  //private Node mSelectedNode = null;
  private Edge mSelectedEdge = null;
  private Comment mSelectedComment = null;
  private CmdBadge mSelectedCmdBadge = null;
  private Rectangle2D.Double mAreaSelection = null;
  private Rectangle2D.Double mDrawArea = null;
  private Point mLastMousePosition = new Point(0, 0);
  private boolean mDoAreaSelection = false;
  private boolean mDoAreaAction = false;
  private Set<Node> mSelectedNodes = new HashSet<>();

  // Variables for edge creation
  private AbstractEdge mEdgeInProgress = null;
  private Node mEdgeSourceNode = null;
  private Point mSelectNodePoint = null;
  private final AttributedString sEdgeCreationHint = new AttributedString("Select Target Node");

  //
  private boolean mIgnoreMouseInput = false;
  private boolean mEdgeTargetNodeReassign = false;
  private Node mReassignNode = null;

  // Snap to grid support
  public GridManager mGridManager = null;

  //
  public final Observable mObservable = new Observable();
  private final EventDispatcher mEventCaster = EventDispatcher.getInstance();

  // Drag & Drop support
  private DropTarget mDropTarget;
  private DropTargetListener mDropTargetListener;
  private int mAcceptableActions;

  // The parent SceneFlowEditor (TODO: remove)
  private final SceneFlowEditor mSceneFlowEditor;
  private final EditorProject mProject;

  /**
   *
   *
   */
  public WorkSpacePanel(SceneFlowEditor sceneFlowEditor, EditorProject project) {
    mSceneFlowEditor = sceneFlowEditor;
    mProject = project;
    mGridManager = new GridManager(this);

    // Add the mouse listeners
    addMouseMotionListener(this);
    addMouseListener(this);
    setKeyBindings();

    // Init the drag & drop support
    initDnDSupport();

    // init layout
    setLayout(new SceneFlowLayoutManager());
    setBorder(BorderFactory.createEmptyBorder());
    mDrawArea = new Rectangle2D.Double();

    // init selection
    mAreaSelection = new Rectangle2D.Double();

    // Add the element editor to the event multicaster
    mEventCaster.register(this);
    //show all elements
    showCurrentWorkSpace();
  }

  //
  public void refresh() {
    // Print some information
    // mLogger.message("Refreshing '" + this + "'");
    // mLogger.message("WorkSpace.update(" + obj + ")");
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
  private void checkChangesOnWorkspace() {
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
  private void launchProjectChangedEvent() {
    if (mProject.hasChanged()) {
      ProjectChangedEvent ev = new ProjectChangedEvent(this);
      mEventCaster.convey(ev);
    }
  }

  /** Return the SuperNode this WorkSpace currently displays */
  private SuperNode getSuperNode() {
    return mSceneFlowEditor.getActiveSuperNode();
  }

  public void clearClipBoard() {
    mClipboard.clear();
  }

  public ClipBoard getClipBoard() {
    return mClipboard;
  }

  public EditorConfig getEditorConfig() {
    return mProject.getEditorConfig();
  }

  public SceneFlowEditor getSceneFlowEditor() {
    return mSceneFlowEditor;
  }

  public CmdBadge getCmdBadge(Node id) {
    return id.getCmdBadge();
  }

  public GridManager getGridManager() {
    return mGridManager;
  }

  public Node getNode(String id) {
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

  public Set<Edge> getEdges() {
    return mEdgeSet;
  }

  /**
   *
   *
   */
  private void initDnDSupport() {
    mAcceptableActions = DnDConstants.ACTION_COPY;
    mDropTargetListener = new DropTargetAdapter() {
      @Override
      public void dragEnter(DropTargetDragEvent dtde) {
      }

      @Override
      public void dragOver(DropTargetDragEvent dtde) {
        Object data = null;
        DataFlavor flavor = null;

        try {
          try {
            flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
          } catch (ClassNotFoundException e) {
            e.printStackTrace(System.out);
          }

          data = dtde.getTransferable().getTransferData(flavor);
        } catch (java.awt.datatransfer.UnsupportedFlavorException | java.io.IOException e) {
          e.printStackTrace(System.out);
        }

        if (data instanceof Comment) {
          dtde.acceptDrag(dtde.getDropAction());

          // System.err.println("Accept Drag over");
        }

        if (data instanceof AbstractEdge) {
          Point pos = dtde.getLocation();
          dtde.acceptDrag(dtde.getDropAction());
          mSceneFlowEditor.setMessageLabelText("Drag edge on a node to select edge source");
          for (Node node : mNodeSet) {
            if (node.containsPoint(pos.x, pos.y) && !node.isEdgeAllowed((AbstractEdge)data)) {
              mSceneFlowEditor.setMessageLabelText("Edge is not allowed at this node");
            }
          }
        }
      }

      @Override
      public void dragExit(DropTargetEvent dte) {
        mSceneFlowEditor.setMessageLabelText("");
      }

      @Override
      public void drop(DropTargetDropEvent dtde) {
        mSceneFlowEditor.setMessageLabelText("");

        try {

          // Get the data of the transferable
          Object data
                  = dtde.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));

          if (data instanceof Node.Type) {
            createNode(dtde.getLocation(), (Node.Type) data);

            // revalidate();
            // repaint(100);
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else if (data instanceof AbstractEdge) {
            AbstractEdge e = AbstractEdge.getNewEdge((AbstractEdge)data);
            createNewEdgeSelectSourceNode(e, dtde.getLocation().x, dtde.getLocation().y);

            // revalidate();
            // repaint(100);
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else if (data instanceof Comment) {
            createComment(dtde.getLocation());

            // revalidate();
            // repaint(100);
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else {
            dtde.rejectDrop();
          }
        } catch (ClassNotFoundException | UnsupportedFlavorException | IOException e) {
          dtde.rejectDrop();
        }

        // Update whole editor after a drop!!!!
        EditorInstance.getInstance().refresh();
      }
    };
    mDropTarget = new DropTarget(this, mDropTargetListener);
  }

  /**
   *
   *
   */
  private void selectNodesInArea() {
    mSelectedNodes = new HashSet<>();

    for (Node node : mNodeSet) {

      // add node only if it is not a history node
      if (node.getBounds().intersects(mDrawArea)) {
        node.mSelected = true;
        mSelectedNodes.add(node);
      } else {
        node.mSelected = false;
      }
    }
  }

  /**
   *
   *
   */
  public void deselectAllNodes() {
    mSelectedNodes = new HashSet<>();

    for (Node node : mNodeSet) {
      node.mSelected = false;
    }
    //mSelectedNode = null;

    repaint(100);
  }

  /**
   *
   *
   */
  private void createNode(Point point, Node.Type type) {
    point = mGridManager.getNodeLocation(point);
    Position p = new Position(point.x, point.y);
    BasicNode model;
    if (type == Node.Type.BasicNode) {
      model = new BasicNode(mSceneFlowEditor.getIDManager(), p, getSuperNode());
    } else {
      model = new SuperNode(mSceneFlowEditor.getIDManager(), p, getSuperNode());
    }
    Node node = new Node(this, model);
    addNewNode(node);
    // repaint
    revalidate();
    repaint(100);
  }

  /**
   *
   *
   */
  private void createComment(Point point) {
    new CreateCommentAction(this, point).run();
  }

  /**
   * AbstractEdge creation
   *
   */
  private void createNewEdgeSelectSourceNode(AbstractEdge edge, int x, int y) {

    // Try to find the c on which the edge was dropped. If we do not find
    // such a c then the edge was dropped on the drawing area of the workspace
    // and we exit the method without creating a new edge.
    Node sourceNode = null;

    for (Node node : mNodeSet) {
      if (node.containsPoint(x, y)) {
        sourceNode = node;

        break;
      }
    }

    // Check if the type of this edge is allowed to be connected to the
    // source c. If the edge is not allowed then we exit the method.
    if (sourceNode == null || !sourceNode.isEdgeAllowed(edge)) {
      return;
    }

    mSelectNodePoint = new Point(x, y);

    // Set the current edge in process, the cien colegaurrent source
    // c and enter the target c selection mode --> edge creation starts
    mEdgeInProgress = edge;
    mEdgeSourceNode = sourceNode;

    // repaint(100);
    //
    mSceneFlowEditor.setMessageLabelText("Select target node or click on workspace to abort");
  }

  /**
   * At the end of an edge drag, this function is called to create a new edge.
   */
  private void createNewEdgeSelectTargetNode(int x, int y) {
    // repaint(100);
    mSceneFlowEditor.setMessageLabelText("");

    // Try to find the c on which the mouse was clicked. If we do not find
    // such a c then the mouse was clicked on the drawing area of the workspace
    // and we exit the method without creating a new edge.
    Node targetNode = null;

    for (Node n : mNodeSet) {
      if (n.containsPoint(x, y)) {
        targetNode = n;
        new CreateEdgeAction(this, mEdgeSourceNode, targetNode,
            mEdgeInProgress).run();
        break;
      }
    }
    // edge creation ends
    mEdgeInProgress = null;
    mEdgeSourceNode = null;
  }

  /**
   *
   *
   */
  public void cleanup() {

    // TODO: proper cleanup
    clearClipBoard();
    clear();
  }

  /**
   *
   *
   */
  private void clear() {

    // Reset mouse interaction
    mIgnoreMouseInput = true;

    // synchronized (ActivityEventMulticaster.getInstance().mActivityEventListenerListLock) {
    // removeEventListeners();
    mObservable.deleteObservers();

    // }
    // Clear the list of currently shown nodes and edges and
    // remove all components from the workspace. Additionally
    // clear the selected edges and nodes of the workspace.
    mNodeSet.clear();
    mEdgeSet.clear();
    mCmtSet.clear();
    removeAll();
  }

  /**
   *
   *
   */
  public void add(Comment c) {
    mCmtSet.add(c);
    super.add(c);
    mObservable.addObserver(c);
  }

  /**
   *
   *
   */
  public void remove(Comment c) {
    mCmtSet.remove(c);
    super.remove(c);
    mObservable.deleteObserver(c);
  }

  /**
   *
   *
   */
  public void addNode(Node node) {
    mNodeSet.add(node);
    super.add(node);
    mObservable.addObserver(node);
  }

  /**
   *
   *
   */
  public void remove(Node node) {
    deselectAllNodes();
    /*
    // TODO: deselect all components instead
    if (mSelectedNode != null) {
      mSelectedNode = (mSelectedNode.equals(node)) ? null : mSelectedNode;
    }
    */

    super.remove(node);
    mNodeSet.remove(node);
    mObservable.deleteObserver(node);
  }

  /**
   *
   *
   */
  public void add(Edge edge) {
    super.add(edge);
    mEdgeSet.add(edge);
    mObservable.addObserver(edge);
  }

  /**
   *
   *
   */
  public void remove(Edge edge) {
    // TODO: deselect all components instead
    if (mSelectedEdge != null) {
      mSelectedEdge = (mSelectedEdge.equals(edge)) ? null : mSelectedEdge;
    }

    mEdgeSet.remove(edge);
    super.remove(edge);
    mObservable.deleteObserver(edge);
  }

  /**
   *
   *
   */
  public void straightenAllEdges() {
    for (Edge edge : mEdgeSet) {
      edge.straightenEdge();
    }

    repaint(100);
  }

  private void straightenAllOutOfBoundEdges() {
    for (Edge edge : mEdgeSet) {
      if (edge.outOfBounds()) { edge.straightenEdge(); }
    }
  }

  /**
   *
   *
   */
  public void normalizeAllEdges() {
    for (Edge edge : mEdgeSet) {
      edge.rebuildEdgeNicely();
    }

    repaint(100);
  }

  /**
   *
   *
   */
  public void showContextMenu(MouseEvent evt, Edge edge) {
    JPopupMenu pop = new JPopupMenu();
    JMenuItem item;// = new JMenuItem("Modify");
    //ModifyEdgeAction modifyAction = new ModifyEdgeAction(edge, this);

    //item.addActionListener(modifyAction.getActionListener());
    //pop.add(item);
    item = new JMenuItem("Delete");

    RemoveEdgeAction deleteAction = new RemoveEdgeAction(this, edge);

    item.addActionListener(deleteAction.getActionListener());
    pop.add(item);
    item = new JMenuItem("Shortest Path");
    item.setEnabled(true);

    ShortestEdgeAction shortestAction = new ShortestEdgeAction(this, edge);

    item.addActionListener(shortestAction.getActionListener());
    pop.add(item);
    item = new JMenuItem("Straighten");

    StraightenEdgeAction renameAction = new StraightenEdgeAction(this, edge);

    item.addActionListener(renameAction.getActionListener());
    pop.add(item);
    item = new JMenuItem("Smart Path");

    NormalizeEdgeAction normalizeAction = new NormalizeEdgeAction(this, edge);

    item.addActionListener(normalizeAction.getActionListener());
    pop.add(item);
    pop.show(this, evt.getX(), evt.getY());
  }

  /**
   *
   *
   */
  public void showContextMenu(MouseEvent evt, Comment comment) {
    JPopupMenu pop = new JPopupMenu();
    JMenuItem item = new JMenuItem("Delete");
    RemoveCommentAction deleteAction = new RemoveCommentAction(this, comment);

    item.addActionListener(deleteAction.getActionListener());
    pop.add(item);
    pop.show(this, comment.getX() + comment.getWidth(), comment.getY());
  }

  /**
   *
   * TODO: ADD "CREATE XEDGE" FOR ALL LEGAL EDGES STARTING AT THIS NODE
   */
  public void showContextMenu(MouseEvent evt, Node node) {
    JPopupMenu pop = new JPopupMenu();
    JMenuItem item = null;
    SuperNode current = getSuperNode();

    item = new JMenuItem(
        current.isStartNode(node.getDataNode()) ? "Unset Start" : "Set Start");

    ToggleStartNodeAction toggleStartnodeAction = new ToggleStartNodeAction(
        node, this);

    item.addActionListener(toggleStartnodeAction.getActionListener());
    pop.add(item);
    pop.add(new JSeparator());

    if (!(node.getDataNode() instanceof SuperNode)) {
      item = new JMenuItem("To Supernode");

      ChangeNodeTypeAction changetypeAction = new ChangeNodeTypeAction(this,
          node);

      item.addActionListener(changetypeAction.getActionListener());
      pop.add(item);
      pop.add(new JSeparator());
    }

    // TODO: MAYBE INVERT: IF NO CMD, ADD ONE
    if (node.getDataNode().getCmd() != null) {
      item = new JMenuItem("Edit Command");

      EditCommandAction editCommandAction = new EditCommandAction(this,
          node.getCmdBadge());

      mSelectedCmdBadge = node.getCmdBadge();
      item.addActionListener(editCommandAction.getActionListener());
      pop.add(item);
      pop.add(new JSeparator());
    }

    item = new JMenuItem("Copy");

    CopyNodesAction copyAction = new CopyNodesAction(this, node);

    item.addActionListener(copyAction.getActionListener());
    pop.add(item);
    item = new JMenuItem("Cut");

    CutNodesAction cutAction = new CutNodesAction(this, node);

    item.addActionListener(cutAction.getActionListener());
    pop.add(item);
    pop.add(new JSeparator());

    item = new JMenuItem("Delete");

    RemoveNodesAction deleteAction = new RemoveNodesAction(this, node);

    item.addActionListener(deleteAction.getActionListener());
    pop.add(item);

    pop.show(this, node.getX() + node.getWidth(), node.getY());
  }

  /**
   *
   *
   */
  private void multipleNodesContextMenu(MouseEvent evt, Node node) {

    JPopupMenu pop = new JPopupMenu();
    JMenuItem item = new JMenuItem("Copy Nodes");
    CopyNodesAction copyAction = new CopyNodesAction(this, mSelectedNodes);

    item.addActionListener(copyAction.getActionListener());
    pop.add(item);
    item = new JMenuItem("Cut Nodes");

    CutNodesAction cutAction = new CutNodesAction(this, mSelectedNodes);

    item.addActionListener(cutAction.getActionListener());
    pop.add(item);
    pop.add(new JSeparator());
    item = new JMenuItem("Delete Nodes");

    RemoveNodesAction deleteAction = new RemoveNodesAction(this, mSelectedNodes);

    item.addActionListener(deleteAction.getActionListener());
    pop.add(item);
    pop.show(this, node.getX() + node.getWidth(), node.getY());
  }

  /**
   *
   *
   */
  public void copyNodes() {
    /*
    if ((mSelectedNode != null) && (mSelectedNodes.isEmpty())) {
      CopyNodesAction copyAction = new CopyNodesAction(this, mSelectedNode);

      mSceneFlowEditor.setMessageLabelText("Node copied");
      copyAction.run();
      return;
    }
    */

    if (mSelectedNodes.size() > 0) {
      CopyNodesAction copyAction = new CopyNodesAction(this, mSelectedNodes);
      String message = (mSelectedNodes.size() > 1)
              ? "Nodes copied"
              : "Node copied";

      mSceneFlowEditor.setMessageLabelText(mSelectedNodes.size() + message);
      copyAction.run();
    }
  }

  /**
   *
   *
   */
  public void cutNodes() {
    /*
    if ((mSelectedNode != null) && (mSelectedNodes.isEmpty())) {
      CutNodesAction cutAction = new CutNodesAction(this, mSelectedNode);

      mSceneFlowEditor.setMessageLabelText("Node cut");
      cutAction.run();
      return;
    }
    */

    if (mSelectedNodes.size() > 0) {
      CutNodesAction cutAction = new CutNodesAction(this, mSelectedNodes);
      String message = (mSelectedNodes.size() > 1)
              ? "Nodes cut"
              : "Node cut";

      mSceneFlowEditor.setMessageLabelText(mSelectedNodes.size() + message);
      cutAction.run();
    }
  }

  /**
   *
   * TODO: NEEDS FULL REVAMP
   */
  public void pasteNodes() {

    //PasteNodesAction pasteAction = new PasteNodesAction(this);
    //pasteAction.run();
  }

  /**
   *
   *
   */
  public void increaseWorkSpaceLevel(Node node) {

    // Reset mouse interaction
    mIgnoreMouseInput = true;

    clearCurrentWorkspace();

    SuperNode superNode = (SuperNode) node.getDataNode();

    mSceneFlowEditor.addActiveSuperNode(superNode);

    mGridManager.update();

    showCurrentWorkSpace();
  }


  /**
   *  Pop out to the specified SuperNode
   */
  public void selectNewWorkSpaceLevel(SuperNode supernode) {
    if (getSuperNode().equals(supernode)) {
      return;
    }
    SuperNode parent = mSceneFlowEditor.removeActiveSuperNode();
    while (parent != null && parent != supernode) {
      parent = mSceneFlowEditor.removeActiveSuperNode();
    }
    clearCurrentWorkspace();
    mEventCaster.convey(
        new ElementSelectedEvent(getSuperNode()));
    mGridManager.update();
    showCurrentWorkSpace();
 }

  /**
   *
   *
   */
  public void decreaseWorkSpaceLevel() {
    SuperNode parent = mSceneFlowEditor.removeActiveSuperNode();
    if (parent == null) return;

    clearCurrentWorkspace();
    // Pop the current active supernode from the list of
    // active supernodes and remove it's name from the path
    mEventCaster.convey(
        new ElementSelectedEvent(getSuperNode()));
    mGridManager.update();
    showCurrentWorkSpace();
  }

  /**
   *
   *
   */
  private void clearCurrentWorkspace() {
    mObservable.deleteObservers();

    // Clear the list of currently shown nodes and edges and
    // remove all components from the workspace. Additionally
    // clear the selected edges and nodes of the workspace.
    mNodeSet.clear();
    mEdgeSet.clear();
    mCmtSet.clear();
    removeAll();
    super.removeAll();
    mSelectedEdge = null;
    mSelectedComment = null;
    // Create a new Gridmanager for the workspace
    mGridManager.update();
    revalidate();
    mEventCaster.convey(new ClearCodeEditorEvent(this));
    repaint(100);
    // TODO: Refresh here!
  }

  //reset components on works space
  private void showCurrentWorkSpace() {
    // Show the nodes and supernodes on the workspace.
    // Show the edges on the workspace.
    // Show the variables on workspace.
    showNodesOnWorkSpace();
    showEdgesOnWorkSpace();
    revalidate();
    repaint(100);
  }

  /** Add views for all the (sub)node models in this workspace's SuperNode */
  public void showNodesOnWorkSpace() {
    for (BasicNode n : getSuperNode()) {
      Point p = mGridManager.getNodeLocation(
          new Point(n.getPosition().getXPos(), n.getPosition().getYPos()));

      n.setPosition(new Position(p.x, p.y));
      Node guiNode = new Node(this, n);
      mNodeSet.add(guiNode);
      add(guiNode);
      add(guiNode.getCmdBadge());
    }

    ArrayList<CommentBadge> commentList = getSuperNode().getCommentList();

    for (CommentBadge n : commentList) {
      add(new Comment(this, n));
    }
  }

  /** Add views for all edges between nodes in this workspace */
  public void showEdgesOnWorkSpace() {
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

  /**
   * Changed to public method by M. Fallas due to issue 126
   * https://github.com/SceneMaker/VisualSceneMaker/issues/126
   */
  public void deselectAllOtherComponents(JComponent comp) {
    if ((!comp.equals(mSelectedComment)) && (mSelectedComment != null)) {
      mSelectedComment.setDeselected();
      mSelectedComment = null;
    }

    if ((!comp.equals(mSelectedCmdBadge)) && (mSelectedCmdBadge != null)) {
      mSelectedCmdBadge.endEditMode();
      mSelectedCmdBadge = null;
    }

    /*
    if ((!comp.equals(mSelectedNode)) && (mSelectedNode != null)) {
      mSelectedNode.setDeselected();
      mSelectedNode = null;
    }
    */

    if ((!comp.equals(mSelectedEdge)) && (mSelectedEdge != null)) {
      mSelectedEdge.setDeselected();
      mSelectedEdge = null;
    }
  }

  /**
   *
   *
   */
  @Override
  public void mouseClicked(MouseEvent event) {
    mLastMousePosition = event.getPoint();
    launchWorkSpaceSelectedEvent();

    if (mEdgeSourceNode != null) {
      try {
        createNewEdgeSelectTargetNode(event.getX(), event.getY());
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

      return;
    }

    // handle mouse click for area selections
    if (!mSelectedNodes.isEmpty()) {
      if (mSelectedNodes.size() > 1) {
        mDoAreaAction = false;

        Node clickedNode = null;

        for (Node node : mSelectedNodes) {
          if (node.containsPoint(event.getX(), event.getY())) {
            clickedNode = node;
            mDoAreaAction = true;
          }
        }

        if (!mDoAreaAction) {
          mDoAreaSelection = false;
          deselectAllNodes();
        } else {

          // show contect menu
          if (mSelectedNodes.size() > 1) {
            if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
              multipleNodesContextMenu(event, clickedNode);
            }

            return;
          } else {
            // mSelectedNode = mSelectedNodes.iterator().next();
            mDoAreaSelection = false;
            deselectAllNodes();
          }
        }
      }
    } else {
      mDoAreaAction = false;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null) {
      if (mSelectedEdge.containsPoint(new Point(event.getX(), event.getY()))) {

        // System.out.println(mSelectedEdge.getType() + " clicked - (re) selected");
        mSelectedEdge.mouseClicked(event);

        return;
      } else {

        // System.out.println(mSelectedEdge.getType() + " not clicked - deselected");
        mSelectedEdge.setDeselected();
        mSelectedEdge = null;
      }
    }

    // if there is a specific selected c use it - much faster than checking all nodes
    if (mSelectedNodes.size() == 1) {
      if (mSelectedNodes.iterator().next().containsPoint(event.getX(), event.getY())) {

        // DEBUG System.out.println(mSelectedNode.getDataNode().getName() + " clicked - (re) selected");
        // tell c that it has been clicked
        mSelectedNodes.iterator().next().mouseClicked(event);

        return;
      }
    }

    // if there is a specific selected comment use it - much faster than checking all nodes
    if (mSelectedComment != null) {
      if (mSelectedComment.containsPoint(event.getPoint())) {

        // DEBUG System.out.println(mSelectedNode.getDataNode().getName() + " clicked - (re) selected");
        // tell c that it has been clicked
        mSelectedComment.mouseClicked(event);

        return;
      } else {

        // System.out.println(mSelectedNode.getDataNode().getName() + " not clicked - deselected");
        mSelectedComment.setDeselected();
        mSelectedComment = null;
      }
    }

    if (!mIgnoreMouseInput) {
      boolean entityClicked = false;

      // look if mouse click was on a c
      for (Node node : mNodeSet) {
        if (node.containsPoint(event.getX(), event.getY())) {
          mSelectedNodes.add(node);

          // DEBUG System.out.println(mSelectedNode.getDataNode().getName() + " clicked - found and selected");
          node.mouseClicked(event);
          entityClicked = true;

          return;
        }
      }

      // look if mouse click was on a edge
      for (Edge edge : mEdgeSet) {
        if (edge.containsPoint(new Point(event.getX(), event.getY()))) {
          mSelectedEdge = edge;

          // System.out.println(mSelectedEdge.getType() + " clicked - found and selected");
          mSelectedEdge.mouseClicked(event);
          entityClicked = true;

          return;
        }
      }

      // look if mouse click was on a comment
      for (Comment comment : mCmtSet) {
        if (comment.containsPoint(event.getPoint())) {
          mSelectedComment = comment;
          mSelectedComment.mouseClicked(event);
          entityClicked = true;

          return;
        }
      }

      if (!entityClicked) {
        mEventCaster.convey(
            new ElementSelectedEvent(getSuperNode()));
      }
    } else {

      // System.out.println("mouse input ignored");
      mIgnoreMouseInput = false;
    }

    deselectAllNodes();
    if (!hasFocus()) {
      requestFocus();
    }
  }

  private void launchWorkSpaceSelectedEvent() {
    WorkSpaceSelectedEvent ev = new WorkSpaceSelectedEvent(this);

    mEventCaster.convey(ev);
  }

  /**
   * Eventually show "Paste" menu item, when clicking on workspace
   */
  public void globalContextMenu(MouseEvent event) {
    int eventX = event.getX();
    int eventY = event.getY();
    JPopupMenu pop = new JPopupMenu();
    //PASTE NODES MENU ITEM
    if (! mClipboard.isEmpty()) {
      JMenuItem itemPasteNodes = new JMenuItem("Paste");
      PasteNodesAction pasteAction = new PasteNodesAction(this, event.getPoint());
      itemPasteNodes.addActionListener(pasteAction.getActionListener());
      pop.add(itemPasteNodes);
      pop.show(this, eventX, eventY);
    }
  }

  /**
   *
   *
   */
  @Override
  public void mousePressed(MouseEvent event) {
    mLastMousePosition = event.getPoint();

    // System.out.println("mouse pressed");
    if (mEdgeSourceNode != null) {
      try {
        createNewEdgeSelectTargetNode(event.getX(), event.getY());
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

      return;
    }

    // handle mouse pressed for area selections
    if (!mSelectedNodes.isEmpty()) {
      if (mSelectedNodes.size() > 1) {
        mDoAreaAction = false;

        Node clickedNode = null;

        for (Node node : mSelectedNodes) {
          if (node.containsPoint(event.getX(), event.getY())) {
            clickedNode = node;
            mDoAreaAction = true;
          }
        }

        if (!mDoAreaAction) {
          mDoAreaSelection = false;
          deselectAllNodes();
        } else {

          // show contect menu
          if (mSelectedNodes.size() > 1) {
            if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
              multipleNodesContextMenu(event, clickedNode);
            }

            return;
          } else {
            //mSelectedNode = (Node) (mSelectedNodes.toArray())[0];
            mDoAreaSelection = false;
            deselectAllNodes();
          }
        }
      }
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null) {
      if (mSelectedEdge.containsPoint(new Point(event.getX(), event.getY()))) {

        // System.out.println(mSelectedEdge.getType() + " pressed - (re) selected");
        mSelectedEdge.mousePressed(event);

        return;
      } else {

        // System.out.println(mSelectedEdge.getType() + " not pressed - deselected");
        mSelectedEdge.setDeselected();
        mSelectedEdge = null;
      }
    }

    // if there is a specific selected c use it - much faster than checking all nodes
    //if (mSelectedNode != null) {
      if (mSelectedNodes.size() == 1) {
        Node mSelectedNode = mSelectedNodes.iterator().next();
        if (mSelectedNode.containsPoint(event.getX(), event.getY())) {

          // System.out.println(mSelectedNode.getDataNode().getName() + " pressed");
          // tell c that it has been clicked
          mSelectedNode.mousePressed(event);

          return;
        } else {

          // System.out.println(mSelectedNode.getDataNode().getName() + " not pressed - deselected");
          mSelectedNode.setDeselected();
          mSelectedNode = null;
        }
      }
    //}

    // if there is a specific selected comment use it - much faster than checking all nodes
    if (mSelectedComment != null) {
      if (mSelectedComment.containsPoint(event.getPoint())) {

        // System.out.println(mSelectedNode.getDataNode().getName() + " pressed");
        // tell c that it has been clicked
        mSelectedComment.mousePressed(event);

        return;
      } else {

        // System.out.println(mSelectedNode.getDataNode().getName() + " not pressed - deselected");
        mSelectedComment.setDeselected();
        mSelectedComment = null;
      }
    }


    //
    // Fall back cases - lookup
    //
    // look if mouse pressed (without a click) was on a c
    for (Node node : mNodeSet) {
      if (node.containsPoint(event.getX(), event.getY())) {
        deselectAllNodes();
        mSelectedNodes.add(node);
        this.requestFocusInWindow();
        deselectAllOtherComponents(node);

        // System.out.println(mSelectedNode.getDataNode().getName() + " pressed - found and pressed");
        node.mousePressed(event);

        return;
      }
    }

    // look if mouse click was on a edge
    for (Edge edge : mEdgeSet) {
      if (edge.containsPoint(new Point(event.getX(), event.getY()))) {
        mSelectedEdge = edge;
        deselectAllOtherComponents(mSelectedEdge);
        this.requestFocusInWindow();
        // System.out.println(mSelectedEdge.getType() + " pressed - found and selected");
        mSelectedEdge.mousePressed(event);

        return;
      }
    }

    // look if mouse click was on a comment
    for (Comment comment : mCmtSet) {
      if (comment.containsPoint(event.getPoint())) {
        mSelectedComment = comment;
        deselectAllOtherComponents(mSelectedComment);
        mSelectedComment.mousePressed(event);

        return;
      }
    }

    deselectAllNodes();

    // enable global context menu for clipbaord actions
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      if (! mClipboard.isEmpty()) {
        globalContextMenu(event);
      }
      return;
    }

    // get point as possible point for area selection!
    mAreaSelection.x = event.getX();
    mAreaSelection.width = event.getX();
    mAreaSelection.y = event.getY();
    mAreaSelection.height = event.getY();
  }

  /**
   *
   *
   */
  @Override
  public void mouseReleased(MouseEvent event) {
    launchProjectChangedEvent();
    straightenAllOutOfBoundEdges();

    if (mDoAreaSelection) {
      mDoAreaSelection = false;
      repaint(100);

      return;
    }

    if (mEdgeSourceNode != null) {
      try {
        createNewEdgeSelectTargetNode(event.getX(), event.getY());
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

      return;
    }

    if (mDoAreaAction) {
      for (Node node : mSelectedNodes) {
        Point p = node.getLocation();

        // check location of each c
        if (node.mDragged) {
          node.resetLocation(mGridManager.getNodeLocation(p));
        }

        // update workspace area - if dragged beyond current borders
        // sWorkSpaceDrawArea = getSize();
        node.mouseReleased(event);
        repaint(100);
      }

      // mGridManager.normalizeGridWeight();
      return;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null) {
      if (mSelectedEdge.containsPoint(event.getPoint())) {

        // if the edge can be connected to an other node, do so!
        if (mEdgeTargetNodeReassign) {
          new DeflectEdgeAction(this, mSelectedEdge, mReassignNode, event.getPoint()).run();
          mEdgeTargetNodeReassign = false;
          mReassignNode = null;

          return;
        }

        mSelectedEdge.mouseReleased(event);

        // mGridManager.normalizeGridWeight();
        return;
      } else {

        // System.out.println(mSelectedEdge.getType() + " not released - deselected");
        mSelectedEdge.setDeselected();
        mSelectedEdge = null;
      }
    }

    // if there is a specific selected c use it - much faster than checking all nodes
    if (mSelectedNodes.size() == 1) {
      Node mSelectedNode = mSelectedNodes.iterator().next();

      if (mSelectedNode.mDragged) {
        Point p = mSelectedNode.getLocation();

        mSelectedNode.resetLocation(mGridManager.getNodeLocation(p));

        // Update sceneflow with new node position
        //mSelectedNode.getDataNode().setPosition(new Position(mSelectedNode.getX(), mSelectedNode.getY()));

        // update workspace area - if dragged beyond current borders
        // sWorkSpaceDrawArea = getSize();
      }
      //mSceneFlowEditor.setViewPosition(new Point(event.getX(), event.getY()));
      mSelectedNode.mouseReleased(event);
      refresh();
      revalidate();
      repaint(100);

//            if (mSelectedNode.containsPoint(event.getX(), event.getY())) {
//
//                // System.out.println(mSelectedNode.getDataNode().getName() + " released");
//                // tell c that it has been clicked
//                // let the gridmanager do a repositioning if c has been dragged
//                if (mSelectedNode.mDragged) {
//                    Point p = mSelectedNode.getLocation();
//
//                    mSelectedNode.resetLocation(mGridManager.getNodeLocation(p));
//
//                    // Update sceneflow with new node position
//                    mSelectedNode.getDataNode().getGraphics().setPosition(mSelectedNode.getX(), mSelectedNode.getY());
//
//                    // update workspace area - if dragged beyond current borders
//                    // sWorkSpaceDrawArea = getSize();
//                }
//
//                mSelectedNode.mouseReleased(event);
//                revalidate();
//                repaint(100);
//
//                // mGridManager.normalizeGridWeight();
//                return;
//            } else {
//
//                // System.out.println(mSelectedNode.getDataNode().getName() + " not released - deselected");
//
//
//                mSelectedNode.setDeselected();
//                mSelectedNode = null;
//            }
    }

    // if there is a specific selected comment use it - much faster than checking all nodes
    if (mSelectedComment != null) {
      if (mSelectedComment.containsPoint(event.getPoint())) {
        mSelectedComment.mouseReleased(event);
        revalidate();
        repaint(100);

        return;
      } else {

        // System.out.println(mSelectedNode.getDataNode().getName() + " not released - deselected");
        // mSelectedComment.setDeselected();
        mSelectedComment = null;
      }

      // finally do a repaint
      repaint(100);
    }
  }

  /**
   *
   *
   */
  @Override
  public void mouseDragged(MouseEvent event) {
    if (mEdgeSourceNode != null) {
      try {
        createNewEdgeSelectTargetNode(event.getX(), event.getY());
        mSelectedEdge = null;
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

      checkChangesOnWorkspace();

      return;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null) {

      // DEBUG //System.out.println("EDGE SELECTED!");
      if (mSelectedEdge.mCP1Selected || mSelectedEdge.mCP2Selected || mSelectedEdge.mCSPSelected
              || mSelectedEdge.mCEPSelected) {
        if (mSelectedEdge.mCSPSelected) {

          // look if mouse pressed (without a click) was on a c
          for (Node node : mNodeSet) {
            if (node != mSelectedEdge.getSourceNode()) {
              if (node.containsPoint(event.getX(), event.getY())) {
                mReassignNode = node;
                break;
              }
            }
          }
        }

        if (mSelectedEdge.mCEPSelected) {

          // look if mouse pressed (without a click) was on a c
          mEdgeTargetNodeReassign = false;

          for (Node node : mNodeSet) {
            if (node != mSelectedEdge.getTargetNode()) {
              if (node.containsPoint(event.getX(), event.getY())) {
                mReassignNode = node;
                mEdgeTargetNodeReassign = true;

                break;
              }
            }
          }
        }

        mSelectedEdge.mouseDragged(event);
        revalidate();
        repaint(100);
        checkChangesOnWorkspace();

        return;
      }
    }

    // moving multiple nodes those which are selected before
    if (mDoAreaAction) {

      // compute movement trajectory vectors
      Point currentMousePosition = event.getPoint();
      Point mouseMoveVector = new Point(currentMousePosition.x - mLastMousePosition.x,
              currentMousePosition.y - mLastMousePosition.y);

      mLastMousePosition = new Point(currentMousePosition.x, currentMousePosition.y);
      dragNodes(mSelectedNodes, event, mouseMoveVector);    // BUG
      checkChangesOnWorkspace();

      return;
    }

    // if there is a specific selected c use it - much faster than checking all nodes
    if (mSelectedNodes.size() == 1) {
      Node mSelectedNode = mSelectedNodes.iterator().next();
      if (mSelectedNode.mPressed) {

        // compute movement trajectory vectors
        Point currentMousePosition = event.getPoint();
        Point mouseMoveVector = new Point(currentMousePosition.x - mLastMousePosition.x,
                currentMousePosition.y - mLastMousePosition.y);

        mLastMousePosition = new Point(currentMousePosition.x, currentMousePosition.y);
        dragNode(mSelectedNode, event, mouseMoveVector);
        checkChangesOnWorkspace();

        return;
      } else {
        deselectAllNodes();
      }
    }

    // if there is a specific selected comment use it
    if (mSelectedComment != null) {
      Point currentMousePosition = event.getPoint();

      // if not dragged, but once resized, leave it by resized and vice versa, leave it by dragged
      if (!mSelectedComment.mDragged) {
        mSelectedComment.mResizing = mSelectedComment.isResizingAreaSelected(currentMousePosition);
      }

      if (mSelectedComment.mPressed) {

        // compute movement trajectory vectors
        Point mouseMoveVector = new Point(currentMousePosition.x - mLastMousePosition.x,
                currentMousePosition.y - mLastMousePosition.y);

        mLastMousePosition = new Point(currentMousePosition.x, currentMousePosition.y);

        if (mSelectedComment.mResizing) {
          resizeComment(mSelectedComment, event, mouseMoveVector);
        } else {
          dragComment(mSelectedComment, event, mouseMoveVector);
        }

        checkChangesOnWorkspace();

        return;
      } else {

        // System.out.println(mSelectedNode.getDataNode().getName() + " not dragged - deselected");
        mSelectedComment = null;
      }
    }

    // mouse interaction has to be the selection of an area ...
    mDoAreaSelection = true;
    mAreaSelection.width = event.getX() - mAreaSelection.x;
    mAreaSelection.height = event.getY() - mAreaSelection.y;
    selectNodesInArea();    // comment this to avoid bug but eliminate selection
    repaint(100);
  }

  /**
   *
   *
   */
  @Override
  public void mouseEntered(MouseEvent event) {
  }

  /**
   *
   *
   */
  @Override
  public void mouseExited(MouseEvent event) {
  }

  /**
   *
   *
   */
  @Override
  public void mouseMoved(MouseEvent event) {
    if (mEdgeSourceNode != null) {
      mSelectNodePoint = event.getPoint();

      for (Node node : mNodeSet) {
        if (node.containsPoint(mSelectNodePoint.x, mSelectNodePoint.y)) {
          // TODO: MAYBE REACTIVATE
          // node.highlightNode();

          break;
        }
      }

      repaint(100);

      return;
    }
  }

  /**
   *
   *
   */
  private void dragNode(Node node, MouseEvent event, Point moveVec) {
    boolean validDragging = true;
    Point nodePos = node.getLocation();

    if (((nodePos.x + moveVec.x) <= 0) || ((nodePos.y + moveVec.y) <= 0)) {
      validDragging = false;
    }

    if (validDragging) {
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

  /**
   *
   *
   */
  private void dragComment(Comment comment, MouseEvent event, Point moveVec) {
    boolean validDragging = true;
    Point commentPos = comment.getLocation();

    if (((commentPos.x + moveVec.x) <= 0) || ((commentPos.y + moveVec.y) <= 0)) {

      // stop dragging, if upper and left border would be passed!
      validDragging = false;
    }

    if (validDragging) {
      comment.updateLocation(moveVec);

      if ((event.getModifiersEx() == 1024)) {
        comment.mDragged = true;
      }

      revalidate();
      repaint(100);
    }
  }

  /**
   *
   *
   */
  private void resizeComment(Comment comment, MouseEvent event, Point moveVec) {
    comment.resize(moveVec);

    if ((event.getModifiersEx() == 1024)) {
      comment.mDragged = true;
    }

    revalidate();
    repaint(100);
  }

  /**
   *
   *
   */
  private void dragNodes(Set<Node> nodes, MouseEvent event, Point moveVec) {
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

  /**
   * Implementation of the delete button. the del-key is bound to the function
   * mWorkspace.deleteSelectedItem this detects which items are selected and
   * will throw them away selection will be canceled. 1-2-2014 Bert Bierman
   * TNO
   */
  private void setKeyBindings() {
    ActionMap actionMap = getActionMap();
    int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
    InputMap inputMap = getInputMap(condition);
    String vkDel = "VK_DEL";

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), vkDel);
    actionMap.put(vkDel, new KeyAction(this));
  }

  /**
   *
   *
   */
  public void deleteSelectedItem() {
    if (mSelectedEdge != null) {
      removeEdge();
    }

    if (!mSelectedNodes.isEmpty()) {
      removeNodes();
    }

    //EditorInstance.getInstance().refresh();
  }

  /**
   *
   *
   */
  private void removeEdge() {
    mSelectedEdge.mIsSelected = false;

    RemoveEdgeAction deleteAction = new RemoveEdgeAction(this, mSelectedEdge);

    deleteAction.run();
  }

  /**
   *
   *
   */
  private void removeNodes() {
    for (Node node : mNodeSet) {
      node.mSelected = false;
    }

    RemoveNodesAction deleteAction = new RemoveNodesAction(this, mSelectedNodes);

    deleteAction.run();
  }

  /**
   *
   *
   */
  @Override
  public void paintComponent(Graphics g) {

    // mLogger.message("Drawing Workspace");
    Graphics2D g2d = (Graphics2D) g;

    if (mEdgeSourceNode != null) {
      setBackground(Color.LIGHT_GRAY);
    } else {
      setBackground(Color.WHITE);
    }

    if (mSelectedEdge != null) {
      if (mSelectedEdge.isInEditMode()) {
        setBackground(Color.LIGHT_GRAY);
      }
    }

    super.paintComponent(g);
    mGridManager.drawGrid(g2d);

    if (mDoAreaSelection) {
      mDrawArea.x = (mAreaSelection.width > 0)
              ? mAreaSelection.x
              : mAreaSelection.x + mAreaSelection.width;
      mDrawArea.y = (mAreaSelection.height > 0)
              ? mAreaSelection.y
              : mAreaSelection.y + mAreaSelection.height;
      mDrawArea.width = Math.abs(mAreaSelection.width);
      mDrawArea.height = Math.abs(mAreaSelection.height);
      g2d.draw(mDrawArea);
    }

    Color indicator = Color.WHITE;

    switch (getSuperNode().getFlavour()) {
      case CNODE:
        indicator = sCEDGE_COLOR;

        break;

      case PNODE:
        indicator = sPEDGE_COLOR;

        break;

      case FNODE:
        indicator = sFEDGE_COLOR;

        break;

      case INODE:
        indicator = sIEDGE_COLOR;

        break;

      case TNODE:
        indicator = sTEDGE_COLOR;

        break;
    }

    g2d.setColor(indicator);
    g2d.setStroke(new BasicStroke(3.0f));
    g2d.drawRect(1, 1, getSize().width - 3, getSize().height - 4);
    //scrollRectToVisible(new Rectangle(mLastMousePosition));

    // draw line between source c and current mouse position
    if (mEdgeSourceNode != null) {
      if (mSelectNodePoint != null) {
        Point sourceNodeCenter = mEdgeSourceNode.getCenterPoint();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawLine(sourceNodeCenter.x, sourceNodeCenter.y, mSelectNodePoint.x, mSelectNodePoint.y);

        TextLayout textLayout = new TextLayout(sEdgeCreationHint.getIterator(), g2d.getFontRenderContext());
        int height = (int) (textLayout.getAscent() + textLayout.getDescent()
                + textLayout.getLeading());
        int width = (int) textLayout.getVisibleAdvance();

        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawLine(mSelectNodePoint.x, mSelectNodePoint.y, mSelectNodePoint.x,
                mSelectNodePoint.y - (getEditorConfig().sNODEHEIGHT / 2) + (height / 2));
        g2d.setColor(new Color(100, 100, 100, 100));
        g2d.fillRoundRect(mSelectNodePoint.x - (width / 2) - 5,
                mSelectNodePoint.y - (getEditorConfig().sNODEHEIGHT / 2) - (height / 2) - 6, width + 10,
                height + 5, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(mSelectNodePoint.x - (width / 2) - 5,
                mSelectNodePoint.y - (getEditorConfig().sNODEHEIGHT / 2) - (height / 2) - 6, width + 10,
                height + 5, 5, 5);
        g2d.drawString(sEdgeCreationHint.getIterator(), mSelectNodePoint.x - (width / 2),
                mSelectNodePoint.y - (getEditorConfig().sNODEHEIGHT / 2) + 1);
      }
    }
  }

  /**
   *
   *
   */
  private class KeyAction extends AbstractAction {

    WorkSpacePanel mWorkspace = null;

    public KeyAction(WorkSpacePanel workspace) {
      mWorkspace = workspace;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvt) {

      // System.out.println(actionEvt.getActionCommand() + " pressed");
      mWorkspace.deleteSelectedItem();
    }
  }

  /**
   *
   *
   */
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
   *  inside the set
   */
  private List<Edge> computeIncomingEdges(Collection<Node> nodes) {
    return mEdgeSet.stream().filter(
        (e) -> (!nodes.contains(e.getSourceNode()) &&
            nodes.contains(e.getTargetNode()))).collect(Collectors.toList());
  }

  /** For a given set of nodes that are subnodes of the same SuperNode, compute
   *  all edge views that emerge from a node inside the set and end in a node
   *  inside the set
   */
  private List<Edge> computeInnerEdges(Collection<Node> nodes) {
    return mEdgeSet.stream().filter(
        (e) -> (nodes.contains(e.getSourceNode()) &&
            nodes.contains(e.getTargetNode()))).collect(Collectors.toList());
  }

  private void removeFromWorkSpace(Edge e) {
    mEdgeSet.remove(e);
    // Free the docking points on source and target node
    e.disconnect();
    // remove from Panel
    super.remove(e);
    mObservable.deleteObserver(e);
  }

  private void addToWorkSpace(Edge e) {
    mEdgeSet.add(e);
    // Connect the docking points on source and target node
    e.connect();
    // add to from Panel
    super.add(e);
    mObservable.addObserver(e);
  }

  private void removeFromWorkSpace(Node n) {
    mNodeSet.remove(n);
    mGridManager.freeGridPosition(n.getLocation());
    // remove from Panel
    super.remove(n);
    mObservable.deleteObserver(n);
  }

  private void addToWorkSpace(Node n) {
    mNodeSet.add(n);
    mGridManager.getNodeLocation(n.getLocation());
    // add to Panel
    super.add(n);
    mObservable.addObserver(n);
  }

  private interface Undoable {}

  private class CombinedUndoable implements Undoable {
    public List<Undoable> undoables = new ArrayList<>(2);

    public CombinedUndoable(Undoable ... us) {
      undoables.addAll(Arrays.asList(us));
    }

    public void add(Undoable u) {
      undoables.add(u);
    }
  }

  private class UndoRemoveNodes implements Undoable {
    /** The edges were removed from the view, the nodes from view and the
     *  SuperNode
     */
    public UndoRemoveNodes(WorkSpacePanel wsp,
        Collection<Edge> view, Collection<Node> nodes) {
    }
  }
  private class UndoRemoveEdges implements Undoable {
    /** The edges were removed from the view and the model */
    public UndoRemoveEdges(WorkSpacePanel wsp, Collection<Edge> full){

    }
  }

  private class UndoAddNodesEdges implements Undoable {
    /** The edges were removed from the view and the model */
    public UndoAddNodesEdges(WorkSpacePanel wsp,
        Collection<Node> nodes, Collection<Edge> edges){

    }
  }


  Deque<Undoable> undoList = new ArrayDeque<>();

  /** Remove a set of edges, in an undoable way */
  private Collection<Edge> removeEdges(Collection<Edge> edges) {
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
  private Collection<Edge> addEdges(Collection<Edge> edges) {
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


  /** Remove and return all edges that start at a node outside the given set of
   *  nodes, and end inside this set
   */
  private List<Edge> removeIncomingEdges(Collection<Node> nodes) {
    List<Edge> incomingEdges = computeIncomingEdges(nodes);
    removeEdges(incomingEdges);
    return incomingEdges;
  }

  /** Remove the nodes in the given collection.
   *  This is only legal if none of the selected nodes is a start node, and
   *  no edges are pointing into the node set from the outside. To achieve
   *  this in the general case, call removeIncomingEdges(nodes) first.
   *
   *  remove the nodes and all outgoing edges from these nodes from the view
   *  and model graph.
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
        removeFromWorkSpace(e);
      }
    }
    return emergingEdges;
  }

  /** Helper function, adjusting the positions of node views and models
   *  such that the center of the covered area is at the given position.
   */
  private void translateNodes(Collection<Node> nodes, Point p) {
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
    int translateX = p.x < (maxX - minX) / 2 ? 0 : p.x - (maxX + minX) / 2;
    int translateY = p.y < (maxY - minY) / 2 ? 0 : p.y - (maxY + minY) / 2;
    // should move the edges, too
    for (Node n : nodes) {
      n.translate(new Point(translateX, translateY));
      n.getDataNode().translate(translateX, translateY);
    }
  }

  // ######################################################################
  // Paired methods for action/undo/redo for nodes and edges
  // ######################################################################

  /** Add the given nodes and edges as is to the view and model. This is used
   *  if re-inserted after a cut operation, or as part of an undo. If this is
   *  used for undo, we must take care that this does not add another undoable
   *  item.
   *
   *  The nodes should be selected after paste (not undo).
   *
   *  TODO: rename once finished
   */
  private void add(Collection<Node> nodes, Collection<Edge> edges) {
    for (Node vn : nodes) {
      BasicNode n = vn.getDataNode();
      getSuperNode().addNode(n);
      addToWorkSpace(vn);
    }
  }

  /** Remove the nodes in the selected nodes set (mSelectedNodes).
   *  This is only legal if none of the selected nodes is a start node!
   *
   *  This must first collect all edges pointing into the node set from the
   *  outside and save them for an undo, remove them from the view and the
   *  model, and then remove the nodes from the view and model graph.
   *
   *  If it's a cut operation, not a delete, put the nodes in the set into the
   *  clipboard.
   *
  public void remove(Collection<Node> nodes, Collection<Edge> edges) {
    // collect set of edges between nodes inside and outside the selected set
    Collection<Edge> internalEdges = edges == null ? removeNodes(nodes): edges;
  }*/

  public void changeType(Node node) {
    Collection<Edge> incoming =
        computeIncomingEdges(new ArrayList<Node>(){{add(node);}});
    if (! node.changeType(mSceneFlowEditor.getIDManager(), incoming)) {
      // complain: operation not legal
    }
  }

  // ######################################################################
  // Undoable actions for nodes and edges
  // ######################################################################

  /** Add edge e, which was just created */
  private void addNewEdge(Edge e) {
    pasteNodes(Collections.emptyList(), new ArrayList<Edge>() {{ add(e); }});
  }


  /** Remove selected edge
   *
   *  As undo structure, we can use the same as for cut nodes: only put the
   *  removed edge into the edge set, and the node set is empty.
   */
  public void removeEdge(Edge e) {
    removeEdges(new ArrayList<Edge>() {{ add(e); }});
  }

  /** Add node n, which was just created. Avoid copy on add.
   * @param n the new node to add
   */
  public void addNewNode(Node n) {
    // upon creation, n has got the right position already
    pasteNodes(new ArrayList<Node>() {{ add(n); }}, Collections.emptyList());
  }

  /** Add the given nodes and edges as is to the view and model. This is used
   *  if re-inserted after a cut operation, or as part of an undo. If this is
   *  used for undo, we must take care that this does not add another undoable
   *  item.
   *
   *  The nodes should be selected after the paste.
   *
   *  TODO: rename once finished
   */
  private void pasteNodes(Collection<Node> nodes, Collection<Edge> edges) {
    add(nodes, edges);
    undoList.add(new UndoAddNodesEdges(this, nodes, edges));
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
  public void pasteNodesFromClipboard(Point mousePosition) {
    List<Node> nodes = mClipboard.getNodes();
    List<Edge> edges = computeInnerEdges(nodes);
    if (mClipboard.needsCopy(this)) {
      Pair<Collection<Node>, List<Edge>> toAdd =
          Node.copyGraph(this, getSuperNode(), nodes, edges);
      if (mousePosition != null)
        translateNodes(toAdd.getFirst(), mousePosition);
      pasteNodes(toAdd.getFirst(), toAdd.getSecond());
    } else {
      // just add nodes and edges to the view and model as is: same positions,
      // etc.
      pasteNodes(nodes, edges);
      // now the clipboard must be set to copy: the nodes are used.
      mClipboard.forceCopy();
    }
  }

  /** Remove the nodes in the selected nodes set (mSelectedNodes).
   *  This is only legal if none of the selected nodes is a start node!
   *
   *  This must first collect all edges pointing into the node set from the
   *  outside and save them for an undo, remove them from the view and the
   *  model, and then remove the nodes from the view and model graph.
   *
   *  If it's a cut operation, not a delete, put the nodes in the set into the
   *  clipboard.
   */
  public void removeNodes(boolean isCutOperation, Collection<Node> nodes) {
    // collect set of edges between nodes inside and outside the selected set
    /** Remove the nodes in the given collection.
     *  This is only legal if none of the selected nodes is a start node!
     *
     *  This must first collect all edges pointing into the node set from the
     *  outside and save them for an undo, remove them from the view and the
     *  model, and then remove the nodes and all outgoing edges from these nodes
     *  from the view and model graph.
     */
    // collect set of edges between nodes inside and outside the selected set
    List<Edge> incomingEdges = removeIncomingEdges(nodes);
    List<Edge> emergingEdges = removeDisconnectedNodes(nodes);
    CombinedUndoable u = new CombinedUndoable(
        new UndoRemoveNodes(this, emergingEdges, nodes),
        new UndoRemoveEdges(this, incomingEdges));
    undoList.push(u);
    if (isCutOperation) {
      List<Edge> internalEdges = emergingEdges.stream()
          .filter((e) -> (nodes.contains(e.getTargetNode())))
          .collect(Collectors.toList());
      mClipboard.set(this, nodes, internalEdges);
    }
  }

  /** Copy nodes in the selected nodes set (mSelectedNodes) to the clipboard for
   *  the Copy operation (lazy copy).
   *
   *  TODO: rename once finished
   */
  public void copyNodesNew(Collection<Node> nodes) {
    mClipboard.setToCopy(this, nodes, Collections.emptyList());
  }

  /** Cut selected nodes: remove from graph and put into the clipboard.
   *
   *  TODO: rename once finished
   */
  public void cutNodesNew(Collection<Node> nodes) {
    removeNodes(true, nodes);
  }
}
