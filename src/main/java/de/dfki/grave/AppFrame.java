package de.dfki.grave;

import static de.dfki.grave.Preferences.getPrefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.dialog.AboutDialog;
import de.dfki.grave.editor.dialog.OptionsDialog;
import de.dfki.grave.editor.dialog.QuitDialog;
import de.dfki.grave.editor.panels.AddButton;
import de.dfki.grave.editor.panels.ClipBoard;
import de.dfki.grave.editor.panels.EditorMenuBar;
import de.dfki.grave.editor.panels.EditorStarter;
import de.dfki.grave.editor.panels.OpenProjectView;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.model.project.ProjectConfig;
import de.dfki.grave.util.ResourceLoader;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class AppFrame extends JFrame implements ChangeListener {

  // The singelton editor instance
  public static AppFrame sInstance = null;
  // The singelton runtime instance
  //private final RunTimeInstance mRunTime = RunTimeInstance.getInstance();
  // The singelton logger instance
  private final Logger mLogger = LoggerFactory.getLogger(MainGrave.class);;
  // The editor's GUI components
  private final EditorMenuBar mEditorMenuBar;
  private final JTabbedPane mProjectEditors;
  private final JScrollPane mWelcomeScreen;
  private final EditorStarter mWelcomePanel;

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
        ClipBoard currentCB = projectEditor.getSceneFlowEditor().getWorkSpace().getClipBoard();
        currentCB.set(previousCB.get());
      }

      previousCB = projectEditor.getSceneFlowEditor().getWorkSpace().getClipBoard();
      */
    }
  }

  //
  private ComponentListener mComponentListener = new ComponentListener() {
    @Override
    public void componentResized(ComponentEvent e) {
      getPrefs().FRAME_HEIGHT = getHeight();
      getPrefs().FRAME_WIDTH = getWidth();
      getPrefs();
      Preferences.save();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      getPrefs().FRAME_POS_X = getX();
      getPrefs().FRAME_POS_Y = getY();
      getPrefs();
      Preferences.save();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
  };

  // Private construction of an editor
  private AppFrame() {
    Preferences.configure();

    // Load the preferences
    Preferences.load();

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

    // Preferences.info();
    // Init the menu bar
    mEditorMenuBar = new EditorMenuBar(this);
    // Hide the menu bar
    mEditorMenuBar.setVisible(false);

    // Init the project editor list
    mProjectEditors = new JTabbedPane(
            JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
    //mObservable.addObserver(mProjectEditors);

    // Init welcome screen
    mWelcomePanel = new EditorStarter(this);
    mWelcomeScreen = new JScrollPane(mWelcomePanel);
    mWelcomeScreen.setMaximumSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
    mWelcomeScreen.setOpaque(false);
    mWelcomeScreen.getViewport().setOpaque(false);
    add(mWelcomeScreen);
    setIconImage(ResourceLoader.loadImageIcon("img/dociconsmall.png").getImage());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    // Init the windows closing support
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        // Close all project editors
        closeAll();
        // And finally exit the system
        //System.exit(0);
      }
    });

    // Init the editor application frame
    // TODO: static property fields
    Dimension editorSize = new Dimension(getPrefs().FRAME_WIDTH,
            getPrefs().FRAME_WIDTH);

    setPreferredSize(editorSize);
    setSize(editorSize);
    checkAndSetLocation();
    setTitle(getPrefs().FRAME_TITLE);
    setName(getPrefs().FRAME_NAME);
    setJMenuBar(mEditorMenuBar);
    // setContentPane(jsWelcome);
    // add(mProjectEditorList); // COMMENTED BY M.FALLAS
    pack();

    // handle resize and positioning
    this.addComponentListener(mComponentListener);
  }

  private void setUIFonts() {
    Font uiFont = Preferences.getPrefs().editorConfig.sUI_FONT.getFont();
    Font treeFont = Preferences.getPrefs().editorConfig.sTREE_FONT.getFont();

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
    mWelcomePanel.updateWelcomePanel();
  }

  private void checkAndSetLocation() {
    Point finalPos = new Point(0, 0);
    Point editorPosition = new Point(getPrefs().FRAME_POS_X,
            getPrefs().FRAME_POS_Y);
    Dimension editorSize = new Dimension(getPrefs().FRAME_WIDTH,
            getPrefs().FRAME_HEIGHT);

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


  public final boolean newProject(String projectName) {
    // Create a new project config, and new EditorProject
    EditorProject epj = new EditorProject(new ProjectConfig(projectName));

    // Create a new project editor
    final ProjectEditor editor = new ProjectEditor(epj);

    // Set default name  main superNode
    //editor.getSceneFlowEditor().getSceneFlow().setName(editor.getEditorProject().getEditorConfig().sMAINSUPERNODENAME);
    // happening in RuntimeProject now, where scene flow is created

    // Add the new project editor
    addProjectTab(projectName, editor);

    // Show the editor projects now
    if (mProjectEditors.getTabCount() == 1) {
      // Show the project editors
      setContentPane(mProjectEditors);
      // Show the menu bar items
      mEditorMenuBar.setVisible(true);
    }
    // Refresh the appearance
    refresh();
    // Return true at success
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  // Open a new project editor with chooser
  public final boolean openProject() {
    // Create a new file chooser
    final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    // Configure The File Chooser
    chooser.setFileView(new OpenProjectView());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return OpenProjectView.isAcceptedFile(f);
      }

      @Override
      public String getDescription() {
        return "SceneMaker Project File Filter";
      }
    });
    // Show the file chooser in open mode
    final int option = chooser.showOpenDialog(this);
    // Check the result of the file chooser
    if (option == JFileChooser.APPROVE_OPTION) {
      // Get the chooser's selected file
      final File file = chooser.getSelectedFile();
      // And try to open the file then
      return openProject(file.getPath());
    } else {
      // Print an error message
      mLogger.warn("Warning: Canceled opening of a project file");
      // And return failure here
      return false;
    }
  }

  public final void hideWelcomeStickman() {
    mWelcomePanel.showWelcomeStickman(false);
  }

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
    setContentPane(mWelcomeScreen);
    // Refresh the appearance
    refresh();
    // Set editor invisible
    setVisible(false);
    // Return true at success
    return true;
  }

  public final boolean openProject(String path) {
    if (path == null) {
      mLogger.error("Error: Cannot open editor project from a bad Stream");
      // And return failure here
      return false;
    }
    final EditorProject project = new EditorProject();
    // Try to loadRunTimePlugins it from the file
    if (project.parse(path)) {
      // Toggle the editor main screen
      if (mProjectEditors.getTabCount() == 0) {
        // Show the project editors
        setContentPane(mProjectEditors);
        // Show the menu bar items
        mEditorMenuBar.setVisible(true);

      }
      // Create a new project editor from project
      final ProjectEditor projectEditor = new ProjectEditor(project);
      // Add the project editor to list of project
      // editors and select it in the tabbed pane
      addProjectTab(project.getProjectName(), projectEditor);
      // Update the recent project list
      updateRecentProjects(project);
      // Print some info message
      //mLogger.message("Opening project editor from Stream");
      // Refresh the appearance
      refresh();
      //projectEditor.expandTree();
      // Return true at success
      return true;
    } else {
      // Print an error message
      mLogger.error("Error: Cannot load editor project from Stream");
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
      // Check if the project is valid
      if (project != null) {
        // Check if the project is pending
        if (!project.isPending()) {
          // Try to write the editor project
          if (project.write()) {
            // Refresh the title of the project tab
            //final int index = mProjectEditors.getSelectedIndex();
            //final String title = mProjectEditors.getTitleAt(index);
            setTabNameSaved();
            // Update rectent project list
            updateRecentProjects(project);
            // Refresh the appearance
            refresh();
            // Return true at success
            return true;
          } else {
            // Print an error message
            mLogger.error("Error: Cannot write the editor project '"
                    + project.getProjectName() + "'");
            // And return failure here
            return false;
          }
        } else {
          // Choose a new file to save to
          return saveAs(editor);
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

  //ADD NEW PROJECT TAB
  void addProjectTab(String tabName, final JComponent content) {

    JEditorPane ep = new JEditorPane();
    ep.setEditable(false);
    mProjectEditors.addTab(null, new JScrollPane(ep));

    JLabel tabLabel = new JLabel(tabName);

    // Create an AddButton
    final AddButton mCloseButton = new AddButton();
    getPrefs();
    mCloseButton.setIcon(Preferences.ICON_CANCEL_STANDARD_TINY);
    mCloseButton.setTabPos(mProjectEditors.getTabCount() - 1);
    mCloseButton.removeMouseListener(mCloseButton.getMouseListeners()[1]);
    mCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent me) {
        getPrefs();
        mCloseButton.setIcon(Preferences.ICON_CANCEL_ROLLOVER_TINY);
      }

      @Override
      public void mouseExited(MouseEvent me) {
        getPrefs();
        mCloseButton.setIcon(Preferences.ICON_CANCEL_STANDARD_TINY);
      }

      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        close((ProjectEditor) content, QuitDialog.CLOSE_PROJ_DIALOG);
      }
    });
    if (mProjectEditors.getTabCount() > 0) {
      JPanel pnl = new JPanel();
      pnl.setOpaque(false);
      pnl.add(tabLabel);
      pnl.add(mCloseButton);
      mProjectEditors.setTabComponentAt(mProjectEditors.getTabCount() - 1, pnl);
      mProjectEditors.setComponentAt(mProjectEditors.getTabCount() - 1, content);
      mProjectEditors.setSelectedIndex(mProjectEditors.getTabCount() - 1);
      mProjectEditors.setTitleAt(mProjectEditors.getTabCount() - 1, tabName);
      mProjectEditors.setToolTipTextAt(mProjectEditors.getTabCount() - 1, ((ProjectEditor) content).getEditorProject().getProjectPath());

    }

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
          if (project.write(file)) {
            // Refresh the title of the project tab
            //final int index = mProjectEditors.getSelectedIndex();
            setTabNameSaved();
            // Update rectent project list
            updateRecentProjects(project);
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

    // Check if the project has changed
    if (editor.getEditorProject().hasChanged()) {

      QuitDialog qDiag = new QuitDialog(DialogType);
      int exitMessage = qDiag.getExitMessage();
      System.out.println(exitMessage);
      if (exitMessage == QuitDialog.CANCEL_CLOSING) {
        return exitMessage;
      }
      if (exitMessage == QuitDialog.SAVE_AND_EXIT) {
        save(editor);
      }

      // Close the project editor itself
      editor.close();
      // Remove the component
      mProjectEditors.remove(editor);
      // Toggle the editor main screen
      if (mProjectEditors.getTabCount() == 0) {
        // Show the project editors
        setContentPane(mWelcomeScreen);
        // Hide the menu bar items
        mEditorMenuBar.setVisible(false);
      }
      // Refresh the appearance
      refresh();
      editor = null;
      System.gc();
      return exitMessage;
    } else {
      // Close the project editor itself
      editor.close();
      // Remove the component
      mProjectEditors.remove(editor);

      // Toggle the editor main screen
      if (mProjectEditors.getTabCount() == 0) {
        // Show the project editors
        setContentPane(mWelcomeScreen);
        // Hide the menu bar items
        mEditorMenuBar.setVisible(false);
      }
      // Refresh the appearance
      refresh();
      editor = null;
      System.gc();
      return QuitDialog.SAVE_AND_EXIT;
    }

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

  //ESCAPE LISTENER- Closes dialog with escape key
  public static void addEscapeListener(final JDialog dialog) {
    ActionListener escListner = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ae) {
        dialog.dispose();
      }
    };
    dialog.getRootPane().registerKeyboardAction(escListner, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  /*

     private final void refreshRecentProjects(final EditorProject project) throws ParseException {
     final String projectPath = project.getProjectPath();
     final String projectName = project.getProjectName();
     // Create the list of recent projects
     final ArrayList<TPLTriple<String, String, Date>> projects = new ArrayList<>();
     // Get all remembered recent projects
     for (int i = 0; i <= getPrefs().sMAX_RECENT_PROJECTS; i++) {
     final String path = getPrefs().getProperty("recentproject." + i + ".path");
     final String name = getPrefs().getProperty("recentproject." + i + ".name");
     final Date date = new SimpleDateFormat("dd.MM.yyyy").parse(
     getPrefs().getProperty("recentproject." + i + ".date"));
     // Create the current recent project
     TPLTriple<String, String, Date> recent = new TPLTriple(name, path, date);
     //
     projects.add(recent);
     }
     // Interate over the recent projects
     for (TPLTriple<String, String, Date> recent : projects) {
     //if () {

     //}
     }

     }*/

  // Update list of recent projects
  public void updateRecentProjects(final EditorProject project) {
    String projectPath = project.getProjectPath();
    String projectName = project.getProjectName();
    getPrefs();
    String projectDate = Preferences.sDATE_FORMAT.format(new Date());

    if (getPrefs().recentProjectPaths.contains(projectPath)) {
      int index = getPrefs().recentProjectPaths.indexOf(projectPath);
      // case: project in recent list
      if (getPrefs().recentProjectNames.contains(projectName)) {
        // case: project is on list - has now to be at first pos
        //getPrefs().setProperty("recentproject." + index + ".date", getPrefs().sDATE_FORMAT.format(new Date()));
        //if (index != 0) {
          getPrefs().recentProjectPaths.add(0, projectPath);
          getPrefs().recentProjectNames.add(0, projectName);
          getPrefs().recentProjectDates.add(0, projectDate);
          getPrefs().recentProjectNames.remove(index + 1);
          getPrefs().recentProjectPaths.remove(index + 1);
          getPrefs().recentProjectDates.remove(index + 1);
        //}
      /* TODO: Isn't this wrong anyways?
      } else {
        //getPrefs().setProperty("recentproject." + index + ".date", getPrefs().sDATE_FORMAT.format(new Date()));
        getPrefs().recentProjectNames.remove(index);
        getPrefs().recentProjectNames.add(index, projectName); */
      }
    } else {
      getPrefs();
      getPrefs();
      if (projectPath != null && !projectPath.contains(Preferences.sSAMPLE_PROJECTS) && !projectPath.contains(Preferences.sTUTORIALS_PROJECTS)) {
        // case: project not in recent list
        getPrefs().recentProjectPaths.add(0, projectPath);
        getPrefs().recentProjectNames.add(0, projectName);
        getPrefs().recentProjectDates.add(0, projectDate);
      }
    }
    int si = getPrefs().recentProjectPaths.size();
    if (si > getPrefs().sMAX_RECENT_PROJECTS) {
      getPrefs().recentProjectPaths.remove(si);
      getPrefs().recentProjectNames.remove(si);
    }

    /* set properties
    String dir = null;
    String name = null;
    int maxCnt = ((recentProjectPaths.size() <= getPrefs().sMAX_RECENT_PROJECTS) ? recentProjectPaths.size() : getPrefs().sMAX_RECENT_PROJECTS);
    for (int i = 0; i < maxCnt; i++) {
      dir = recentProjectPaths.get(i);
      name = recentProjectNames.get(i);

      if ((dir != null) && (name != null)) {
        getPrefs().setProperty("recentproject." + i + ".path", dir);
        getPrefs().setProperty("recentproject." + i + ".name", name);
        //getPrefs().setProperty("recentproject." + i + ".date", getPrefs().sDATE_FORMAT.format(new Date()));
      } else {
        break;
      }
    }*/

    // save properties
    Preferences.save();
    mWelcomePanel.createRecentAndSamplePrjList();
    mEditorMenuBar.refreshRecentFileMenu();
  }

  // Show the options dialog
  public final void showOptions() {
    final OptionsDialog optionsDialog = OptionsDialog.getInstance();

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
    // Print some information
    //mLogger.message("Refreshing '" + this + "'");
    // Get the selected project editor
    final ProjectEditor editor = getSelectedProjectEditor();
    // Refresh the selected project editor
    if (editor != null) {
      editor.refresh();
    }
    // Refresh the editor's menu bar
    mEditorMenuBar.refresh();
    // Refresh editor welcome panel
    mWelcomePanel.refresh();
  }
}