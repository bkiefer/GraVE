package de.dfki.vsm.editor.event;

import de.dfki.vsm.model.flow.BasicNode;

/**
 * @author Gregor Mehlmann
 */
public class NodeSelectedEvent  {

  private Object mSource;
  private BasicNode mNode;

  public NodeSelectedEvent(Object source, BasicNode node) {
    mSource = source;
    mNode = node;
  }

  public BasicNode getNode() {
    return mNode;
  }

  public String getEventDescription() {
    return "NodeSelectedEvent(" + mNode.getId() + ")";
  }

  public Object getSource() { return mSource; }
}
