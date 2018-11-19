package de.dfki.vsm.editor.project.sceneflow;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.Preferences;
import de.dfki.vsm.editor.Comment;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.model.flow.*;

/**
 * @author Sergio Soto
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class SceneFlowPalettePanel extends JPanel {

  private final SceneFlowPaletteItem mSuperNodeEntry
          = new SceneFlowPaletteItem("Super Node", "Holds Sub-Scences flow",
                  Preferences.ICON_SUPERNODE_STANDARD, Preferences.ICON_SUPERNODE_ROLLOVER, Preferences.ICON_SUPERNODE_DRAGGING,
                  Node.Type.SuperNode);

  private final SceneFlowPaletteItem mBasicNodeEntry
          = new SceneFlowPaletteItem("Basic Node", "Holds Scenes Actions",
                  Preferences.ICON_BASICNODE_STANDARD, Preferences.ICON_BASICNODE_ROLLOVER, Preferences.ICON_BASICNODE_DRAGGING,
                  Node.Type.BasicNode);

  private final SceneFlowPaletteItem mEEdgeEntry
          = new SceneFlowPaletteItem("Epsilon Edge", "Creates Epsilon Transition",
                  Preferences.ICON_EEDGE_ENTRY_STANDARD, Preferences.ICON_EEDGE_ENTRY_ROLLOVER, Preferences.ICON_EEDGE_ENTRY_DRAGGING,
                  new EpsilonEdge());

  private final SceneFlowPaletteItem mTEdgeEntry
          = new SceneFlowPaletteItem("Timeout Edge", "Creates Timeout Transition",
                  Preferences.ICON_TEDGE_ENTRY_STANDARD, Preferences.ICON_TEDGE_ENTRY_ROLLOVER, Preferences.ICON_TEDGE_ENTRY_DRAGGING,
                  new TimeoutEdge());

  private final SceneFlowPaletteItem mPEdgeEntry
          = new SceneFlowPaletteItem("Probability Edge", "Creates Probability Transition",
                  Preferences.ICON_PEDGE_ENTRY_STANDARD, Preferences.ICON_PEDGE_ENTRY_ROLLOVER, Preferences.ICON_PEDGE_ENTRY_DRAGGING,
                  new RandomEdge());

  private final SceneFlowPaletteItem mCEdgeEntry
          = new SceneFlowPaletteItem("Conditional Edge", "Creates Conditional Transition",
                  Preferences.ICON_CEDGE_ENTRY_STANDARD, Preferences.ICON_CEDGE_ENTRY_ROLLOVER, Preferences.ICON_CEDGE_ENTRY_DRAGGING,
                  new GuardedEdge());

  private final SceneFlowPaletteItem mIEdgeEntry
          = new SceneFlowPaletteItem("Interruptive Edge", "Creates Interrutive Transition",
                  Preferences.ICON_IEDGE_ENTRY_STANDARD, Preferences.ICON_IEDGE_ENTRY_ROLLOVER, Preferences.ICON_IEDGE_ENTRY_DRAGGING,
                  new InterruptEdge());

  private final SceneFlowPaletteItem mFEdgeEntry
          = new SceneFlowPaletteItem("Fork Edge", "Creates Forked Transition",
                  Preferences.ICON_FEDGE_ENTRY_STANDARD, Preferences.ICON_FEDGE_ENTRY_ROLLOVER, Preferences.ICON_FEDGE_ENTRY_DRAGGING,
                  new ForkingEdge());

  private final SceneFlowPaletteItem mCommentEntry
          = new SceneFlowPaletteItem("Comment", "Adds a Comment",
                  Preferences.ICON_COMMENT_ENTRY_STANDARD, Preferences.ICON_COMMENT_ENTRY_ROLLOVER, Preferences.ICON_COMMENT_ENTRY_DRAGGING,
                  new Comment());

  //
  private static final Logger mLogger =
      LoggerFactory.getLogger(SceneFlowPalettePanel.class);;

  //
  private final int paletteDimension = 230;
  // Construct the tool panel

  public SceneFlowPalettePanel() {
    setLayout(new GridLayout(0, 3));
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(paletteDimension, paletteDimension));
    setMinimumSize(new Dimension(paletteDimension, paletteDimension));
    setMaximumSize(new Dimension(paletteDimension, paletteDimension));
    //setBorder(BorderFactory.createEtchedBorder());
    add(mSuperNodeEntry);
    add(mBasicNodeEntry);
    add(mCommentEntry);
    add(mEEdgeEntry);
    add(mPEdgeEntry);
    add(mFEdgeEntry);
    add(mCEdgeEntry);
    add(mTEdgeEntry);
    add(mIEdgeEntry);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public final void refresh() {
    // Print some information
    //mLogger.message("Refreshing '" + this + "'");
    //
  }
}
