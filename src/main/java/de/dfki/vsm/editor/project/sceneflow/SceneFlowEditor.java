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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.undo.UndoManager;

//~--- non-JDK imports --------------------------------------------------------
import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.event.ElementEditorToggledEvent;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.project.EditorProject;
import de.dfki.vsm.editor.project.sceneflow.attributes.NameEditor;
import de.dfki.vsm.editor.project.sceneflow.elements.SceneFlowElementPanel;
import de.dfki.vsm.editor.project.sceneflow.elements.SceneFlowPalettePanel;
import de.dfki.vsm.editor.project.sceneflow.workspace.WorkSpacePanel;
import de.dfki.vsm.editor.util.SceneFlowManager;
import de.dfki.vsm.model.sceneflow.chart.SceneFlow;
import de.dfki.vsm.model.sceneflow.chart.SuperNode;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public final class SceneFlowEditor extends JPanel implements EventListener {

  // The singelton logger instance
  //private final Logger mLogger = LoggerFactory.getLogger(SceneFlowEditor.class);

  // TODO: move undo manager up at least to project editor
  private UndoManager mUndoManager = null;

  //
  private final SceneFlow mSceneFlow;
  private final EditorProject mEditorProject;

  // TODO: remove sceneflow manager
  private final SceneFlowManager mSceneFlowManager;

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

    // Initialize the sceneflow
    mSceneFlow = mEditorProject.getSceneFlow();

    // PREPARE THE VERTICAL SPLIT REGION (NOW USED AS CODE EDITOR)
    final Polygon pUp = new Polygon();
    pUp.addPoint(1, 4);
    pUp.addPoint(5, 0);
    pUp.addPoint(9, 4);

    final Polygon pDown = new Polygon();
    pDown.addPoint(13, 0);
    pDown.addPoint(17, 4);
    pDown.addPoint(21, 0);
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
                ElementEditorToggledEvent ev = new ElementEditorToggledEvent(this);
                mEventCaster.convey(ev);
            }
          }
        };
      }
    });
    mUndoManager = new UndoManager();
    mSceneFlowManager = new SceneFlowManager(mSceneFlow);

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
    mSceneFlowToolBar.addPathComponent(mSceneFlow); // ADD FIRST NODE
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
    mNewElementDisplay.setVisible(Boolean.valueOf(Preferences.getProperty("showelements"))
            ? true
            : false);
    // INITIALIZE THE SPLIT PANEL WITH WORK SPACE AND ELEMENTEDITOR
    mWorkSpaceScrollPane.setMinimumSize(new Dimension(10, 10));
    mWorkSpaceScrollPane.setMaximumSize(new Dimension(10000, 3000));
    mSplitPane.setRightComponent(mWorkSpaceScrollPane);
    mSplitPane.setResizeWeight(0.0);
    mSplitPane.setLeftComponent(mNewElementDisplay);
    if (Boolean.valueOf(Preferences.getProperty("showelements"))) {
      mSplitPane.setDividerLocation(
          Integer.parseInt(mEditorProject.getEditorConfig()
              .getProperty("propertiesdividerlocation")));
      //THIS EVENT IS CASTED ONLY TO ACTIVATE THE ELEMENT EDITOR WITH THE INFO OF THE CURRENT PROJECT
    } else {
      mSplitPane.setDividerLocation(1.0d);
    }
    add(mSplitPane, BorderLayout.CENTER);

    mSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent pce) {

        // solve issue here
        if (Preferences.getProperty("showelements").equals("true")) {
          mEditorProject.getEditorConfig().setProperty("propertiesdividerlocation", "" + mSplitPane.getDividerLocation());
        }
      }
    });

    //ACTIVATE THE CONTENT OF THE ElementEditor
    NodeSelectedEvent e = new NodeSelectedEvent(this, getSceneFlowManager().getCurrentActiveSuperNode());
    mEventCaster.convey(e);

    //
    mFooterLabel.setForeground(Color.red);
    add(mFooterLabel, BorderLayout.SOUTH);

  }

  // Update the visualization
  @Override
  public final void update(final EventObject event) {
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
    if (Boolean.valueOf(Preferences.getProperty("showelements"))) {
      mNewElementDisplay.setVisible(false);
      Preferences.setProperty("showelements", "false");
      Preferences.save();
      mSplitPane.setDividerLocation(1d);
    } else {
      mNewElementDisplay.setVisible(true);
      Preferences.setProperty("showelements", "true");
      Preferences.save();
      mSplitPane.setDividerLocation(
          Integer.parseInt(mEditorProject.getEditorConfig().getProperty("propertiesdividerlocation")));
    }

  }

  public void expandTree() {
    mDynamicElementsPanel.expandTree();
  }


  public boolean isElementEditorVisible() {
    return mNewElementDisplay.isVisible();
  }

  public void showElementDisplay() {
    if (mNewElementDisplay.isVisible()) {
      mNewElementDisplay.setVisible(false);
      Preferences.setProperty("showelements", "false");
      Preferences.save();
    } else {
      mNewElementDisplay.setVisible(true);
      Preferences.setProperty("showelements", "true");
      Preferences.save();
      Preferences.save();
    }
  }

  public boolean isElementDisplayVisible() {
    return mNewElementDisplay.isVisible();
  }

  public final SceneFlowToolBar getToolBar() {
    return mSceneFlowToolBar;
  }

  public SceneFlowManager getSceneFlowManager() {
    return mSceneFlowManager;
  }

  public SceneFlow getSceneFlow() {
    return mSceneFlow;
  }

  public WorkSpacePanel getWorkSpace() {
    return mWorkSpacePanel;
  }

  public UndoManager getUndoManager() {
    return mUndoManager;
  }

  // TODO: adding not explicit but via refresh method
  public void addPathComponent(SuperNode supernode) {

    // TODO: adding not explicit but via refresh method
    mSceneFlowToolBar.addPathComponent(supernode);
  }

  public SuperNode removePathComponent() {
    return mSceneFlowToolBar.removePathComponent();
  }

  public void setMessageLabelText(String value) {
      mFooterLabel.setText(value);
  }

  public void close() {
    // Delete all observers
    // mObservable.deleteObservers();

    // Cleanup worspace
    mWorkSpacePanel.cleanup();
  }


  public JSplitPane getSplitPane() {
    return mSplitPane;
  }

  public JLabel getFooterLabel() {
    return mFooterLabel;
  }

//
//  /**
//   *
//   *
//   *
//   *
//   *
//   */
//  private class Observable extends java.util.Observable {
//
//      public void update(Object obj) {
//          setChanged();
//          notifyObservers(obj);
//      }
//  }
  public final void refresh() {

    // Print some information
    //mLogger.message("Refreshing '" + this + "'");
    // Refresh editor toolbar
    mSceneFlowToolBar.refresh();
    mStaticElementsPanel.refresh();
    mDynamicElementsPanel.refresh();
    mWorkSpacePanel.refresh();
    //mElementEditor.refresh();
  }

  class SceneFlowImage extends TransferHandler implements Transferable {

    private final DataFlavor flavors[]
            = {
              DataFlavor.imageFlavor
            };
    private JPanel source;
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
      source = null;
      image = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);

      if (comp instanceof JPanel) {
        JPanel panel = (JPanel) comp;
        Graphics g = image.getGraphics();

        comp.paint(g);
        g.dispose();
        source = panel;

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
