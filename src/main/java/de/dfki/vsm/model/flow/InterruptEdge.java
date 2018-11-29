package de.dfki.vsm.model.flow;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="IEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class InterruptEdge extends AbstractEdge {

  protected Expression mCondition = new Expression("false");

  @XmlElement(name="Condition")
  public String getCondition() {
    return mCondition.getContent();
  }

  public void setCondition(String mOldCondition) {
    mCondition.setContent(mOldCondition);
  }

  public String getContent() {
    return getCondition();
  }

  public void setContent(String s) {
    setCondition(s);
  }

  @Override
  public InterruptEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    InterruptEdge result = deepCopy(new InterruptEdge(), orig2copy);
    result.mCondition = this.mCondition.deepCopy();
    return result;
  }

  public int getHashCode() {
    return super.hashCode() + 71;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getCondition().equals(((InterruptEdge)o).getCondition())
        && super.equals(o);
  }

}
