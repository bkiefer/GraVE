package de.dfki.vsm.editor.event;

import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Gregor Mehlmann
 */
public class NodeExecutedEvent extends EventObject {

  private BasicNode mNode;

  public NodeExecutedEvent(Object source, BasicNode node) {
    super(source);
    mNode = node;
  }

  public BasicNode getNode() {
    return mNode;
  }

  public String getEventDescription() {
    return "NodeEvent(" + mNode.getId() + ")";
  }
}
