package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.editor.action.RedoAction;
import de.dfki.vsm.editor.action.UndoAction;
import de.dfki.vsm.editor.event.EdgeEditEvent;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.project.EditorConfig;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.editor.util.EdgeGraphics;
import de.dfki.vsm.model.flow.*;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class Edge extends JComponent implements EventListener, Observer, MouseListener {

  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  // Reference to data model edges and nodes
  private AbstractEdge mDataEdge = null;

  // The two graphical nodes to which this edge is connected
  private Node mSourceNode = null;
  private Node mTargetNode = null;
  private boolean hasAlternativeTargetNodes = false;
  private boolean mPointingToSameNode = false;
  public EdgeGraphics mEg = null;
  private WorkSpacePanel mWorkSpace = null;

  // rendering issues
  private FontMetrics mFM = null;
  private int mFontWidthCorrection = 0;
  private int mFontHeightCorrection = 0;

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

  // Activity monitor
  private static final Logger mLogger = LoggerFactory.getLogger(Edge.class);

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

  public Edge(WorkSpacePanel ws, AbstractEdge edge, Node sourceNode, Node targetNode) {
    this(ws, edge, sourceNode, targetNode, null, null);
  }

  // TODO: Neuer Konstruktor, der Source und Target dockpoint "mitbekommt"
  public Edge(WorkSpacePanel ws, AbstractEdge edge, Node sourceNode, Node targetNode,
          Point sourceDockPoint, Point targetDockpoint) {
    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    mDataEdge = edge;
    mWorkSpace = ws;
    mEditorConfig = mWorkSpace.getEditorConfig();
    mSourceNode = sourceNode;
    mTargetNode = targetNode;
    mPointingToSameNode = (mTargetNode == mSourceNode);

    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    mEg = new EdgeGraphics(this, sourceDockPoint, targetDockpoint);
    update();
    setVisible(true);
    initEditBox();
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

  private void update() {
    if (mDataEdge != null) {
      if (mEdgeTextArea != null)
        mEdgeTextArea.setForeground(color());
      hasAlternativeTargetNodes = !mDataEdge.getAltMap().isEmpty();
    }

    // Update the font and the font metrics that have to be
    // recomputed if the node's font size has changed
    // TODO: Move attributes to preferences and make editable
    /*Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();

    map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    map.put(TextAttribute.FAMILY, Font.SANS_SERIF);

    // map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_DEMIBOLD);
    map.put(TextAttribute.SIZE, mEditorConfig.sWORKSPACEFONTSIZE);

    // Derive the font from the attribute map
    Font font = Font.getFont(map);*/
    if (mEditorConfig.sWORKSPACEFONTSIZE != getFont().getSize())
      getFont().deriveFont(mEditorConfig.sWORKSPACEFONTSIZE);

    // Derive the node's font metrics from the font
    mFM = getFontMetrics(getFont());

    // Set the edge's font to the updated font
    //setFont(font);
    mFontWidthCorrection = mFM.stringWidth(name()) / 2;
    mFontHeightCorrection = (mFM.getAscent() + mFM.getDescent()) / 2;

    if (mEdgeTextArea != null)
      // do an exact font positioning
      computeTextBoxBounds();
  }

  private class MyDocumentListener implements DocumentListener {
    @Override
    // character addsed
    public void insertUpdate(DocumentEvent e) {
      computeTextBoxBounds();
    }

    @Override
    // character removed
    public void removeUpdate(DocumentEvent e) {
      computeTextBoxBounds();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      //Plain text components do not fire these events
      computeTextBoxBounds();
    }
  }

  /*
    * Initialize mTextPane and mValueEditor
   */
  private void initEditBox() {
    Color borderColor = color();
    mEdgeTextArea = new RSyntaxTextArea();
    this.add(mEdgeTextArea);
    mEdgeTextArea.setBackground(Color.WHITE);
    // Get rid of annoying yellow line
    mEdgeTextArea.setHighlighter(null);
    mEdgeTextArea.setHighlightCurrentLine(false);
    mEdgeTextArea.setHighlightSecondaryLanguages(false);

    mEdgeTextArea.setBorder(BorderFactory.createLineBorder(borderColor));
    mEdgeTextArea.getDocument().addDocumentListener(new MyDocumentListener());
    mEdgeTextArea.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        mDispatcher.convey(new EdgeSelectedEvent(Edge.this, getDataEdge()));
      }
      @Override
      public void focusLost(FocusEvent e) {}
    });

    // Attributes
    mEdgeTextArea.setFont(this.getFont());

    if (mDataEdge != null) {
      mEdgeTextArea.setText(mDataEdge.getContent());
      mEdgeTextArea.setVisible(mEdgeTextArea.getText().trim().length() > 0);
    }

    // do an exact font positioning
    computeTextBoxBounds();

    Action pressedAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();

        NodeSelectedEvent evt = new NodeSelectedEvent(mWorkSpace,
                mWorkSpace.getSceneFlowManager().getCurrentActiveSuperNode());

        mDispatcher.convey(evt);
        updateFromTextEditor();
      }
    };

    Action escapeAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();
      }
    };

    mEdgeTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
    mEdgeTextArea.getActionMap().put("enter", pressedAction);
    mEdgeTextArea.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
    mEdgeTextArea.getActionMap().put("escape", escapeAction);
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

  @Override
  public void mouseClicked(java.awt.event.MouseEvent event) {

    mDispatcher.convey(new EdgeSelectedEvent(this, this.getDataEdge()));
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
      mWorkSpace.showContextMenu(event, this);
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
      if (!mPointingToSameNode) {
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
      mUndoManager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();
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
      if (!mPointingToSameNode) {
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
    if (!mPointingToSameNode) {
      mTargetNode.getDockingManager().freeDockPoint(this);
    } else {
      mTargetNode.getDockingManager().freeSecondDockPoint(this);
    }

    mSourceNode.getDockingManager().freeDockPoint(this);
    mEg.initEdgeGraphics(this, null, null);
  }

  private void computeTextBoxBounds() {
    // do an exact font positioning
    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getHeight();
    int width = fm.stringWidth(mEdgeTextArea.getText() + "p");
    mFontWidthCorrection = width / 2;

    mEdgeTextArea.setBounds((int) Math.round(mEg.mLeftCurve.x2 - mFontWidthCorrection),
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
      graphics.setColor(color());
      mEdgeTextArea.requestFocusInWindow();
    }
    computeTextBoxBounds();

    graphics.setColor(color());

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

    if (hasAlternativeTargetNodes) {

      // String targets = mDataEdge.getStart();
      String targets = mDataEdge.getAltStartNodesAsString();

      // center the text
      mFontWidthCorrection = mFM.stringWidth(targets);

      // Get the current transform
      AffineTransform currentAT = graphics.getTransform();

      // Perform transformation
      AffineTransform at = new AffineTransform();

      graphics.translate(mEg.mAbsoluteEndPos.x, mEg.mAbsoluteEndPos.y);
      at.setToRotation((2 * Math.PI) - (mEg.mArrowDir + (Math.PI / 2)));
      graphics.transform(at);
      graphics.setColor(Color.WHITE);
      computeTextBoxBounds();
      graphics.setTransform(currentAT);
    }
  }

  public boolean isInEditMode() {
    return mEditMode;
  }

  /*
   * Implements EventListener
   */
  @Override
  public synchronized void update(EventObject event) {
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
