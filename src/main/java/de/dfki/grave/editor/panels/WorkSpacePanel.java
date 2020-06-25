package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedString;

import javax.swing.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

@SuppressWarnings("serial")
public class WorkSpacePanel extends WorkSpace implements MouseListener, MouseMotionListener {

  // Drag & Drop support
  @SuppressWarnings("unused")
  private DropTarget mDropTarget;
  private DropTargetListener mDropTargetListener;
  private int mAcceptableActions;

  private Point mLastMousePos = null;

  private Rectangle2D.Double mAreaSelection = null;

  private AbstractEdge mEdgeInProgress = null;
  private Node mEdgeSourceNode = null;
  private Point mSelectNodePoint = null;
  private final AttributedString sEdgeCreationHint = new AttributedString("Select Target Node");

  
  public WorkSpacePanel(ProjectEditor editor) {
    super(editor);
    // Add the mouse listeners
    addMouseMotionListener(this);
    addMouseListener(this);
    setKeyBindings();
    // Init the drag & drop support
    initDnDSupport();
    mSelectedEdge = null;
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
        deselectAll();
        if (mSelectedEdge != null) {
          new RemoveEdgeAction(getEditor(), mSelectedEdge.getDataEdge()).run();
        }

        if (!mSelectedNodes.isEmpty()) {
          new RemoveNodesAction(getEditor(), getSelectedNodes(), false).run();
        }

        if (!mSelectedComments.isEmpty()) {
          new RemoveCommentsAction(getEditor(), getSelectedComments(), false).run();
        }
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
            BasicNode newModel = createNode(dtde.getLocation(), n);
            if (newModel != null) {
              new CreateNodeAction(getEditor(), newModel).run();
            } else {
              setMessageLabelText("First (Start) node must be basic node");
            }
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else if (data instanceof AbstractEdge) {
            AbstractEdge e = AbstractEdge.getNewEdge((AbstractEdge)data);
            startNewEdge(e, dtde.getLocation());
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else if (data instanceof Comment) {
            new CreateCommentAction(getEditor(), 
                toModelPos(dtde.getLocation())).run();
            dtde.acceptDrop(mAcceptableActions);
            dtde.getDropTargetContext().dropComplete(true);
          } else {
            dtde.rejectDrop();
          }
        } catch (ClassNotFoundException | UnsupportedFlavorException | IOException e) {
          dtde.rejectDrop();
        }

        // Update whole editor after a drop!!!!
        AppFrame.getInstance().refresh();
      }
    };
    mDropTarget = new DropTarget(this, mDropTargetListener);
  }


  protected void clearCurrentWorkspace() {
    deselectAll();
    super.clearCurrentWorkspace();
  }

  /** Add an item with name and action a to the menu m */
  public static void addItem(JPopupMenu m, String name, ActionListener a) {
    JMenuItem item = new JMenuItem(name);
    item.addActionListener(a);
    m.add(item);
  }

  /** Show the context menu if multiple nodes are selected */
  private void multipleNodesContextMenu(MouseEvent evt, Node node) {
    JPopupMenu pop = new JPopupMenu();
    // copy is not undoable
    addItem(pop, "Copy Nodes", 
        new CopyNodesAction(getEditor(), getSelectedNodes()));
    addItem(pop, "Cut Nodes",
        new RemoveNodesAction(getEditor(), getSelectedNodes(), true));
    pop.add(new JSeparator());
    addItem(pop, "Delete Nodes",
        new RemoveNodesAction(getEditor(), getSelectedNodes(), false));
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
        new CreateEdgeAction(getEditor(), mEdgeSourceNode.getDataNode(),
            targetNode.getDataNode(), mEdgeInProgress).run();
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
      getEditor().increaseWorkSpaceLevel(clickedNode);
      return;
    }
    // show context menu on single right click
    if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() == 1) {
      if (mSelectedNodes.size() > 1
          && mSelectedNodes.containsKey(clickedNode)) {
        multipleNodesContextMenu(event, clickedNode);
      } else {
        selectSingleNode(clickedNode);
        clickedNode.showContextMenu(this);
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
    for (Comment c : getComments().values()) {
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
    for (Edge edge : getEdges()) {
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
    Point current = event.getPoint();
    if (shouldIgnoreMouseInput()) {
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
    Object o = SwingUtilities.getDeepestComponentAt(
        this, current.x, current.y);
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
      if (!hasFocus()) {
        requestFocus();
      }
    }
    if (! somethingSelected()) launchElementSelectedEvent(null);
  }


  /**
   * Eventually show "Paste" menu item, when clicking on workspace
   */
  private void globalContextMenu(MouseEvent event) {
    int eventX = event.getX();
    int eventY = event.getY();
    JPopupMenu pop = new JPopupMenu();
    // paste nodes menu item
    if (! getEditor().mClipboard.isEmpty()) {
      JMenuItem itemPasteNodes = new JMenuItem("Paste");
      PasteNodesAction pasteAction = new PasteNodesAction(getEditor(), 
          toModelPos(event.getPoint()));
      itemPasteNodes.addActionListener(pasteAction);
      pop.add(itemPasteNodes);
    }
    // refresh menu item
    JMenuItem refresh = new JMenuItem("Refresh");
    refresh.addActionListener(new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent e) {
        refreshAll();
      }
    });
    pop.add(refresh);
    pop.show(this, eventX, eventY);
  }

  /**
   */
  @Override
  public void mousePressed(MouseEvent event) {
    if (mLastMousePos == null)
      mLastMousePos = event.getPoint();
    // we need to check the selected edge first, otherwise it's almost impossible
    // to grab the control point in some situations
    Edge e = findEdgeAt(event.getPoint());
    if (e != null) {
      if (mSelectedEdge != e) {
        selectEdge(e);
      } else {
        mSelectedEdge.mousePressed(event);
      }
      return;
    }

    if (mAreaSelection == null) {
      // get point as possible point for area selection!
      mAreaSelection = new Rectangle2D.Double(event.getX(), event.getY(), 0, 0);
    }

    Node node;
    if ((node = findNodeAtPoint(event.getPoint())) != null) {
      if (event.getButton() == MouseEvent.BUTTON1
          && (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
        mDoAreaSelection = false;
        if (!mSelectedNodes.containsKey(node)) {
          selectSingleNode(node);
        }
      }
    } else {
      // right click: global context menu for clipboard actions
      if ((event.getButton() == MouseEvent.BUTTON3)
          && (event.getClickCount() == 1)) {
        globalContextMenu(event);
      } else {
        mDoAreaSelection = true;
      }
    }
    if (! somethingSelected()) launchElementSelectedEvent(null);
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
    //launchProjectChangedEvent();
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

    if (! somethingSelected()) launchElementSelectedEvent(null);
  }

  /**
   *
   *
   */
  @Override
  public void mouseDragged(MouseEvent event) {
    if (mEdgeSourceNode != null) {
      createNewEdge(event.getPoint());
      return;
    }

    // if there is a specific selected edge use it - much faster than checking all edges
    if (mSelectedEdge != null && mSelectedEdge.controlPointSelected()) {
      mSelectedEdge.mouseDragged(event);
      revalidate();
      repaint(100);
      return;
    }

    // moving multiple nodes those which are selected before
    if (!mDoAreaSelection && ! mSelectedNodes.isEmpty()) {
      // compute movement trajectory vectors, in view coordinates
      Point currentMousePosition = event.getPoint();
      Point delta = new Point(currentMousePosition.x - mLastMousePos.x,
              currentMousePosition.y - mLastMousePos.y);
      if (dragNodes(mSelectedNodes.keySet(), delta)) {
        revalidate();
        repaint(100);
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
    if (DEBUG_MOUSE_LOCATIONS) {
      setMessageLabelText(String.format("%d, %d", event.getPoint().x, event.getPoint().y));
    }
    return;
  }

  /** Paint for things specific to mouse selection */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    // Edge construction in progress?
    if (mEdgeSourceNode != null) {
      setBackground(Color.LIGHT_GRAY);
      // draw line between source node and current mouse position
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
    } else {
      setBackground(Color.WHITE);
    }

    if (mAreaSelection != null) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setStroke(new BasicStroke(3.0f));
      g2d.setColor(Color.LIGHT_GRAY);
      g2d.draw(mAreaSelection);
    }

    /* Debugging: check boundaries of all components on workspace */
    if (DEBUG_COMPONENT_BOUNDARIES) {
      g2d.setColor(Color.pink);
      g2d.setStroke(new BasicStroke(0.5f));
      for (int i = 0 ; i <  this.getComponentCount(); ++i) {
        Rectangle r = getComponent(i).getBounds();
        g2d.drawRect(r.x, r.y, r.width, r.height);
      }
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

}
