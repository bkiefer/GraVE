package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.getPrefs;
import static java.awt.event.InputEvent.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.Preferences;
import de.dfki.grave.editor.action.UndoRedoProvider;
import de.dfki.grave.editor.dialog.QuitDialog;
import de.dfki.grave.model.project.EditorConfig;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class EditorMenuBar extends JMenuBar {

  //private final Logger mLogger = LoggerFactory.getLogger(EditorMenuBar.class);
  private final AppFrame mEditorInstance;

  // File menu
  private JMenu mOpenRecentFileMenu;
  private JMenuItem mCloseFileMenuItem;
  private JMenuItem mSaveFileMenuItem;
  private JMenuItem mSaveAsMenuItem;
  private JMenuItem mSaveAllMenuItem;
  // Edit menu
  private JMenu mEditMenu;
  private JMenuItem mCutMenuItem;
  private JMenuItem mCopyMenuItem;
  private JMenuItem mPasteMenuItem;
  private JMenuItem mDeleteMenuItem;
  // View Menu
  private JMenu mViewMenu;
  private JCheckBoxMenuItem mShowGridMenuItem;
  private JCheckBoxMenuItem mShowIdMenuItem;
  private JCheckBoxMenuItem mSnapToGridMenuItem;

  // Construct the editor's menu bar
  public EditorMenuBar(final AppFrame editor) {
    // Initialize the parent editor
    mEditorInstance = editor;
    // Initialize the GUI components
    initComponents();
  }

  private WorkSpacePanel getCurrentWorkSpace() {
    return mEditorInstance.getWorkSpace();
  }

  private EditorConfig getEditorConfig() {
    return getCurrentWorkSpace().getEditorConfig();
  }

  private static KeyStroke getAccel(int code, int mask) {
    return KeyStroke.getKeyStroke(code,
        mask | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
  }

  private static KeyStroke getAccel(int code) { return getAccel(code, 0); }

  // Refresh the state of all items
  public final void refreshViewOptions() {
    // View Menu
    if (mViewMenu.isEnabled()) {
      mShowGridMenuItem.setState(getEditorConfig().sSHOWGRID);
      mShowIdMenuItem.setState(getEditorConfig().sSHOWIDSOFNODES);
      mSnapToGridMenuItem.setState(getEditorConfig().sSNAPTOGRID);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;

    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
  }

  public void someProjectOpen(boolean flag, boolean currentChanged) {
    mSaveFileMenuItem.setEnabled(flag && currentChanged);
    mSaveAsMenuItem.setEnabled(flag);
    mSaveAllMenuItem.setEnabled(flag);
    mCloseFileMenuItem.setEnabled(flag);
    mEditMenu.setEnabled(flag);
    mViewMenu.setEnabled(flag);
  }

  public void somethingSelected(boolean flag, boolean sthOnClipboard) {
    if (!mEditMenu.isEnabled()) return;
    mCutMenuItem.setEnabled(flag);
    mCopyMenuItem.setEnabled(flag);
    mPasteMenuItem.setEnabled(sthOnClipboard);
    mDeleteMenuItem.setEnabled(flag);
  }
  
  
  private void initComponents() {
    initFileMenu();
    initEditMenu();
    initViewMenu();
    initHelpMenu();
  }

  public void refreshRecentFileMenu() {
    mOpenRecentFileMenu.removeAll();
    boolean hasEntries = false;

    for (int i = 0; i < getPrefs().recentProjectPaths.size(); i++) {
      String projectDirName = getPrefs().recentProjectNames.get(i);
      String projectName = getPrefs().recentProjectPaths.get(i);

      if (projectDirName != null) {
        final File projectDir = new File(projectDirName);

        if (projectDir.exists()) {
          hasEntries = true;

          JMenuItem recentFileMenuItem = new JMenuItem(projectName);

          recentFileMenuItem.setAccelerator(
              getAccel(getPrefs().sDYNAMIC_KEYS.get(i)));
          recentFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              mEditorInstance.openProject(projectDir.getPath());
            }
          });
          mOpenRecentFileMenu.add(recentFileMenuItem);
        }
      }
    }

    mOpenRecentFileMenu.setEnabled(hasEntries);

    if (hasEntries) {
      mOpenRecentFileMenu.add(new JSeparator());
      addItem(mOpenRecentFileMenu, "Clear List", null, 
          (e) -> mEditorInstance.clearRecentProjects());
    }
  }

  private JMenuItem addItem(JMenu menu, String text, KeyStroke accel, ActionListener act) {
    JMenuItem item = new JMenuItem(text);
    if (accel != null) item.setAccelerator(accel);
    item.addActionListener(act);
    menu.add(item);
    return item;
  }
    
  private void initFileMenu() {
    JMenu fileMenu = new JMenu("File");
    addItem(fileMenu, "New Project...", getAccel(KeyEvent.VK_N),
        (e) -> mEditorInstance.newProject());
    addItem(fileMenu, "Open Project...", getAccel(KeyEvent.VK_O),
        (e) -> mEditorInstance.openProject());
    mOpenRecentFileMenu = new JMenu("Open Recent Project");
    fileMenu.add(mOpenRecentFileMenu);
    // mOpenRecentFileMenu.setIcon(new ImageIcon("data/img/recent.png"));
    fileMenu.add(new JSeparator());
    
    refreshRecentFileMenu();

    // someOpen
    mCloseFileMenuItem = 
        addItem(fileMenu, "Close Project", getAccel(KeyEvent.VK_W),
            (e) -> mEditorInstance.close(QuitDialog.CLOSE_PROJ_DIALOG));

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    
    // someOpen && projectChanged
    mSaveFileMenuItem = 
        addItem(fileMenu, "Save", getAccel(KeyEvent.VK_S), 
            (e) -> mEditorInstance.save());
    // someOpen
    mSaveAsMenuItem =
        addItem(fileMenu, "Save As", getAccel(KeyEvent.VK_S, SHIFT_DOWN_MASK),
            (e) -> mEditorInstance.saveAs());
    //mSaveAsMenuItem.setIcon(new ImageIcon("data/img/saveas.png"));

    // someOpen
    mSaveAllMenuItem = 
        addItem(fileMenu, "Save All", getAccel(KeyEvent.VK_S, ALT_DOWN_MASK),
            (e) -> mEditorInstance.saveAll());
    //mSaveAllMenuItem.setIcon(new ImageIcon("data/img/saveall.png"));

    if (Preferences.isWindows()) {
      fileMenu.add(new JSeparator());
      addItem(fileMenu, "Quit", getAccel(KeyEvent.VK_Q),
          (e) -> mEditorInstance.closeAll());
      // mExitEditorMenuItem.setIcon(new ImageIcon("data/img/exit.png"));
    }

    add(fileMenu);
  }

  private void initEditMenu() {
    mEditMenu = new JMenu("Edit");

    mEditMenu.add(UndoRedoProvider.getInstance().getUndoAction());
    mEditMenu.add(UndoRedoProvider.getInstance().getRedoAction());
    mEditMenu.add(new JSeparator());
    // sth selected
    mCopyMenuItem = addItem(mEditMenu, "Copy", getAccel(KeyEvent.VK_C), 
        (e) -> getCurrentWorkSpace().copySelectedNodes());
    // sth selected
    mCutMenuItem = addItem(mEditMenu, "Cut", getAccel(KeyEvent.VK_X),
        (e) -> getCurrentWorkSpace().cutSelectedNodes());

    // sth on clipboard
    mPasteMenuItem = addItem(mEditMenu, "Paste", getAccel(KeyEvent.VK_V),
        (e) -> getCurrentWorkSpace().pasteNodesFromClipboard());
    // sth selected TODO: MISSING
    mDeleteMenuItem = addItem(mEditMenu, "Delete", getAccel(KeyEvent.VK_DELETE),
        (e) -> getCurrentWorkSpace()//.delete()
        );
    mEditMenu.add(new JSeparator());
    // always
    addItem(mEditMenu, "Normalize all Edges", getAccel(KeyEvent.VK_N, ALT_DOWN_MASK),
        (e) -> getCurrentWorkSpace().normalizeAllEdges());
    addItem(mEditMenu, "Straighen all Edges", getAccel(KeyEvent.VK_B, ALT_DOWN_MASK),
        (e) -> getCurrentWorkSpace().straightenAllEdges());
    add(mEditMenu);
  }

  private void initViewMenu() {
    mViewMenu = new JMenu("View");

    mShowGridMenuItem = new JCheckBoxMenuItem("Show Grid");
    mShowGridMenuItem.addActionListener((e) -> {
      getCurrentWorkSpace().setShowGrid(
          ((JCheckBoxMenuItem)e.getSource()).getState());
    });

    mViewMenu.add(mShowGridMenuItem);

    mShowIdMenuItem = new JCheckBoxMenuItem("Show Node IDs");
    mShowIdMenuItem.addActionListener((e) -> {
      getCurrentWorkSpace().setShowNodeIds(
          ((JCheckBoxMenuItem)e.getSource()).getState());
    });
    mViewMenu.add(mShowIdMenuItem);

    mSnapToGridMenuItem = new JCheckBoxMenuItem("Snap to Grid");
    mSnapToGridMenuItem.addActionListener((e) -> {
      getCurrentWorkSpace().setSnapToGrid(
          ((JCheckBoxMenuItem)e.getSource()).getState());
    });
    mViewMenu.add(mSnapToGridMenuItem);

    // **************************OPTIONS*************************************
    mViewMenu.add(new JSeparator());
    addItem(mViewMenu, "Options", getAccel(KeyEvent.VK_COMMA),
        (e) -> mEditorInstance.showOptions());

    add(mViewMenu);
  }

  private void initHelpMenu() {
    JMenu helpMenu = new JMenu("Help");
    addItem(helpMenu, 
        "Help", getAccel(KeyEvent.VK_H), (e) -> mEditorInstance.showHelp());
    helpMenu.add(new JSeparator());
    addItem(helpMenu, 
        "About", getAccel(KeyEvent.VK_I), (e) -> mEditorInstance.showAbout());
    add(helpMenu);
  }
}
