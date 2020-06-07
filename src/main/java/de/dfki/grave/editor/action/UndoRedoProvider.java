package de.dfki.grave.editor.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.util.evt.EventDispatcher;

/** A singleton to handle all undo/redo actions and connect it to the UI 
 *  elements that allow to trigger them
 * @author kiefer
 */
public class UndoRedoProvider {
  private static final Logger mLogger = LoggerFactory.getLogger(UndoRedoProvider.class);

  @SuppressWarnings("serial")
  private class UndoRedoAction extends AbstractAction {
    private final String name;
    private final Supplier<String> getName;
    private final Runnable action;
    private final Supplier<Boolean> canDo;

    UndoRedoAction(String what, KeyStroke accel,
        Supplier<String> getname, Runnable act, Supplier<Boolean> active) {
      super(what);
      name = what;
      getName = getname;
      action = act;
      canDo = active;
      putValue(ACCELERATOR_KEY, accel);
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        action.run();
      } catch (CannotUndoException ex) {
        mLogger.error(ex.getMessage());
      }

      UndoRedoProvider.this.refreshState();
      EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
    }

    public void refreshState() {
      boolean val = canDo.get();
      setEnabled(val);
      putValue(Action.SHORT_DESCRIPTION,
          val ? getName.get() : name + " last action");
    }
  }
  
  private static UndoRedoProvider mInstance;
  
  private final UndoRedoAction undoAction;
  private final UndoRedoAction redoAction;

  // undo manager for typing changes in comments and code areas
  private final UndoManager mTextUndoManager;
  
  private boolean mInTextMode;
  
  private UndoRedoProvider() {
    undoAction =
      new UndoRedoAction("Undo",
          KeyStroke.getKeyStroke(KeyEvent.VK_Z,
              Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
          (() -> getManager().getUndoPresentationName()),
          (() -> getManager().undo()),
          (() -> getManager().canUndo()));

    redoAction =
      new UndoRedoAction("Redo",
          KeyStroke.getKeyStroke(KeyEvent.VK_Z,
              java.awt.event.InputEvent.SHIFT_DOWN_MASK
              | Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
          (() -> getManager().getRedoPresentationName()),
          (() -> getManager().redo()),
          (() -> getManager().canRedo()));
    mTextUndoManager = new UndoManager();
    mInTextMode = false;
  }
  
  public static UndoRedoProvider getInstance() {
    if (mInstance == null) {
      mInstance = new UndoRedoProvider();
    }
    return mInstance;
  }
  

  private UndoManager getManager() {
    if (mInTextMode) {
      return mTextUndoManager;
    }
    return AppFrame.getInstance().getSelectedProjectEditor()
        .getUndoManager();
  }

  private void refreshState() {
    undoAction.refreshState();
    redoAction.refreshState();
  }

  public Action getUndoAction() { return undoAction; }

  public Action getRedoAction() { return redoAction; }

  /** TODO: The provider should have two states: either it's in text editing
   *  mode or not, and accept either one edit or the other.
   */
  
  /** This is for edits of an item, not text */
  public void addEdit(UndoableEdit e) {
    getManager().addEdit(e);
    refreshState();
  }
  
  /** This is for edits of text, and will be ignored if not in text mode, since
   *  it's triggered not from the UI
   */
  public void addTextEdit(UndoableEdit e) {
    if (mInTextMode) {
      getManager().addEdit(e);
      refreshState();
    }
  }

  /** refresh tool and menu bar */
  private void refreshMenus() {
    refreshState();
    AppFrame.getInstance().refreshMenuBar();
    AppFrame.getInstance().getSelectedProjectEditor().refreshToolBar();
  }
  
  public void startTextMode() {
    mInTextMode = true;
    refreshMenus();
  }

  public void endTextMode() {
    if (mInTextMode) {
      mInTextMode = false;
      mTextUndoManager.discardAllEdits();
      refreshMenus();
    }
  }
}
