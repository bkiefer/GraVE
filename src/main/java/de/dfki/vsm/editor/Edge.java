package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;
import static de.dfki.vsm.editor.project.WorkSpacePanel.addItem;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.vsm.editor.action.*;
import de.dfki.vsm.editor.event.EdgeEditEvent;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.editor.util.EdgeArrow;
import de.dfki.vsm.model.flow.*;
import de.dfki.vsm.model.flow.geom.Geom;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.evt.EventDispatcher;


/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Edge extends EditorComponent
  implements MouseListener, DocumentContainer {
  /** MIN POSITION OF THE CONTROLPOINTS OF THE EDGE */

  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // Reference to data model edges and nodes
  private AbstractEdge mDataEdge = null;

  // The two graphical nodes to which this edge is connected
  private Node mSourceNode = null;
  private Node mTargetNode = null;
  private EdgeArrow mArrow = null;
  private WorkSpace mWorkSpace = null;

  // edit panel
  private RSyntaxTextArea mTextArea = null;
  private boolean mEditMode = false;

  //
  private Document mDocument = null;

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

  private EditorConfig mEditorConfig;

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
    mEditorConfig = mWorkSpace.getEditorConfig();
    mSourceNode = sourceNode;
    mTargetNode = targetNode;

    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    initEditBox();
    mArrow = new EdgeArrow();
    setDeselected();

    update();
    setVisible(true);
  }

  /** Does this edge point back to the source node? */
  private boolean isLoop() {
    return mTargetNode == mSourceNode;
  }

  @Override
  public void update(Observable o, Object obj) {
    update();
  }

  public AbstractEdge getDataEdge() {
    return mDataEdge;
  }

  public Node getSourceNode() {
    return mSourceNode;
  }

  public Node getTargetNode() {
    return mTargetNode;
  }

  @Override
  public String getName() {
    return name() + "(" + mSourceNode.getDataNode().getId() + "->" + mTargetNode.getDataNode().getId() + ")";
  }

  public Document getDocument() {
    return mDocument;
  }

  public String getDescription() {
    return mDataEdge != null ? mDataEdge.getContent() : null;
  }

  /*
  public void setDescription(String s) {
    mDataEdge.setContent(s);
    mTextArea.setText(s);
  }*/

  /** Disconnect this edge view from the node view it is connected to */
  public void disconnect() {
    mSourceNode.disconnectSource(this);
    mTargetNode.disconnectTarget(this);
  }

  /** Connect this edge view to the node view it should be connected to */
  public void connect() {
    mSourceNode.connectSource(this);
    mTargetNode.connectTarget(this);
  }

  public boolean containsPoint(Point p) {
    return mArrow.curveContainsPoint(p);
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
    //mEg.updateDrawingParameters(this);
  }

  private void update() {
    // Adapt font size
    if (mEditorConfig.sWORKSPACEFONTSIZE != getFont().getSize())
      getFont().deriveFont(mEditorConfig.sWORKSPACEFONTSIZE);

    updateEdgeGraphics();
  }

  /**
   * Initialize mTextPane and mValueEditor
   */
  private void initEditBox() {
    //if (getDescription() == null) return;
    // TODO: ACTIVATE AFTER FIXING CODEEDITOR.SETEDITEDOBJECT

    mTextArea = new RSyntaxTextArea();
    mDocument = new ObserverDocument();
    try {
      mDocument.insertString(0, getDescription(), null);
    } catch (BadLocationException ex) {
      Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
    }
    mTextArea.setDocument(mDocument);
    this.add(mTextArea);
    mTextArea.setBackground(Color.WHITE);
    // Get rid of annoying yellow line
    mTextArea.setHighlighter(null);
    mTextArea.setHighlightCurrentLine(false);
    mTextArea.setHighlightSecondaryLanguages(false);

    mTextArea.setBorder(BorderFactory.createLineBorder(color()));
    mTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      // character added
      public void insertUpdate(DocumentEvent e) {
        mDataEdge.setContent(mTextArea.getText());
        computeTextBoxBounds();
      }

      @Override
      // character removed
      public void removeUpdate(DocumentEvent e) { insertUpdate(e); }

      @Override
      public void changedUpdate(DocumentEvent e) { insertUpdate(e); }
    });

    mTextArea.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        mDispatcher.convey(new ElementSelectedEvent(Edge.this));
      }
      @Override
      public void focusLost(FocusEvent e) {}
    });

    // Attributes
    mTextArea.setFont(this.getFont());
    mTextArea.setForeground(color());

    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
    mTextArea.getActionMap().put("enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();
        mDispatcher.convey(new ElementSelectedEvent(null));
        updateFromTextEditor();
      }
    });
    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
    mTextArea.getActionMap().put("escape", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();
      }
    });
    mTextArea.setVisible(mTextArea.getText().trim().length() > 0);
  }

  /*
   *   Take input value of mValueEditor and set it as value of the edge
   */
  private void updateFromTextEditor() {
    if (mTextArea == null) return;
    String input = mTextArea.getText();
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
  }

  public void setSelected() {
    mEditMode = true;
    repaint(100);
  }

  public void setDeselected() {
    mArrow.deselectMCs();
    mEditMode = false;
    repaint(100);
  }

  /**
   */
  public void showContextMenu(MouseEvent evt, Edge edge) {
    JPopupMenu pop = new JPopupMenu();
    //addItem(pop, "Modify", new ModifyEdgeAction(edge, this));
    addItem(pop, "Delete", new RemoveEdgeAction(mWorkSpace, edge));
    addItem(pop, "Shortest Path", new ShortestEdgeAction(mWorkSpace, edge));
    addItem(pop, "Straighten", new StraightenEdgeAction(mWorkSpace, edge));
    addItem(pop, "Smart Path", new NormalizeEdgeAction(mWorkSpace, edge));
    pop.show(this, evt.getX(), evt.getY());
  }

  /** Return true if one of the end or control points is selected */
  public boolean controlPointSelected() {
    return mArrow.mSelected >= EdgeArrow.S && mArrow.mSelected <= EdgeArrow.E;
  }

  @Override
  public void mouseClicked(MouseEvent event) {

    mDispatcher.convey(new ElementSelectedEvent(this));
    mousePressed(event);

    // show context menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      showContextMenu(event, this);
    } else if ((event.getClickCount() == 2) && getDescription() != null
        && mTextArea != null) {
      mTextArea.requestFocus();
      mEditMode = true;
      mDispatcher.convey(new EdgeEditEvent(this, this.getDataEdge()));
    }
  }

  /**
   * Handles the mouse pressed event
   */
  @Override
  public void mousePressed(MouseEvent e) {
    mArrow.edgeSelected(e.getPoint());
    // revalidate();
    repaint(100);
  }

  private void deflectSource(Node newNode, int dock) {
    // change the SOURCE view and model to reflect edge change
    // disconnect view
    mSourceNode.disconnectSource(this);
    // disconnect model
    mSourceNode.getDataNode().removeEdge(mDataEdge);
    // modify source node and dock of edge model
    mDataEdge.setSource(newNode.getDataNode(), dock);
    // new source view
    mSourceNode = newNode;
    // add new outgoing edge to the new source node (model)
    mSourceNode.getDataNode().addEdge(mDataEdge);
    // new source view
    mSourceNode.connectSource(this);
  }

  private void deflectTarget(Node newNode, int dock) {
    // change the TARGET view and model to reflect edge change
    // disconnect view
    mTargetNode.disconnectTarget(this);
    // incoming edges are not registered in the model, only the dock
    mTargetNode.getDataNode().freeDock(mDataEdge.getTargetDock());
    // modify target node and dock of model
    mDataEdge.setTarget(newNode.getDataNode(), dock);
    // new target view
    mTargetNode = newNode;
    // take dock in the target node (for source done by addEdge)
    mTargetNode.getDataNode().occupyDock(dock);
    // new target view
    mTargetNode.connectTarget(this);
  }

  public void deflect(Node newNode, int newDock, boolean start) {
    if (start)
      deflectSource(newNode, newDock);
    else
      deflectTarget(newNode, newDock);
    updateEdgeGraphics();
  }

  public void moveCtrlPoint(Point toLocation, boolean start) {
    AbstractEdge edge = getDataEdge();
    if (start)
      edge.setSourceCtrlPoint(toLocation);
    else
      edge.setTargetCtrlPoint(toLocation);
    updateEdgeGraphics();
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
        new MoveEdgeEndPointAction(mWorkSpace, this, true, dock, newNode).run();
      }
      break;
    }
    case EdgeArrow.E: {
      Node newNode = mWorkSpace.findNodeAtPoint(p);
      if (canDeflect(newNode, mTargetNode)) {
        int dock = newNode.getNearestFreeDock(p);
        new MoveEdgeEndPointAction(mWorkSpace, this, false, dock, newNode).run();
      }
      break;
    }
    case EdgeArrow.C1: {
      // compute vector from dock point to p (relative ctrls)
      Point dock = mSourceNode.getDockPoint(mDataEdge.getSourceDock());
      p.translate(-dock.x, -dock.y);
      new MoveEdgeCtrlAction(mWorkSpace, this, true, p).run();
      break;
    }
    case EdgeArrow.C2: {
      Point dock = mTargetNode.getDockPoint(mDataEdge.getTargetDock());
      p.translate(-dock.x, -dock.y);
      new MoveEdgeCtrlAction(mWorkSpace, this, false, p).run();
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

  public void straightenEdge() {
    mDataEdge.straightenEdge(mSourceNode.getWidth());
    updateEdgeGraphics();
  }

  public void rebuildEdgeNicely() {
    // disconnectEdge
    straightenEdge();
  }

  public Rectangle computeTextBoxBounds() {
    if (mTextArea == null) return null;
    // do an exact font positioning
    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getHeight();
    int width = fm.stringWidth(mTextArea.getText() + "p");
    // Derive the node's font metrics from the font
    int mFontHeightCorrection = (fm.getAscent() + fm.getDescent()) / 2;

    mTextArea.setBounds((int) Math.round(mArrow.mLeftCurve.x2 - width/2),
        (int) Math.round(mArrow.mLeftCurve.y2 - mFontHeightCorrection), width, height);
    return mTextArea.getBounds();
  }

  private void computeBounds() {
    // set bounds of edge
    Rectangle bounds = mArrow.computeBounds();

    /* add some boundaries */
    bounds.add(new Point(bounds.x - 10, bounds.y - 10));
    bounds.width = bounds.width + 10;
    bounds.height = bounds.height + 10;

    // add the size of the text box
    Rectangle textBox = computeTextBoxBounds();
    if (textBox != null)
      bounds.add(textBox);
    // set the components bounds
    setBounds(bounds);
  }

  public void paintComponent(Graphics g) {
    float lineWidth = mSourceNode.getWidth() / 30.0f;
    Graphics2D graphics = (Graphics2D) g;

    graphics.setColor(color());
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER));

    // NOTE: This is the magic command that makes all drawing relative
    // to the position of this component, and removes the "absolute" position
    // part
    graphics.translate(-getLocation().x, -getLocation().y);
    // draw the arrow
    mArrow.paintArrow(graphics, lineWidth, color());

    if (mEditMode == true) {
      //graphics.setColor(color());
      mTextArea.requestFocusInWindow();
    }
  }

  public boolean isInEditMode() {
    return mEditMode;
  }

  // **********************************************************************
  // New AbstractEdge functionality
  // **********************************************************************

  /** Absolute position of edge start */
  public Point getStart() {
    return mSourceNode.getDockPoint(mDataEdge.getSourceDock());
  }

  /** Absolute position of edge end */
  public Point getEnd() {
    return mTargetNode.getDockPoint(mDataEdge.getTargetDock());
  }

  /** Absolute position of edge start control point */
  public Point getStartCtrl() {
    return Geom.add(getStart(), mDataEdge.getSourceCtrlPoint());
  }

  /** Absolute position of edge end control point */
  public Point getEndCtrl() {
    return Geom.add(getEnd(), mDataEdge.getTargetCtrlPoint());
  }
}
