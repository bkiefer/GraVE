package de.dfki.grave.editor.panels;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.util.evt.EventDispatcher;

public class UndoRedoProvider {
  private static final Logger mLogger = LoggerFactory.getLogger(UndoRedoProvider.class);

  private static final UndoRedoAction undoAction = new UndoRedoAction(true);
  private static final UndoRedoAction redoAction = new UndoRedoAction(false);

  @SuppressWarnings("serial")
  private static class UndoRedoAction extends AbstractAction {
    private final boolean isUndo;

    UndoRedoAction(boolean undo) {
      super(undo ? "Undo" : "Redo");
      isUndo = undo;
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
          undo ? Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
               : (java.awt.event.InputEvent.SHIFT_MASK
                   | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
      setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      UndoManager manager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();
      try {
        if (isUndo)
          manager.undo();
        else
          manager.redo();
      } catch (CannotUndoException ex) {
        mLogger.error(ex.getMessage());
      }

      UndoRedoProvider.refreshState();
      EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
    }

    public void refreshState() {
      UndoManager manager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();
      boolean val = isUndo ? manager.canUndo() : manager.canRedo();
      setEnabled(val);
      putValue(Action.SHORT_DESCRIPTION, val
          ? (isUndo ? manager.getUndoPresentationName()
                    : manager.getRedoPresentationName())
          : (isUndo ? "Undo" : "Redo") + " last action");
    }
  }

  public static Action getUndoAction() { return undoAction; }

  public static Action getRedoAction() { return redoAction; }

  public static void refreshState() {
    undoAction.refreshState();
    redoAction.refreshState();
  }
}
