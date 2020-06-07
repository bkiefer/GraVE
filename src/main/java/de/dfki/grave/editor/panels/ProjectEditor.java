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
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.fife.ui.autocomplete.AutoCompletion;

import de.dfki.grave.Preferences;
import de.dfki.grave.editor.EditorComponent;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.event.TreeEntrySelectedEvent;
import de.dfki.grave.model.flow.SceneFlow;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.util.evt.EventDispatcher;
import de.dfki.grave.util.evt.EventListener;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class ProjectEditor extends JSplitPane implements EventListener {

  private static final Logger mLogger = LoggerFactory.getLogger(ProjectEditor.class);
  
  // The singelton event multicaster
  private final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
  // The editor project of this editor
  private EditorProject mEditorProject;
  
  // TODO: move undo manager up at least to project editor
  private UndoManager mUndoManager = null;

  /** The list of active supernodes from the Sceneflow to the currently
   *  displayed node
   */
  private LinkedList<SuperNode> mActiveSuperNodes;

  // The GUI components of the editor
  private WorkSpacePanel mWorkSpacePanel;
  private SceneFlowToolBar mSceneFlowToolBar;
  private NameEditor mNameEditor;
  private SceneFlowElementPanel mDynamicElementsPanel;
  private JPanel mNewElementDisplay;
  private JLabel mFooterLabel;
  private JSplitPane mSplitPane;
  
  // Code editing panel
  private CodeEditPanel mCodeEditor;

  // Construct a project editor with a project
  public ProjectEditor(final EditorProject project) {
    // Initialize the parent split pane
    super(JSplitPane.VERTICAL_SPLIT, true);
    // Initialize the editor project
    mEditorProject = project;
    // Initialize Code Editing Region
    mCodeEditor = //new CodeEditor(mEditorProject);
        new CodeEditPanel(mEditorProject.getEditorConfig().sCODE_FONT.getFont());
    // Register at the event dispatcher
    mEventDispatcher.register(this);
    // Initialize the GUI components
    initComponents();
  }

  // Get the editor project
  public final EditorProject getEditorProject() {
    return mEditorProject;
  }

  // Clean up the editor component
  public final void close() {
    mEditorProject = null;
    // Remove from event dispatcher
    mEventDispatcher.remove(this);
    // Cleanup workspace
    mWorkSpacePanel.cleanup();
  }

  private void initUpperCompos() {
    // PREPARE THE VERTICAL SPLIT REGION (NOW USED AS CODE EDITOR)
    mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
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

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
    mUndoManager = new UndoManager();

    mActiveSuperNodes = new LinkedList<SuperNode>();

    // TOOLBAR: NORTH ELEMENT
    mSceneFlowToolBar = new SceneFlowToolBar(this, mEditorProject);
    // The center component is the workspace
    mWorkSpacePanel = new WorkSpacePanel(this, mEditorProject);
    mWorkSpacePanel.setTransferHandler(new SceneFlowImage());

    JScrollPane mWorkSpaceScrollPane = new JScrollPane(mWorkSpacePanel);
    mWorkSpaceScrollPane.setBorder(BorderFactory.createEtchedBorder());

    // The west component is the workbar
    mFooterLabel = new JLabel();
    mDynamicElementsPanel = new SceneFlowElementPanel(mEditorProject);
    SceneFlowPalettePanel mStaticElementsPanel = new SceneFlowPalettePanel();
    mNameEditor = new NameEditor();

    mNewElementDisplay = new JPanel();
    mNewElementDisplay.setLayout(new BoxLayout(mNewElementDisplay, BoxLayout.Y_AXIS));
    mNewElementDisplay.add(mNameEditor);
    mNewElementDisplay.add(mStaticElementsPanel);
    mNewElementDisplay.add(mDynamicElementsPanel);

    // PG 17.12.13 - FUTURE FEATURE! mNewElementDisplay.add(new EdgeTypeSelection(), BorderLayout.NORTH);
    mNewElementDisplay.setVisible(mEditorProject.getEditorConfig().sSHOW_ELEMENTS);
    // INITIALIZE THE SPLIT PANEL WITH WORK SPACE AND ELEMENTEDITOR
    mWorkSpaceScrollPane.setMinimumSize(new Dimension(10, 10));
    mWorkSpaceScrollPane.setMaximumSize(new Dimension(10000, 3000));
    mSplitPane.setRightComponent(mWorkSpaceScrollPane);
    mSplitPane.setResizeWeight(0.0);
    mSplitPane.setLeftComponent(mNewElementDisplay);
    if (mEditorProject.getEditorConfig().sSHOW_ELEMENTS) {
      mSplitPane.setDividerLocation(mEditorProject.getEditorConfig().sELEMENTS_DIVIDER_LOCATION);
      //THIS EVENT IS CASTED ONLY TO ACTIVATE THE ELEMENT EDITOR WITH THE INFO OF THE CURRENT PROJECT
    } else {
      mSplitPane.setDividerLocation(1.0d);
    }

    mSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent pce) {

        // solve issue here
        if (mEditorProject.getEditorConfig().sSHOW_ELEMENTS) {
          mEditorProject.getEditorConfig().sELEMENTS_DIVIDER_LOCATION = mSplitPane.getDividerLocation();
        }
      }
    });

    mFooterLabel.setForeground(Color.red);
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
    
    final JPanel mUpper = new JPanel();
    mUpper.setLayout(new BorderLayout());
    // Set Background Color
    mUpper.setBackground(Color.WHITE);
    // Set An Empty Border
    mUpper.setBorder(BorderFactory.createEmptyBorder());
    mUpper.setMinimumSize(new Dimension(10, 10));
    mUpper.setMaximumSize(new Dimension(10000, 3000));
    
    initUpperCompos();
    
    mUpper.add(mSceneFlowToolBar, BorderLayout.NORTH);
    mUpper.add(mSplitPane, BorderLayout.CENTER);
    mUpper.add(mFooterLabel, BorderLayout.SOUTH);

    setTopComponent(mUpper);
    mCodeEditor.setMinimumSize(new Dimension(10, 10));
    mCodeEditor.setMaximumSize(new Dimension(10000, 3000));
    setBottomComponent(mCodeEditor);

    if (mEditorProject.getEditorConfig().sSHOW_CODEEDITOR) {
      showAuxiliaryEditor();
    }

    mCodeEditor.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        if (mCodeEditor.getSize().height == 0) {
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = false;
        } else {
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = true;
        }
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

  public WorkSpacePanel getWorkSpace() {
    return mWorkSpacePanel;
  }

  public UndoManager getUndoManager() {
    return mUndoManager;
  }

  // TODO: adding not explicit but via refresh method
  public void addActiveSuperNode(SuperNode supernode) {
    mActiveSuperNodes.addLast(supernode);
    mSceneFlowToolBar.addPathComponent(supernode);
    // mWorkSpacePanel.setActiveSuperNode(supernode);
  }

  public SuperNode removeActiveSuperNode() {
    if (mActiveSuperNodes.size() == 1) return null;
    mActiveSuperNodes.removeLast();
    mSceneFlowToolBar.removePathComponent();
    // mWorkSpacePanel.setActiveSuperNode(getActiveSuperNode());
    return getActiveSuperNode();
  }

  public SuperNode getActiveSuperNode() {
    return mActiveSuperNodes.getLast();
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
    mSceneFlowToolBar.refresh();
  }
  
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
    public Object getTransferData(DataFlavor flavor) {
      if (isDataFlavorSupported(flavor)) {
        return image;
      }

      return null;
    }

    public DataFlavor[] getTransferDataFlavors() {
      return flavors;
    }

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

  // Update when an event happened
  @Override
  public void update(final Object event) {
    if (event instanceof TreeEntrySelectedEvent) {
      // Show the auxiliary editor
      //showAuxiliaryEditor();
    } else if (event instanceof ElementSelectedEvent) {
      Object edited = ((ElementSelectedEvent) event).getElement();
      if (edited instanceof EditorComponent) {
        mCodeEditor.setEditedObject((EditorComponent) edited);
      } else {
        mCodeEditor.setEditedObject(null);
      } 
    }
  }

}
