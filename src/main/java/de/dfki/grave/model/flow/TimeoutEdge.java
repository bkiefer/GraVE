package de.dfki.grave.model.flow;

import java.util.Map;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.Expression;
import de.dfki.grave.model.flow.TimeoutEdge;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="TEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class TimeoutEdge extends AbstractEdge {

  private static final long DEFAULT_TIMEOUT = 1000; // 1 sec

  @XmlAttribute(name="timeout")
  protected long mTimeout = DEFAULT_TIMEOUT;
  @XmlElement(name="Commands", nillable=true)
  @XmlJavaTypeAdapter(ExpressionAdapter.class)
  protected Expression mExpression = new Expression();

  public long getTimeout() {
    return mTimeout;
  }

  public void setTimeout(long value) throws NumberFormatException {
    if (value >= 0) {
      mTimeout = value;
    } else {
      throw new NumberFormatException("Invalid Timeout Value");
    }
    // mTimeout = value;
  }

  public void setExpression(String value) {
    mExpression = new Expression(value);
  }

  public String getContent() {
    return "" + mTimeout;
  }

  /** EDGE MODIFICATION (?) */
  public void setContent(String s) {
    s = s.trim();
    if (s.isEmpty()) return;
    try {
      long newTimeout = Long.parseLong(s);
      mTimeout = newTimeout;
    } catch (NumberFormatException ex) {
      // keep old value
    }
  }

  public TimeoutEdge deepCopy(Map<BasicNode, BasicNode> orig2copy) {
    TimeoutEdge result = deepCopy(new TimeoutEdge(), orig2copy);
    result.mTimeout = this.mTimeout;
    result.mExpression = this.mExpression;
    return result;
  }

  public int getHashCode() {
    return super.hashCode() + 67;
  }

  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 59 * hash + mExpression.hashCode();
    hash = 59 * hash + Long.hashCode(mTimeout);
    return hash;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getTimeout() == ((TimeoutEdge)o).getTimeout()
        && mExpression.content.equals(((TimeoutEdge)o).mExpression.content)
        && super.equals(o);
  }

}
