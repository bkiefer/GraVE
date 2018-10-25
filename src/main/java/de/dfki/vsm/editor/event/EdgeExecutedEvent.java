package de.dfki.vsm.editor.event;

import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Gregor Mehlmann
 */
public class EdgeExecutedEvent extends EventObject {

  private AbstractEdge mEdge;

  public EdgeExecutedEvent(Object source, AbstractEdge edge) {
    super(source);
    mEdge = edge;
  }

  public AbstractEdge getEdge() {
    return mEdge;
  }

  public String getEventDescription() {
    //System.err.println(mEdge.getSourceNode());
    //System.err.println(mEdge.getTargetNode());

    return "EdgeEvent(" /* + mEdge.getSourceNode().getId() + "," + mEdge.getTargetNode().getId() */ + ")";
  }
}
