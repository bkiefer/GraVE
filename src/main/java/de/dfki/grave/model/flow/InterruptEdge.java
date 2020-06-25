package de.dfki.grave.model.flow;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.Expression;
import de.dfki.grave.model.flow.InterruptEdge;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="IEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class InterruptEdge extends AbstractEdge {

  @XmlElement(name="Condition")
  @XmlJavaTypeAdapter(ExpressionAdapter.class)
  protected Expression mCondition = new Expression("false");

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

  @Override
  public InterruptEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    InterruptEdge result = deepCopy(new InterruptEdge(), orig2copy);
    result.mCondition = this.mCondition.deepCopy();
    return result;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode() + 71;
    hash = 59 * hash + mCondition.hashCode();
    return hash;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getCondition().equals(((InterruptEdge)o).getCondition())
        && super.equals(o);
  }

}
