package de.dfki.vsm.editor.action;

import static de.dfki.vsm.editor.Node.Type.*;

import java.awt.Point;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import de.dfki.vsm.editor.CmdBadge;
import de.dfki.vsm.editor.Node.Type;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CreateNodeAction extends NodeAction {
  //

  private final EventDispatcher mDispatcher
          = EventDispatcher.getInstance();

  public CreateNodeAction(WorkSpacePanel workSpace, de.dfki.vsm.model.flow.BasicNode node) {
    mWorkSpace = workSpace;
    mCoordinate = new Point(node.getPosition().getXPos(),
            node.getPosition().getYPos());
    mGUINodeType = (SuperNode.class.isInstance(node))
            ? de.dfki.vsm.editor.Node.Type.SuperNode
            : de.dfki.vsm.editor.Node.Type.BasicNode;
    mSceneFlowPane = mWorkSpace.getSceneFlowEditor();
    mUndoManager = mSceneFlowPane.getUndoManager();
    mIDManager = mSceneFlowPane.getIDManager();
    mDataNodeId = node.getId();
    mDataNode = node;

    // DEBUG mDataNode.writeXML(new IndentOutputStream(System.out));
    mParentDataNode = mSceneFlowPane.getActiveSuperNode();

    // Create the GUI-BasicNode
    mGUINode = new de.dfki.vsm.editor.Node(mWorkSpace, mDataNode);

    // Create the command badge of the GUI-BasicNode
    mCmdBadge = new CmdBadge(mGUINode);
    mGUINode.resetLocation(mWorkSpace.mGridManager.getNodeLocation(mCoordinate));
  }

  public CreateNodeAction(WorkSpacePanel workSpace, Point coordinate, Type type) {
    mWorkSpace = workSpace;
    mCoordinate = coordinate;
    mGUINodeType = type;
    mSceneFlowPane = mWorkSpace.getSceneFlowEditor();
    mUndoManager = mSceneFlowPane.getUndoManager();
    mIDManager = mSceneFlowPane.getIDManager();

    if (mGUINodeType == BasicNode) {
      mDataNode = new BasicNode();
      mDataNodeId = mIDManager.getNextFreeID(mDataNode);
      mDataNode.setNameAndId(mDataNodeId);
      mDataNode.setPosition(new Position(mCoordinate.x, mCoordinate.y));
      mParentDataNode = mSceneFlowPane.getActiveSuperNode();
    } else if (mGUINodeType == SuperNode) {
      mDataNode = new SuperNode();
      mDataNodeId = mIDManager.getNextFreeID(mDataNode);
      mDataNode.setNameAndId(mDataNodeId);
      mDataNode.setPosition(new Position(mCoordinate.x, mCoordinate.y));
      mParentDataNode = mSceneFlowPane.getActiveSuperNode();

      //////////////////
      BasicNode mHistoryDataNode = new BasicNode();

      mHistoryDataNode.setHistoryNodeFlag(true);
      mHistoryDataNode.setName("History");
      //mHistoryDataNode.setId(mIDManager.getNextFreeID(mHistoryDataNode));
      mHistoryDataNode.setPosition(new Position(0, 0));
      mHistoryDataNode.setParentNode((SuperNode) mDataNode);
      ((SuperNode) mDataNode).addNode(mHistoryDataNode);
      ((SuperNode) mDataNode).setHistoryNode(mHistoryDataNode);
    }

    // Create the GUI-BasicNode
    mGUINode = new de.dfki.vsm.editor.Node(mWorkSpace, mDataNode);

    // Create the command badge of the GUI-BasicNode
    mCmdBadge = new CmdBadge(mGUINode);

    // Make newly created node selected
    mDispatcher.convey(new ElementSelectedEvent(mGUINode));

  }

  public void run() {
    create();
    mUndoManager.addEdit(new Edit());
    UndoAction.getInstance().refreshUndoState();
    RedoAction.getInstance().refreshRedoState();
  }

  private class Edit extends AbstractUndoableEdit {

    @Override
    public void undo() throws CannotUndoException {
      delete();
    }

    @Override
    public void redo() throws CannotRedoException {
      create();
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
      return "Undo Creation Of Node " + mDataNode.getName();
    }

    @Override
    public String getRedoPresentationName() {
      return "Redo Creation Of Node " + mDataNode.getName();
    }
  }
}
