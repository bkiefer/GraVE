package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="PEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class RandomEdge extends AbstractEdge {

  protected int mProbability = Integer.MIN_VALUE;

  @XmlAttribute(name="probability")
  public int getProbability() {
    return mProbability;
  }

  public void setProbability(int value) {
    mProbability = value;
  }

  public String getContent() {
    return "" + mProbability;
  }

  public void setContent(String s) {
    mProbability = Integer.parseInt(s);
  }

  // TODO:
  public RandomEdge getCopy() {
    RandomEdge result = copyFieldsTo(new RandomEdge());
    result.mProbability = this.mProbability;
    return result;
  }

  public int getHashCode() {
    return super.hashCode() + 73;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getProbability() == ((RandomEdge)o).getProbability()
        && super.equals(o);
  }

}
