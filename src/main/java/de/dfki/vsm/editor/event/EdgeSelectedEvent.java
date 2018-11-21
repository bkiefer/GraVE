package de.dfki.vsm.editor.event;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.model.flow.AbstractEdge;

/**
 * @author Gregor Mehlmann
 */
public class EdgeSelectedEvent {

  private Edge mSource;

  private AbstractEdge mEdge;

  public EdgeSelectedEvent(Edge source, AbstractEdge edge) {
    mSource = source;
    mEdge = edge;
  }

  public AbstractEdge getEdge() {
    return mEdge;
  }

  public String getEventDescription() {
    return "NodeSelectedEvent(" + mEdge.getSourceUnid() + " -> " + mEdge.getTargetUnid() + ")";
  }

  public Edge getSource() { return mSource; }
}
