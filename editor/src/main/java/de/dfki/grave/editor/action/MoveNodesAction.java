package de.dfki.grave.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.util.Map;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.Position;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class MoveNodesAction extends EditorAction {

  private Map<BasicNode, Position> mOldLocations, mNewLocations;

  public MoveNodesAction(ProjectEditor editor, Map<BasicNode, Position> orig,
      Map<BasicNode, Position> newlocs) {
    super(editor);
    mNewLocations = newlocs;
    mOldLocations = orig;
  }

  private void setNodeLocations(Map<BasicNode, Position> map) {
    for (Map.Entry<BasicNode, Position> e : map.entrySet()) {
      e.getKey().setPosition(e.getValue());
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
