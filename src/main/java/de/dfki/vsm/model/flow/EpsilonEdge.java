package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="EEdge")
public class EpsilonEdge extends AbstractEdge {

  // TODO:
  public EpsilonEdge getCopy() {
    return copyFieldsTo(new EpsilonEdge());
  }

  public int getHashCode() {
    return super.hashCode() + 77;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass()) && super.equals(o);
  }

}
