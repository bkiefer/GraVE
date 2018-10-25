package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="FEdge")
public class ForkingEdge extends AbstractEdge {

  // TODO:
  public ForkingEdge getCopy() {
    return copyFieldsTo(new ForkingEdge());
  }

  public int getHashCode() {
    return super.hashCode() + 79;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass()) && super.equals(o);
  }

}
