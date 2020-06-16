package de.dfki.grave.editor.panels;

import static de.dfki.grave.Preferences.getPrefs;
import static de.dfki.grave.AppFrame.getAccel;
import static java.awt.event.InputEvent.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.RecentProject;
import de.dfki.grave.editor.action.UndoRedoProvider;
import de.dfki.grave.editor.dialog.QuitDialog;
import de.dfki.grave.model.project.EditorConfig;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class EditorMenuBar extends JMenuBar {

  // private final Logger mLogger =
  // LoggerFactory.getLogger(EditorMenuBar.class);

  private static final int[] sDYNAMIC_KEYS = { 
      KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
      KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
  };
  
  private final AppFrame mAppInstance;

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
    mAppInstance = editor;
    // Initialize the GUI components
    initComponents();
  }

  private WorkSpacePanel getCurrentWorkSpace() {
    return mAppInstance.getWorkSpace();
  }

  private EditorConfig getEditorConfig() {
    return getCurrentWorkSpace().getEditorConfig();
  }

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
    if (!mEditMenu.isEnabled())
      return;
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

    int i = 0;
    for (RecentProject rp : getPrefs().getRecentProjects()) {
      if (rp.name != null) {
        final File projectDir = new File(rp.path);

        if (projectDir.exists()) {
          hasEntries = true;

          JMenuItem recentFileMenuItem = new JMenuItem(rp.name);
          recentFileMenuItem.setAccelerator(getAccel(sDYNAMIC_KEYS[i++]));
          recentFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              mAppInstance.openProject(projectDir);
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
          (e) -> mAppInstance.clearRecentProjects());
    }
  }

  private JMenuItem addItem(JMenu menu, String text, KeyStroke accel,
      ActionListener act) {
    JMenuItem item = new JMenuItem(text);
    if (accel != null)
      item.setAccelerator(accel);
    item.addActionListener(act);
    menu.add(item);
    return item;
  }

  private void initFileMenu() {
    JMenu fileMenu = new JMenu("File");
    addItem(fileMenu, "New Project...", getAccel(KeyEvent.VK_N),
        (e) -> mAppInstance.newProject());
    addItem(fileMenu, "Open Project...", getAccel(KeyEvent.VK_O),
        (e) -> mAppInstance.openProject());
    mOpenRecentFileMenu = new JMenu("Open Recent Project");
    fileMenu.add(mOpenRecentFileMenu);
    // mOpenRecentFileMenu.setIcon(new ImageIcon("data/img/recent.png"));
    fileMenu.add(new JSeparator());

    refreshRecentFileMenu();

    // someOpen
    mCloseFileMenuItem = addItem(fileMenu, "Close Project",
        getAccel(KeyEvent.VK_W),
        (e) -> mAppInstance.close(QuitDialog.CLOSE_PROJ_DIALOG));

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    // someOpen && projectChanged
    mSaveFileMenuItem = addItem(fileMenu, "Save", getAccel(KeyEvent.VK_S),
        (e) -> mAppInstance.save());
    // someOpen
    mSaveAsMenuItem = addItem(fileMenu, "Save As",
        getAccel(KeyEvent.VK_S, SHIFT_DOWN_MASK),
        (e) -> mAppInstance.saveAs());
    // mSaveAsMenuItem.setIcon(new ImageIcon("data/img/saveas.png"));

    // someOpen
    mSaveAllMenuItem = addItem(fileMenu, "Save All",
        getAccel(KeyEvent.VK_S, ALT_DOWN_MASK),
        (e) -> mAppInstance.saveAll());
    // mSaveAllMenuItem.setIcon(new ImageIcon("data/img/saveall.png"));

    fileMenu.add(new JSeparator());
    addItem(fileMenu, "Quit", getAccel(KeyEvent.VK_Q),
        (e) -> mAppInstance.closeAll());
    // mExitEditorMenuItem.setIcon(new ImageIcon("data/img/exit.png"));

    add(fileMenu);
  }

  private void initEditMenu() {
    mEditMenu = new JMenu("Edit");

    mEditMenu.add(mAppInstance.getUndoAction());
    mEditMenu.add(mAppInstance.getRedoAction());
    mEditMenu.add(new JSeparator());
    // sth selected
    mCopyMenuItem = addItem(mEditMenu, "Copy", getAccel(KeyEvent.VK_C),
        (e) -> getCurrentWorkSpace().copySelected());
    // sth selected
    mCutMenuItem = addItem(mEditMenu, "Cut", getAccel(KeyEvent.VK_X),
        (e) -> getCurrentWorkSpace().cutSelected());

    // sth on clipboard
    mPasteMenuItem = addItem(mEditMenu, "Paste", getAccel(KeyEvent.VK_V),
        (e) -> getCurrentWorkSpace().pasteClipboard());
    // sth selected TODO: MISSING
    mDeleteMenuItem = addItem(mEditMenu, "Delete", getAccel(KeyEvent.VK_DELETE),
        (e) -> getCurrentWorkSpace().deleteSelected()
    );
    mEditMenu.add(new JSeparator());
    // always
    addItem(mEditMenu, "Normalize all Edges",
        getAccel(KeyEvent.VK_N, ALT_DOWN_MASK),
        (e) -> getCurrentWorkSpace().normalizeAllEdges());
    addItem(mEditMenu, "Straighen all Edges",
        getAccel(KeyEvent.VK_B, ALT_DOWN_MASK),
        (e) -> getCurrentWorkSpace().straightenAllEdges());
    add(mEditMenu);
  }

  private void initViewMenu() {
    mViewMenu = new JMenu("View");

    mShowGridMenuItem = new JCheckBoxMenuItem("Show Grid");
    mShowGridMenuItem.addActionListener((e) -> {
      getCurrentWorkSpace()
          .setShowGrid(((JCheckBoxMenuItem) e.getSource()).getState());
    });

    mViewMenu.add(mShowGridMenuItem);

    mShowIdMenuItem = new JCheckBoxMenuItem("Show Node IDs");
    mShowIdMenuItem.addActionListener((e) -> {
      getCurrentWorkSpace()
          .setShowNodeIds(((JCheckBoxMenuItem) e.getSource()).getState());
    });
    mViewMenu.add(mShowIdMenuItem);

    mSnapToGridMenuItem = new JCheckBoxMenuItem("Snap to Grid");
    mSnapToGridMenuItem.addActionListener((e) -> {
      getCurrentWorkSpace()
          .setSnapToGrid(((JCheckBoxMenuItem) e.getSource()).getState());
    });
    mViewMenu.add(mSnapToGridMenuItem);

    // **************************OPTIONS*************************************
    mViewMenu.add(new JSeparator());
    addItem(mViewMenu, "Options", getAccel(KeyEvent.VK_COMMA),
        (e) -> mAppInstance.showOptions());

    add(mViewMenu);
  }

  private void initHelpMenu() {
    JMenu helpMenu = new JMenu("Help");
    addItem(helpMenu, "Help", getAccel(KeyEvent.VK_H),
        (e) -> mAppInstance.showHelp());
    helpMenu.add(new JSeparator());
    addItem(helpMenu, "About", getAccel(KeyEvent.VK_I),
        (e) -> mAppInstance.showAbout());
    add(helpMenu);
  }
}
