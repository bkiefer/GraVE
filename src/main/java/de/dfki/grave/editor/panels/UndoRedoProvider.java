package de.dfki.grave.editor.panels;

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

public class UndoRedoProvider {
  private static final Logger mLogger = LoggerFactory.getLogger(UndoRedoProvider.class);

  @SuppressWarnings("serial")
  private static class UndoRedoAction extends AbstractAction {
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

      UndoRedoProvider.refreshState();
      EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
    }

    public void refreshState() {
      boolean val = canDo.get();
      setEnabled(val);
      putValue(Action.SHORT_DESCRIPTION,
          val ? getName.get() : name + " last action");
    }
  }

  private static final UndoRedoAction undoAction =
      new UndoRedoAction("Undo",
          KeyStroke.getKeyStroke(KeyEvent.VK_Z,
              Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
          (() -> getManager().getUndoPresentationName()),
          (() -> getManager().undo()),
          (() -> getManager().canUndo()));

  private static final UndoRedoAction redoAction =
      new UndoRedoAction("Redo",
          KeyStroke.getKeyStroke(KeyEvent.VK_Z,
              java.awt.event.InputEvent.SHIFT_MASK
              | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
          (() -> getManager().getRedoPresentationName()),
          (() -> getManager().redo()),
          (() -> getManager().canRedo()));


  private static UndoManager getManager() {
    return AppFrame.getInstance().getSelectedProjectEditor()
        .getSceneFlowEditor().getUndoManager();
  }

  private static void refreshState() {
    undoAction.refreshState();
    redoAction.refreshState();
  }

  public static Action getUndoAction() { return undoAction; }

  public static Action getRedoAction() { return redoAction; }

  public static void addEdit(UndoableEdit e) {
    getManager().addEdit(e);
    refreshState();
  }
}
