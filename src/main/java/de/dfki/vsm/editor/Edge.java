package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.Hashtable;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.editor.action.ModifyEdgeAction;
import de.dfki.vsm.editor.action.RedoAction;
import de.dfki.vsm.editor.action.UndoAction;
import de.dfki.vsm.editor.event.EdgeEditEvent;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.project.EditorConfig;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.editor.util.EdgeGraphics;
import de.dfki.vsm.model.flow.GuardedEdge;
import de.dfki.vsm.model.flow.InterruptEdge;
import de.dfki.vsm.model.flow.RandomEdge;
import de.dfki.vsm.model.flow.TimeoutEdge;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
public class Edge extends JComponent implements EventListener, Observer, MouseListener {

  private final EventDispatcher mDispatcher
          = EventDispatcher.getInstance();

  // The actual type
  private TYPE mType = null;

  // Reference to data model edges and nodes
  private de.dfki.vsm.model.flow.AbstractEdge mDataEdge = null;

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
  private final Logger mLogger = LoggerFactory.getLogger(Edge.class);;

  // edit panel
  private RSyntaxTextArea mEdgeTextArea = null;
  //private JTextPane mValueEditor = null;
  private boolean mEditMode = false;
  SimpleAttributeSet attribs;

  //
  // other stuff
  private String mName;
  private String mDescription;
  private Color mColor;
  private EditorConfig mEditorConfig;
  private Timer mVisualisationTimer;
  private UndoManager mUndoManager;
  private boolean firstDrag = false;

  public enum TYPE {
    EEDGE, TEDGE, CEDGE, PEDGE, IEDGE, FEDGE
  }

  public Edge(TYPE type) {
    // TODO: remove constructor
    mType = type;
    switch (type) {
      case EEDGE:
        mName = "Epsilon";
        mDescription = "Conditionless edge";
        mColor = sEEDGE_COLOR;
        break;

      case FEDGE:
        mName = "Fork";
        mDescription = "Fork edge";
        mColor = sFEDGE_COLOR;
        break;

      case TEDGE:
        mName = "Timeout";
        mDescription = "Edge with a time condition";
        mColor = sTEDGE_COLOR;
        break;

      case CEDGE:
        mName = "Conditional";
        mDescription = "Edge with a logical condition";
        mColor = sCEDGE_COLOR;
        break;

      case PEDGE:
        mName = "Probability";
        mDescription = "Edge with a probalistic condition";
        mColor = sPEDGE_COLOR;
        break;

      case IEDGE:
        mName = "Interruptive";
        mDescription = "Edge with a logical condition that interrupts supernodes";
        mColor = sIEDGE_COLOR;
        break;
    }
    //initEditBox();
    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
  }

  public Edge(WorkSpacePanel ws, de.dfki.vsm.model.flow.AbstractEdge edge, TYPE type, Node sourceNode, Node targetNode) {
    this(ws, edge, type, sourceNode, targetNode, null, null);
  }

  // TODO: Neuer Konstruktor, der Source und Target dockpoint "mitbekommt"
  public Edge(WorkSpacePanel ws, de.dfki.vsm.model.flow.AbstractEdge edge, TYPE type, Node sourceNode, Node targetNode,
          Point sourceDockPoint, Point targetDockpoint) {
    mDataEdge = edge;
    mWorkSpace = ws;
    // mEditorConfig = EditorInstance.getInstance().getSelectedProjectEditor()
    //                 .getEditorProject().getEditorConfig();
    mEditorConfig = mWorkSpace.getEditorConfig();
    mSourceNode = sourceNode;
    mTargetNode = targetNode;
    mType = type;
    mPointingToSameNode = (mTargetNode == mSourceNode)
            ? true
            : false;

    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
    // Timer
    mVisualisationTimer = new Timer("Edge(" + mDataEdge.getSourceUnid() +
            "->" + mDataEdge.getTargetUnid() + ")-Visualization-Timer");
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

  public de.dfki.vsm.model.flow.AbstractEdge getDataEdge() {
    return mDataEdge;
  }

  public Node getSourceNode() {
    return mSourceNode;
  }

  public Node getTargetNode() {
    return mTargetNode;
  }

  public TYPE getType() {
    return mType;
  }

  @Override
  public String getName() {
    return mName + "(" + mSourceNode.getDataNode().getId() + "->" + mTargetNode.getDataNode().getId() + ")";
  }

  public String getDescription() {
    return mDescription;
  }

  public void update() {

    // configure type
    if (mDataEdge != null) {
      mDescription = "";
      switch (mType) {
        case EEDGE:
          mName = "Epsilon";
          mColor = sEEDGE_COLOR;
          break;

        case FEDGE:
          mName = "Fork";
          mColor = sFEDGE_COLOR;
          break;

        case TEDGE:
          mName = "Timeout";
          mColor = sTEDGE_COLOR;
          mDescription = ((TimeoutEdge) mDataEdge).getTimeout() + "ms";
          break;

        case CEDGE:
          mName = "Conditional";
          mColor = sCEDGE_COLOR;
          if (((GuardedEdge) mDataEdge).getCondition() != null)
            mDescription = ((GuardedEdge) mDataEdge).getCondition();
          break;

        case PEDGE:
          mName = "Probabilistic";
          mColor = sPEDGE_COLOR;
          mDescription = ((RandomEdge) mDataEdge).getProbability() + "%";
          break;

        case IEDGE:
          mName = "Interruptive";
          mColor = sIEDGE_COLOR;
          if (((InterruptEdge) mDataEdge).getCondition() != null)
            mDescription = ((InterruptEdge) mDataEdge).getCondition();
          break;
      }
      if (mEdgeTextArea != null)
        mEdgeTextArea.setForeground(mColor);
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
    mFontWidthCorrection = mFM.stringWidth(mName) / 2;
    mFontHeightCorrection = (mFM.getAscent() - mFM.getDescent()) / 2;
    
    if (mEdgeTextArea != null)
      // do an exact font positioning    
      computeTextBoxBounds(mDescription);
  }

  class MyDocumentListener implements DocumentListener {
    @Override
    // character added
    public void insertUpdate(DocumentEvent e) {
      if (mType == TYPE.CEDGE) {
        if (!validate(mEdgeTextArea.getText())) {
        } else {
        }
      }
    }

    @Override
    // character removed
    public void removeUpdate(DocumentEvent e) {
      if (mType == TYPE.CEDGE) {
        if (!validate(mEdgeTextArea.getText())) {
        } else {
        }
      }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      //Plain text components do not fire these events
    }
  }

  private boolean validate(String condition) {

    String inputString = condition;

    try {
      //ChartParser.parseResultType = ChartParser.LOG;
      //ChartParser.parseResultType = ChartParser.EXP;

      //LogicalCond log = ChartParser.logResult;
      //Expression log = ChartParser.expResult;
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /*
    * Initialize mTextPane and mValueEditor
   */
  private void initEditBox() {
    //setLayout(null);

    Color borderColor = mColor;

    mEdgeTextArea = new RSyntaxTextArea();
    this.add(mEdgeTextArea);
    //mEdgeTextArea.setLayout(new BoxLayout(mEdgeTextArea, BoxLayout.Y_AXIS));
    mEdgeTextArea.setBackground(Color.WHITE);
    // Get rid of annoying yellow line
    mEdgeTextArea.setHighlighter(null);
    mEdgeTextArea.setHighlightCurrentLine(false);
    mEdgeTextArea.setHighlightSecondaryLanguages(false);

    mEdgeTextArea.setBorder(BorderFactory.createLineBorder(borderColor));
    mEdgeTextArea.getDocument().addDocumentListener(new MyDocumentListener());

    // Attributes
    mEdgeTextArea.setFont(this.getFont());

    //attribs = new SimpleAttributeSet();
    //StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
    //StyleConstants.setFontFamily(attribs, Font.SANS_SERIF);
    //StyleConstants.setFontSize(attribs, 16);
    //mValueEditor.setParagraphAttributes(attribs, true);

    // TODO: What does this do?
    //mTextPanel.add(Box.createRigidArea(new Dimension(5, 5)));
    //mTextPanel.add(mValueEditor);
    //mEdgeTextArea.add(Box.createRigidArea(new Dimension(5, 5)));

    if (mDataEdge != null) {
      if (mType.equals(TYPE.TEDGE)) {
        mEdgeTextArea.setText("" + ((TimeoutEdge) mDataEdge).getTimeout());
      } else if (mType.equals(TYPE.PEDGE)) {
        mEdgeTextArea.setText("" + ((RandomEdge) mDataEdge).getProbability());
      } else {
        mEdgeTextArea.setText(mDescription);
      }
      mEdgeTextArea.setVisible(mEdgeTextArea.getText().trim().length() > 0);
    }
    
    // do an exact font positioning    
    computeTextBoxBounds(mDescription);

    Action pressedAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();

        NodeSelectedEvent evt = new NodeSelectedEvent(mWorkSpace,
                mWorkSpace.getSceneFlowManager().getCurrentActiveSuperNode());

        mDispatcher.convey(evt);
        updateFromTextEditor();

        if (!validate(mEdgeTextArea.getText())) {
          EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getFooterLabel().setForeground(Preferences.sIEDGE_COLOR);
          EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().setMessageLabelText(
                  "Invalid Condition");
          EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getFooterLabel().setForeground(Color.BLACK);
          // wrong condition
        } else {
          // correct condition
        }
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
  public void updateFromTextEditor() {
    String input = mEdgeTextArea.getText();

    if (mType.equals(TYPE.TEDGE)) {
      try {
        ((TimeoutEdge) mDataEdge).setTimeout(Long.valueOf(input));
      } catch (NumberFormatException e) {
        mLogger.warn("Invalid Number Format");
      }

    } else if (mType.equals(TYPE.CEDGE)) {
      try {
        if (input != null) {
          ((GuardedEdge) mDataEdge).setCondition(input);
        } else {
          EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().setMessageLabelText(
                  "Remember to wrap condition in parenthesis");
          // Do nothing
        }
      } catch (Exception e) {
      }

    } else if (mType.equals(TYPE.IEDGE)) {
      try {
        if (input != null) {
          ((InterruptEdge) mDataEdge).setCondition(input);
        } else {
        }
      } catch (Exception e) {
        e.printStackTrace();
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

    // showActivity();
    // revalidate();
    repaint(100);

    // show context menu
    if ((event.getButton() == MouseEvent.BUTTON3) && (event.getClickCount() == 1)) {
      mWorkSpace.showContextMenu(event, this);
    } else if ((event.getClickCount() == 2)) {

      if (mType.equals(TYPE.TEDGE)) {
        String timeout = getDescription();
        timeout = timeout.replace("m", "");
        timeout = timeout.replace("s", "");
        timeout = timeout.replace(" ", "");
        timeout = timeout.replace("\n", "");
        mEdgeTextArea.setText(timeout);

      } else if (mType.equals(TYPE.PEDGE)) {
        ModifyEdgeAction modifyAction = new ModifyEdgeAction(this, mWorkSpace);

        modifyAction.run();
        EditorInstance.getInstance().refresh();

      } else if (mType.equals(TYPE.CEDGE) || mType.equals(TYPE.IEDGE)) {
        mEdgeTextArea.setText(this.getDescription());
      }

      if (mType.equals(TYPE.TEDGE) || mType.equals(TYPE.CEDGE) || mType.equals(TYPE.IEDGE)) {
        mEdgeTextArea.requestFocus();
        mEditMode = true;
        mDispatcher.convey(new EdgeEditEvent(this, this.getDataEdge()));
      }
    }
  }

  /*
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
      mSourceNode.mDockingManager.freeDockPoint(this);
      mSourceNode.mDockingManager.getNearestDockPoint(this, relPos);
    }

    if (mCEPSelected) {
      Point relPos = (Point) mTargetNode.getLocation().clone();

      relPos.setLocation(e.getX() - relPos.x, e.getY() - relPos.y);

      // DEBUG System.out.println("set new dock point for pos " + relPos);
      if (!mPointingToSameNode) {
        mTargetNode.mDockingManager.freeDockPoint(this);
        mTargetNode.mDockingManager.getNearestDockPoint(this, relPos);
      } else {
        mTargetNode.mDockingManager.freeSecondDockPoint(this);
        mTargetNode.mDockingManager.getNearestSecondDockPoint(this, relPos);
      }
    }
    deselectMCs();
    // revalidate();
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
        mLastTargetNodeDockPoint = mTargetNode.mDockingManager.freeDockPoint(this);
      } else {
        mLastTargetNodeDockPoint = mTargetNode.mDockingManager.freeSecondDockPoint(this);
      }
      mEg.mAbsoluteEndPos.setLocation(p);
    }

    if (mCSPSelected) {
      mSourceNode.mDockingManager.freeDockPoint(this);
      // TODO store last start /end node and start and end pos
      mEg.mAbsoluteStartPos.setLocation(p);
    }
    // revalidate();
    repaint(100);
  }

  @Override
  public void mouseEntered(java.awt.event.MouseEvent e) {}

  @Override
  public void mouseExited(java.awt.event.MouseEvent e) {}

  public void mouseMoved(java.awt.event.MouseEvent e) {}

  public void straightenEdge() {
    mEg.initCurve();
  }

  public void rebuildEdgeNicely() {

    // disconnectEdge
    if (!mPointingToSameNode) {
      mTargetNode.mDockingManager.freeDockPoint(this);
    } else {
      mTargetNode.mDockingManager.freeSecondDockPoint(this);
    }

    mSourceNode.mDockingManager.freeDockPoint(this);
    mEg.initEdgeGraphics(this, null, null);
  }

  private void computeTextBoxBounds(String text) {
    // do an exact font positioning    
    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getHeight();
    int width = fm.stringWidth(text) + 2;
    mFontWidthCorrection = width / 2;
    
    mEdgeTextArea.setBounds((int) Math.round(mEg.mLeftCurve.x2 - mFontWidthCorrection),
        (int) Math.round(mEg.mLeftCurve.y2), width, height);
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    Graphics2D graphics = (Graphics2D) g;
    mEg.updateDrawingParameters();

    graphics.setColor(mColor);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setStroke(new BasicStroke(mEditorConfig.sNODEWIDTH / 30.0f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER));
    graphics.draw(mEg.mCurve);

    //if (mDescription.length() > 0) {
    //  computeTextBoxBounds(mDescription);
    //}
    if (mEditMode == true) {
      graphics.setColor(mColor);
      mEdgeTextArea.requestFocusInWindow();
    }

    graphics.setColor(mColor);

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
      graphics.setStroke(new BasicStroke(mEditorConfig.sNODEWIDTH / 30.0f, BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER));

      if (mCP1Selected) {
        graphics.setColor(mColor);
      }

      graphics.drawOval((int) mEg.mCurve.ctrlx1 - 7, (int) mEg.mCurve.ctrly1 - 7, 14, 14);
      graphics.fillOval((int) mEg.mCurve.ctrlx1 - 7, (int) mEg.mCurve.ctrly1 - 7, 14, 14);
      graphics.setColor(Color.DARK_GRAY);

      if (mCP2Selected) {
        graphics.setColor(mColor);
      }

      graphics.drawOval((int) mEg.mCurve.ctrlx2 - 7, (int) mEg.mCurve.ctrly2 - 7, 14, 14);
      graphics.fillOval((int) mEg.mCurve.ctrlx2 - 7, (int) mEg.mCurve.ctrly2 - 7, 14, 14);
      graphics.setColor(Color.DARK_GRAY);
      graphics.drawRect((int) mEg.mCurve.x1 - 7, (int) mEg.mCurve.y1 - 7, 14, 14);
      graphics.drawPolygon(mEg.mHead);
      graphics.fillRect((int) mEg.mCurve.x1 - 7, (int) mEg.mCurve.y1 - 7, 14, 14);
      graphics.fillPolygon(mEg.mHead);
    } else {
      graphics.setStroke(new BasicStroke(mEditorConfig.sNODEWIDTH / 30.0f, BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_MITER));
      graphics.fillPolygon(mEg.mHead);
      graphics.setColor(mColor);
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
      computeTextBoxBounds(targets);
      graphics.setTransform(currentAT);
    }
  }

  public boolean isInEditMode() {
    return mEditMode;
  }

  // TODO: Why is this never called, and why can we see the arrow anyways?
  public void drawArrow(Graphics2D g2d, int x, int y, float stroke) {    // int xCenter, int yCenter,
    g2d.setColor(mColor);

    double aDir = Math.atan2(x, y);    // xCenter-x,yCenter-y);

    g2d.setStroke(new BasicStroke(stroke));
    g2d.drawLine(x + 2, y + 2, 40, 40);
    g2d.setStroke(new BasicStroke(1f));    // make the arrow head solid even if dash pattern has been specified

    Polygon tmpPoly = new Polygon();
    int i1 = 16;    // + (int) (stroke * 2);

    tmpPoly.addPoint(x, y);      // arrow tip
    tmpPoly.addPoint(x + xCor(i1, aDir + .5), y + yCor(i1, aDir + .5));
    tmpPoly.addPoint(x + xCor(i1, aDir - .5), y + yCor(i1, aDir - .5));
    tmpPoly.addPoint(x, y);      // arrow tip
    g2d.fillPolygon(tmpPoly);    // paint arrow head
  }

  /**
   * helper method used to provive an arrow for the edge
   *
   * @param len
   * @param dir
   * @return
   */
  private static int yCor(int len, double dir) {
    return (int) (len * Math.cos(dir));
  }

  /**
   * helper method used to provive an arrow for the edge
   *
   * @param len
   * @param dir
   * @return
   */
  private static int xCor(int len, double dir) {
    return (int) (len * Math.sin(dir));
  }

  /**
   * Nullifies the VisalisationTimer thread
   */
  public void stopVisualisation() {
    mVisualisationTimer.purge();
    mVisualisationTimer.cancel();
    mVisualisationTimer = null;
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
