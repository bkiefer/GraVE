package de.dfki.grave;

import static de.dfki.grave.Icons.*;
import static de.dfki.grave.Preferences.*;
import static de.dfki.grave.Constants.*;
import static java.awt.event.InputEvent.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.action.UndoRedoProvider;
import de.dfki.grave.editor.dialog.AboutDialog;
import de.dfki.grave.editor.dialog.NewProjectDialog;
import de.dfki.grave.editor.dialog.OptionsDialog;
import de.dfki.grave.editor.dialog.QuitDialog;
import de.dfki.grave.editor.panels.*;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.util.ResourceLoader;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class AppFrame extends JFrame implements ChangeListener {
  public static final Logger mLogger = LoggerFactory.getLogger(MainGrave.class);
  
  // The singelton editor instance
  public static AppFrame sInstance = null;
  // The editor's GUI components
  private final EditorMenuBar mEditorMenuBar;
  private final JTabbedPane mProjectEditors;
  
  // Global undo/redo for global menu
  private final AbstractAction undoAction;
  private final AbstractAction redoAction;
  
  // Get the singelton editor instance
  public synchronized static AppFrame getInstance() {
    if (sInstance == null) {
      sInstance = new AppFrame();
    }
    return sInstance;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  ClipBoard previousCB = null;

  @Override
  public void stateChanged(ChangeEvent e) {
    if (getSelectedProjectEditor().getEditorProject() != null) {
      getSelectedProjectEditor().refresh();
      //mObservable.update(getSelectedProjectEditor().getEditorProject());
    }

    // copy and paste of nodes between the different projects
    ProjectEditor projectEditor = ((ProjectEditor) mProjectEditors.getSelectedComponent());

    if (projectEditor != null) {
      /** ClipBoard is a singleton
      if (previousCB != null) {
        ClipBoard currentCB = projectEditor.getWorkSpace().getClipBoard();
        currentCB.set(previousCB.get());
      }

      previousCB = projectEditor.getWorkSpace().getClipBoard();
      */
    }
  }

  //
  private ComponentListener mComponentListener = new ComponentListener() {
    @Override
    public void componentResized(ComponentEvent e) {
      getPrefs().updateBounds(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      getPrefs().updateBounds(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void componentShown(ComponentEvent e) { }

    @Override
    public void componentHidden(ComponentEvent e) { }
  };

  
  // Private construction of an editor
  private AppFrame() {
    configure();

    getContentPane().setBackground(Color.WHITE);
    /*try {
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         } catch (final Exception e) {
         // If Nimbus is not available, you can set the GUI to another look and feel.
         }*/
    // SET FONTS
    setUIFonts();

    // SET BACKGROUNDS
    setUIBackgrounds();

    undoAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ProjectEditor pe = getSelectedProjectEditor();
        if (pe != null) {
          UndoRedoProvider um = pe.getUndoManager();
          if (um != null) {
            um.doUndo(this, e);
            refreshMenuBar();
            pe.refreshToolBar();
            //EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
          }
        }
      }
    };
    undoAction.putValue(Action.ACCELERATOR_KEY, getAccel(KeyEvent.VK_Z));
    undoAction.putValue(Action.NAME, "Undo");
    redoAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ProjectEditor pe = getSelectedProjectEditor();
        if (pe != null) {
          UndoRedoProvider um = pe.getUndoManager();
          if (um != null) {
            um.doRedo(this, e);
            refreshMenuBar();
            pe.refreshToolBar();
            //EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
          }
        }
      }
    };
    redoAction.putValue(Action.ACCELERATOR_KEY, 
        getAccel(KeyEvent.VK_Z, SHIFT_DOWN_MASK));
    redoAction.putValue(Action.NAME, "Redo");
    // Init the menu bar

    // Init the project editor list
    mProjectEditors = new JTabbedPane(
            JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
    //mObservable.addObserver(mProjectEditors);
    
    mEditorMenuBar = new EditorMenuBar(this);
    // Hide the menu bar
    mEditorMenuBar.setVisible(true);


    setIconImage(ResourceLoader.loadImageIcon("img/dociconsmall.png").getImage());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    // Init the windows closing support
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        // Close all project editors
        closeAll();
      }
    });

    // Init the editor application frame
    setJMenuBar(mEditorMenuBar);
    
    setContentPane(mProjectEditors);
    
    Dimension editorSize = getPrefs().getFrameDimension();

    setPreferredSize(editorSize);
    setSize(editorSize);
    setTitle(getPrefs().FRAME_TITLE);
    setName(getPrefs().FRAME_NAME);
    checkAndSetLocation();

    // setContentPane(jsWelcome);
    // add(mProjectEditorList); // COMMENTED BY M.FALLAS
    pack();
    
    refresh();

    // handle resize and positioning
    this.addComponentListener(mComponentListener);
  }
  
  public void refreshUndoRedo(UndoRedoProvider.UndoRedoAction undo, 
      UndoRedoProvider.UndoRedoAction redo) {
    undo.refreshState(undoAction);
    redo.refreshState(redoAction);
  }
  
  public AbstractAction getUndoAction() { return undoAction; } 
  public AbstractAction getRedoAction() { return redoAction; }

  private void setUIFonts() {
    Font uiFont = getPrefs().editorConfig.sUI_FONT.getFont();
    Font treeFont = getPrefs().editorConfig.sTREE_FONT.getFont();

    String[] uiElements = {
        "Button.font",
        "ToggleButton.font",
        "RadioButton.font",
        "CheckBox.font",
        "ColorChooser.font",
        "ComboBox.font",
        "Label.font",
        "List.font",
        "MenuBar.font",
        "MenuItem.font",
        "RadioButtonMenuItem.font",
        "CheckBoxMenuItem.font",
        "Menu.font",
        "PopupMenu.font",
        "OptionPane.font",
        "Panel.font",
        "ProgressBar.font",
        "ScrollPane.font",
        "Viewport.font",
        "TabbedPane.font",
        "Table.font",
        "TableHeader.font",
        "TextField.font",
        "PasswordField.font",
        "TextArea.font",
        "TextPane.font",
        "EditorPane.font",
        "TitledBorder.font",
        "ToolBar.font",
        "ToolTip.font"
    };
    for (String uiElement : uiElements) {
      UIManager.put(uiElement, uiFont);
    }
    UIManager.put("Tree.font", treeFont);
  }

  private void setUIBackgrounds() {

    UIManager.put("Frame.background", Color.WHITE);
    UIManager.put("Panel.background", Color.WHITE);
    UIManager.put("MenuBar.opaque", true);
    UIManager.put("MenuBar.background", Color.WHITE);
    UIManager.put("Menu.opaque", true);
    UIManager.put("Menu.background", Color.WHITE);
    UIManager.put("MenuItem.opaque", true);
    UIManager.put("MenuItem.background", Color.WHITE);
    UIManager.put("ToolBar.opaque", true);
    UIManager.put("ToolBar.background", Color.WHITE);
    UIManager.put("TabbedPane.background", Color.WHITE);
    UIManager.put("EditorPane.background", Color.WHITE);
    UIManager.put("ScrollPane.background", Color.WHITE);
    UIManager.put("Viewport.background", Color.WHITE);
    UIManager.put("ScrollBar.background", Color.GRAY);
    UIManager.put("ScrollBar.thumb", Color.LIGHT_GRAY);
  }

  
  public void clearRecentProjects() {
    clearRecentProjects();
    mEditorMenuBar.refreshRecentFileMenu();
  }
  
  private void checkAndSetLocation() {
    Point finalPos = new Point(0, 0);
    Point editorPosition = getPrefs().getFramePosition();
    Dimension editorSize = getPrefs().getFrameDimension();

    // check systems monitor setup
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    for (int j = 0; j < gs.length; j++) {
      GraphicsDevice gd = gs[j];
      GraphicsConfiguration[] gc = gd.getConfigurations();

      for (int i = 0; i < gc.length; i++) {
        // check position
        if (((editorPosition.x > gc[i].getBounds().x)
            && (gc[i].getBounds().width > editorSize.width))
            && ((editorPosition.y > gc[i].getBounds().y)
                && (gc[i].getBounds().height > editorSize.height))) {

          // component can be place there
          finalPos = new Point(editorPosition.x, editorPosition.y);

          break;
        }
      }
    }

    setLocation(finalPos);
  }

  ////////////////////////////////////////////////////////////////////////////
  // Get the current project editor
  public final ProjectEditor getSelectedProjectEditor() {
    return (ProjectEditor) mProjectEditors.getSelectedComponent();
  }
  
  /** Get the current workspace, if any */
  public final WorkSpacePanel getWorkSpace() {
    ProjectEditor pe = getSelectedProjectEditor();
    if (pe == null) return null;
    return pe.getWorkSpace();
  }
  
  public final boolean newProject() {
    String[] newName = { "New Project" };
    @SuppressWarnings("unused")
    NewProjectDialog np = new NewProjectDialog(newName);
    if (newName[0] == null) // cancel
      return false;
    
    // Create a new project config, and new EditorProject
    EditorProject epj = new EditorProject(newName[0]);

    // Create a new project editor
    final ProjectEditor editor = new ProjectEditor(epj);

    // Add the new project editor
    addProjectTab(editor);

    // Refresh the appearance
    refresh();
    // Return true at success
    return true;
  }

  private class GraveFileFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
      if (f.isDirectory()) {
        if (EditorProject.isProjectDirectory(f)) return true;
        for (File g : f.listFiles()) {
          if (accept(g)) return true;
        }
      }
      return false;
    }

    @Override
    public String getDescription() { return "GraVE Project File Filter"; }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  // Open a new project editor with chooser
  public final boolean openProject() {
    // Create a new file chooser
    final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    // Configure The File Chooser
    chooser.setFileView(new FileView() {
      @Override
      public final Icon getIcon(final File file) {
        return EditorProject.isProjectDirectory(file) ? Icons.ICON_FILE : null;
      }
      
    });
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(new GraveFileFilter());
    // Show the file chooser in open mode
    final int option = chooser.showOpenDialog(this);
    // Check the result of the file chooser
    if (option == JFileChooser.APPROVE_OPTION) {
      // Get the chooser's selected file
      final File file = chooser.getSelectedFile();
      // And try to open the file then
      return openProject(file);
    } else {
      // Print an error message
      mLogger.warn("Warning: Canceled opening of a project file");
      // And return failure here
      return false;
    }
  }

  /*
  public final ProjectEditor showProject(final EditorProject project) {
    // Show the project editors
    setContentPane(mProjectEditors);
    // Create a new project editor from project
    final ProjectEditor editor = new ProjectEditor(project);
    // Add the project editor to list of project
    // editors and select it in the tabbed pane
    addProjectTab(project.getProjectName(), editor);
    // Set editor visible
    setVisible(true);
    // Refresh the appearance
    refresh();
    // Return true at success
    return editor;
  }

  public final boolean hideProject(final ProjectEditor editor) {
    // Close selected editor
    editor.close();
    // Remove the component
    mProjectEditors.remove(editor);
    // Hide the project editors
    //setContentPane(mWelcomeScreen);
    // Refresh the appearance
    refresh();
    // Set editor invisible
    setVisible(false);
    // Return true at success
    return true;
  }
  */

  public final boolean openProject(File path) {
    if (path == null) {
      mLogger.error("Error: Cannot open editor project from a bad Stream");
      // And return failure here
      return false;
    }
    final EditorProject project = EditorProject.load(path);
    // Try to loadRunTimePlugins it from the file
    if (project != null) {
      // Toggle the editor main screen
      if (mProjectEditors.getTabCount() == 0) {
        // Show the project editors
        setContentPane(mProjectEditors);
        // Show the menu bar items
        mEditorMenuBar.setVisible(true);
      }
      // Create a new project editor from project
      final ProjectEditor projectEditor = new ProjectEditor(project);
      // Add the project editor to list of project editors and select it
      addProjectTab(projectEditor);
      // Update the recent project list
      updateRecent(project);
      // Refresh the appearance
      refresh();
      //projectEditor.expandTree();
      // Return true at success
      return true;
    } else {
      // Print an error message
      mLogger.error("Cannot load editor project");
      // Return false at failure
      return false;
    }
  }

  // Save the selected project editor
  public final boolean save() {
    return save(getSelectedProjectEditor());
  }

  // Save the specific project editor
  public final boolean save(final ProjectEditor editor) {
    // Check if the editor is valid
    if (editor != null) {
      // Get the selected editor project
      final EditorProject project = editor.getEditorProject();
      // Check if the project is pending
      if (project.isNew()) {
        // Choose a new parent directory to save to
        return saveAs(editor);
      }
      if (project.hasChanged()) {
        // Try to write the editor project
        if (project.saveProject()) {
          // Refresh the title of the project tab
          setTabNameSaved();
          // Update rectent project list
          updateRecent(project);
          // Refresh the appearance
          refresh();
          // Return true at success
        } else {
          // Print an error message
          mLogger.error("Error: Cannot write the editor project '"
              + project.getProjectName() + "'");
          // And return failure here
          return false;
        }
      }
    } else {
      // Print an error message
      mLogger.error("Error: Cannot save a bad project editor");
      // And return failure here
      return false;
    }
    return true;
  }

  /** Add new project tab */
  void addProjectTab(ProjectEditor editor) {
    EditorProject project = editor.getEditorProject();
    String tabName = project.getProjectName();
    String path = project.isNew() 
        ? "" 
        : project.getProjectPath().getAbsolutePath();
    
    JEditorPane ep = new JEditorPane();
    ep.setEditable(false);
    mProjectEditors.addTab(null, new JScrollPane(ep));

    JLabel tabLabel = new JLabel(tabName);

    // Create an AddButton
    final AddButton addButton = new AddButton();
    addButton.setIcon(ICON_CANCEL_STANDARD_TINY);
    addButton.setTabPos(mProjectEditors.getTabCount() - 1);
    addButton.removeMouseListener(addButton.getMouseListeners()[1]);
    addButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent me) {
        addButton.setIcon(ICON_CANCEL_ROLLOVER_TINY);
      }

      @Override
      public void mouseExited(MouseEvent me) {
        addButton.setIcon(ICON_CANCEL_STANDARD_TINY);
      }

      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        close(editor, QuitDialog.CLOSE_PROJ_DIALOG);
      }
    });
    int lastTab = mProjectEditors.getTabCount() - 1;
    JPanel tab = new JPanel();
    tab.setOpaque(false);
    tab.add(tabLabel);
    tab.add(addButton);
    mProjectEditors.setTabComponentAt(lastTab, tab);
    mProjectEditors.setComponentAt(lastTab, editor);
    mProjectEditors.setSelectedIndex(lastTab);
    mProjectEditors.setTitleAt(lastTab, tabName);
    mProjectEditors.setToolTipTextAt(lastTab, path);
  }

  // SETS A SYMBOL TO SHOW THAT THE PROJECT WAS MODIFIED
  public void setTabNameModified() {
    JPanel pnl = (JPanel) mProjectEditors.getTabComponentAt(mProjectEditors.getSelectedIndex());
    String tabName = ((JLabel) pnl.getComponent(0)).getText();
    if (!tabName.endsWith("*")) {
      ((JLabel) pnl.getComponent(0)).setText(tabName + "*");
    }
  }

  //REMOVES MODIFIED SYMBOL
  public void setTabNameSaved() {
    JPanel pnl = (JPanel) mProjectEditors.getTabComponentAt(mProjectEditors.getSelectedIndex());
    String tabName = ((JLabel) pnl.getComponent(0)).getText();
    if (tabName.endsWith("*")) {
      tabName = tabName.substring(0, tabName.length() - 1);
      ((JLabel) pnl.getComponent(0)).setText(tabName);
    }
  }

  public final boolean saveAs() {
    return saveAs(getSelectedProjectEditor());
  }

  // Save the specific project editor
  public final boolean saveAs(final ProjectEditor editor) {
    // Check if the editor is valid
    if (editor != null) {
      // Get currently selected project
      final EditorProject project = editor.getEditorProject();
      // Check if the project is valid
      if (project != null) {
        // Set ProjectName
        String projectName = mProjectEditors.getTitleAt(mProjectEditors.getSelectedIndex()).replace("*", "");
        project.setProjectName(projectName);
        // Create a new file chooser
        final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        // Configure The File Chooser
        // TODO: Set the correct view and filter
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // Show the file chooser in open mode
        final int option = chooser.showOpenDialog(this);
        // Check the result of the file chooser
        if (option == JFileChooser.APPROVE_OPTION) {
          // Get the chooser's selected file
          final File file = chooser.getSelectedFile();
          // Try to write the editor project
          if (project.saveNewProject(file)) {
            // Refresh the title of the project tab
            //final int index = mProjectEditors.getSelectedIndex();
            setTabNameSaved();
            // Update rectent project list
            updateRecent(project);
            // Refresh the appearance
            refresh();
            // Return true at success
            return true;
          } else {
            // Print an error message
            mLogger.error("Error: Cannot write the editor project '" + project + "'");
            // And return failure here
            return false;
          }
        } else {
          // Print an error message
          mLogger.warn("Warning: Canceled saving of a project file");
          // And return failure here
          return false;
        }
      } else {
        // Print an error message
        mLogger.error("Error: Cannot save a bad editor project");
        // And return failure here
        return false;
      }
    } else {
      // Print an error message
      mLogger.error("Error: Cannot save a bad project editor");
      // And return failure here
      return false;
    }
  }

  // Close the selected project editor
  public final void close(int DialogType) {
    // Close the current project editor
    close(getSelectedProjectEditor(), DialogType);
  }

  // Close a specific project editor
  private int close(ProjectEditor editor, int DialogType) {
    int exitMessage = QuitDialog.SAVE_AND_EXIT;
    if (editor == null) return exitMessage;
    // Check if the project has changed
    if (editor.getEditorProject().hasChanged()) {

      QuitDialog qDiag = new QuitDialog(DialogType);
      exitMessage = qDiag.getExitMessage();
      System.out.println(exitMessage);
      if (exitMessage == QuitDialog.CANCEL_CLOSING) {
        return exitMessage;
      }
      if (exitMessage == QuitDialog.SAVE_AND_EXIT) {
        save(editor);
      }
    }
    // Close the project editor itself
    editor.close();
    // Remove the component
    mProjectEditors.remove(editor);
    // Refresh the appearance
    refresh();
    editor = null;
    System.gc();
    return exitMessage; 
  }

  // Save all project editors
  public final void saveAll() {
    for (int i = 0; i < mProjectEditors.getTabCount(); i++) {
      save(((ProjectEditor) mProjectEditors.getComponentAt(i)));
    }
  }

  // Close all project editors
  public final void closeAll() {
    for (int i = 0; i < mProjectEditors.getTabCount(); i++) {
      int result = close((ProjectEditor) mProjectEditors.getComponentAt(i), QuitDialog.EXIT_DIALOG);
      if (result == QuitDialog.CANCEL_CLOSING) {
        return;
      }
    }
    System.exit(0);
  }

  public static KeyStroke getAccel(int code, int mask) {
    return KeyStroke.getKeyStroke(code,
        mask | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
  }

  public static KeyStroke getAccel(int code) {
    return getAccel(code, 0);
  }

  //ESCAPE LISTENER- Closes dialog with escape key
  public static void addEscapeListener(final JDialog dialog) {
    ActionListener escListner = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ae) {
        dialog.dispose();
      }
    };
    dialog.getRootPane().registerKeyboardAction(escListner,
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  /** Update list of recent projects */
  public void updateRecent(final EditorProject project) {
    String path = project.getProjectPath().getAbsolutePath();
    String date = DATE_FORMAT.format(new Date());
    getPrefs().updateRecentProjects(project.getProjectName(), path, date);
    
    mEditorMenuBar.refreshRecentFileMenu();
  }

  // Show the options dialog
  public final void showOptions() {
    final OptionsDialog optionsDialog =
        new OptionsDialog(getSelectedProjectEditor());
    addEscapeListener(optionsDialog);
    optionsDialog.setVisible(true);
  }

  // Show the help dialog
  public final void showHelp() {
    final AboutDialog aboutDialog = AboutDialog.getInstance();

    aboutDialog.setVisible(true);
  }

  // Show the about dialog
  public final void showAbout() {
    final AboutDialog aboutDialog = AboutDialog.getInstance();

    aboutDialog.setVisible(true);
  }

  // Refresh this editor component
  public final void refresh() {
    // Get the selected project editor
    final ProjectEditor editor = getSelectedProjectEditor();
    // Refresh the selected project editor
    if (editor != null) {
      editor.refresh();
      // Refresh the editor's menu bar
    }
    refreshMenuBar();
  }
  
  public final void refreshMenuBar() {
    final ProjectEditor editor = getSelectedProjectEditor();
    boolean hasChanged = 
        (editor != null && editor.getEditorProject().hasChanged());
    // set status of file menu
    mEditorMenuBar.someProjectOpen(
        mProjectEditors.getTabCount() > 0, hasChanged);
    if (editor != null)
      mEditorMenuBar.somethingSelected(
          editor.getWorkSpace().isSomethingSelected(),
          ! ClipBoard.getInstance().isEmpty()
          );
    
    mEditorMenuBar.refreshViewOptions();
  }
}
