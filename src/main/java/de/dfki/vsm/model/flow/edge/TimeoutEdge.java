package de.dfki.vsm.model.flow.edge;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.graphics.edge.EdgeArrow;
import de.dfki.vsm.util.Pair;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="TEdge")
public class TimeoutEdge extends AbstractEdge {

  @XmlAttribute(name="timeout")
  protected long mTimeout = Long.MIN_VALUE;
  @XmlElement(name="Commands")
  protected String mExpression = null;

  public TimeoutEdge() {
  }

  // PG: Allow expression for mor flexibility. Consistency check through GUI
  public TimeoutEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap,
          String expression) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
    mExpression = expression;
  }

  public TimeoutEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap,
          long timeout) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
    mTimeout = timeout;
  }

  @XmlTransient
  public long getTimeout() {
    return mTimeout;
  }

  public void setTimeout(long value) throws NumberFormatException {
    if (value >= 0) {
      mTimeout = value;
    } else {
      throw new NumberFormatException("Invalid Time Out Egde Value");
    }
    // mTimeout = value;
  }

  public void setExpression(String value) {
    mExpression = value;
  }

  @XmlTransient
  public String getExpression() {
    return mExpression;
  }

  public EdgeType getEdgeType() {
    return EdgeType.TimeoutEdge;
  }

  public String getAbstractSyntax() {
    return null;
  }

  public String getConcreteSyntax() {
    return null;
  }

  public String getFormattedSyntax() {
    return null;
  }

  // TODO:
  public TimeoutEdge getCopy() {
    return new TimeoutEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mArrow.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap(), mTimeout);
  }
}
