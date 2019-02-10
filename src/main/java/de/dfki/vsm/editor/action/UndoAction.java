package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
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

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 *
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class UndoAction extends AbstractAction {
  private final Logger mLogger = LoggerFactory.getLogger(UndoAction.class);;

  private static UndoAction sSingeltonInstance = null;

  private UndoAction() {
    super("Undo");
    putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    setEnabled(false);
  }

  public static UndoAction getInstance() {
    if (sSingeltonInstance == null) {
      sSingeltonInstance = new UndoAction();
    }

    return sSingeltonInstance;
  }

  public void actionPerformed(ActionEvent evt) {
    UndoManager manager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();

    try {
      manager.undo();
    } catch (CannotUndoException e) {
      mLogger.error(e.getMessage());
    }

    refreshUndoState();
    RedoAction.getInstance().refreshRedoState();
    EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
  }

  public void refreshUndoState() {
    UndoManager manager = EditorInstance.getInstance().getSelectedProjectEditor().getSceneFlowEditor().getUndoManager();

    if (manager.canUndo()) {
      setEnabled(true);
      putValue(Action.SHORT_DESCRIPTION, manager.getUndoPresentationName());
    } else {
      setEnabled(false);
      putValue(Action.SHORT_DESCRIPTION, "Undo last action");
    }
  }

}
