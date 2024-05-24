package de.dfki.mlt.g2v;

import java.util.Iterator;
import java.util.stream.StreamSupport;

import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.EpsilonEdge;
import de.dfki.grave.model.ForkingEdge;
import de.dfki.grave.model.GuardedEdge;
import de.dfki.grave.model.InterruptEdge;
import de.dfki.grave.model.RandomEdge;
import de.dfki.grave.model.TimeoutEdge;

public class Utils {

  private static <T> Iterable<T> getItbl(Iterator<T> it) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() { return it; }
    };
  }

  static Iterable<AbstractEdge> getInterruptiveEdges(BasicNode s) {
    return getItbl(StreamSupport.stream(s.getEdgeList().spliterator(), false)
        .filter(e -> e instanceof InterruptEdge).iterator());
  }

  static Iterable<AbstractEdge> getTimeoutEdges(BasicNode s) {
    return getItbl(StreamSupport.stream(s.getEdgeList().spliterator(), false)
        .filter(e -> e instanceof TimeoutEdge).iterator());
  }

  static Iterable<AbstractEdge> getConditionalEdges(BasicNode s) {
    return getItbl(StreamSupport.stream(s.getEdgeList().spliterator(), false)
        .filter(e -> e instanceof GuardedEdge).iterator());
  }

  static Iterable<AbstractEdge> getProbabilityEdges(BasicNode s) {
    return getItbl(StreamSupport.stream(s.getEdgeList().spliterator(), false)
        .filter(e -> e instanceof RandomEdge).iterator());
  }

  static Iterable<AbstractEdge> getEpsilonEdges(BasicNode s) {
    return getItbl(StreamSupport.stream(s.getEdgeList().spliterator(), false)
        .filter(e -> e instanceof EpsilonEdge).iterator());
  }

  static Iterable<AbstractEdge> getForkingEdges(BasicNode s) {
    return getItbl(StreamSupport.stream(s.getEdgeList().spliterator(), false)
      .filter(e -> e instanceof ForkingEdge).iterator());
  }

}
