package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

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
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Edge extends JComponent implements Observer, MouseListener {

  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // Reference to data model edges and nodes
  private AbstractEdge mDataEdge = null;

  // The two graphical nodes to which this edge is connected
  private Node mSourceNode = null;
  private Node mTargetNode = null;
  private EdgeGraphics mEg = null;
  private WorkSpace mWorkSpace = null;

  // For mouse interaction ...
  public boolean mIsSelected = false;

  // edge control points
  public boolean mCP1Selected = false;
  public boolean mCP2Selected = false;

  // start an end points of edge
  public boolean mCEPSelected = false;
  public boolean mCSPSelected = false;

  // last dockpoints
  public Point mLastTargetNodeDockPoint = null;

  // edit panel
  private RSyntaxTextArea mEdgeTextArea = null;
  //private JTextPane mValueEditor = null;
  private boolean mEditMode = false;


  //
  // other stuff
  private static class Props {
    public Props(String n, String d, Color c) {
      mName = n; mDescription = d; mColor = c;
    }
    String mName;
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

  public Edge(WorkSpace workSpace, AbstractEdge edge, Node sourceNode, Node targetNode) {
    this(workSpace, edge, sourceNode, targetNode, null, null);
  }

  // TODO: Neuer Konstruktor, der Source und Target dockpoint "mitbekommt"
  public Edge(WorkSpace ws, AbstractEdge edge, Node sourceNode, Node targetNode,
          Point sourceDockPoint, Point targetDockpoint) {
    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    mDataEdge = edge;
    mWorkSpace = ws;
    mEditorConfig = mWorkSpace.getEditorConfig();
    mSourceNode = sourceNode;
    mTargetNode = targetNode;

    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    mEg = new EdgeGraphics(this, sourceDockPoint, targetDockpoint);
    update();
    setVisible(true);
    initEditBox();
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

  public String getDescription() {
    return mDataEdge != null ? mDataEdge.getContent() : null;
  }

  public void setDescription(String s) {
    mDataEdge.setContent(s);
    mEdgeTextArea.setText(s);
  }

  /** Disconnect this edge view from the node view it is connected to */
  public void disconnect() {
    mSourceNode.disconnectEdge(this);
    if (isLoop()) {
      mTargetNode.disconnectSelfPointingEdge(this);
    } else {
      mTargetNode.disconnectEdge(this);
    }
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
    return (mEg.mCCrtl1.x < 0) || (mEg.mCCrtl1.y < 0)
        || (mEg.mCCrtl2.x < 0) || (mEg.mCCrtl2.y < 0);
  }

  public boolean isIntersectByRectangle(double x1, double x2, double y1, double y2) {
    return mEg.isIntersectByRectangle(x1, x2, y1, y2);
  }

  public void updateRelativeEdgeControlPointPos(Node n, int xOffset, int yOffset) {
    mEg.updateRelativeEdgeControlPointPos(n, xOffset, yOffset);
  }

  private void update() {
    // Adapt font size
    if (mEditorConfig.sWORKSPACEFONTSIZE != getFont().getSize())
      getFont().deriveFont(mEditorConfig.sWORKSPACEFONTSIZE);

    if (mEdgeTextArea != null)
      // do an exact font positioning
      computeTextBoxBounds();
  }

  /*
    * Initialize mTextPane and mValueEditor
   */
  private void initEditBox() {
    mEdgeTextArea = new RSyntaxTextArea();
    this.add(mEdgeTextArea);
    mEdgeTextArea.setBackground(Color.WHITE);
    // Get rid of annoying yellow line
    mEdgeTextArea.setHighlighter(null);
    mEdgeTextArea.setHighlightCurrentLine(false);
    mEdgeTextArea.setHighlightSecondaryLanguages(false);

    mEdgeTextArea.setBorder(BorderFactory.createLineBorder(color()));
    mEdgeTextArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      // character added
      public void insertUpdate(DocumentEvent e) {
        mDataEdge.setContent(mEdgeTextArea.getText());
        computeTextBoxBounds();
      }

      @Override
      // character removed
      public void removeUpdate(DocumentEvent e) { insertUpdate(e); }

      @Override
      public void changedUpdate(DocumentEvent e) { insertUpdate(e); }
    });

    mEdgeTextArea.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        mDispatcher.convey(new ElementSelectedEvent(Edge.this));
      }
      @Override
      public void focusLost(FocusEvent e) {}
    });

    // Attributes
    mEdgeTextArea.setFont(this.getFont());
    mEdgeTextArea.setForeground(color());

    if (mDataEdge != null) {
      mEdgeTextArea.setText(mDataEdge.getContent());
      mEdgeTextArea.setVisible(mEdgeTextArea.getText().trim().length() > 0);
    }

    // do an exact font positioning
    computeTextBoxBounds();

    mEdgeTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
    mEdgeTextArea.getActionMap().put("enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();
        mDispatcher.convey(new ElementSelectedEvent(null));
        updateFromTextEditor();
      }
    });
    mEdgeTextArea.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
    mEdgeTextArea.getActionMap().put("escape", new AbstractAction() {
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
    String input = mEdgeTextArea.getText();
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

  public void setDeselected() {
    deselectMCs();
    mIsSelected = false;
    mEditMode = false;
    //remove(mEdgeTextArea);
    repaint(100);
  }

  private void deselectMCs() {
    mCP1Selected = false;
    mCP2Selected = false;
    mCSPSelected = false;
    mCEPSelected = false;
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


  @Override
  public void mouseClicked(java.awt.event.MouseEvent event) {

    mDispatcher.convey(new ElementSelectedEvent(this));
    mIsSelected = true;

    if (mEg.controlPoint1HandlerContainsPoint(event.getPoint(), 10)) {
      deselectMCs();
      mCP1Selected = true;
    }
    if (mEg.controlPoint2HandlerContainsPoint(event.getPoint(), 10)) {
      deselectMCs();
      mCP2Selected = true;
    }
    if (!(mCP1Selected || mCP2Selected) && mEg.curveStartPointContainsPoint(event.getPoint(), 10)) {
      deselectMCs();
      mCSPSelected = true;
    }
    if (!(mCP1Selected || mCP2Selected) && mEg.curveEndPointContainsPoint(event.getPoint(), 10)) {
      deselectMCs();
      mCEPSelected = true;
    }
    repaint(100);

    // show context menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      showContextMenu(event, this);
    } else if ((event.getClickCount() == 2) && getDescription() != null) {
      mEdgeTextArea.requestFocus();
      mEditMode = true;
      mDispatcher.convey(new EdgeEditEvent(this, this.getDataEdge()));
    }
  }

  /**
   * Handles the mouse pressed event
   */
  @Override
  public void mousePressed(java.awt.event.MouseEvent e) {
    mIsSelected = true;

    if (mEg.controlPoint1HandlerContainsPoint(e.getPoint(), 10)) {
      firstDrag = true;
      deselectMCs();
      mCP1Selected = true;
    }
    if (mEg.controlPoint2HandlerContainsPoint(e.getPoint(), 10)) {
      firstDrag = true;
      deselectMCs();
      mCP2Selected = true;
    }
    if (!(mCP1Selected || mCP2Selected) && mEg.curveStartPointContainsPoint(e.getPoint(), 10)) {
      deselectMCs();
      mCSPSelected = true;
    }
    if (!(mCP1Selected || mCP2Selected) && mEg.curveEndPointContainsPoint(e.getPoint(), 10)) {
      deselectMCs();
      mCEPSelected = true;
    }

    // revalidate();
    repaint(100);
  }

  @Override
  public void mouseReleased(java.awt.event.MouseEvent e) {

    // System.out.println("edge - mouse released");
    if (mCSPSelected) {
      Point relPos = (Point) mSourceNode.getLocation().clone();

      relPos.setLocation(e.getX() - relPos.x, e.getY() - relPos.y);

      // DEBUG System.out.println("set new dock point for pos " + relPos);
      mSourceNode.getDockingManager().freeDockPoint(this);
      mSourceNode.getDockingManager().getNearestDockPoint(this, relPos);
    }

    if (mCEPSelected) {
      Point relPos = (Point) mTargetNode.getLocation().clone();

      relPos.setLocation(e.getX() - relPos.x, e.getY() - relPos.y);

      // DEBUG System.out.println("set new dock point for pos " + relPos);
      if (!isLoop()) {
        mTargetNode.getDockingManager().freeDockPoint(this);
        mTargetNode.getDockingManager().getNearestDockPoint(this, relPos);
      } else {
        mTargetNode.getDockingManager().freeSecondDockPoint(this);
        mTargetNode.getDockingManager().getNearestSecondDockPoint(this, relPos);
      }
    }
    deselectMCs();
    repaint(100);
  }

  public void mouseDragged(java.awt.event.MouseEvent e) {
    if (firstDrag) {
      UndoManager mUndoManager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();
      mUndoManager.addEdit(new UndoDragEdge(mEg.mCCrtl1.getLocation(), mEg.mCCrtl2.getLocation()));
      UndoAction.getInstance().refreshUndoState();
      RedoAction.getInstance().refreshRedoState();
      firstDrag = false;
    }
    Point p = e.getPoint();

    // do not allow x and y values below 10
    if (p.x - 10 < 0)
      p.x = 10;
    if (p.y - 10 < 0)
      p.y = 10;
    if (mCP1Selected)
      mEg.mCCrtl1.setLocation(p);
    if (mCP2Selected)
      mEg.mCCrtl2.setLocation(p);
    if (mCEPSelected) {
      if (!isLoop()) {
        mLastTargetNodeDockPoint = mTargetNode.getDockingManager().freeDockPoint(this);
      } else {
        mLastTargetNodeDockPoint = mTargetNode.getDockingManager().freeSecondDockPoint(this);
      }
      mEg.mAbsoluteEndPos.setLocation(p);
    }

    if (mCSPSelected) {
      mSourceNode.getDockingManager().freeDockPoint(this);
      // TODO store last start /end node and start and end pos
      mEg.mAbsoluteStartPos.setLocation(p);
    }
    repaint(100);
  }

  @Override
  public void mouseEntered(java.awt.event.MouseEvent e) {}

  @Override
  public void mouseExited(java.awt.event.MouseEvent e) {}

  public void straightenEdge() {
    mEg.initCurve();
  }

  public void rebuildEdgeNicely() {

    // disconnectEdge
    if (!isLoop()) {
      mTargetNode.getDockingManager().freeDockPoint(this);
    } else {
      mTargetNode.getDockingManager().freeSecondDockPoint(this);
    }

    mSourceNode.getDockingManager().freeDockPoint(this);
    mEg.initEdgeGraphics(null, null);
  }

  private void computeTextBoxBounds() {
    // do an exact font positioning
    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getHeight();
    int width = fm.stringWidth(mEdgeTextArea.getText() + "p");
    // Derive the node's font metrics from the font
    int mFontHeightCorrection = (fm.getAscent() + fm.getDescent()) / 2;

    mEdgeTextArea.setBounds((int) Math.round(mEg.mLeftCurve.x2 - width/2),
        (int) Math.round(mEg.mLeftCurve.y2 - mFontHeightCorrection), width, height);
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    float lineWidth = mSourceNode.getWidth() / 30.0f;
    Graphics2D graphics = (Graphics2D) g;
    mEg.updateDrawingParameters();

    graphics.setColor(color());
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER));
    graphics.draw(mEg.mCurve);

    if (mEditMode == true) {
      //graphics.setColor(color());
      mEdgeTextArea.requestFocusInWindow();
    }
    computeTextBoxBounds();

    //graphics.setColor(color());

    // draw head
    mEg.computeHead();

    // if selected draw interface control points
    if (mIsSelected) {
      graphics.setColor(Color.DARK_GRAY);
      graphics.setStroke(new BasicStroke(0.5f));
      graphics.drawLine((int) mEg.mCurve.x1, (int) mEg.mCurve.y1, (int) mEg.mCurve.ctrlx1,
              (int) mEg.mCurve.ctrly1);
      graphics.drawLine((int) mEg.mCurve.x2, (int) mEg.mCurve.y2, (int) mEg.mCurve.ctrlx2,
              (int) mEg.mCurve.ctrly2);
      graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER));

      if (mCP1Selected) {
        graphics.setColor(color());
      }

      graphics.drawOval((int) mEg.mCurve.ctrlx1 - 7, (int) mEg.mCurve.ctrly1 - 7, 14, 14);
      graphics.fillOval((int) mEg.mCurve.ctrlx1 - 7, (int) mEg.mCurve.ctrly1 - 7, 14, 14);
      graphics.setColor(Color.DARK_GRAY);

      if (mCP2Selected) {
        graphics.setColor(color());
      }

      graphics.drawOval((int) mEg.mCurve.ctrlx2 - 7, (int) mEg.mCurve.ctrly2 - 7, 14, 14);
      graphics.fillOval((int) mEg.mCurve.ctrlx2 - 7, (int) mEg.mCurve.ctrly2 - 7, 14, 14);
      graphics.setColor(Color.DARK_GRAY);
      graphics.drawRect((int) mEg.mCurve.x1 - 7, (int) mEg.mCurve.y1 - 7, 14, 14);
      graphics.drawPolygon(mEg.mHead);
      graphics.fillRect((int) mEg.mCurve.x1 - 7, (int) mEg.mCurve.y1 - 7, 14, 14);
      // This draws the arrow head
      graphics.fillPolygon(mEg.mHead);
    } else {
      graphics.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER));
      // This draws the arrow head
      graphics.fillPolygon(mEg.mHead);
      graphics.setColor(color());
      graphics.drawPolygon(mEg.mHead);
    }
  }

  public boolean isInEditMode() {
    return mEditMode;
  }

  /**
   * UNDOABLE ACTION
   */
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

}
