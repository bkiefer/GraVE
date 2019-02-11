package de.dfki.grave.editor.event;

import de.dfki.grave.editor.Edge;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 * @author Sergio Soto
 */
public class EdgeEditEvent {

  private Edge mView;
  private AbstractEdge mEdge;

  public EdgeEditEvent(Edge view, AbstractEdge edge) {
    mEdge = edge;
  }

  public AbstractEdge getEdge() {
    return mEdge;
  }

  public String getEventDescription() {
    return "EdgeEditEvent(" + mEdge.getSourceUnid() + " -> " + mEdge.getTargetUnid() + ")";
  }

  public Edge getGUIEdge() {
    return mView;
  }

}
