package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import de.dfki.vsm.editor.action.*;
import de.dfki.vsm.editor.event.EdgeEditEvent;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.editor.util.EdgeGraphics;
import de.dfki.vsm.model.flow.*;
import de.dfki.vsm.model.flow.geom.ControlPoint;
import de.dfki.vsm.model.flow.geom.Geom;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.evt.EventDispatcher;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;


/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Edge extends EditorComponent implements MouseListener {

  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // Reference to data model edges and nodes
  private AbstractEdge mDataEdge = null;

  // The two graphical nodes to which this edge is connected
  private Node mSourceNode = null;
  private Node mTargetNode = null;
  private EdgeGraphics mEg = null;
  private WorkSpace mWorkSpace = null;

  // edit panel
  private RSyntaxTextArea mTextArea = null;
  //private JTextPane mValueEditor = null;
  private boolean mEditMode = false;

  // For changing the edge's start or end node with a mouse drag
  private Node mReassignNode = null;
  private ObserverDocument edgeCodeDocument = null;

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
  private UndoManager mUndoManager;
  private boolean firstDrag = false;

  private Color color() {
    if (mDataEdge == null) return null;
    return edgeProperties.get(mDataEdge.getClass()).mColor;
  }

  private String name() {
    if (mDataEdge == null) return null;
    return edgeProperties.get(mDataEdge.getClass()).mName;
  }

  /** This constructor assumes that the edge model has reasonable values for
   *  the docks and control points.
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
    mEg = new EdgeGraphics(this);

    update();
    setVisible(true);
    initEditBox();
    if (mTextArea != null) {
      edgeCodeDocument = new ObserverDocument();
      try {
        edgeCodeDocument.insertString(0, getDescription(), null);
      } catch (BadLocationException ex) {
        Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
      }
      mTextArea.setDocument(edgeCodeDocument);
    }
  }

  /** Does this edge point back to the source node? */
  private boolean isLoop() {
    return mTargetNode == mSourceNode;
  }

  @Override
  public void update(Observable o, Object obj) {
    // mLogger.message("AbstractEdge.update(" + obj + ")");
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

  public ObserverDocument getCodeDocument() {
    return edgeCodeDocument;
  }

  public String getDescription() {
    return mDataEdge != null ? mDataEdge.getContent() : null;
  }

  public void setDescription(String s) {
    mDataEdge.setContent(s);
    mTextArea.setText(s);
  }

  /** Disconnect this edge view from the node view it is connected to */
  public void disconnect() {
    mSourceNode.disconnectSource(this);
    mTargetNode.disconnectTarget(this);
  }

  /** Disconnect this edge view from the node view it is connected to */
  public void connect() {
    List<ControlPoint> pl = mDataEdge.getArrow().getPointList();
    mSourceNode.connectAsSource(this, pl.get(0).getPoint());
    mTargetNode.connectAsTarget(this, pl.get(1).getPoint());
  }

  public void deflect(Node newTarget, Point newDockingPoint) {

  }

  public boolean containsPoint(Point p) {
    return mEg.curveContainsPoint(p);
  }

  public boolean outOfBounds() {
    Point ctrl1 = getStartCtrl();
    Point ctrl2 = getEndCtrl();
    return ctrl1.x < 0 || ctrl1.y < 0 || ctrl2.x < 0 || ctrl2.y < 0;
  }

  public boolean isIntersectByRectangle(double x1, double x2, double y1, double y2) {
    return mEg.isIntersectByRectangle(x1, x2, y1, y2);
  }

  // TODO: STILL NECESSARY?
  public void updateRelativeEdgeControlPointPos(Node n, int xOffset, int yOffset) {
    //mEg.updateRelativeEdgeControlPointPos(n, xOffset, yOffset);
  }

  private void update() {
    // Adapt font size
    if (mEditorConfig.sWORKSPACEFONTSIZE != getFont().getSize())
      getFont().deriveFont(mEditorConfig.sWORKSPACEFONTSIZE);

    if (mTextArea != null)
      // do an exact font positioning
      computeTextBoxBounds();
  }

  /*
    * Initialize mTextPane and mValueEditor
   */
  private void initEditBox() {
    mTextArea = new RSyntaxTextArea();
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

    if (mDataEdge != null) {
      mTextArea.setText(mDataEdge.getContent());
      mTextArea.setVisible(mTextArea.getText().trim().length() > 0);
    }

    // do an exact font positioning
    computeTextBoxBounds();

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
    update();
  }

  /*
    *   Take input value of mValueEditor and set it as value of the edge
   */
  private void updateFromTextEditor() {
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
  }

  public void setDeselected() {
    mEg.deselectMCs();
    mEditMode = false;
    //remove(mEdgeTextArea);
    repaint(100);
  }

  /**
   */
  public void showContextMenu(MouseEvent evt, Edge edge) {
    JPopupMenu pop = new JPopupMenu();
    JMenuItem item;// = new JMenuItem("Modify");
    //ModifyEdgeAction modifyAction = new ModifyEdgeAction(edge, this);

    //item.addActionListener(modifyAction.getActionListener());
    //pop.add(item);
    item = new JMenuItem("Delete");
    RemoveEdgesAction deleteAction = new RemoveEdgesAction(mWorkSpace, edge);
    item.addActionListener(deleteAction.getActionListener());
    pop.add(item);

    item = new JMenuItem("Shortest Path");
    item.setEnabled(true);
    ShortestEdgeAction shortestAction = new ShortestEdgeAction(mWorkSpace, edge);
    item.addActionListener(shortestAction.getActionListener());
    pop.add(item);

    item = new JMenuItem("Straighten");
    StraightenEdgeAction renameAction = new StraightenEdgeAction(mWorkSpace, edge);
    item.addActionListener(renameAction.getActionListener());
    pop.add(item);

    item = new JMenuItem("Smart Path");
    NormalizeEdgeAction normalizeAction = new NormalizeEdgeAction(mWorkSpace, edge);
    item.addActionListener(normalizeAction.getActionListener());
    pop.add(item);

    pop.show(this, evt.getX(), evt.getY());
  }

  /** Return true if one of the control points is selected */
  public boolean controlPointSelected() {
    return mEg.mSelected >= EdgeGraphics.S && mEg.mSelected <= EdgeGraphics.E;
  }

  @Override
  public void mouseClicked(java.awt.event.MouseEvent event) {

    mDispatcher.convey(new ElementSelectedEvent(this));
    mousePressed(event);

    // show context menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      showContextMenu(event, this);
    } else if ((event.getClickCount() == 2) && getDescription() != null) {
      mTextArea.requestFocus();
      mEditMode = true;
      mDispatcher.convey(new EdgeEditEvent(this, this.getDataEdge()));
    }
  }

  /**
   * Handles the mouse pressed event
   */
  @Override
  public void mousePressed(java.awt.event.MouseEvent e) {
    mEg.edgeSelected(e.getPoint());
    // revalidate();
    repaint(100);
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

    // System.out.println("edge - mouse released");
    if (mEg.mSelected == EdgeGraphics.S) {
      Point relPos = (Point) mSourceNode.getLocation().clone();

      relPos.setLocation(e.getX() - relPos.x, e.getY() - relPos.y);

      // DEBUG System.out.println("set new dock point for pos " + relPos);
      freeSourceDock();
      mSourceNode.getNearestDockPoint(this, relPos);
    }

    if (mEg.mSelected == EdgeGraphics.E) {
      Point relPos = (Point) mTargetNode.getLocation().clone();

      relPos.setLocation(e.getX() - relPos.x, e.getY() - relPos.y);

      // DEBUG System.out.println("set new dock point for pos " + relPos);
      freeTargetDock();
      if (!isLoop()) {
        mTargetNode.getNearestDockPoint(this, relPos);
      } else {
        //mTargetNode.getNearestSecondDockPoint(this, relPos);
        mTargetNode.getNearestDockPoint(this, relPos);
      }
    }
    mEg.deselectMCs();
    repaint(100);
  }

  public void mouseDragged(java.awt.event.MouseEvent e) {
    if (mEg.mSelected == EdgeGraphics.S) {
      mReassignNode = mWorkSpace.findNodeAtPoint(e.getPoint());
      if (mReassignNode == mSourceNode) {
        mReassignNode = null;
      }
    } else if (mEg.mSelected == EdgeGraphics.E) {
      // look if mouse pressed (without a click) was on a c
      mReassignNode = mWorkSpace.findNodeAtPoint(e.getPoint());
      if (mReassignNode == mTargetNode) {
        mReassignNode = null;
      }
    }
    /* UNDO SHOULD BE DONE ON RELEASE
    if (firstDrag) {
      UndoManager mUndoManager =
          EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();
      mUndoManager.addEdit(new
          UndoDragEdge(mEg.mCCrtl1.getLocation(), mEg.mCCrtl2.getLocation()));
      UndoAction.getInstance().refreshUndoState();
      RedoAction.getInstance().refreshRedoState();
      firstDrag = false;
    }
    */
    Point p = e.getPoint();

    // do not allow x and y values below 10
    if (p.x - 10 < 0)
      p.x = 10;
    if (p.y - 10 < 0)
      p.y = 10;

    /*
    if (mCP1Selected)
      mEg.mCCrtl1.setLocation(p);
    if (mCP2Selected)
      mEg.mCCrtl2.setLocation(p);
    if (mCEPSelected) {
      freeTargetDock(); // TODO: DO AT RELEASE
      mEg.mAbsoluteEndPos.setLocation(p);
    }

    if (mCSPSelected) {
      freeSourceDock(); // TODO: DO AT RELEASE
      // TODO store last start /end node and start and end pos
      mEg.mAbsoluteStartPos.setLocation(p);
    }
    */
    mEg.mouseDragged(p);
    repaint(100);
  }

  @Override
  public void mouseEntered(java.awt.event.MouseEvent e) {}

  @Override
  public void mouseExited(java.awt.event.MouseEvent e) {}

  public void straightenEdge() {
    mEg.initCurve();
  }

  private void freeTargetDock() {
    if (!isLoop()) {
      mTargetNode.freeDockPoint(this.mDataEdge.mTargetDock);
    } else {
      mTargetNode.freeSecondDockPoint(this.mDataEdge.mTargetDock);
    }
  }

  private void freeSourceDock() {
    mSourceNode.freeDockPoint(this.mDataEdge.mSourceDock);
  }

  public void rebuildEdgeNicely() {
    // disconnectEdge
    freeTargetDock();
    freeSourceDock();
    mEg.initEdgeGraphics(null, null);
  }

  private void computeTextBoxBounds() {
    // do an exact font positioning
    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getHeight();
    int width = fm.stringWidth(mTextArea.getText() + "p");
    // Derive the node's font metrics from the font
    int mFontHeightCorrection = (fm.getAscent() + fm.getDescent()) / 2;

    mTextArea.setBounds((int) Math.round(mEg.mLeftCurve.x2 - width/2),
        (int) Math.round(mEg.mLeftCurve.y2 - mFontHeightCorrection), width, height);
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    float lineWidth = mSourceNode.getWidth() / 30.0f;
    Graphics2D graphics = (Graphics2D) g;

    graphics.setColor(color());
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER));

    // draw the arrow
    mEg.paintArrow(graphics, lineWidth, color());

    if (mEditMode == true) {
      //graphics.setColor(color());
      mTextArea.requestFocusInWindow();
    }
    computeTextBoxBounds();
  }

  public boolean isInEditMode() {
    return mEditMode;
  }

  /**
   * UNDOABLE ACTION
   *
  private class UndoDragEdge extends AbstractUndoableEdit {

    Point oldCP1;
    Point oldCP2;

    Point currentCP1;
    Point currentCP2;

    public UndoDragEdge(Point p1, Point p2) {
      oldCP1 = p1;
      oldCP2 = p2;
    }

    @Override
    public void undo() throws CannotUndoException {
      currentCP1 = mEg.mCCrtl1.getLocation();
      currentCP2 = mEg.mCCrtl2.getLocation();
      mEg.mCCrtl1.setLocation(oldCP1);
      mEg.mCCrtl2.setLocation(oldCP2);
      repaint(100);
    }

    @Override
    public void redo() throws CannotRedoException {
      mEg.mCCrtl1.setLocation(currentCP1);
      mEg.mCCrtl2.setLocation(currentCP2);
      repaint(100);
    }

    @Override
    public boolean canUndo() {
      return true;
    }

    @Override
    public boolean canRedo() {
      return true;
    }

    @Override
    public String getUndoPresentationName() {
      return "Undo creation of edge";
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo creation of edge";
    }
  }
  */

  // **********************************************************************
  // New AbstractEdge functionality
  // **********************************************************************

  /** Absolute position of edge start */
  public Point getStart() {
    return mSourceNode.getDockPoint(mDataEdge.mSourceDock);
  }

  /** Absolute position of edge end */
  public Point getEnd() {
    return mTargetNode.getDockPoint(mDataEdge.mTargetDock);
  }

  /** Absolute position of edge start control point */
  public Point getStartCtrl() {
    return Geom.add(getStart(), mDataEdge.mSourceCtrlPoint);
  }

  /** Absolute position of edge end control point */
  public Point getEndCtrl() {
    return Geom.add(getEnd(), mDataEdge.mTargetCtrlPoint);
  }
}
