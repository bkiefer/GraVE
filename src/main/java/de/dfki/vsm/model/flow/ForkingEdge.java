package de.dfki.vsm.model.flow;

import java.util.Map;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="FEdge")
public class ForkingEdge extends AbstractEdge {

  public ForkingEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    return deepCopy(new ForkingEdge(), orig2copy);
  }

  public int getHashCode() {
    return super.hashCode() + 79;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass()) && super.equals(o);
  }

}
