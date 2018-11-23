package de.dfki.vsm.editor.project;

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
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.event.ClearCodeEditorEvent;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.event.TreeEntrySelectedEvent;
import de.dfki.vsm.editor.project.sceneflow.SceneFlowEditor;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class ProjectEditor extends JSplitPane implements EventListener {

  // The singelton logger instance
  private static final Logger mLogger = LoggerFactory.getLogger(ProjectEditor.class);
  // The singelton event multicaster
  private final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
  // The editor project of this editor
  private EditorProject mEditorProject;
  // The sceneflow editor of this project
  private final SceneFlowEditor mSceneFlowEditor;
  // Code editing panel
  private final CodeEditor mCodeEditor;

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
    mCodeEditor = new CodeEditor(mEditorProject);
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
    mSceneFlowEditor.setBackground(Color.WHITE);
    // Set An Empty Border
    mSceneFlowEditor.setBorder(BorderFactory.createEmptyBorder());

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
              if (!mCodeEditor.isPinPricked())
                showAuxiliaryEditor();
              break;
            case MouseEvent.MOUSE_RELEASED:
              int value = ProjectEditor.this.getDividerLocation();
              mEditorProject.getEditorConfig().sCODE_DIVIDER_LOCATION = value;
              mCodeEditor.setPinPricked();
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
    mCodeEditor.setMinimumSize(new Dimension(10, 10));
    mCodeEditor.setMaximumSize(new Dimension(10000, 3000));
    setBottomComponent(mCodeEditor);

    // setting size

    if (!mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR) {
      setDividerLocation(0);
    }

    if (mEditorProject.getEditorConfig().sSHOW_CODEEDITOR
        && mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR) {
      showAuxiliaryEditor();
    }

    mCodeEditor.addComponentListener(new ComponentListener() {
      @Override
      public void componentResized(ComponentEvent e) {
        if (mSceneFlowEditor.getSize().height == 0) {
          mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR = false;
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = true;
        } else {
          mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR = true;
          mCodeEditor.updateBorders();
        }
        if (mCodeEditor.getSize().height == 0) {
          mEditorProject.getEditorConfig().sSHOW_SCENEFLOWEDITOR = true;
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = false;
        } else {
          mEditorProject.getEditorConfig().sSHOW_CODEEDITOR = true;
          mCodeEditor.updateBorders();
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
  public void update(final Object event) {
    if (event instanceof TreeEntrySelectedEvent) {
      // Show the auxiliary editor
      //showAuxiliaryEditor();
    } else if (event instanceof ElementSelectedEvent) {
      Object edited = ((ElementSelectedEvent) event).getElement();
      mCodeEditor.setEditedNodeOrEdge(edited);
    } else if (event instanceof ClearCodeEditorEvent) {
      mCodeEditor.setEditedNodeOrEdge(null);
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
