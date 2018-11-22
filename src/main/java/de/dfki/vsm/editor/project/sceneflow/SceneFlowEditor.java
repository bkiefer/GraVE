package de.dfki.vsm.editor.project.sceneflow;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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

//~--- non-JDK imports --------------------------------------------------------
import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

import de.dfki.vsm.editor.NameEditor;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.project.EditorProject;
import de.dfki.vsm.editor.util.IDManager;
import de.dfki.vsm.model.flow.SceneFlow;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 *
 * This class should contain all objects and information that are necessary
 * to work on the WHOLE Sceneflow, which is in mSceneFlow
 */
@SuppressWarnings("serial")
public final class SceneFlowEditor extends JPanel {

  // The singelton logger instance
  //private final Logger mLogger = LoggerFactory.getLogger(SceneFlowEditor.class);

  // TODO: move undo manager up at least to project editor
  private UndoManager mUndoManager = null;

  //
  private final EditorProject mEditorProject;

  private final SceneFlow mSceneFlow;
  private final IDManager mIDManager; // manages new IDs for the SceneFlow

  private final LinkedList<SuperNode> mActiveSuperNodes;

  // The GUI components of the editor
  private final WorkSpacePanel mWorkSpacePanel;
  private final SceneFlowToolBar mSceneFlowToolBar;
  private final SceneFlowPalettePanel mStaticElementsPanel;
  private final NameEditor mNameEditor;
  private final SceneFlowElementPanel mDynamicElementsPanel;
  private final JPanel mNewElementDisplay;
  private final JLabel mFooterLabel;
  private final JSplitPane mSplitPane;
  private final JScrollPane mWorkSpaceScrollPane;
  private final EventDispatcher mEventCaster = EventDispatcher.getInstance();

  // Create a sceneflow editor
  public SceneFlowEditor(final EditorProject project) {

    // Initialize the editor project
    mEditorProject = project;

    // PREPARE THE VERTICAL SPLIT REGION (NOW USED AS CODE EDITOR)
    /*
    final Polygon pUp = new Polygon();
    pUp.addPoint(1, 4);
    pUp.addPoint(5, 0);
    pUp.addPoint(9, 4);

    final Polygon pDown = new Polygon();
    pDown.addPoint(13, 0);
    pDown.addPoint(17, 4);
    pDown.addPoint(21, 0);
    */
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
//
//                        // graphics.setColor(new Color(100, 100, 100));
            graphics.fillRect((r.width / 2 - 25), 0, 50, r.height);
//                        graphics.drawPolygon(pUp);
//                        graphics.fillPolygon(pUp);
//                        graphics.drawPolygon(pDown);
//                        graphics.fillPolygon(pDown);
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
    mSceneFlow = project.getSceneFlow();
    mIDManager = new IDManager(mSceneFlow);
    mActiveSuperNodes = new LinkedList<SuperNode>();
    mActiveSuperNodes.addLast(mSceneFlow);

    // The center component is the workspace
    mWorkSpacePanel = new WorkSpacePanel(this, mEditorProject);
    mWorkSpacePanel.setTransferHandler(new SceneFlowImage());

    mWorkSpaceScrollPane = new JScrollPane(mWorkSpacePanel);
    mWorkSpaceScrollPane.getVerticalScrollBar().setUI(new WindowsScrollBarUI());
    mWorkSpaceScrollPane.getHorizontalScrollBar().setUI(new WindowsScrollBarUI());
    mWorkSpaceScrollPane.setBorder(BorderFactory.createEtchedBorder());

    // The west component is the workbar
    mFooterLabel = new JLabel();
    mDynamicElementsPanel = new SceneFlowElementPanel(mEditorProject);
    mStaticElementsPanel = new SceneFlowPalettePanel();
    mNameEditor = new NameEditor();

    // TOOLBAR: NORTH ELEMENT
    mSceneFlowToolBar = new SceneFlowToolBar(this, mEditorProject);
    // TODO: adding not explicit but via refresh method
    mSceneFlowToolBar.addPathComponent(getSceneFlow()); // ADD FIRST NODE
    //
    setLayout(new BorderLayout());
    add(mSceneFlowToolBar, BorderLayout.NORTH);

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
    NodeSelectedEvent e = new NodeSelectedEvent(this, getActiveSuperNode());
    mEventCaster.convey(e);

    //
    mFooterLabel.setForeground(Color.red);
    add(mFooterLabel, BorderLayout.SOUTH);

  }

  public void setViewPosition(Point p) {
    mWorkSpaceScrollPane.getViewport().setViewPosition(p);
  }

  /**
   *
   *
   *
   *
   *
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
      mEditorProject.getEditorConfig().save(mEditorProject.getProjectFile()
              .getParentFile());

  }

  public void expandTree() {
    mDynamicElementsPanel.expandTree();
  }


  public boolean isElementEditorVisible() {
    return mNewElementDisplay.isVisible();
  }

  public boolean isElementDisplayVisible() {
    return mNewElementDisplay.isVisible();
  }

  public SceneFlow getSceneFlow() {
    return mEditorProject.getSceneFlow();
  }

  public IDManager getIDManager() {
    return mIDManager;
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


  public JSplitPane getSplitPane() {
    return mSplitPane;
  }

  public JLabel getFooterLabel() {
    return mFooterLabel;
  }

  public final void refresh() {
    // Refresh editor toolbar
    mSceneFlowToolBar.refresh();
    mDynamicElementsPanel.refresh();
    mWorkSpacePanel.refresh();
  }

  private class SceneFlowImage extends TransferHandler implements Transferable {

    private final DataFlavor flavors[]
            = {
              DataFlavor.imageFlavor
            };
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
