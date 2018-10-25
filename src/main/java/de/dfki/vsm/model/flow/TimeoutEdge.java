package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.*;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="TEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class TimeoutEdge extends AbstractEdge {

  @XmlAttribute(name="timeout")
  protected long mTimeout = Long.MIN_VALUE;
  @XmlElement(name="Commands", nillable=true)
  protected Expression mExpression = null;

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

  public String getExpression() {
    return mExpression.getContent();
  }

  public void setExpression(String value) {
    mExpression = new Expression(value);
  }

  // TODO:
  public TimeoutEdge getCopy() {
    TimeoutEdge result = copyFieldsTo(new TimeoutEdge());
    result.mTimeout = this.mTimeout;
    result.mExpression = this.mExpression;
    return result;
  }

  public int getHashCode() {
    return super.hashCode() + 67;
  }

  public boolean equals(Object o) {
    return o.getClass().equals(this.getClass())
        && getTimeout() == ((TimeoutEdge)o).getTimeout()
        && getExpression().equals(((TimeoutEdge)o).getExpression())
        && super.equals(o);
  }

}
