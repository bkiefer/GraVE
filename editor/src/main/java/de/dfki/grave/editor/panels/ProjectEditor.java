package de.dfki.grave.editor.panels;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.fife.ui.autocomplete.AutoCompletion;

import de.dfki.grave.app.AppFrame;
import de.dfki.grave.app.Preferences;
import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.editor.event.CodeEditedEvent;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.editor.event.TreeEntrySelectedEvent;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.CommentBadge;
import de.dfki.grave.model.Position;
import de.dfki.grave.model.SceneFlow;
import de.dfki.grave.model.SuperNode;
import de.dfki.grave.editor.project.EditorProject;
import de.dfki.grave.util.evt.EventDispatcher;
import de.dfki.grave.util.evt.EventListener;

/**
 * @author Bernd Kiefer
 */
@SuppressWarnings("serial")
public final class ProjectEditor extends JSplitPane implements EventListener {

  private static final Logger mLogger = LoggerFactory.getLogger(ProjectEditor.class);

  // The singleton event multicaster
  private final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
  // The editor project of this editor
  private EditorProject mEditorProject;

 // The clipboard
  public final ClipBoard mClipboard = ClipBoard.getInstance();

  /** The currently displayed SuperNode */
  private SuperNode mActiveSuperNode;

  private UndoRedoProvider mUndoManager;

  // The GUI components of the editor
  // Area showing the graph
  private final WorkSpace mWorkSpacePanel;
  // The tool bar
  private final ProjectEditorToolBar mSceneFlowToolBar;
  // Editor to change the name of a node
  private final NameEditor mNameEditor;
  // currently not used: for the scenes
  private final SceneFlowElementPanel mDynamicElementsPanel;
  // the panel with prototypes to insert new nodes or edges by dragging
  private final JPanel mNewElementDisplay;
  // line for status and other messages
  private final JLabel mFooterLabel;
  // split between upper area and code editor
  private final JSplitPane mSplitPane;

  // Code editing panel
  private final CodeEditPanel mCodeEditor;
  // Supernode definitions panel
  private final DefinitionsPanel mDefinitionsEditor;

  // Bottom split pane, holding the two code editors
  private final JSplitPane mBottom;

  // Construct a project editor with a project
  public ProjectEditor(final EditorProject project) {
    // Initialize the parent split pane
    super(JSplitPane.VERTICAL_SPLIT, true);
    // Initialize the editor project
    mEditorProject = project;
    // Initialize Code Editing Region
    Font codeFont = mEditorProject.getEditorConfig().sCODE_FONT.getFont();
    mCodeEditor = //new CodeEditor(mEditorProject);
        new CodeEditPanel(codeFont);
    mDefinitionsEditor = new DefinitionsPanel(codeFont);
    mBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    // Register at the event dispatcher
    mEventDispatcher.register(this);
    // TOOLBAR: NORTH ELEMENT
    mUndoManager = new UndoRedoProvider();
    mSceneFlowToolBar = new ProjectEditorToolBar(this);

    // The west component is the workbar
    mFooterLabel = new JLabel();
    mDynamicElementsPanel = new SceneFlowElementPanel(mEditorProject);
    // To edit node names
    mNameEditor = new NameEditor(this);
    // left element in the mSplitPane
    mNewElementDisplay = newElementDisplay(
        mEditorProject.getEditorConfig().sSHOW_ELEMENTS);
    // prepare the vertical split region (now used as code editor)
    mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    // The right component is the workspace
    mWorkSpacePanel = new WorkSpace(this);
    mWorkSpacePanel.setTransferHandler(new SceneFlowImage());

    setActiveSuperNode(getSceneFlow());

    //  Initialize the GUI components
    initComponents();
  }

  // Get the editor project
  public final EditorProject getEditorProject() {
    return mEditorProject;
  }

  // Clean up the editor component
  public final void close() {
    // Remove from event dispatcher
    mEventDispatcher.remove(this);
    // Cleanup workspace
    mWorkSpacePanel.cleanup();
    mEditorProject = null;
  }

  private JPanel newElementDisplay(boolean visible) {
    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
    result.add(Box.createVerticalStrut(10));
    result.add(mNameEditor);
    result.add(new SceneFlowPalettePanel());
    result.add(mDynamicElementsPanel);
    // PG 17.12.13 - FUTURE FEATURE! result.add(new EdgeTypeSelection(), BorderLayout.NORTH);
    result.setVisible(visible);
    return result;
  }

  private JPanel initUpperCompos() {
    JScrollPane wsScrollPane = new JScrollPane(mWorkSpacePanel);
    wsScrollPane.setBorder(BorderFactory.createEtchedBorder());
    wsScrollPane.setMinimumSize(new Dimension(10, 10));
    wsScrollPane.setMaximumSize(new Dimension(10000, 3000));

    mSplitPane.setBorder(BorderFactory.createEmptyBorder());
    mSplitPane.setContinuousLayout(true);
    mSplitPane.setUI(new BasicSplitPaneUI() {
      @Override
      public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
          @Override
          public void setBorder(Border b) { }

          @Override
          public void paint(Graphics g) {
            Graphics2D graphics = (Graphics2D) g;

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle r = getBounds();
            graphics.setColor(UIManager.getColor("Panel.background"));
            graphics.fillRect(0, 0, r.width - 1, r.height);
            graphics.fillRect((r.width / 2 - 25), 0, 50, r.height);
          }

          @Override
          protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);
            switch (me.getID()) {
              case MouseEvent.MOUSE_CLICKED:
                toggleElementEditor();
            }
          }
        };
      }
    });

    // initialize the split panel with work space and new element panel
    mSplitPane.setRightComponent(wsScrollPane);
    mSplitPane.setResizeWeight(0.0);
    mSplitPane.setLeftComponent(mNewElementDisplay);
    if (mEditorProject.getEditorConfig().sSHOW_ELEMENTS) {
      mSplitPane.setDividerLocation(mEditorProject.getEditorConfig().sELEMENTS_DIVIDER_LOCATION);
    } else {
      mSplitPane.setDividerLocation(1.0d);
    }

    mSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent pce) {
        if (mEditorProject.getEditorConfig().sSHOW_ELEMENTS) {
          mEditorProject.getEditorConfig().sELEMENTS_DIVIDER_LOCATION =
              mSplitPane.getDividerLocation();
        }
      }
    });

    mFooterLabel.setForeground(Color.red);

    final JPanel upper = new JPanel();
    upper.setLayout(new BorderLayout());
    // Set Background Color
    upper.setBackground(Color.WHITE);
    // Set An Empty Border
    upper.setBorder(BorderFactory.createEmptyBorder());
    upper.setMinimumSize(new Dimension(10, 10));
    upper.setMaximumSize(new Dimension(10000, 3000));

    upper.add(mSceneFlowToolBar, BorderLayout.NORTH);
    upper.add(mSplitPane, BorderLayout.CENTER);
    upper.add(mFooterLabel, BorderLayout.SOUTH);
    return upper;
  }

  // Initialize the GUI components
  private void initComponents() {

    setOneTouchExpandable(true);

    setResizeWeight(mEditorProject.getEditorConfig().sSCENEFLOW_SCENE_EDITOR_RATIO);

    setUI(new BasicSplitPaneUI() {
      @Override
      public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
          /**
           * Shows the bottom part of the editor when mouse goes over
           * the border
           *
           * @param me
           */
          @Override
          protected void processMouseEvent(MouseEvent me) {
            super.processMouseEvent(me);
            switch (me.getID()) {

            case MouseEvent.MOUSE_ENTERED:
              //if (!mCodeEditor.isPinPricked())
              //  showAuxiliaryEditor();
              break;
            case MouseEvent.MOUSE_RELEASED:
              int value = ProjectEditor.this.getDividerLocation();
              mEditorProject.getEditorConfig().sCODE_DIVIDER_LOCATION = value;
              //mCodeEditor.setPinPricked();
              break;
            }
          }
        };
      }
    });

    setDividerSize(10);

    setContinuousLayout(true);

    setTopComponent(initUpperCompos());
    mCodeEditor.setMinimumSize(new Dimension(10, 10));
    mCodeEditor.setMaximumSize(new Dimension(10000, 3000));
    mDefinitionsEditor.setMinimumSize(new Dimension(10, 10));
    mDefinitionsEditor.setMaximumSize(new Dimension(10000, 3000));
    mBottom.setResizeWeight(0.5);
    mBottom.add(mDefinitionsEditor);
    mBottom.add(mCodeEditor);
    setBottomComponent(mBottom);

    if (mEditorProject.getEditorConfig().sSHOW_CODEEDITOR) {
      showAuxiliaryEditor();
    }

    mCodeEditor.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        mEditorProject.getEditorConfig().sSHOW_CODEEDITOR =
            (mBottom.getSize().height == 0);
        Preferences.savePrefs();
      }
    });
  }

  /**
   */
  public void toggleElementEditor() {
    if (mEditorProject.getEditorConfig().sSHOW_ELEMENTS) {
      mNewElementDisplay.setVisible(false);
      mEditorProject.getEditorConfig().sSHOW_ELEMENTS = false;
      mSplitPane.setDividerLocation(1d);
    } else {
      mNewElementDisplay.setVisible(true);
      mEditorProject.getEditorConfig().sSHOW_ELEMENTS = true;
      mSplitPane.setDividerLocation(mEditorProject.getEditorConfig()
              .sELEMENTS_DIVIDER_LOCATION);
    }
    mEditorProject.saveEditorConfig();
  }

  public void expandTree() {
    mDynamicElementsPanel.expandTree();
  }

  public boolean isElementDisplayVisible() {
    return mNewElementDisplay.isVisible();
  }

  public SceneFlow getSceneFlow() {
    return mEditorProject.getSceneFlow();
  }

  public WorkSpace getWorkSpace() {
    return mWorkSpacePanel;
  }

  public UndoRedoProvider getUndoManager() {
    //return mUndoManagers.getLast();
    return mUndoManager;
  }


  public void clearClipBoard() {
    mClipboard.clear();
  }

  // ##########################################################################
  // Methods for interactive functionality in menus and buttons for the project
  // ##########################################################################

  /** Copy nodes in the selected nodes set (mSelectedNodes) to the clipboard for
   *  the Copy operation (lazy copy).
   */
  public void copyNodes(Collection<BasicNode> nodes) {
    mClipboard.setToCopy(this, nodes, //BasicNode.computeInnerEdges(nodes));
        Collections.emptyList(), Collections.emptyList());
  }

  // TODO: only to be used by this class and the ActiveSuperNodeAction
  public void setActiveSuperNode(SuperNode supernode) {
    // Reset mouse interaction
    mWorkSpacePanel.ignoreMouseInput(true);
    //mActiveSuperNodes.addLast(supernode);
    mActiveSuperNode = supernode;
    mSceneFlowToolBar.updateBreadcrumbs(supernode);
    // mWorkSpacePanel.setActiveSuperNode(supernode);
    mDefinitionsEditor.setSuperNode(supernode);

    mWorkSpacePanel.showNewSuperNode();
    mWorkSpacePanel.ignoreMouseInput(false);
  }

  public SuperNode getActiveSuperNode() {
    return mActiveSuperNode;
  }

  public void switchActiveSuperNodeAction(SuperNode supernode) {
    if (supernode != null && supernode != getActiveSuperNode()) {
      ActiveSuperNodeAction ase = new ActiveSuperNodeAction(this, supernode);
      ase.run();
    }
  }

  /** Jump into the SuperNode node (currently present on the WorkSpace) */
  public void switchToSuperNode(Node node) {
    switchActiveSuperNodeAction((SuperNode) node.getDataNode());
  }

  /** Pop out one level, if possible */
  public void decreaseWorkSpaceLevel() {
    switchActiveSuperNodeAction(getActiveSuperNode().getParentNode());
  }

  void zoomOut() {
    float mZoomFactor = mEditorProject.getEditorConfig().sZOOM_FACTOR;
    if (mZoomFactor > 0.5) mZoomFactor -= .1;
    mEditorProject.getEditorConfig().sZOOM_FACTOR = mZoomFactor;
    //saveEditorConfig(); // TODO: activate
    mWorkSpacePanel.refreshAll();
  }

  void nozoom() {
    mEditorProject.getEditorConfig().sZOOM_FACTOR = 1.0f;
    //saveEditorConfig(); // TODO: activate
    mWorkSpacePanel.refreshAll();
  }

  void zoomIn() {
    float mZoomFactor = mEditorProject.getEditorConfig().sZOOM_FACTOR;
    if (mZoomFactor < 3.0) mZoomFactor += .1;
    mEditorProject.getEditorConfig().sZOOM_FACTOR = mZoomFactor;
    //saveEditorConfig(); // TODO: activate
    mWorkSpacePanel.refreshAll();
  }

  public void setMessageLabelText(String value) {
    mFooterLabel.setText(value);
  }

  public final void refresh() {
    // Refresh editor toolbar
    refreshToolBar();
    mDynamicElementsPanel.refresh();
    mWorkSpacePanel.refresh();
  }

  public final void refreshToolBar() {
    mSceneFlowToolBar.refresh(getEditorProject().hasChanged(), isElementDisplayVisible());
  }

  /* ######################################################################
   * Provide functionality for the global menu bar
   * ###################################################################### */

  // TODO: MAYBE ADD THIS TO EDITORACTION
  private void actionMessage(Collection<BasicNode> sel, String act) {
    String what= (sel.size() > 1) ? " Nodes " : " Node ";
    setMessageLabelText(sel.size() + what + act);
  }

  // TODO: MAYBE ADD THIS TO EDITORACTION
  private void actionMessage(int size, String act) {
    String what= (size > 1) ? " Elements " : " Element ";
    setMessageLabelText(size + what + act);
  }

  /** Copy the selected nodes to the clipboard, if any */
  public void copySelected() {
    Collection<BasicNode> sel = mWorkSpacePanel.getSelectedNodes();
    if (sel.isEmpty()) return;
    // copy is not undoable
    CopyNodesAction action = new CopyNodesAction(this, sel);
    actionMessage(sel, "copied");
    action.actionPerformed(null);
  }

  /** Cut the selected nodes and add them to clipboard, if any */
  public void cutSelected() {
    Collection<BasicNode> sel = mWorkSpacePanel.getSelectedNodes();
    if (sel.isEmpty()) return;
    RemoveNodesAction action = new RemoveNodesAction(this, sel, true);
    actionMessage(sel, "cut");
    action.run();
  }

  /** Paste nodes in the upper left corner */
  public void pasteClipboard() {
    PasteNodesAction action = new PasteNodesAction(this, new Position(0, 0));
    action.run();
  }

  public void deleteSelected() {
    int elements = 0;
    List<EditorAction> actions = new ArrayList<>();
    Collection<BasicNode> sel = mWorkSpacePanel.getSelectedNodes();
    if (! sel.isEmpty()) {
      actions.add(new RemoveNodesAction(this, sel, false));
      elements += sel.size();
    }
    Collection<CommentBadge> cmt = mWorkSpacePanel.getSelectedComments();
    if (! cmt.isEmpty()) {
      actions.add(new RemoveCommentsAction(this, cmt, false));
      elements += cmt.size();
    }
    if (mWorkSpacePanel.getSelectedEdge() != null) {
      actions.add(new RemoveEdgeAction(this, mWorkSpacePanel.getSelectedEdge()));
      elements += 1;
    }
    if (actions.isEmpty()) return;
    EditorAction action;
    if (actions.size() > 1) {
      action = new CompoundAction(this, actions, "Delete");
    } else {
      action = actions.get(0);
    }

    actionMessage(elements, "deleted");
    action.run();
  }


  /** Try to get all edges as straight as possible: menu/button */
  public void straightenAllEdges() {
    List<EditorAction> actions = new ArrayList<>();
    for (Edge edge : mWorkSpacePanel.getEdges()) {
      actions.add(new StraightenEdgeAction(this, edge.getDataEdge()));
    }
    new CompoundAction(this, actions, "Straighten all Edges").run();
  }

  /** Try to find nice paths for all edges: menu/button */
  public void normalizeAllEdges() {
    List<EditorAction> actions = new ArrayList<>();
    for (Edge edge : mWorkSpacePanel.getEdges()) {
      actions.add(new NormalizeEdgeAction(this, edge.getDataEdge()));
    }
    new CompoundAction(this, actions, "Normalize all Edges").run();
  }

  /* ######################################################################
   * End provide functionality for the global menu bar
   * ###################################################################### */

  private static class SceneFlowImage extends TransferHandler implements Transferable {

    private final DataFlavor flavors[] = { DataFlavor.imageFlavor };
    private Image image;

    @Override
    public int getSourceActions(JComponent c) {
      return TransferHandler.COPY;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor flavor[]) {
      if (!(comp instanceof JPanel)) {
        return false;
      }

      for (int i = 0, n = flavor.length; i < n; i++) {
        for (int j = 0, m = flavors.length; j < m; j++) {
          if (flavor[i].equals(flavors[j])) {
            return true;
          }
        }
      }

      return false;
    }

    @Override
    public Transferable createTransferable(JComponent comp) {

      // Clear
      image = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);

      if (comp instanceof JPanel) {
        Graphics g = image.getGraphics();
        comp.paint(g);
        g.dispose();
        return this;
      }

      return null;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
      if (comp instanceof JPanel) {
        JPanel panel = (JPanel) comp;

        if (t.isDataFlavorSupported(flavors[0])) {
          try {
            image = (Image) t.getTransferData(flavors[0]);
            panel.paint(image.getGraphics());

            return true;
          } catch (UnsupportedFlavorException ignored) {
          } catch (IOException ignored) {
          }
        }
      }

      return false;
    }

    // Transferable
    @Override
    public Object getTransferData(DataFlavor flavor) {
      if (isDataFlavorSupported(flavor)) {
        return image;
      }

      return null;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavor.equals(DataFlavor.imageFlavor);
    }
  }


  // Show the bottom part of the project editor
  private void showAuxiliaryEditor() {
    setDividerLocation(mEditorProject.getEditorConfig().sCODE_DIVIDER_LOCATION);
  }

  // Hides the bottom part of the project editor
  private void hideAuxiliaryEditor() {
    // TODO: Can we do that by adding and removing the component?
    setDividerLocation(1d);
  }

  private void endEdit() {
    if (mCodeEditor.getActiveArea() != null) {
      getUndoManager().endTextMode();
      mCodeEditor.getActiveArea().setDeselected();
      mCodeEditor.setDisabled();
    }
  }

  // Update when an event happened
  @Override
  public void update(final Object event) {
    if (event instanceof TreeEntrySelectedEvent) {
      // Show the auxiliary editor
      //showAuxiliaryEditor();
      // XXXXXX THIS IS THE PLACE TO START EDITING THE DEFINITIONS XXXXXXX
    } else if (event instanceof ElementSelectedEvent) {
      Object edited = ((ElementSelectedEvent) event).getElement();
      endEdit();
      if (edited instanceof Node) {
        mNameEditor.setNode(((Node)edited).getDataNode());
      } else {
        mNameEditor.setNode(null);
      }
      AppFrame.getInstance().refreshMenuBar();
      mDefinitionsEditor.setDeselected();
    } else if (event instanceof CodeEditedEvent) {
      CodeEditedEvent ev = (CodeEditedEvent)event;
      if (ev.getContainer() == null || ! ev.isActive()) {
        endEdit();
      } else {
        getUndoManager().startTextMode();
        if (ev.getContainer() != null) {
          ev.getContainer().setSelected();
          mCodeEditor.setEditedObject(ev.getContainer());
          mDefinitionsEditor.setDeselected();
        }
      }
    } if (event instanceof ProjectChangedEvent) {
      refreshToolBar();
    }
  }
}
