package de.dfki.grave.editor.panels;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.Comment;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.action.CreateCommentAction;
import de.dfki.grave.editor.action.CreateNodeAction;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.CommentBadge;

/** Handle drag and drop for the elements on the workspace:
 *  - Comments
 *  - Nodes
 *  - Edges
 */
public class WorkSpaceDropAdapter extends DropTargetAdapter {
  private static final Logger logger = LoggerFactory
      .getLogger(WorkSpaceDropAdapter.class);

  private final int mAcceptableActions = DnDConstants.ACTION_COPY;

  private WorkSpace mWorkSpace;

  public WorkSpaceDropAdapter(WorkSpace wsp) {
    mWorkSpace = wsp;
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
  }

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
    Object data = null;
    try {
      DataFlavor flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
      data = dtde.getTransferable().getTransferData(flavor);
    } catch (ClassNotFoundException | UnsupportedFlavorException
        | IOException e) {
      logger.error("DragNDrop Error: {}", e);
    }

    if (data instanceof Comment) {
      dtde.acceptDrag(dtde.getDropAction());
    }

    if (data instanceof AbstractEdge) {
      Point pos = dtde.getLocation();
      dtde.acceptDrag(dtde.getDropAction());
      mWorkSpace
          .setMessageLabelText("Drag edge on a node to select edge source");
      Node node = mWorkSpace.findNodeAtPoint(pos);
      if (node != null && !node.isEdgeAllowed((AbstractEdge) data)) {
        mWorkSpace.setMessageLabelText("Edge is not allowed at this node");
      }
    }
  }

  @Override
  public void dragExit(DropTargetEvent dte) {
    mWorkSpace.setMessageLabelText("");
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {
    mWorkSpace.setMessageLabelText("");
    try {
      // Get the data of the transferable
      Object data = dtde.getTransferable().getTransferData(
          new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));

      if (data instanceof BasicNode) {
        BasicNode n = (BasicNode) data;
        BasicNode newModel = mWorkSpace.createNode(dtde.getLocation(), n);
        if (newModel != null) {
          new CreateNodeAction(mWorkSpace.getEditor(), newModel).run();
        } else {
          mWorkSpace.setMessageLabelText("First (Start) node must be basic node");
        }
        dtde.acceptDrop(mAcceptableActions);
        dtde.getDropTargetContext().dropComplete(true);
      } else if (data instanceof AbstractEdge) {
        mWorkSpace.startNewEdge((AbstractEdge) data, dtde.getLocation());
        dtde.acceptDrop(mAcceptableActions);
        dtde.getDropTargetContext().dropComplete(true);
      } else if (data instanceof Comment) {
        CommentBadge comm = mWorkSpace.createComment(dtde.getLocation());
        new CreateCommentAction(mWorkSpace.getEditor(), comm).run();
        dtde.acceptDrop(mAcceptableActions);
        dtde.getDropTargetContext().dropComplete(true);
      } else {
        dtde.rejectDrop();
      }
    } catch (ClassNotFoundException | UnsupportedFlavorException
        | IOException e) {
      dtde.rejectDrop();
    }

    // Update whole editor after a drop!!!!
    AppFrame.getInstance().refresh();
  }

}
