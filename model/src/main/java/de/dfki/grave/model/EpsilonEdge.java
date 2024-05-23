package de.dfki.grave.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="EEdge")
public class EpsilonEdge extends AbstractEdge {

  public EpsilonEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    return deepCopy(new EpsilonEdge(), orig2copy);
  }

  public int getHashCode() {
    return super.hashCode() + 77;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass()) && super.equals(o);
  }

}
