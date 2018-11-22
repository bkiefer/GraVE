package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.*;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="TEdge")
@XmlAccessorType(XmlAccessType.NONE)
public class TimeoutEdge extends AbstractEdge {

  private static final long DEFAULT_TIMEOUT = 1000; // 1 sec

  @XmlAttribute(name="timeout")
  protected long mTimeout = DEFAULT_TIMEOUT;
  @XmlElement(name="Commands", nillable=true)
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

  public String getExpression() {
    return mExpression.getContent();
  }

  public void setExpression(String value) {
    mExpression = new Expression(value);
  }

  public String getContent() {
    return "" + mTimeout;
  }

  public void setContent(String s) {
    mTimeout = Long.parseLong(s.trim());
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
