package de.dfki.grave.editor.panels;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedString;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.project.EditorProject;

@SuppressWarnings("serial")
public class WorkSpacePanel extends WorkSpace implements MouseListener, MouseMotionListener {

  private static final Logger logger = LoggerFactory.getLogger(WorkSpacePanel.class);

  // Drag & Drop support
  @SuppressWarnings("unused")
  private DropTarget mDropTarget;
  private DropTargetListener mDropTargetListener;
  private int mAcceptableActions;

  private Point mLastMousePos = new Point(0, 0);
  private Edge mSelectedEdge = null;
  private Comment mSelectedComment = null;
  //private CmdBadge mSelectedCmdBadge = null;
  private Rectangle2D.Double mAreaSelection = null;
  private boolean mDoAreaSelection = false;
  protected Set<Node> mSelectedNodes = new HashSet<>();

  private AbstractEdge mEdgeInProgress = null;
  private Node mEdgeSourceNode = null;
  private Point mSelectNodePoint = null;
  private final AttributedString sEdgeCreationHint = new AttributedString("Select Target Node");

  //
  private boolean mIgnoreMouseInput = false;


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
          new RemoveEdgeAction(WorkSpacePanel.this, mSelectedEdge).run();
        }

        if (!mSelectedNodes.isEmpty()) {
          new RemoveNodesAction(WorkSpacePanel.this, mSelectedNodes, false).run();
        }

        if (mSelectedComment != null) {
          new RemoveCommentAction(WorkSpacePanel.this, mSelectedComment).run();
        }
        deselectAll();
      }
    });
  }


  /**
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
          logger.error("DragNDrop Error: {}", e);
        }

        if (data instanceof Comment) {
          dtde.acceptDrag(dtde.getDropAction());
        }

        if (data instanceof AbstractEdge) {
          Point pos = dtde.getLocation();
          dtde.acceptDrag(dtde.getDropAction());
          setMessageLabelText("Drag edge on a node to select edge source");
          Node node = findNodeAtPoint(pos);
          if (node != null && ! node.isEdgeAllowed((AbstractEdge)data)) {
            setMessageLabelText("Edge is not allowed at this node");
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
          Object data = dtde.getTransferable().getTransferData(
              new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));

          if (data instanceof BasicNode) {
            BasicNode n = (BasicNode) data;
            new CreateNodeAction(WorkSpacePanel.this, dtde.getLocation(), n).run();
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else if (data instanceof AbstractEdge) {
            AbstractEdge e = AbstractEdge.getNewEdge((AbstractEdge)data);
            startNewEdge(e, dtde.getLocation());
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else if (data instanceof Comment) {
            new CreateCommentAction(WorkSpacePanel.this, dtde.getLocation()).run();
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
  private void deselectAllNodes() {
    for (Node node : mSelectedNodes) {
      node.setDeselected();
    }
    mSelectedNodes.clear();
    repaint(100);
  }

  private void deselectAll() {
    deselectEdge();
    deselectComment();
    deselectAllNodes();
  }

  private void selectComment(Comment e) {
    deselectAll();
    mSelectedComment = e;
    e.setSelected();
  }

  private void deselectComment() {
    if (mSelectedComment != null) {
      mSelectedComment.setDeselected();
      mSelectedComment = null;
    }
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

  /** Select a single node, leave all other selected nodes selected */
  private void selectNode(Node n) {
    deselectEdge();
    deselectComment();
    mSelectedNodes.add(n);
    n.setSelected();
  }

  /** Deselect a single node, leave all other selected nodes selected */
  private void deselectNode(Node n) {
    mSelectedNodes.remove(n);
    n.setDeselected();
  }

  private void selectSingleNode(Node n) {
    mDoAreaSelection = false;
    deselectAllNodes();
    selectNode(n);
  }

  /** Select all nodes intersecting the given area */
  private void selectNodesInArea(Rectangle2D area) {
    deselectAllNodes();
    for (Node node : getNodes()) {
      // add node only if it is not a history node
      if (node.getBounds().intersects(area)) {
        node.setSelected();
        mSelectedNodes.add(node);
      }
    }
  }

  public void selectNodes(Collection<Node> nodes) {
    for (Node node : mSelectedNodes) {
      node.setDeselected();
    }
    mSelectedNodes.clear();
    for (Node node : nodes) {
      node.setSelected();
      mSelectedNodes.add(node);
    }
    repaint(100);
  }

  protected void clearCurrentWorkspace() {
    super.clearCurrentWorkspace();
    deselectAll();
  }

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

  /** To provide the functionality to the global menu bar:
   *  Paste nodes in the upper left corner
   */
  public void pasteNodesFromClipboard() {
    PasteNodesAction action = new PasteNodesAction(this, new Point(0, 0));
    action.run();
  }

  public static void addItem(JPopupMenu m, String name, EditorAction a) {
    JMenuItem item = new JMenuItem(name);
    item.addActionListener(a.getActionListener());
    m.add(item);
  }

  /** Show the context menu if multiple nodes are selected */
  private void multipleNodesContextMenu(MouseEvent evt, Node node) {
    JPopupMenu pop = new JPopupMenu();
    addItem(pop, "Copy Nodes", new CopyNodesAction(this, mSelectedNodes));
    addItem(pop, "Cut Nodes", new RemoveNodesAction(this, mSelectedNodes, true));
    pop.add(new JSeparator());
    addItem(pop, "Delete Nodes", new RemoveNodesAction(this, mSelectedNodes, false));
    pop.show(this, node.getX() + node.getWidth(), node.getY());
  }

  /**
   * this function starts the creation of a new edge.
   */
  private void startNewEdge(AbstractEdge edge, Point p) {
    Node sourceNode = findNodeAtPoint(p);

    // Check if the type of this edge is allowed to be connected to the
    // source node. If the edge is not allowed then we exit the method.
    if (sourceNode == null || !sourceNode.isEdgeAllowed(edge)) {
      return;
    }

    mSelectNodePoint = new Point(p);

    // Set the current edge in process, and the current source node
    // and enter the target node selection mode --> edge creation starts
    mEdgeInProgress = edge;
    mEdgeSourceNode = sourceNode;

    setMessageLabelText("Select target node or click on workspace to abort");
  }

  /**
   * At the end of an edge drag, this function is called to create a new edge.
   */
  private void createNewEdge(Point p) {
    try {
      setMessageLabelText("");

      // Try to find the c on which the mouse was clicked. If we do not find
      // such a c then the mouse was clicked on the drawing area of the workspace
      // and we exit the method without creating a new edge.
      Node targetNode = findNodeAtPoint(p);
      if (targetNode != null) {
        new CreateEdgeAction(this, mEdgeSourceNode, targetNode, mEdgeInProgress).run();
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
    // enter supernode, if it has been double clicked
    if (!clickedNode.isBasic()
        && event.getButton() == MouseEvent.BUTTON1
        && event.getClickCount() == 2) {
      increaseWorkSpaceLevel(clickedNode);
      return;
    }
    // show context menu on right click
    if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
      if (mSelectedNodes.size() > 1
          && mSelectedNodes.contains(clickedNode)) {
        multipleNodesContextMenu(event, clickedNode);
      } else {
        selectSingleNode(clickedNode);
        clickedNode.showContextMenu(this);
      }
      return;
    }
    // add/remove node from selected with CTRL-Click
    if (event.getButton() == MouseEvent.BUTTON1
        && (event.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
      if (mSelectedNodes.contains(clickedNode)) {
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
    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge == null || mSelectedEdge != edge) {
      selectEdge(edge);
    }
    mSelectedEdge.mouseClicked(event);
  }

  private void commentClicked(MouseEvent event, Comment comment) {
    // if there is a specific selected comment use it - much faster than checking all nodes
    if (mSelectedComment == null || mSelectedComment != comment) {
      selectComment(comment);
    }
    // tell c that it has been clicked
    mSelectedComment.mouseClicked(event);
  }

  private Edge findEdgeAt(Point p) {
    // We'll try the selected edge first
    if (mSelectedEdge != null && mSelectedEdge.containsPoint(p)) {
      return mSelectedEdge;
    }
    // look if mouse click was on a edge
    for (Edge edge : getEdges()) {
      if (edge.containsPoint(p)) {
        return edge;
      }
    }
    return null;
  }

  /**
   *
   */
  @Override
  public void mouseClicked(MouseEvent event) {
    mLastMousePos = event.getPoint();
    launchWorkSpaceSelectedEvent();
    if (mIgnoreMouseInput) {
      mIgnoreMouseInput = false;
      return;
    }

    // TODO: undisputed
    if (mEdgeSourceNode != null) {
      createNewEdge(event.getPoint());
      return;
    }
    Edge e = findEdgeAt(event.getPoint());
    if (e != null) {
      edgeClicked(event, e);
      return;
    }
    Object o = SwingUtilities.getDeepestComponentAt(
        this, mLastMousePos.x, mLastMousePos.y);
    if (o instanceof Node) {
      nodeClicked(event, (Node)o);
    } else if (o instanceof JComponent
        && ((JComponent)o).getParent() instanceof Comment) {
      commentClicked(event, (Comment)((JComponent)o).getParent());
    } else {
      deselectAll();
      mAreaSelection = null;
      if (!hasFocus()) {
        requestFocus();
      }
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
        WorkSpacePanel.super.clearCurrentWorkspace();
        WorkSpacePanel.this.showCurrentWorkSpace();
      }
    });
    pop.add(refresh);
    pop.show(this, eventX, eventY);
  }

  /**
   */
  @Override
  public void mousePressed(MouseEvent event) {
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

    Object o = SwingUtilities.getDeepestComponentAt(this,
        mLastMousePos.x, mLastMousePos.y);

    if (mAreaSelection == null) {
      // get point as possible point for area selection!
      mAreaSelection = new Rectangle2D.Double(event.getX(), event.getY(), 0, 0);
    }

    if (o instanceof Node) {
      if (event.getButton() == MouseEvent.BUTTON1
          && (event.getModifiers() & MouseEvent.CTRL_MASK) == 0) {
        Node node = (Node)o;
        mDoAreaSelection = false;
        if (!mSelectedNodes.contains(node)) {
          selectSingleNode(node);
        }
      }
    } else if (o instanceof JComponent
        && ((JComponent)o).getParent() instanceof Comment) {
      Comment c = (Comment)((JComponent)o).getParent();
      if (mSelectedComment == null && c != mSelectedComment) {
        selectComment(c);
      }
      mSelectedComment.mousePressed(event);
    } else {
      // right click: global context menu for clipboard actions
      if ((event.getButton() == MouseEvent.BUTTON3)
          && (event.getClickCount() == 1)) {
        globalContextMenu(event);
      } else {
        mDoAreaSelection = true;
      }
    }
  }

  private void straightenAllOutOfBoundEdges() {
    for (Edge edge : getEdges()) {
      if (edge.outOfBounds()) { edge.straightenEdge(); }
    }
  }

  /**
   *
   */
  @Override
  public void mouseReleased(MouseEvent event) {
    launchProjectChangedEvent();
    straightenAllOutOfBoundEdges();

    if (mAreaSelection != null) {
      mAreaSelection = null;
      repaint(100);
    }

    if (mEdgeSourceNode != null) {
      createNewEdge(event.getPoint());
      return;
    }

    if (! mSelectedNodes.isEmpty()) {
      dragNodesFinished(event);
      refresh();
      revalidate();
      repaint(100);
      return;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null) {
      if (mSelectedEdge.containsPoint(event.getPoint())) {
        mSelectedEdge.mouseReleased(event);

        // mGridManager.normalizeGridWeight();
        return;
      } else {
        deselectEdge();
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
        deselectComment();
      }

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
      createNewEdge(event.getPoint());
      checkChangesOnWorkspace();
      return;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null && mSelectedEdge.controlPointSelected()) {
      mSelectedEdge.mouseDragged(event);
      revalidate();
      repaint(100);
      checkChangesOnWorkspace();
      return;
    }

    // moving multiple nodes those which are selected before
    if (!mDoAreaSelection && ! mSelectedNodes.isEmpty()) {
      // compute movement trajectory vectors
      Point currentMousePosition = event.getPoint();
      Point mouseMoveVector = new Point(currentMousePosition.x - mLastMousePos.x,
              currentMousePosition.y - mLastMousePos.y);

      mLastMousePos = new Point(currentMousePosition.x, currentMousePosition.y);
      if (dragNodes(mSelectedNodes, mouseMoveVector)) {
        revalidate();
        repaint(100);
      }
      checkChangesOnWorkspace();

      return;
    }

    // if there is a specific selected comment use it
    if (mSelectedComment != null) {
      if (mSelectedComment.mPressed) {
        mSelectedComment.mouseDragged(event);
        revalidate();
        repaint(100);
        checkChangesOnWorkspace();
        return;
      } else {
        // System.out.println(mSelectedNode.getDataNode().getName() + " not dragged - deselected");
        deselectComment();
      }
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
      mSelectNodePoint = event.getPoint();
      // TODO: MAYBE REACTIVATE
      /*
      Node n = findNodeAtPoint(getNodes(), mSelectNodePoint);
      if (n != null) {
         node.highlightNode();
      }
      */
      repaint(100);
    }
    return;
  }

  /** Paint for things specific to mouse selection */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    if (mEdgeSourceNode != null) {
      setBackground(Color.LIGHT_GRAY);
    } else {
      setBackground(Color.WHITE);
    }

    if (mAreaSelection != null) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setStroke(new BasicStroke(3.0f));
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.draw(mAreaSelection);
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
    /* Debugging: check boundaries of all components on workspace */
    g2d.setColor(Color.pink);
    g2d.setStroke(new BasicStroke(0.5f));
    for (int i = 0 ; i <  this.getComponentCount(); ++i) {
      Rectangle r = getComponent(i).getBounds();
      g2d.drawRect(r.x, r.y, r.width, r.height);
    }
    /**/
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

}
