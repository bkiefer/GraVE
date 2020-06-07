package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.getPrefs;

import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;

import javax.swing.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.action.UndoRedoProvider;
import de.dfki.grave.editor.dialog.OptionsDialog;
import de.dfki.grave.editor.dialog.SaveFileDialog;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.grave.util.ResourceLoader;
import de.dfki.grave.util.evt.EventDispatcher;
import de.dfki.grave.util.evt.EventListener;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings({ "serial" })
public class SceneFlowToolBar extends JToolBar implements EventListener {

  /**
   * ************************************************************************************************************************
   * ICONS INITIALIZATION
   * *************************************************************************************************************************
   */
  private final ImageIcon ICON_PLAY_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/play.png");
  private final ImageIcon ICON_PLAY_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/play_blue.png");
  private final ImageIcon ICON_PLAY_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/play_disabled.png");

  private final ImageIcon ICON_STOP_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/stop.png");
  private final ImageIcon ICON_STOP_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/stop_blue.png");
  private final ImageIcon ICON_STOP_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/stop_disabled.png");

  private final ImageIcon ICON_PAUSE_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/pause.png");
  private final ImageIcon ICON_PAUSE_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/pause_blue.png");

  private final ImageIcon ICON_MORE_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/more.png");
  private final ImageIcon ICON_MORE_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/more_blue.png");

  private final ImageIcon ICON_LESS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/less.png");
  private final ImageIcon ICON_LESS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/less_blue.png");

  private final ImageIcon ICON_SAVE_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/save_icon.png");
  private final ImageIcon ICON_SAVE_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/save_icon_blue.png");
  private final ImageIcon ICON_SAVE_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/save_icon_disable.png");

  private final ImageIcon ICON_UNDO_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/undo_icon.png");
  private final ImageIcon ICON_UNDO_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/undo_icon_blue.png");
  private final ImageIcon ICON_UNDO_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/undo_icon_disabled.png");

  private final ImageIcon ICON_REDO_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/redo_icon.png");
  private final ImageIcon ICON_REDO_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/redo_icon_blue.png");
  private final ImageIcon ICON_REDO_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/redo_icon_disabled.png");

  private final ImageIcon ICON_NORMALIZE_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/normalize_edges_gray.png");
  private final ImageIcon ICON_NORMALIZE_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/normalize_edges_blue.png");

  private final ImageIcon ICON_STRAIGHTEN_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/straighten_gray.png");
  private final ImageIcon ICON_STRAIGHTEN_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/straighten_blue.png");

  private final ImageIcon ICON_VARS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/var.png");
  private final ImageIcon ICON_VARS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/var_blue.png");
  private final ImageIcon ICON_VARS_HIDDEN_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/var_hidden.png");
  private final ImageIcon ICON_VARS_HIDDEN_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/var_hidden_blue.png");

  private final ImageIcon ICON_STACK_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/stack_icon.png");
  private final ImageIcon ICON_STACK_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/stack_icon_blue.png");

  private final ImageIcon ICON_UP_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/up.png");
  private final ImageIcon ICON_UP_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/up_blue.png");

  private final ImageIcon ICON_SCREENSHOT_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/screenshot.png");
  private final ImageIcon ICON_SCREENSHOT_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/screenshot_blue.png");

  private final ImageIcon ICON_NOZOOM_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/nozoom.png");
  private final ImageIcon ICON_NOZOOM_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/nozoom_blue.png");

  private final ImageIcon ICON_ZOOMIN_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/zoomin.png");
  private final ImageIcon ICON_ZOOMIN_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/zoomin_blue.png");

  private final ImageIcon ICON_ZOOMOUT_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/zoomout.png");
  private final ImageIcon ICON_ZOOMOUT_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/zoomout_blue.png");

  private final ImageIcon ICON_SETTINGS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/settings.png");
  private final ImageIcon ICON_SETTINGS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/settings_blue.png");

  private final ImageIcon ICON_PROJECT_SETTINGS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/project_config.png");
  private final ImageIcon ICON_PROJECT_SETTINGS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/project_config_blue.png");

  /**
   * ***********************************************************************************************************************
   */
  // The singleton app instance
  private final AppFrame mEditorInstance = AppFrame.getInstance();
  // The singleton system clipboard
  private final Clipboard mSystemClipBoard = getToolkit().getSystemClipboard();
  // The parent sceneflow editor
  private final ProjectEditor mEditor;

  // The button GUI components
  private JButton mTogglePalette;
  //private JButton mToggleElementEditor;
  private JButton mPlayButton;
  private JButton mStopButton;
  //private JButton mShowVarButton;
  private JButton mSaveProject;
  private JButton mPreferences;
  private JButton mProjectSettings;

  //Dimension for buttons
  private Dimension tinyButtonDim = new Dimension(40, 40);
  private Dimension smallButtonDim = new Dimension(50, 40);

  // Path Display GUI Components
  private BreadCrumb mBreadCrumb; 

  // Construct a sceneflow editor toolbar
  public SceneFlowToolBar(final ProjectEditor editor) {
    // Create a horizontal toolbar
    super("SceneFlowToolBar", JToolBar.HORIZONTAL);
    //Set maximum size
    setMinimumSize(new Dimension((int) (getPrefs().SCREEN_HORIZONTAL * 0.6), 40));
    //setPreferredSize(new Dimension(SCREEN_HORIZONTAL, 40));
    setMaximumSize(new Dimension(getPrefs().SCREEN_HORIZONTAL, 40));
    // Initialize the sceneflow editor
    mEditor = editor;
    // Initialize the GUI components
    setRollover(true);
    setFloatable(false);
    setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    initComponents();
    // Add the sceneflowtoolbar to the event multicaster
    EventDispatcher.getInstance().register(this);
  }
  
  public void close() {
    EventDispatcher.getInstance().remove(this);
  }

  @Override
  public void update(Object event) {
    refreshButtons();
    if (event instanceof ProjectChangedEvent) {
      if (((ProjectChangedEvent)event).getSource() instanceof OptionsDialog) {
        mSaveProject.setEnabled(true);
      }
    }
  }

  private WorkSpace getWorkSpace() {
    return mEditor.getWorkSpace();
  }

  private void sanitizeButton(JButton b, Dimension bDim) {

    b.setMinimumSize(bDim);
    b.setMaximumSize(bDim);
    b.setPreferredSize(bDim);
    b.setOpaque(false);
    b.setFocusable(false);
    b.setContentAreaFilled(false);
    b.setText(null);
    b.setBorder(BorderFactory.createEmptyBorder());
  }

  private JSeparator createSeparator() {
    JSeparator js = new JSeparator(SwingConstants.VERTICAL);
    js.setPreferredSize(new Dimension(10, 30));
    js.setMinimumSize(new Dimension(10, 30));
    js.setMaximumSize(new Dimension(10, 30));
    return js;
  }

  /**
   *
   */
  private void initComponents() {

    //menu separator
    // 3 Layout sections in toolbar
    // | Element Space |  Sceneflow Space | Property Space
    //
    // Element space
    add(Box.createHorizontalStrut(2));
    /**
     * LESS AND MORE BUTTONS ARE INVERTED TO MATCH WITH THE LEFT SIZE
     */
    mTogglePalette = add(new AbstractAction("ACTION_SHOW_ELEMENTS",
            mEditor.getEditorProject().getEditorConfig().sSHOW_ELEMENTS
            ? ICON_MORE_STANDARD
            : ICON_LESS_STANDARD) {
      public void actionPerformed(ActionEvent evt) {
        mEditor.toggleElementEditor();
        refreshButtons();
      }
    });
    mTogglePalette.setRolloverIcon(
        mEditor.getEditorProject().getEditorConfig().sSHOW_ELEMENTS
            ? ICON_MORE_ROLLOVER
            : ICON_LESS_ROLLOVER);
    sanitizeButton(mTogglePalette, tinyButtonDim);
    //add(Box.createHorizontalGlue());
    add(Box.createHorizontalStrut(200));

    //EDIT PROJECT SECTION
    //Save project
    mSaveProject = add(new AbstractAction("ACTION_SAVEPROJECT", ICON_SAVE_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditorInstance.save();
        mSaveProject.setEnabled(false);
        mProjectSettings.setEnabled(true);
      }
    });
    mSaveProject.setRolloverIcon(ICON_SAVE_ROLLOVER);
    mSaveProject.setDisabledIcon(ICON_SAVE_DISABLED);
    mSaveProject.setToolTipText("Save current project");
    sanitizeButton(mSaveProject, tinyButtonDim);
    mSaveProject.setEnabled(false);


    /*add(Box.createHorizontalStrut(10));
        add(createSeparator());*/
    //Preferences
    mPreferences = add(new AbstractAction("ACTION_SHOW_OPTIONS", ICON_SETTINGS_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditorInstance.showOptions();
      }
    });
    mPreferences.setRolloverIcon(ICON_SETTINGS_ROLLOVER);
    mPreferences.setToolTipText("Project Preferences");

    mProjectSettings = add(new AbstractAction("ACTION_SHOW_OPTIONS", ICON_PROJECT_SETTINGS_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (! mEditor.getEditorProject().isNew()) {
          /*
          PropertyManagerGUI gui = new PropertyManagerGUI();
          gui.init(mEditorProject);
          gui.setVisible(true);
          */
        } else {
          mProjectSettings.setEnabled(false);
        }

      }
    });
    if (! mEditor.getEditorProject().isNew()) {
      mProjectSettings.setEnabled(false);
    }
    mProjectSettings.setRolloverIcon(ICON_PROJECT_SETTINGS_ROLLOVER);
    mProjectSettings.setToolTipText("Project Settings");

    sanitizeButton(mProjectSettings, tinyButtonDim);

    add(Box.createHorizontalStrut(20));
    add(createSeparator());

    //Undo last action
    JButton mUndo = add(UndoRedoProvider.getInstance().getUndoAction());
    mUndo.setIcon(ICON_UNDO_STANDARD);
    mUndo.setRolloverIcon(ICON_UNDO_ROLLOVER);
    mUndo.setDisabledIcon(ICON_UNDO_DISABLED);
    mUndo.setToolTipText("Undo last action");
    sanitizeButton(mUndo, tinyButtonDim);

    //Redo last action
    JButton mRedo = add(UndoRedoProvider.getInstance().getRedoAction());
    mRedo.setIcon(ICON_REDO_STANDARD);
    mRedo.setRolloverIcon(ICON_REDO_ROLLOVER);
    mRedo.setDisabledIcon(ICON_REDO_DISABLED);
    mRedo.setToolTipText("Redo last action");
    sanitizeButton(mRedo, tinyButtonDim);
    //******************************************************************************************************

    //******************************************************************************************************
    //PROJECT EDITION SECTION
    // Button to straighten all edeges
    JButton mNormalize = add(new AbstractAction("ACTION_NORMALIZE", ICON_NORMALIZE_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        getWorkSpace().normalizeAllEdges();
      }
    });
    mNormalize.setRolloverIcon(ICON_NORMALIZE_ROLLOVER);
    mNormalize.setToolTipText("Normalize all edges");
    sanitizeButton(mNormalize, tinyButtonDim);
    // Button to straighten all edeges
    JButton mStraighten = add(new AbstractAction("ACTION_STRAIGHTEN", ICON_STRAIGHTEN_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        getWorkSpace().straightenAllEdges();
      }
    });
    mStraighten.setRolloverIcon(ICON_STRAIGHTEN_ROLLOVER);
    mStraighten.setToolTipText("Straighten all edges");
    sanitizeButton(mStraighten, tinyButtonDim);
    // The Show Variables Button
    /*
    mShowVarButton = add(new AbstractAction("ACTION_SHOW_VARIABLES",
            Boolean.valueOf(Preferences.getProperty("showVariables")) ? ICON_VARS_STANDARD : ICON_VARS_HIDDEN_STANDARD) {
      public void actionPerformed(ActionEvent evt) {
        updateShowVarsButtons();
        revalidate();
        repaint(100);
      }
    });
    mShowVarButton.setRolloverIcon(Boolean.valueOf(Preferences.getProperty("showVariables")) ? ICON_VARS_ROLLOVER : ICON_VARS_HIDDEN_ROLLOVER);
    mShowVarButton.setToolTipText(Boolean.valueOf(Preferences.getProperty("showVariables")) ? "Show Variables" : "Hide Variables");
    // Format The Button As Tiny
    sanitizeButton(mShowVarButton, tinyButtonDim);
    */
    add(Box.createHorizontalStrut(10));
    add(createSeparator());
    add(Box.createHorizontalStrut(10));
    // The Play SceneFlow Button
    mPlayButton = add(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditorInstance.save();
        mStopButton.setEnabled(true);
      }
    });
    mPlayButton.setIcon(ICON_PLAY_STANDARD);
    mPlayButton.setRolloverIcon(ICON_PLAY_ROLLOVER);
    mPlayButton.setDisabledIcon(ICON_PLAY_DISABLED);
    mPlayButton.setToolTipText("Initialize project/Start the execution of the sceneflow");
    sanitizeButton(mPlayButton, tinyButtonDim);
    // The Stop SceneFlow Button
    mStopButton = add(new AbstractAction("ACTION_STOP", ICON_STOP_STANDARD) {
      @Override
      public final void actionPerformed(ActionEvent e) {
        mStopButton.setEnabled(false);
      }
    });
    mStopButton.setRolloverIcon(ICON_STOP_ROLLOVER);
    mStopButton.setDisabledIcon(ICON_STOP_DISABLED);
    mStopButton.setToolTipText("Shutdown project/stop the execution of the sceneflow");
    // Format The Button As Tiny
    sanitizeButton(mStopButton, tinyButtonDim);
    mStopButton.setEnabled(false);

    /*
        JButton b = add(new AbstractAction("ACTION_WINDOW", ICON_STACK_STANDARD) {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorInstance.getInstance().showMonitor();
            }
        });
        b.setRolloverIcon(ICON_STACK_ROLLOVER);
        b.setToolTipText("Variable Manager");
        sanitizeButton(b, tinyButtonDim);
        add(Box.createHorizontalStrut(10));
        add(createSeparator());
     */
    //******************************************************************************************************
    // CONTROL OF NODES
    // Add Some Horizontal Space
    mBreadCrumb = new BreadCrumb();
    add(mBreadCrumb);

    //UP TO PARENT NODE
    JButton b = add(new AbstractAction("ACTION_LEVEL_UP", ICON_UP_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        getWorkSpace().decreaseWorkSpaceLevel();
      }
    });
    b.setToolTipText("Up to parent node");
    b.setRolloverIcon(ICON_UP_ROLLOVER);
    sanitizeButton(b, tinyButtonDim);
    add(Box.createHorizontalStrut(10));
    add(createSeparator());
    //******************************************************************************************************
    // SCREEN CONTROL
    //SCREENSHOT BUTTON
    Action action = new AbstractAction("ACTION_SCREEN_SHOT", ICON_SCREENSHOT_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) {
        TransferHandler handler = getWorkSpace().getTransferHandler();

        if (handler != null) {
          handler.exportToClipboard(getWorkSpace(), mSystemClipBoard, TransferHandler.COPY);
          SaveFileDialog fileChooser = new SaveFileDialog();
          fileChooser.save();
        } else {
          System.err.println("handler null");
        }
      }
    };
    action.putValue(Action.SHORT_DESCRIPTION, "Add/Remove window");
    b = add(action);
    b.setToolTipText("Take a screenshot");
    b.setRolloverIcon(ICON_SCREENSHOT_ROLLOVER);

    //ZOOM IN BUTTON
    sanitizeButton(b, smallButtonDim);
    b = add(new AbstractAction("ACTION_ZOOM_IN", ICON_ZOOMIN_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) {
        getWorkSpace().zoomIn();
      }
    });
    b.setToolTipText("Zoom In");
    b.setRolloverIcon(ICON_ZOOMIN_ROLLOVER);

    //ZOOM ORIGINAL SIZE BUTTON
    sanitizeButton(b, smallButtonDim);
    b = add(new AbstractAction("ACTION_ZOOM_ORIG", ICON_NOZOOM_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) {
        getWorkSpace().nozoom();
      }
    });
    b.setToolTipText("Zoom 100%");
    b.setRolloverIcon(ICON_NOZOOM_ROLLOVER);

    //ZOOM OUT BUTTON
    sanitizeButton(b, smallButtonDim);
    b = add(new AbstractAction("ACTION_ZOOM_OUT", ICON_ZOOMOUT_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) {
        getWorkSpace().zoomOut();
      }
    });
    b.setToolTipText("Zoom Out");
    b.setRolloverIcon(ICON_ZOOMOUT_ROLLOVER);
    sanitizeButton(b, smallButtonDim);
    //add(Box.createHorizontalGlue());
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  private void refreshButtons() {
    // Print some information
    //mLogger.message("Refreshing Buttons Of '" + this + "'");
    //*************************************
    // Refresh the button SAVE when project have been changed
    mSaveProject.setEnabled(mEditor.getEditorProject().hasChanged());
    //*************************************

    //*************************************
    // Refresh the element display buttons
    mTogglePalette.setIcon(mEditor.isElementDisplayVisible()
            ? ICON_MORE_STANDARD : ICON_LESS_STANDARD);
    mTogglePalette.setRolloverIcon(mEditor.getEditorProject().getEditorConfig().sSHOW_ELEMENTS
            ? ICON_MORE_ROLLOVER : ICON_LESS_ROLLOVER);
  }

  /*
  private void updateShowVarsButtons() {
    mShowVarButton.setIcon(mEditor.getWorkSpace().isVarBadgeVisible()
            ? ICON_VARS_HIDDEN_STANDARD
            : ICON_VARS_STANDARD);
    mShowVarButton.setRolloverIcon(mEditor.getWorkSpace().isVarBadgeVisible()
            ? ICON_VARS_HIDDEN_ROLLOVER
            : ICON_VARS_ROLLOVER);
    mShowVarButton.setToolTipText(mEditor.getWorkSpace().isVarBadgeVisible()
            ? "Hide Variables"
            : "Show Variables");
  }
  */
  
  public void addPathComponent(SuperNode supernode) {
    mBreadCrumb.addPathComponent(supernode);
  }

  public void removePathComponent() {
    mBreadCrumb.removePathComponent();
  }

  public final void refresh() {
    refreshButtons();
    mBreadCrumb.refreshDisplay();
  }

}
