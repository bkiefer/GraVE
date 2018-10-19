package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 *
 * @author Gregor Mehlmann
 */
public class RedoAction extends AbstractAction {
  // The singelton logger instance

  private final Logger mLogger = LoggerFactory.getLogger(RedoAction.class);;

  private static RedoAction sSingeltonInstance = null;

  private RedoAction() {
    super("Redo");
    putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                    (java.awt.event.InputEvent.SHIFT_MASK
                    | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
    setEnabled(false);
  }

  public static RedoAction getInstance() {
    if (sSingeltonInstance == null) {
      sSingeltonInstance = new RedoAction();
    }

    return sSingeltonInstance;
  }

  public void actionPerformed(ActionEvent evt) {
    UndoManager manager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();

    try {
      manager.redo();
    } catch (CannotRedoException e) {
      mLogger.error(e.getMessage());
    }

    refreshRedoState();
    UndoAction.getInstance().refreshUndoState();
    EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
    /*
         * try {
         * UndoRedoManager.getInstance().redo();
         * } catch (CannotRedoException exc) {
         * exc.printStackTrace();
         * }
         * refreshRedoState();
         * UndoAction.getInstance().refreshUndoState();
     */
  }

  public void refreshRedoState() {
    UndoManager manager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();

    if (manager.canRedo()) {
      setEnabled(true);
      putValue(Action.NAME, manager.getRedoPresentationName());
    } else {
      setEnabled(false);
      putValue(Action.NAME, "Redo");
    }

    /*
         * if (UndoRedoManager.getInstance().canRedo()) {
         * setEnabled(true);
         * putValue(Action.NAME, UndoRedoManager.getInstance().getRedoPresentationName());
         * } else {
         * setEnabled(false);
         * putValue(Action.NAME, "Redo");
         * }
     */
  }
}
