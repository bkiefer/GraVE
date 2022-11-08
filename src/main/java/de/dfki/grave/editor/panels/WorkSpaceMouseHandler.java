package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;


@SuppressWarnings("serial")
public class WorkSpaceMouseHandler implements MouseListener, MouseMotionListener {
  protected static final Logger logger = LoggerFactory.getLogger(WorkSpaceMouseHandler.class);

  // Drag & Drop support
  @SuppressWarnings("unused")
  private DropTarget mDropTarget;

  private Point mLastMousePos = null;

  private Rectangle2D.Double mAreaSelection = null;

  // Selected Elements
  protected Edge mSelectedEdge = null;
  protected Map<Comment, Comment> mSelectedComments = new IdentityHashMap<>();
  protected Map<Node,Node> mSelectedNodes = new IdentityHashMap<>();
  protected boolean mDoAreaSelection = false;

  private AbstractEdge mEdgeInProgress = null;
  private Node mEdgeSourceNode = null;

  private WorkSpace mWorkspace;

  public WorkSpaceMouseHandler(WorkSpace ws) {
    mWorkspace = ws;
    setKeyBindings();
  }

  /**
   * Implementation of the delete button. the del-key is bound to the function
   * mWorkspace.deleteSelectedItem this detects which items are selected and
   * will throw them away selection will be canceled. 1-2-2014 Bert Bierman
   * TNO
   */
  private void setKeyBindings() {
    ActionMap actionMap = mWorkspace.getActionMap();
    int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
    InputMap inputMap = mWorkspace.getInputMap(condition);
    String vkDel = "VK_DEL";

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), vkDel);
    actionMap.put(vkDel, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent actionEvt) {
        deselectAll();
        if (mSelectedEdge != null) {
          new RemoveEdgeAction(mWorkspace.getEditor(), mSelectedEdge.getDataEdge()).run();
        }

        if (!mSelectedNodes.isEmpty()) {
          new RemoveNodesAction(mWorkspace.getEditor(), mWorkspace.getSelectedNodes(), false).run();
        }

        if (!mSelectedComments.isEmpty()) {
          new RemoveCommentsAction(mWorkspace.getEditor(), mWorkspace.getSelectedComments(), false).run();
        }
      }
    });
  }

  // #########################################################################
  // Element Selection
  // #########################################################################

  Rectangle2D getAreaSelection() {
    return mAreaSelection;
  }

  Node edgeConstructionSource() {
    return mEdgeSourceNode;
  }

  /** Return true if something on the workspace is selected */
  boolean isSomethingSelected() {
    return ! mSelectedNodes.isEmpty() || mSelectedEdge != null
        || ! mSelectedComments.isEmpty();
  }

  /** */
  private void deselectAllNodes() {
    for (Node node : mSelectedNodes.keySet()) {
      node.setDeselected();
    }
    mSelectedNodes.clear();
    mWorkspace.repaint(100);
  }

  void deselectAll() {
    deselectEdge();
    deselectAllComments();
    deselectAllNodes();
  }

  private void selectComment(Comment e) {
    deselectAll();
    mSelectedComments.put(e, e);
    e.setSelected();
  }

  /** Deselect a single comment, leave all other selected comments selected */
  void deselectComment(Comment n) {
    if (mSelectedComments.containsKey(n)) {
      mSelectedComments.remove(n);
      n.setDeselected();
    }
  }

  private void deselectAllComments() {
    for (Comment c : mSelectedComments.keySet()) {
      c.setDeselected();
    }
    mSelectedComments.clear();
  }

  private void selectEdge(Edge e) {
    deselectAll();
    mSelectedEdge = e;
    e.setSelected();
  }

  private void deselectEdge() {
    if (mSelectedEdge != null) {
      mSelectedEdge.setDeselected();
      mSelectedEdge = null;
    }
  }

  void deselectEdge(Edge e) {
    if (mSelectedEdge != null && mSelectedEdge == e)
      deselectEdge();
  }

  /** Select a single node, leave all other selected nodes selected */
  private void selectNode(Node n) {
    deselectEdge();
    deselectAllComments();
    mSelectedNodes.put(n, n);
    n.setSelected();
  }

  /** Deselect a single node, leave all other selected nodes selected */
  void deselectNode(Node n) {
    if (mSelectedNodes.containsKey(n)) {
      mSelectedNodes.remove(n);
      n.setDeselected();
    }
  }

  private void selectSingleNode(Node n) {
    mDoAreaSelection = false;
    deselectAllNodes();
    selectNode(n);
  }

  /** Select all nodes intersecting the given area */
  private void selectNodesInArea(Rectangle2D area) {
    deselectAllNodes();
    for (Node node : mWorkspace.getNodes()) {
      if (node.getBounds().intersects(area)) {
        node.setSelected();
        mSelectedNodes.put(node, node);
      }
    }
  }

  void selectNodes(Collection<BasicNode> nodes) {
    deselectAllNodes();
    for (BasicNode node : nodes) {
      Node vn = mWorkspace.getView(node);
      vn.setSelected();
      mSelectedNodes.put(vn, vn);
    }
    mWorkspace.repaint(100);
  }

  Collection<Node> getSelectedNodes() {
    return mSelectedNodes.keySet();
  }

  Collection<Comment> getSelectedComments() {
    return mSelectedComments.keySet();
  }

  AbstractEdge getSelectedEdge() {
    return (mSelectedEdge == null) ? null : mSelectedEdge.getDataEdge();
  }

  /**
   * this function starts the creation of a new edge.
   */
  void startNewEdge(AbstractEdge edge, Node sourceNode) {
    // Set the current edge in process, and the current source node
    // and enter the target node selection mode --> edge creation starts
    mEdgeInProgress = edge;
    mEdgeSourceNode = sourceNode;
  }

  /**
   * At the end of an edge drag, this function is called to create a new edge.
   */
  private void createNewEdge(Point p) {
    try {
      mWorkspace.setMessageLabelText("");

      // Try to find the c on which the mouse was clicked. If we do not find
      // such a c then the mouse was clicked on the drawing area of the workspace
      // and we exit the method without creating a new edge.
      Node targetNode = mWorkspace.findNodeAtPoint(p);
      if (targetNode != null) {
        AbstractEdge e = AbstractEdge.getNewEdge(mEdgeInProgress);
        new CreateEdgeAction(mWorkspace.getEditor(), mEdgeSourceNode.getDataNode(),
            targetNode.getDataNode(), e).run();
      }

      // edge creation ends
      mEdgeInProgress = null;
      mEdgeSourceNode = null;
      deselectEdge();
    } catch (Exception e) {
      logger.error("Create edge error: {}", e);
    }
  }


  private void nodeClicked(MouseEvent event, Node clickedNode) {
    clickedNode.mouseClicked(event);
    // show context menu on single right click
    if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
      if (mSelectedNodes.size() > 1
          && mSelectedNodes.containsKey(clickedNode)) {
        mWorkspace.multipleNodesContextMenu(event);
      } else {
        selectSingleNode(clickedNode);
        clickedNode.showContextMenu();
      }
      return;
    }
    // add/remove node from selected with CTRL-Click
    if (event.getButton() == MouseEvent.BUTTON1
        && (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
      if (mSelectedNodes.containsKey(clickedNode)) {
        deselectNode(clickedNode);
      } else {
        if (! mSelectedNodes.isEmpty()) {
          selectNode(clickedNode);
        } else {
          selectSingleNode(clickedNode);
        }
      }
      return;
    }
    selectSingleNode(clickedNode);
  }

  private void edgeClicked(MouseEvent event, Edge edge) {
    if (mSelectedEdge != edge) {
      selectEdge(edge);
    }
    mSelectedEdge.mouseClicked(event);
  }

  private void commentClicked(Comment comment) {
    if (! mSelectedComments.containsKey(comment)) {
      selectComment(comment);
    }
  }

  private Comment findCommentAt(Point p) {
    // look if mouse click was on a edge
    for (Comment c : mWorkspace.getComments().values()) {
      if (c.containsPoint(p)) {
        return c;
      }
    }
    return null;
  }

  private Edge findEdgeAt(Point p) {
    // We'll try the selected edge first
    if (mSelectedEdge != null && mSelectedEdge.containsPointSelected(p)) {
      return mSelectedEdge;
    }
    // look if mouse click was on a edge
    for (Edge edge : mWorkspace.getEdges()) {
      if (edge.containsPoint(p)) {
        return edge;
      }
    }
    return null;
  }

  private boolean somethingSelected() {
    return isSomethingSelected() || mAreaSelection != null;
  }

  /**
   *
   */
  @Override
  public void mouseClicked(MouseEvent event) {
    logger.debug("Mouse clicked: {}", event);
    Point current = event.getPoint();
    if (mWorkspace.shouldIgnoreMouseInput()) {
      return;
    }

    /** End of drag for creating new edge? */
    if (mEdgeSourceNode != null) {
      createNewEdge(event.getPoint());
      return;
    }
    Edge e = findEdgeAt(event.getPoint());
    if (e != null) {
      edgeClicked(event, e);
      return;
    }
    Object o = SwingUtilities.getDeepestComponentAt(mWorkspace,
        current.x, current.y);
    if (o instanceof Node) {
      nodeClicked(event, (Node)o);
    } else if (o instanceof JTextArea) {
      Comment c;
      if ((c = findCommentAt(event.getPoint())) != null) {
        commentClicked(c);
      }
    } else {
      deselectAll();
      mAreaSelection = null;
      if (!mWorkspace.hasFocus()) {
        mWorkspace.requestFocus();
      }
    }
    if (! somethingSelected())
      mWorkspace.launchElementSelectedEvent(null);
  }


  /**
   */
  @Override
  public void mousePressed(MouseEvent event) {
    logger.debug("Mouse pressed: {}", event);
    if (mLastMousePos == null)
      mLastMousePos = event.getPoint();
    // we need to check the selected edge first, otherwise it's almost impossible
    // to grab the control point in some situations
    Edge e = findEdgeAt(event.getPoint());
    if (e != null) {
      if (mSelectedEdge != e) {
        selectEdge(e);
      }
      mSelectedEdge.mousePressed(event);
      return;
    }

    if (mAreaSelection == null) {
      // get point as possible point for area selection!
      mAreaSelection = new Rectangle2D.Double(event.getX(), event.getY(), 0, 0);
    }

    Node node;
    if ((node = mWorkspace.findNodeAtPoint(event.getPoint())) != null) {
      if (event.getButton() == MouseEvent.BUTTON1
          && (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
        mDoAreaSelection = false;
        if (!mSelectedNodes.containsKey(node)) {
          selectSingleNode(node);
        }
      }
    } else {
      // right click: global context menu for clipboard actions
      if (event.getButton() == MouseEvent.BUTTON3) {
        if (mSelectedNodes.size() > 1)
          mWorkspace.multipleNodesContextMenu(event);
        else
          mWorkspace.globalContextMenu(event);
      } else {
        mDoAreaSelection = true;
      }
    }
    if (! somethingSelected())
      mWorkspace.launchElementSelectedEvent(null);
  }

  private void straightenAllOutOfBoundEdges() {
    for (Edge edge : mWorkspace.getEdges()) {
      if (edge.outOfBounds()) { edge.straightenEdge(); }
    }
  }

  /**
   *
   */
  @Override
  public void mouseReleased(MouseEvent event) {
    logger.debug("Mouse released: {}", event);
    //launchProjectChangedEvent();
    straightenAllOutOfBoundEdges();

    if (mAreaSelection != null) {
      mAreaSelection = null;
      mWorkspace.repaint(100);
    }

    if (mEdgeSourceNode != null) {
      createNewEdge(event.getPoint());
      return;
    }

    if (! mSelectedNodes.isEmpty()) {
      mWorkspace.dragNodesFinished(event);
      mWorkspace.refresh();
      mWorkspace.revalidate();
      mWorkspace.repaint(100);
      mLastMousePos = null;
      return;
    }

    mLastMousePos = null;
    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null) {
      if (mSelectedEdge.containsPointSelected(event.getPoint())) {
        mSelectedEdge.mouseReleased(event);
        return;
      } else {
        deselectEdge();
      }
    }

    if (! somethingSelected())
      mWorkspace.launchElementSelectedEvent(null);
  }

  /**
   *
   *
   */
  @Override
  public void mouseDragged(MouseEvent event) {
    logger.debug("Mouse dragged: {}", event);
    if (mEdgeSourceNode != null) {
      createNewEdge(event.getPoint());
      return;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null && mSelectedEdge.controlPointSelected()) {
      mSelectedEdge.mouseDragged(event);
      mWorkspace.revalidate();
      mWorkspace.repaint(100);
      return;
    }

    // moving multiple nodes those which are selected before
    if (!mDoAreaSelection && ! mSelectedNodes.isEmpty()) {
      // compute movement trajectory vectors, in view coordinates
      Point currentMousePosition = event.getPoint();
      Point delta = new Point(currentMousePosition.x - mLastMousePos.x,
              currentMousePosition.y - mLastMousePos.y);
      if (mWorkspace.dragNodes(mSelectedNodes.keySet(), delta)) {
        mWorkspace.revalidate();
        mWorkspace.repaint(100);
      }
      return;
    }

    // mouse interaction has to be the selection of an area ...
    if (mSelectedEdge == null) {
      mDoAreaSelection = true;
      mAreaSelection.width = Math.abs(event.getX() - mLastMousePos.x);
      mAreaSelection.height = Math.abs(event.getY() - mLastMousePos.y);
      mAreaSelection.x = Math.min(mLastMousePos.x, event.getX());
      mAreaSelection.y = Math.min(mLastMousePos.y, event.getY());
      selectNodesInArea(mAreaSelection);
    }
  }

  /**
   *
   *
   */
  @Override
  public void mouseMoved(MouseEvent event) {
    if (mEdgeSourceNode != null) {
      // TODO: MAYBE REACTIVATE
      /*
      Point mSelectNodePoint = event.getPoint();
      Node n = findNodeAtPoint(getNodes(), mSelectNodePoint);
      if (n != null) {
         node.highlightNode();
      }
      */
      mWorkspace.repaint(100);
    }
    if (DEBUG_MOUSE_LOCATIONS) {
      mWorkspace.setMessageLabelText(String.format("%d, %d", event.getPoint().x, event.getPoint().y));
    }
    return;
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

}
