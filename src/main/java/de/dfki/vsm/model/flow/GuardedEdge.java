package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="CEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class GuardedEdge extends AbstractEdge {

  @XmlElement(name="Condition")
  protected Expression mCondition = null;

  public String getCondition() {
    return mCondition.getContent();
  }

  public void setCondition(String mOldCondition) {
    mCondition.setContent(mOldCondition);
  }

  // TODO:
  public GuardedEdge getCopy() {
    GuardedEdge result = copyFieldsTo(new GuardedEdge());
    result.mCondition = this.mCondition.getCopy();
    return result;
  }

  public int getHashCode() {
    return super.hashCode() + 73;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getCondition().equals(((GuardedEdge)o).getCondition())
        && super.equals(o);
  }

}
