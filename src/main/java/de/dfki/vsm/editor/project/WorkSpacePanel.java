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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.editor.*;
import de.dfki.vsm.editor.action.*;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.project.EditorProject;

@SuppressWarnings("serial")
public class WorkSpacePanel extends WorkSpace implements MouseListener, MouseMotionListener {

  private static final Logger logger = LoggerFactory.getLogger("logger");

  // Drag & Drop support
  private DropTarget mDropTarget;
  private DropTargetListener mDropTargetListener;
  private int mAcceptableActions;

  private Point mLastMousePosition = new Point(0, 0);
  // Flags for mouse interaction
  //private Node mSelectedNode = null;
  private Edge mSelectedEdge = null;
  private Comment mSelectedComment = null;
  private CmdBadge mSelectedCmdBadge = null;
  private Rectangle2D.Double mAreaSelection = null;
  private boolean mDoAreaAction = false;
  private boolean mDoAreaSelection = false;
  private Rectangle2D.Double mDrawArea = null;
  protected Set<Node> mSelectedNodes = new HashSet<>();

  private AbstractEdge mEdgeInProgress = null;
  private Node mEdgeSourceNode = null;
  private Point mSelectNodePoint = null;
  private final AttributedString sEdgeCreationHint = new AttributedString("Select Target Node");

  //
  private boolean mIgnoreMouseInput = false;
  private boolean mEdgeTargetNodeReassign = false;
  private Node mReassignNode = null;


  public WorkSpacePanel(SceneFlowEditor sceneFlowEditor, EditorProject project) {
    super(sceneFlowEditor, project);
    // Add the mouse listeners
    addMouseMotionListener(this);
    addMouseListener(this);
    setKeyBindings();
    // Init the drag & drop support
    initDnDSupport();
    mSelectedEdge = null;
    mSelectedComment = null;
    mDrawArea = new Rectangle2D.Double();
  }

  /** Inhibit mouse actions for a while */
  @Override
  protected void ignoreMouseInput() {
    mIgnoreMouseInput = true;
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
    actionMap.put(vkDel, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent actionEvt) {
        if (mSelectedEdge != null) {
          new RemoveEdgesAction(WorkSpacePanel.this, mSelectedEdge).run();
        }

        if (!mSelectedNodes.isEmpty()) {
          new RemoveNodesAction(WorkSpacePanel.this, mSelectedNodes, false).run();
          deselectAllNodes();
        }

        if (mSelectedComment != null) {
          new RemoveCommentAction(WorkSpacePanel.this, mSelectedComment).run();
        }
        deselectAll();
      }
    });
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
        try {
          DataFlavor flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
          data = dtde.getTransferable().getTransferData(flavor);
        } catch (ClassNotFoundException | UnsupportedFlavorException | IOException e) {
          e.printStackTrace(System.out);
        }

        if (data instanceof Comment) {
          dtde.acceptDrag(dtde.getDropAction());

          // System.err.println("Accept Drag over");
        }

        if (data instanceof AbstractEdge) {
          Point pos = dtde.getLocation();
          dtde.acceptDrag(dtde.getDropAction());
          setMessageLabelText("Drag edge on a node to select edge source");
          for (Node node : getNodes()) {
            if (node.containsPoint(pos.x, pos.y) && !node.isEdgeAllowed((AbstractEdge)data)) {
              setMessageLabelText("Edge is not allowed at this node");
            }
          }
        }
      }

      @Override
      public void dragExit(DropTargetEvent dte) {
        setMessageLabelText("");
      }

      @Override
      public void drop(DropTargetDropEvent dtde) {
        setMessageLabelText("");

        try {

          // Get the data of the transferable
          Object data
                  = dtde.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));

          if (data instanceof Node.Type) {
            new CreateNodeAction(WorkSpacePanel.this, dtde.getLocation(), (Node.Type) data).run();

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
            new CreateCommentAction(WorkSpacePanel.this, dtde.getLocation()).run();

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

  /** */
  public void deselectAllNodes() {
    for (Node node : mSelectedNodes) {
      node.setDeselected();
    }
    mSelectedNodes.clear();
    repaint(100);
  }

  /**
   * Changed to public method by M. Fallas due to issue 126
   * https://github.com/SceneMaker/VisualSceneMaker/issues/126
   */
  public void deselectAllOtherComponents(JComponent comp) {
    if ((mSelectedComment != null) && (!mSelectedComment.equals(comp))) {
      mSelectedComment.setDeselected();
      mSelectedComment = null;
    }

    if ((mSelectedCmdBadge != null) && (!mSelectedCmdBadge.equals(comp)) ) {
      mSelectedCmdBadge.endEditMode();
      mSelectedCmdBadge = null;
    }

    for (Iterator<Node> it = mSelectedNodes.iterator(); it.hasNext();) {
      Node n = it.next();
      if (n != comp) {
        it.remove();
        n.setDeselected();
      }
    }


    if ((mSelectedEdge != null) && (!mSelectedEdge.equals(comp))) {
      mSelectedEdge.setDeselected();
      mSelectedEdge = null;
    }
  }

  /**
   * Changed to public method by M. Fallas due to issue 126
   * https://github.com/SceneMaker/VisualSceneMaker/issues/126
   */
  private void deselectAll() {
    deselectAllOtherComponents(null);
  }

  protected void clearCurrentWorkspace() {
    super.clearCurrentWorkspace();
    deselectAll();
  }

  // stop dragging, if upp

  /** To provide the functionality to the global menu bar */
  public void copySelectedNodes() {
    if (mSelectedNodes.size() == 0) return;
    CopyNodesAction action = new CopyNodesAction(this, mSelectedNodes);
    String message = (mSelectedNodes.size() > 1) ? "Nodes copied" : "Node copied";
    setMessageLabelText(mSelectedNodes.size() + message);
    action.run();

  }

  /** To provide the functionality to the global menu bar */
  public void cutSelectedNodes() {
    if (mSelectedNodes.size() == 0) return;
    RemoveNodesAction action = new RemoveNodesAction(this, mSelectedNodes, true);
    String message = (mSelectedNodes.size() > 1) ? "Nodes cut" : "Node cut";
    setMessageLabelText(mSelectedNodes.size() + message);
    action.run();
  }

  /** Paste nodes in the upper left corner */
  public void pasteNodesFromClipboard() {
    PasteNodesAction action = new PasteNodesAction(this, new Point(0, 0));
    action.run();
  }

  /**
   *
   *
   */
  private void multipleNodesContextMenu(MouseEvent evt, Node node) {

    JPopupMenu pop = new JPopupMenu();

    JMenuItem item = new JMenuItem("Copy Nodes");
    EditorAction action = new CopyNodesAction(this, mSelectedNodes);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    item = new JMenuItem("Cut Nodes");
    action = new RemoveNodesAction(this, mSelectedNodes, true);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    pop.add(new JSeparator());

    item = new JMenuItem("Delete Nodes");
    action = new RemoveNodesAction(this, mSelectedNodes, false);
    item.addActionListener(action.getActionListener());
    pop.add(item);

    pop.show(this, node.getX() + node.getWidth(), node.getY());
  }


  private void createNewEdgeSelectSourceNode(AbstractEdge edge, int x, int y) {
    Node sourceNode = findNode(x, y);

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

    setMessageLabelText("Select target node or click on workspace to abort");
  }

  /**
   * At the end of an edge drag, this function is called to create a new edge.
   */
  private void createNewEdgeSelectTargetNode(int x, int y) {
    setMessageLabelText("");

    // Try to find the c on which the mouse was clicked. If we do not find
    // such a c then the mouse was clicked on the drawing area of the workspace
    // and we exit the method without creating a new edge.
    Node targetNode = findNode(x, y);
    if (targetNode != null) {
      new CreateEdgeAction(this, mEdgeSourceNode, targetNode,
          mEdgeInProgress).run();
    }

    // edge creation ends
    mEdgeInProgress = null;
    mEdgeSourceNode = null;
  }

  /**
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
      //if (mSelectedNodes.size() > 1) {
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
      //}
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
      for (Node node : getNodes()) {
        if (node.containsPoint(event.getX(), event.getY())) {
          mSelectedNodes.add(node);

          // DEBUG System.out.println(mSelectedNode.getDataNode().getName() + " clicked - found and selected");
          node.mouseClicked(event);
          entityClicked = true;

          return;
        }
      }

      // look if mouse click was on a edge
      for (Edge edge : getEdges()) {
        if (edge.containsPoint(new Point(event.getX(), event.getY()))) {
          mSelectedEdge = edge;

          // System.out.println(mSelectedEdge.getType() + " clicked - found and selected");
          mSelectedEdge.mouseClicked(event);
          entityClicked = true;

          return;
        }
      }

      // look if mouse click was on a comment
      for (Comment comment : getComments()) {
        if (comment.containsPoint(event.getPoint())) {
          mSelectedComment = comment;
          mSelectedComment.mouseClicked(event);
          entityClicked = true;

          return;
        }
      }

      if (!entityClicked) {
        launchElementSelectedEvent(null);
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


  /**
   * Eventually show "Paste" menu item, when clicking on workspace
   */
  public void globalContextMenu(MouseEvent event) {
    int eventX = event.getX();
    int eventY = event.getY();
    JPopupMenu pop = new JPopupMenu();
    // paste nodes menu item
    if (! mClipboard.isEmpty()) {
      JMenuItem itemPasteNodes = new JMenuItem("Paste");
      PasteNodesAction pasteAction = new PasteNodesAction(this, event.getPoint());
      itemPasteNodes.addActionListener(pasteAction.getActionListener());
      pop.add(itemPasteNodes);
    }
    // refresh menu item
    JMenuItem refresh = new JMenuItem("Refresh");
    refresh.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent e) {
        revalidate();
        repaint(100);
      }
    });
    pop.add(refresh);
    pop.show(this, eventX, eventY);
  }

    /**
   *
   *
   */
  @Override
  public void mousePressed(MouseEvent event) {
    mLastMousePosition = event.getPoint();
    if (mAreaSelection == null) {
      // get point as possible point for area selection!
      mAreaSelection = new Rectangle2D.Double(event.getX(), event.getY(), 0, 0);
    }

    // System.out.println("mouse pressed");
    if (mEdgeSourceNode != null) {
      try {
        createNewEdgeSelectTargetNode(event.getX(), event.getY());
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }

      return;
    }

    /** handle mouse pressed for area selections */
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
      } else {
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
    /**

    //}**/

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
    for (Node node : getNodes()) {
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
    for (Edge edge : getEdges()) {
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
    for (Comment comment : getComments()) {
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
      globalContextMenu(event);
      return;
    }
    mDoAreaSelection = true;
  }

  private void straightenAllOutOfBoundEdges() {
    for (Edge edge : getEdges()) {
      if (edge.outOfBounds()) { edge.straightenEdge(); }
    }
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
      mAreaSelection = null;
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

    if (! mSelectedNodes.isEmpty()) {
      dragNodesFinished(mSelectedNodes, event);
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
  private Set<Node> selectNodesInArea(Rectangle2D area) {
    mSelectedNodes.clear();

    for (Node node : getNodes()) {
      // add node only if it is not a history node
      if (node.getBounds().intersects(area)) {
        node.mSelected = true;
        mSelectedNodes.add(node);
      } else {
        node.mSelected = false;
      }
    }
    return mSelectedNodes;
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
      if (mSelectedEdge.mCP1Selected || mSelectedEdge.mCP2Selected
          || mSelectedEdge.mCSPSelected || mSelectedEdge.mCEPSelected) {
        if (mSelectedEdge.mCSPSelected) {

          // look if mouse pressed (without a click) was on a c
          for (Node node : getNodes()) {
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

          for (Node node : getNodes()) {
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
    if (!mDoAreaSelection && ! mSelectedNodes.isEmpty()) {

      // compute movement trajectory vectors
      Point currentMousePosition = event.getPoint();
      Point mouseMoveVector = new Point(currentMousePosition.x - mLastMousePosition.x,
              currentMousePosition.y - mLastMousePosition.y);

      mLastMousePosition = new Point(currentMousePosition.x, currentMousePosition.y);
      dragNodes(mSelectedNodes, event, mouseMoveVector);    // BUG
      checkChangesOnWorkspace();

      return;
    }

    /*
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
    }*/

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
    mAreaSelection.width = Math.abs(event.getX() - mLastMousePosition.x);
    mAreaSelection.height = Math.abs(event.getY() - mLastMousePosition.y);
    mAreaSelection.x = Math.min(mLastMousePosition.x, event.getX());
    mAreaSelection.y = Math.min(mLastMousePosition.y, event.getY());
    selectNodesInArea(mAreaSelection);
    logger.error("Area:" + mAreaSelection);
    logger.error("Nodes:" + mSelectedNodes.size());
    // comment this to avoid bug but eliminate selection (??)
    //repaint(100);
  }

  private Node findNode(int x, int y) {
    for (Node node : getNodes()) {
      if (node.containsPoint(x, y)) {
        return node;
      }
    }
    return null;
  }

  /**
   *
   *
   */
  @Override
  public void mouseMoved(MouseEvent event) {
    if (mEdgeSourceNode != null) {
      mSelectNodePoint = event.getPoint();
      // TODO: MAYBE REACTIVATE
      /*
      Node n = findNode(mSelectNodePoint.x, mSelectNodePoint.y);
      if (n != null) {
         node.highlightNode();
      }
      */
    }

    repaint(100);

    return;
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    // TODO: show tool tip?
  }

  @Override
  public void mouseExited(MouseEvent e) {
    // TODO: hide tool tip?
  }

  /** Paint for things specific to mouse selection */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
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

    if (mDoAreaSelection) {
      g2d.setColor(Color.blue);
      g2d.setPaint(Color.BLACK);
      g2d.setBackground(Color.LIGHT_GRAY);
      g2d.setStroke(new BasicStroke(3.0f));
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

    // draw line between source node and current mouse position
    if (mEdgeSourceNode != null) {
      mSelectNodePoint = getMousePosition();
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

}
