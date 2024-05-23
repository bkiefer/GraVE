package de.dfki.grave.editor.panels;

import static de.dfki.grave.app.Preferences.getPrefs;
import static de.dfki.grave.editor.panels.Icons.*;

import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import de.dfki.grave.app.AppFrame;
import de.dfki.grave.editor.dialog.SaveFileDialog;
import de.dfki.grave.model.flow.SuperNode;

/**
 * @author Bernd Kiefer
 *
 * The tool bar that is shown above the main editor work panel and the
 * sceneflow element panel
 */
@SuppressWarnings({ "serial" })
public class ProjectEditorToolBar extends JToolBar {

  /**
   * ***********************************************************************************************************************
   */
  // The singleton system clipboard
  private final Clipboard mSystemClipBoard = getToolkit().getSystemClipboard();

  // The button GUI components
  private JButton mTogglePalette;
  //private JButton mToggleElementEditor;
  //private JButton mPlayButton;
  //private JButton mStopButton;
  //private JButton mShowVarButton;
  private JButton mSaveProject;
  private JButton mPreferences;
  //private JButton mProjectSettings;
  private JButton undo, redo;

  //Dimension for buttons
  private Dimension tinyButtonDim = new Dimension(35, 35);
  private Dimension smallButtonDim = new Dimension(40, 35);

  // Path Display GUI Components
  private BreadCrumb mBreadCrumb;

  // Construct a sceneflow editor toolbar
  public ProjectEditorToolBar(final ProjectEditor editor) {
    // Create a horizontal toolbar
    super("SceneFlowToolBar", JToolBar.HORIZONTAL);
    //Set maximum size
    setMinimumSize(new Dimension((int) (getPrefs().SCREEN_HORIZONTAL * 0.6), 40));
    //setPreferredSize(new Dimension(SCREEN_HORIZONTAL, 40));
    setMaximumSize(new Dimension(getPrefs().SCREEN_HORIZONTAL, 40));
    // Initialize the GUI components
    setRollover(true);
    setFloatable(false);
    setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
    initComponents(editor);
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
    js.setPreferredSize(new Dimension(5, 30));
    js.setMinimumSize(new Dimension(5, 30));
    js.setMaximumSize(new Dimension(5, 30));
    return js;
  }

  /**
   *
   */
  private void initComponents(ProjectEditor mEditor) {

    //menu separator
    // 3 Layout sections in toolbar
    // | Element Space |  Sceneflow Space | Property Space
    //
    // Element space
    add(Box.createHorizontalStrut(2));
    /** Correct icons are shown after calling refreshButtons */
    mTogglePalette = add(new AbstractAction("ACTION_SHOW_ELEMENTS",
        ICON_MORE_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) {
        mEditor.toggleElementEditor();
        mEditor.refreshToolBar();
      }
    });
    mTogglePalette.setRolloverIcon(ICON_MORE_ROLLOVER);
    sanitizeButton(mTogglePalette, tinyButtonDim);
    //add(Box.createHorizontalGlue());
    //add(Box.createHorizontalStrut(200));
    add(createSeparator());

    //EDIT PROJECT SECTION
    //Save project
    mSaveProject = add(new AbstractAction("ACTION_SAVEPROJECT", ICON_SAVE_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        AppFrame.getInstance().save();
        mSaveProject.setEnabled(false);
        //mProjectSettings.setEnabled(true);
      }
    });
    mSaveProject.setRolloverIcon(ICON_SAVE_ROLLOVER);
    mSaveProject.setDisabledIcon(ICON_SAVE_DISABLED);
    mSaveProject.setToolTipText("Save current project");
    sanitizeButton(mSaveProject, tinyButtonDim);
    mSaveProject.setEnabled(false);

    //Preferences
    mPreferences = add(new AbstractAction("ACTION_SHOW_OPTIONS", ICON_SETTINGS_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        AppFrame.getInstance().showOptions();
      }
    });
    mPreferences.setRolloverIcon(ICON_SETTINGS_ROLLOVER);
    mPreferences.setToolTipText("Project Preferences");
    sanitizeButton(mPreferences, tinyButtonDim);
    add(Box.createHorizontalStrut(5));
    add(createSeparator());

    /*
    mProjectSettings = add(new AbstractAction("ACTION_SHOW_OPTIONS", ICON_PROJECT_SETTINGS_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (! mEditor.getEditorProject().isNew()) {
          //PropertyManagerGUI gui = new PropertyManagerGUI();
          //gui.init(mEditorProject);
          //gui.setVisible(true);
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
    */

    //Undo last action
    undo = add(mEditor.getUndoManager().getUndoAction());
    undo.setIcon(ICON_UNDO_STANDARD);
    undo.setRolloverIcon(ICON_UNDO_ROLLOVER);
    undo.setDisabledIcon(ICON_UNDO_DISABLED);
    undo.setToolTipText("Undo last action");
    sanitizeButton(undo, tinyButtonDim);

    //Redo last action
    redo = add(mEditor.getUndoManager().getRedoAction());
    redo.setIcon(ICON_REDO_STANDARD);
    redo.setRolloverIcon(ICON_REDO_ROLLOVER);
    redo.setDisabledIcon(ICON_REDO_DISABLED);
    redo.setToolTipText("Redo last action");
    sanitizeButton(redo, tinyButtonDim);
    //******************************************************************************************************

    //******************************************************************************************************
    //PROJECT EDITION SECTION
    // Button to straighten all edeges
    JButton mNormalize = add(new AbstractAction("ACTION_NORMALIZE", ICON_NORMALIZE_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditor.normalizeAllEdges();
      }
    });
    mNormalize.setRolloverIcon(ICON_NORMALIZE_ROLLOVER);
    mNormalize.setToolTipText("Normalize all edges");
    sanitizeButton(mNormalize, tinyButtonDim);
    /*
    // Button to straighten all edeges
    JButton mStraighten = add(new AbstractAction("ACTION_STRAIGHTEN", ICON_STRAIGHTEN_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditor.straightenAllEdges();
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
    add(Box.createHorizontalStrut(5));
    add(createSeparator());
    add(Box.createHorizontalStrut(5));
    /*
    // The Play SceneFlow Button
    mPlayButton = add(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        AppFrame.getInstance().save();
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

    //***********************************************************************
    // Zoom Control
    JButton b;

//  ZOOM IN BUTTON
    b = add(new AbstractAction("ACTION_ZOOM_IN", ICON_ZOOMIN_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) { mEditor.zoomIn(); }
    });
    b.setToolTipText("Zoom In");
    b.setRolloverIcon(ICON_ZOOMIN_ROLLOVER);

    //ZOOM ORIGINAL SIZE BUTTON
    sanitizeButton(b, tinyButtonDim);
    b = add(new AbstractAction("ACTION_ZOOM_ORIG", ICON_NOZOOM_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) { mEditor.nozoom(); }
    });
    b.setToolTipText("Zoom 100%");
    b.setRolloverIcon(ICON_NOZOOM_ROLLOVER);

    //ZOOM OUT BUTTON
    sanitizeButton(b, tinyButtonDim);
    b = add(new AbstractAction("ACTION_ZOOM_OUT", ICON_ZOOMOUT_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) { mEditor.zoomOut(); }
    });
    b.setToolTipText("Zoom Out");
    b.setRolloverIcon(ICON_ZOOMOUT_ROLLOVER);
    sanitizeButton(b, tinyButtonDim);

    add(Box.createHorizontalStrut(10));
    add(createSeparator());

    //***********************************************************************
    // CONTROL OF ACTIVE SUPERNODES

    //UP TO PARENT NODE
    b = add(new AbstractAction("ACTION_LEVEL_UP", ICON_UP_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditor.decreaseWorkSpaceLevel();
      }
    });
    b.setToolTipText("Up to parent node");
    b.setRolloverIcon(ICON_UP_ROLLOVER);
    sanitizeButton(b, tinyButtonDim);
    add(Box.createHorizontalStrut(5));

    mBreadCrumb = new BreadCrumb(mEditor);
    add(mBreadCrumb);

    add(Box.createHorizontalStrut(10));
    add(createSeparator());

    //SCREENSHOT BUTTON
    Action action = new AbstractAction("ACTION_SCREEN_SHOT", ICON_SCREENSHOT_STANDARD) {
      @Override
      public void actionPerformed(ActionEvent evt) {
        TransferHandler handler = mEditor.getWorkSpace().getTransferHandler();

        if (handler != null) {
          handler.exportToClipboard(mEditor.getWorkSpace(), mSystemClipBoard,
              TransferHandler.COPY);
          SaveFileDialog fileChooser = new SaveFileDialog();
          fileChooser.save();
        } else {
          System.err.println("handler null");
        }
      }
    };
    action.putValue(Action.SHORT_DESCRIPTION, "Screenshot");
    b = add(action);
    b.setToolTipText("Take a screenshot");
    b.setRolloverIcon(ICON_SCREENSHOT_ROLLOVER);
    sanitizeButton(b, smallButtonDim);
    b.setEnabled(false);

  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  private void refreshButtons(boolean projectChanged, boolean elementDisplayVisible) {
    // Refresh the button SAVE when project have been changed
    mSaveProject.setEnabled(projectChanged);
    //*************************************

    //*************************************
    // Refresh the element display buttons
    mTogglePalette.setIcon(elementDisplayVisible? ICON_MORE_STANDARD : ICON_LESS_STANDARD);
    mTogglePalette.setRolloverIcon(elementDisplayVisible? ICON_MORE_ROLLOVER : ICON_LESS_ROLLOVER);
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

  public void updateBreadcrumbs(SuperNode supernode) {
    mBreadCrumb.update(supernode);
  }

  public final void refresh(boolean projectChanged, boolean elementDisplayVisible) {
    refreshButtons(projectChanged, elementDisplayVisible);
    mBreadCrumb.refreshDisplay();
  }

}
