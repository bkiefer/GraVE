package de.dfki.grave.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="CEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class GuardedEdge extends AbstractEdge {

  @XmlElement(name="Condition")
  @XmlJavaTypeAdapter(ExpressionAdapter.class)
  protected Expression mCondition = new Expression("true");

  public String getCondition() {
    return mCondition.getContent();
  }

  /** EDGE MODIFICATION (?) */
  public void setCondition(String mOldCondition) {
    mCondition.setContent(mOldCondition);
  }

  public String getContent() {
    return getCondition();
  }

  public void setContent(String s) {
    setCondition(s);
  }

  public GuardedEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    GuardedEdge result = deepCopy(new GuardedEdge(), orig2copy);
    result.mCondition = this.mCondition.deepCopy();
    return result;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode() + 73;
    hash = 59 * hash + mCondition.hashCode();
    return hash;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getCondition().equals(((GuardedEdge)o).getCondition())
        && super.equals(o);
  }

}
