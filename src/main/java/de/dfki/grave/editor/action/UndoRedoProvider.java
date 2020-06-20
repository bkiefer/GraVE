package de.dfki.grave.editor.action;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.AppFrame;

/** A singleton to handle all undo/redo actions and connect it to the UI 
 *  elements that allow to trigger them
 * @author kiefer
 */
public class UndoRedoProvider {
  private static final Logger mLogger = LoggerFactory.getLogger(UndoRedoProvider.class);

  @SuppressWarnings("serial")
  public static class UndoRedoAction extends AbstractAction {
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
      } catch (CannotUndoException ex) {
        mLogger.error(ex.getMessage());
      }
    }

    public void refreshState(AbstractAction a) {
      boolean val = canDo.get();
      a.setEnabled(val);
      a.putValue(Action.SHORT_DESCRIPTION,
          val ? getName.get() : name + " last action");
    }
  }
  
  private final UndoRedoAction mUndoAction;
  private final UndoRedoAction redoAction;

  // undo manager for elements in the state machine
  private final UndoManager mElementsUndoManager;
  // undo manager for typing changes in comments and code areas
  private final UndoManager mTextUndoManager;
  
  private boolean mInTextMode;
  
  public UndoRedoProvider() {
    mUndoAction =
      new UndoRedoAction("Undo",
          (() -> getManager().getUndoPresentationName()),
          (() -> getManager().undo()),
          (() -> getManager().canUndo()));

    redoAction =
      new UndoRedoAction("Redo",
          (() -> getManager().getRedoPresentationName()),
          (() -> getManager().redo()),
          (() -> getManager().canRedo()));
    mElementsUndoManager = new UndoManager();
    mTextUndoManager = new UndoManager();
    mInTextMode = false;
    refreshState();
  }  

  private UndoManager getManager() {
    if (mInTextMode) {
      return mTextUndoManager;
    }
    return mElementsUndoManager;
  }

  private void refreshState() {
    AppFrame.getInstance().refreshUndoRedo(mUndoAction, redoAction);
  }

  public void doUndo(AbstractAction a, ActionEvent e) {
    mUndoAction.actionPerformed(e);
    refreshState();
  }
  
  public void doRedo(AbstractAction a, ActionEvent e) {
    redoAction.actionPerformed(e);
    refreshState();
  }

  /** The provider has two states: either it's in text editing mode or not, 
   *  and accepts either one edit or the other.
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
  
  public void startTextMode() {
    mInTextMode = true;
    mLogger.debug("Text Edit Mode on");
    refreshState();
  }

  public void endTextMode() {
    if (mInTextMode) {
      mInTextMode = false;
      mLogger.debug("Text Edit Mode off");
      mTextUndoManager.discardAllEdits();
      refreshState();
    }
  }
}
