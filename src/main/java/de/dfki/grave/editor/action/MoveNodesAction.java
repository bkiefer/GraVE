package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.Map;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.geom.Position;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveNodesAction extends EditorAction {

  private Map<BasicNode, Position> mOldLocations, mNewLocations;

  public MoveNodesAction(WorkSpace workSpace, Map<BasicNode, Position> orig,
      Map<BasicNode, Position> newlocs) {
    mWorkSpace = workSpace;
    mNewLocations = newlocs;
    mOldLocations = orig;
  }

  private void setNodeLocations(Map<BasicNode, Position> map) {
    for (Map.Entry<BasicNode, Position> e : map.entrySet()) {
      mWorkSpace.moveTo(e.getKey(), e.getValue());
    }
  }

  protected void undoIt() {
    setNodeLocations(mOldLocations);
  }

  protected void doIt() {
    setNodeLocations(mNewLocations);
  }

  protected String msg() { return "Moving Nodes"; }
}
