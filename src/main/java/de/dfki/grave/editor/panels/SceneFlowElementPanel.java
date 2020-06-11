package de.dfki.grave.editor.panels;

import static de.dfki.grave.Icons.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.Preferences;
import de.dfki.grave.editor.TreeEntry;
import de.dfki.grave.editor.event.TreeEntrySelectedEvent;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public final class SceneFlowElementPanel extends JScrollPane {

  // The singelton logger instance
  private static final Logger mLogger = LoggerFactory
      .getLogger(SceneFlowElementPanel.class);

  /**
  * @author Gregor Mehlmann
  */
  private class ElementTree extends JTree
      implements ActionListener, TreeSelectionListener {
    //List of scene elements

    private final ArrayList<TreeEntry> mSceneEntryList = new ArrayList<TreeEntry>();
    //Tree entry for scenes
    private final TreeEntry mSceneFlowEntry = new TreeEntry("Scenes", null,
        null);
    //Tree entry for functions
    private final TreeEntry mFunctionsEntry = new TreeEntry("Functions", null,
        null);

    //Global element tree
    private final TreeEntry mElementTree = new TreeEntry("SceneFlow",
        ICON_ROOT_FOLDER, null);

    //Popup menu buttons //TODO: does it work?
    private final JMenuItem functionsAdd = new JMenuItem("Add...");
    private final JMenuItem functionModify = new JMenuItem("Modify...");
    private final JMenuItem functionRemove = new JMenuItem("Remove");

    // Drag & Drop support
    private DragSource mDragSource;
    private DragGestureListener mDragGestureListener;
    private DragSourceListener mDragSourceListener;
    private int mAcceptableDnDActions;
    private TreeEntry mSceneEntryRoot;

    public void updateFunctions() {
      mFunctionsEntry.removeAllChildren();
      // TODO: REPLACE WITH SOMETHING SENSIBLE
      /*
      List<FunctionDefinition> functionDefinitions = new ArrayList<FunctionDefinition>(
            //mSceneFlow.getUsrCmdDefMap().values()
            Collections.emptyList()
      );

      Collections.sort((List) functionDefinitions);

      for (final FunctionDefinition def : functionDefinitions) {
      mFunctionsEntry.add(new TreeEntry(def.getName(), def.isValidClass()
              ? ICON_FUNCTION_ENTRY
              : ICON_FUNCTION_ERROR_ENTRY, def));
      }
      */
    }

    /**
     *
     *
     */
    public ElementTree(EditorProject project) {
      super(new DefaultTreeModel(null));

      //mDialogAct = mProject.getDialogAct();
      setBorder(BorderFactory.createEmptyBorder());
      setCellRenderer(new CellRenderer());
      setBackground(Color.WHITE);
      setRootVisible(false);

      // setRowHeight(0);
      //
      initDnDSupport();
      initComponents();
      addMouseListener(getMouseAdapter(this));
      ToolTipManager.sharedInstance().registerComponent(this);

      //
      expandAll();

    }

    //
    @Override
    public void valueChanged(TreeSelectionEvent e) {
      TreePath path = e.getPath();
      int pathCount = path.getPathCount();

      // Make sure that focus can be requested
      if (requestFocusInWindow()) {
        updateUI();
      }

      TreeEntry lastPathComponent = (TreeEntry) path.getLastPathComponent();

      if (pathCount == 3) {
        TreePath parentPath = path.getParentPath();

        if (lastPathComponent.getData() != null) {

        }
      }

    }

    /**
     *
     *
     */
    public void expandAll() {
      for (int i = 0; i < getRowCount(); i++) {
        expandRow(i);
      }

      updateUI();
    }

    public void expandAllRecursive() {
      TreeNode root = (TreeNode) this.getModel().getRoot();
      if (root != null) {
        // Traverse tree from root
        expandAll(new TreePath(root), "Dialog Acts");
      }

    }

    /**
     * @return Whether an expandPath was called for the last node in the parent path
     */
    private boolean expandAll(TreePath parent, String skip) {
      // Traverse children
      // Variable skip is to indicate that we dont want to expand certain node
      TreeNode node = (TreeNode) parent.getLastPathComponent();
      if (((TreeEntry) node).getText().equals(skip)) {
        return false;
      }
      if (node.getChildCount() > 0) {
        boolean childExpandCalled = false;
        for (Enumeration e = node.children(); e.hasMoreElements();) {
          TreeNode n = (TreeNode) e.nextElement();
          TreePath path = parent.pathByAddingChild(n);
          childExpandCalled = expandAll(path, skip) || childExpandCalled; // the OR order is important here, don't let childExpand first. func calls will be optimized out !
        }

        if (!childExpandCalled) { // only if one of the children hasn't called already expand
          // Expansion or collapse must be done bottom-up, BUT only for non-leaf nodes

          this.expandPath(parent);

        }
        return true;
      } else {
        return false;
      }
    }

    /**
     *
     *
     */
    private void initComponents() {

      mElementTree.add(mSceneFlowEntry);
      mElementTree.add(mFunctionsEntry);

      ((DefaultTreeModel) getModel()).setRoot(mElementTree);
      functionsAdd.addActionListener(this);
      functionModify.addActionListener(this);
      functionRemove.addActionListener(this);
      getSelectionModel()
          .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      addTreeSelectionListener(this);
      refresh();
    }

    /**
     * Refresh the element tree
     */
    public final void refresh() {
      // Print some information
      //mLogger.message("Refreshing '" + this + "'");
      updateSceneList();
      updateFunctions();
      //        updateDialogActs();
      //remove empty scenes
      for (int i = 0; i < mSceneFlowEntry.getChildCount(); i++) {
        TreeEntry tempTE = (TreeEntry) mSceneFlowEntry.getChildAt(i);
        if (tempTE.getChildCount() == 0) {
          mSceneEntryList.remove(tempTE);
          mSceneFlowEntry.remove(i);
        }
      }
      // Update the visual appearance of the ElementTree
      updateUI();
    }

    /**
     * Refresh the list of scenes
     *
     */
    private void updateSceneList() {
      // System.out.println("Updating Scenes");
      for (int i = 0; i < mSceneEntryList.size(); i++) {
        mSceneEntryList.get(i).removeAllChildren();

        if (mSceneEntryList.get(i).isNodeChild(mSceneFlowEntry)) {

          mSceneFlowEntry.remove(mSceneEntryList.get(i));
        }
      }

      //
      expandAll();
    }

    //
    private MouseAdapter getMouseAdapter(final JTree tree) {
      return new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          TreePath path = tree.getPathForLocation(e.getX(), e.getY());

          if (path == null) {
            return;
          }

          tree.setSelectionPath(path);

          boolean showPopup = false;
          JPopupMenu menu = new JPopupMenu();
          int pathCount = path.getPathCount();

          // System.out.println(pathCount);
          // TODO: why do we check the pathCount?
          // test if the user clicked on the Functions entry
          if (pathCount == 2) {
            if (path.getLastPathComponent().equals(mFunctionsEntry)) {
              if (e.isPopupTrigger()) {
                menu.add(functionsAdd);
                showPopup = true;
              }
            }

            if (path.getLastPathComponent() instanceof TreeEntry) {
              treeElementSelected(
                  (TreeEntry) path.getLastPathComponent());
            }
          } // test if the user clicked on exact function
          else if (pathCount == 3) {
            TreePath parentPath = path.getParentPath();

          } else if (pathCount == 4 && e.getClickCount() >= 1) {

          }

          if (showPopup) {
            menu.show(tree, e.getX(), e.getY());
          }
        }

        private void treeElementSelected(TreeEntry entry) {

        }

      };
    }

    //
    private String getEntryName(final TreeEntry entry) {
      if (entry != null) {
        //FunctionDefinition oldFunDef = (FunctionDefinition) entry.getData();
        //return oldFunDef.getName();
      }

      return null;
    }

    /**
     * Drag and Drop support
     */
    private void initDnDSupport() {

      // Create the default drag source
      mDragSource = DragSource.getDefaultDragSource();

      // Install the drag source listener
      mDragSourceListener = new DragSourceAdapter() {
      };

      // Install the drag gesture listener
      mDragGestureListener = new DragGestureListener() {
        @Override
        public void dragGestureRecognized(DragGestureEvent event) {

          // TODO: NULLPOINTEREXCEPTION abfangen
          TreeEntry selectedEntry = (TreeEntry) getSelectionPath()
              .getLastPathComponent();

          mDragSource.startDrag(event, DragSource.DefaultCopyDrop,
              selectedEntry, mDragSourceListener);
        }
      };

      // Set the acceptable actions
      mAcceptableDnDActions = DnDConstants.ACTION_COPY;

      // Set the default drag gesture recognizer
      mDragSource.createDefaultDragGestureRecognizer(this,
          mAcceptableDnDActions, mDragGestureListener);
    }

    public boolean isSceneLanguageAlreadyExist(String language) {
      boolean isLang = false;

      for (int i = 0; i < mSceneEntryList.size(); i++) {
        if (mSceneEntryList.get(i).getText()
            .equals("Scenes (" + language + ")")) {
          isLang = true;
        }
      }

      return isLang;
    }

    public TreeEntry getSceneEntry(String language) {
      for (int i = 0; i < mSceneEntryList.size(); i++) {

        //          System.out.println("Compare: " + mSceneListEntry.get(i).getText() +
        //                  " with " + "Scenes (" + language + ")");
        if (mSceneEntryList.get(i).getText()
            .equals("Scenes (" + language + ")")) {
          return mSceneEntryList.get(i);
        }
      }

      return null;
    }

    /**
     *
     *
     */
    private class CellRenderer extends DefaultTreeCellRenderer {

      public CellRenderer() {
        super();

        // setBackgroundNonSelectionColor(UIManager.getColor("Panel.background"));
      }

      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value,
          boolean selection, boolean expanded, boolean leaf, int row,
          boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selection, expanded,
            leaf, row, hasFocus);

        // Get the entry information
        Object data = ((TreeEntry) value).getData();
        String text = ((TreeEntry) value).getText();
        Icon icon = ((TreeEntry) value).getIcon();

        // Render the cell
        setText(text);

        if (icon != null) {
          setIcon(icon);
        }

        setBackgroundNonSelectionColor(Color.WHITE);
        setBackgroundSelectionColor(Color.LIGHT_GRAY);
        setBorderSelectionColor(Color.LIGHT_GRAY);

        return this;
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // TODO Auto-generated method stub

    }
  }

  // The element tree of this panel
  private final ElementTree mElementTree;

  // Construct the element display
  public SceneFlowElementPanel(final EditorProject project) {
    // Initialize the element tree
    mElementTree = new ElementTree(project);
    // Initialize the GUI components
    setBackground(Color.WHITE);
    setViewportView(mElementTree);
    setBorder(BorderFactory.createEtchedBorder());
    setPreferredSize(new Dimension(230, 200));
  }

  public void expandTree() {
    mElementTree.expandAllRecursive();
  }

  // Refresh the element display
  public final void refresh() {
    // Print some information
    //mLogger.message("Refreshing '" + this + "'");
    // Refresh the element tree
    mElementTree.refresh();
  }
}
