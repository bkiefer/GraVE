package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveEdgeEndPointAction extends ModifyEdgeAction {

  public MoveEdgeEndPointAction(WorkSpace workSpace, AbstractEdge e,
      boolean start, int dock, BasicNode newNode) {
    super(workSpace, e);
    mNewDocks[start ? 0 : 1] = dock;
    mNewNodes[start ? 0 : 1] = newNode;
  }

  protected String msg() { return "Moving Edge Endpoint"; }
}