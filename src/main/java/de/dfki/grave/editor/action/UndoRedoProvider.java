package de.dfki.grave.editor.action;

import static de.dfki.grave.AppFrame.*;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A singleton to handle all undo/redo actions and connect it to the UI
 *  elements that allow to trigger them
 * @author kiefer
 */
public class UndoRedoProvider {
  private static final Logger mLogger = LoggerFactory.getLogger(UndoRedoProvider.class);

  @SuppressWarnings("serial")
  public class UndoRedoAction extends AbstractAction {
    private final String name;
    private final Supplier<String> getName;
    private final Runnable action;
    private final Supplier<Boolean> canDo;

    UndoRedoAction(String what,
        Supplier<String> getname, Runnable act, Supplier<Boolean> active) {
      super(what);
      name = what;
      getName = getname;
      action = act;
      canDo = active;
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        action.run();
        refreshAllState();
      } catch (CannotUndoException ex) {
        mLogger.error(ex.getMessage());
      }
    }

    public void refreshState() {
      boolean val = canDo.get();
      setEnabled(val);
      putValue(Action.SHORT_DESCRIPTION,
          val ? getName.get() : name + " last action");
    }
  }

  private final UndoRedoAction undoAction;
  private final UndoRedoAction redoAction;

  // undo manager for elements in the state machine
  private final UndoManager mElementsUndoManager;
  // undo manager for typing changes in comments and code areas
  private final UndoManager mTextUndoManager;

  private boolean mInTextMode;

  public UndoRedoProvider() {
    undoAction =
      new UndoRedoAction("Undo",
          (() -> getManager().getUndoPresentationName()),
          (() -> getManager().undo()),
          (() -> getManager().canUndo()));
    undoAction.putValue(Action.ACCELERATOR_KEY, getAccel(KeyEvent.VK_Z));
    undoAction.putValue(Action.NAME, "Undo");

    redoAction =
      new UndoRedoAction("Redo",
          (() -> getManager().getRedoPresentationName()),
          (() -> getManager().redo()),
          (() -> getManager().canRedo()));
    redoAction.putValue(Action.ACCELERATOR_KEY,
        getAccelMask(KeyEvent.VK_Z, SHIFT_DOWN_MASK));
    redoAction.putValue(Action.NAME, "Redo");

    mElementsUndoManager = new UndoManager();
    mTextUndoManager = new UndoManager();
    mInTextMode = false;
    refreshAllState();
  }

  private UndoManager getManager() {
    if (mInTextMode) {
      return mTextUndoManager;
    }
    return mElementsUndoManager;
  }

  public void refreshAllState() {
    undoAction.refreshState();
    redoAction.refreshState();
  }

  /** The provider has two states: either it's in text editing mode or not,
   *  and accepts either one edit or the other.
   */

  /** This is for edits of an item, not text */
  public void addEdit(UndoableEdit e) {
    getManager().addEdit(e);
    refreshAllState();
  }

  /** This is for edits of text, and will be ignored if not in text mode, since
   *  it's triggered not from the UI
   */
  public void addTextEdit(UndoableEdit e) {
    if (mInTextMode) {
      getManager().addEdit(e);
      refreshAllState();
    }
  }

  public void startTextMode() {
    mInTextMode = true;
    mLogger.debug("Text Edit Mode on");
    refreshAllState();
  }

  public void endTextMode() {
    if (mInTextMode) {
      mInTextMode = false;
      mLogger.debug("Text Edit Mode off");
      mTextUndoManager.discardAllEdits();
      refreshAllState();
    }
  }

  public AbstractAction getUndoAction() {
    return undoAction;
  }

  public AbstractAction getRedoAction() {
    return redoAction;
  }

}
