package de.dfki.vsm.model.flow.edge;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.graphics.edge.EdgeArrow;
import de.dfki.vsm.util.Pair;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="IEdge")
public class InterruptEdge extends AbstractEdge {

  @XmlElement(name="Condition")
  protected String mCondition = null;

  public InterruptEdge() {
  }

  public InterruptEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap,
          String condition) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
    mCondition = condition;
  }

  @XmlTransient
  public void setCondition(String value) {
    mCondition = value;
  }

  public String getCondition() {
    return mCondition;
  }

  @Override
  public EdgeType getEdgeType() {
    return EdgeType.InterruptEdge;
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
  @Override
  public InterruptEdge getCopy() {
    return new InterruptEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mArrow.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap(), new String(mCondition));
  }
}
