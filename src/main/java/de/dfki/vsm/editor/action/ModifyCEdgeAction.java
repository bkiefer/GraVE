package de.dfki.vsm.editor.action;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.dialog.ModifyCEdgeDialog;
import de.dfki.vsm.editor.project.sceneflow.workspace.WorkSpacePanel;
import de.dfki.vsm.model.sceneflow.chart.edge.GuardedEdge;
import de.dfki.vsm.model.sceneflow.glue.command.Command;

/**
 * The action that modifies a conditional edge. This action has to remember the
 * old condition and the new condition of the conditional edge and switches
 * between them when undo or redo.
 *
 * @author Gregor Mehlmann
 */
public class ModifyCEdgeAction extends ModifyEdgeAction {

    /**
     * The old condition of the conditional edge
     */
    private Command mOldCondition;

    /**
     * The new condition of the conditional edge
     */
    private Command mNewCondition;

    /**
     *
     * @param edge
     * @param workSpace
     */
    public ModifyCEdgeAction(Edge edge, WorkSpacePanel workSpace) {
        super(edge, workSpace);
    }

    /**
     * Execute this action
     */
    @Override
    public void run() {

        // Remember the old condition
        mOldCondition = ((GuardedEdge) mDataEdge).getCondition();

        // Show a dialog to modify the condition
        ModifyCEdgeDialog dialog = new ModifyCEdgeDialog(((GuardedEdge) mDataEdge));
        GuardedEdge cedge = dialog.run();

        // If the condition was successfully modified then
        // remember the new condition and update the undomanager
        if (cedge != null) {
            mNewCondition = cedge.getCondition();
            mUndoManager.addEdit(new Edit());
            UndoAction.getInstance().refreshUndoState();
            RedoAction.getInstance().refreshRedoState();
        }
    }

    private class Edit extends AbstractUndoableEdit {

        @Override
        public void undo() throws CannotUndoException {
            ((GuardedEdge) mDataEdge).setCondition(mOldCondition);

            // mGUIEdge.update();
            mGUIEdge.repaint(100);
        }

        @Override
        public void redo() throws CannotRedoException {
            ((GuardedEdge) mDataEdge).setCondition(mNewCondition);

            // mGUIEdge.update();
            mGUIEdge.repaint(100);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public boolean canRedo() {
            return true;
        }

        @Override
        public String getUndoPresentationName() {
            return "Undo modification of CEdge";
        }

        @Override
        public String getRedoPresentationName() {
            return "Redo modification of CEdge";
        }
    }
}
