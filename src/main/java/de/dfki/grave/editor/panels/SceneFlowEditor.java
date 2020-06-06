package de.dfki.grave.editor.panels;

//~--- JDK imports ------------------------------------------------------------
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.undo.UndoManager;

import de.dfki.grave.model.flow.SceneFlow;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.grave.model.project.EditorProject;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 *
 * This class should contain all objects and information that are necessary
 * to work on the WHOLE Sceneflow, which is in mSceneFlow
 */
@SuppressWarnings({ "serial" })
public final class SceneFlowEditor extends JPanel {

  // The singelton logger instance
  //private final Logger mLogger = LoggerFactory.getLogger(SceneFlowEditor.class);

  // TODO: move undo manager up at least to project editor
  private UndoManager mUndoManager = null;

  //
  private final EditorProject mEditorProject;

  private final LinkedList<SuperNode> mActiveSuperNodes;

  // The GUI components of the editor
  private final WorkSpacePanel mWorkSpacePanel;
  private final SceneFlowToolBar mSceneFlowToolBar;
  private final NameEditor mNameEditor;
  private final SceneFlowElementPanel mDynamicElementsPanel;
  private final JPanel mNewElementDisplay;
  private final JLabel mFooterLabel;
  private final JSplitPane mSplitPane;

  // Create a sceneflow editor
  public SceneFlowEditor(final EditorProject project) {

    // Initialize the editor project
    mEditorProject = project;

    // PREPARE THE VERTICAL SPLIT REGION (NOW USED AS CODE EDITOR)
    mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mSplitPane.setBorder(BorderFactory.createEmptyBorder());
    mSplitPane.setContinuousLayout(true);
    mSplitPane.setUI(new BasicSplitPaneUI() {
      @Override
      public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
          @Override
          public void setBorder(Border b) {
          }

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
    setLayout(new BorderLayout());
    add(mSceneFlowToolBar, BorderLayout.NORTH);

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
    // mNewElementDisplay.add(new JSeparator(JSeparator.HORIZONTAL));
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
    add(mSplitPane, BorderLayout.CENTER);

    mSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent pce) {

        // solve issue here
        if (mEditorProject.getEditorConfig().sSHOW_ELEMENTS) {
          mEditorProject.getEditorConfig().sELEMENTS_DIVIDER_LOCATION = mSplitPane.getDividerLocation();
        }
      }
    });

    //ACTIVATE THE CONTENT OF THE ElementEditor
    //mEventCaster.convey(new ElementSelectedEvent(getActiveSuperNode()));

    //
    mFooterLabel.setForeground(Color.red);
    add(mFooterLabel, BorderLayout.SOUTH);
    // Set Background Color
    setBackground(Color.WHITE);
    // Set An Empty Border
    setBorder(BorderFactory.createEmptyBorder());
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

  public void close() {
    // Cleanup workspace
    mWorkSpacePanel.cleanup();
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
  
  private class SceneFlowImage extends TransferHandler implements Transferable {

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
}
