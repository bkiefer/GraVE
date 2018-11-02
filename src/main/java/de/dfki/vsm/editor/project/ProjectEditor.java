package de.dfki.vsm.editor.project;

import static de.dfki.vsm.Preferences.getPrefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.fife.ui.autocomplete.AutoCompletion;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.editor.CmdBadge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
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
  // Code editing panel
  private final CodeEditor mAuxiliaryEditor;

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
    // Initialize Code Editing Region
    mAuxiliaryEditor = new CodeEditor(mEditorProject);
    // Register at the event dispatcher
    mEventDispatcher.register(this);
    // Initialize the GUI components
    initComponents();
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

    setResizeWeight(mEditorProject.getEditorConfig().sSCENEFLOW_SCENE_EDITOR_RATIO);

    setUI(new BasicSplitPaneUI() {

      @SuppressWarnings("serial")
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
              if (!mAuxiliaryEditor.isPinPricked())
                showAuxiliaryEditor();
              break;
            case MouseEvent.MOUSE_RELEASED:
              int value = ProjectEditor.this.getDividerLocation();
              mEditorProject.getEditorConfig().sCODE_DIVIDER_LOCATION = value;
              mAuxiliaryEditor.setPinPricked();
              break;
            }
          }
        };
      }
    });

    setDividerSize(10);

    setContinuousLayout(true);
    mSceneFlowEditor.setMinimumSize(new Dimension(10, 10));
    mSceneFlowEditor.setMaximumSize(new Dimension(10000, 3000));
    setTopComponent(mSceneFlowEditor);
    mAuxiliaryEditor.setMinimumSize(new Dimension(10, 10));
    mAuxiliaryEditor.setMaximumSize(new Dimension(10000, 3000));
    setBottomComponent(mAuxiliaryEditor);

    // setting size

    if (!mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR) {
      setDividerLocation(0);
    }

    if (mEditorProject.getEditorConfig().sSHOW_CODEEDITOR
            && mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR) {
      showAuxiliaryEditor();
    }

    mAuxiliaryEditor.addComponentListener(new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent e) {
        if (mSceneFlowEditor.getSize().height == 0) {
          mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR = false;
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = true;
        } else {
          mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR = true;
        }
        if (mAuxiliaryEditor.getSize().height == 0) {
          mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR = true;
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = false;
        } else {
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = true;
        }
        Preferences.save();
      }

      @Override
      public void componentMoved(ComponentEvent e) { }

      @Override
      public void componentShown(ComponentEvent e) { }

      @Override
      public void componentHidden(ComponentEvent e) { }
    });
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
  public void update(final EventObject event) {
    if (event instanceof TreeEntrySelectedEvent) {
      // Show the auxiliary editor
      showAuxiliaryEditor();
    } else if (event instanceof NodeSelectedEvent
            && ((NodeSelectedEvent) event).getSource() instanceof Node) {
      // the source of the event is the node object
      mAuxiliaryEditor.setEditedNodeOrEdge(
              mSceneFlowEditor.getWorkSpace().getCmdBadge(
                      (Node)((NodeSelectedEvent) event).getSource()));
    } else if (event instanceof EdgeSelectedEvent) {
      // the source of the event is the node object
      mAuxiliaryEditor.setEditedNodeOrEdge(((EdgeSelectedEvent) event).getSource());
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
