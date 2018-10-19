package de.dfki.vsm.editor.project;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.fife.ui.autocomplete.AutoCompletion;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.editor.event.TreeEntrySelectedEvent;
import de.dfki.vsm.editor.project.sceneflow.SceneFlowEditor;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Gregor Mehlmann
 */
public final class ProjectEditor extends JSplitPane implements EventListener {

  // The singelton logger instance
  private final Logger mLogger = LoggerFactory.getLogger(ProjectEditor.class);;
  // The singelton event multicaster
  private final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
  // The editor project of this editor
  private EditorProject mEditorProject;
  // The sceneflow editor of this project
  private final SceneFlowEditor mSceneFlowEditor;

  // Create an empty project editor
  public ProjectEditor() {
    this(new EditorProject());
  }

  // Construct a project editor with a project
  public ProjectEditor(final EditorProject project) {
    // Initialize the parent split pane
    super(JSplitPane.VERTICAL_SPLIT, true);
    // Initialize the editor project
    mEditorProject = project;
    // Initialize the sceneflow editor
    mSceneFlowEditor = new SceneFlowEditor(mEditorProject);
    // Register at the event dispatcher
    mEventDispatcher.register(this);
    // Initialize the GUI components
    initComponents();
  }

  public ProjectEditor(boolean isNewProject) {
    this(new EditorProject(isNewProject));
  }

  public void expandTree() {
    mSceneFlowEditor.expandTree();

  }

  // Get the sceneflow editor
  public final SceneFlowEditor getSceneFlowEditor() {
    return mSceneFlowEditor;
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
    // Close / Cleanup Members
    mSceneFlowEditor.close();

  }

  // Initialize the GUI components
  private void initComponents() {
    // Set Background Color
    setBackground(Color.WHITE);
    // Set An Empty Border
    setBorder(BorderFactory.createEmptyBorder());

    setOneTouchExpandable(true);

    setResizeWeight(Float.valueOf(Preferences.getProperty("sceneflow_sceneeditor_ratio")));

    setUI(new BasicSplitPaneUI() {

      @Override
      public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {

        };

      }

    });

    setDividerSize(10);

    setContinuousLayout(true);
    mSceneFlowEditor.setMinimumSize(new Dimension(10, 10));
    mSceneFlowEditor.setMaximumSize(new Dimension(10000, 3000));
    setTopComponent(mSceneFlowEditor);

    // setting size
    boolean showSceneFlowEditor = Boolean.valueOf(Preferences.getProperty("showscenefloweditor"));
    boolean showSceneDocEditor = Boolean.valueOf(Preferences.getProperty("showsceneeditor"));

    if (!showSceneFlowEditor) {
      setDividerLocation(1d);
    }

    if (showSceneDocEditor && showSceneFlowEditor) {
      setDividerLocation(Integer.parseInt(Preferences.getProperty("propertiesdividerlocation")));
    }

  }

  // Show the bottom part of the project editor
  private void showAuxiliaryEditor() {
    setDividerLocation(
            // TODO: Do we really need to parse this every time here?
            Integer.parseInt(Preferences.getProperty("propertiesdividerlocation")));
  }

  // Hides the bottom part of the project editor
  private void hideAuxiliaryEditor() {
    // TODO: Can we do that by adding and removing the component?
    setDividerLocation(1d);
  }

  // Update when an event happened
  @Override
  public void update(final EventObject event) {
    if (event instanceof TreeEntrySelectedEvent) {
      // Show the auxiliary editor
      showAuxiliaryEditor();
    }
  }

  // Refresh the editor's visual appearance
  public final void refresh() {
    // Print some information
    //mLogger.message("Refreshing '" + this + "'");
    // Refresh the components
    mSceneFlowEditor.refresh();
  }

}
