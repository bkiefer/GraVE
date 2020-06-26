package de.dfki.grave.model.flow;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="PEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class RandomEdge extends AbstractEdge {

  protected int mProbability = 50; // TODO: in percent, or should we use float?

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

  public RandomEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    RandomEdge result = deepCopy(new RandomEdge(), orig2copy);
    result.mProbability = this.mProbability;
    return result;
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 31 + Integer.hashCode(mProbability);
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getProbability() == ((RandomEdge)o).getProbability()
        && super.equals(o);
  }

}
