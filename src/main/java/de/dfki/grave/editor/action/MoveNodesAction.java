package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.Map;

import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveNodesAction extends EditorAction {

  private Map<Node, Point> mOldLocations, mNewLocations;


  public MoveNodesAction(WorkSpace workSpace, Map<Node, Point> orig,
      Map<Node, Point> newlocs) {
    mWorkSpace = workSpace;
    mNewLocations = newlocs;
    mOldLocations = orig;
  }

  private void setNodeLocations(Map<Node, Point> map) {
    for (Map.Entry<Node, Point> e : map.entrySet()) {
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
