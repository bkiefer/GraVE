package de.dfki.vsm.model.flow.edge;

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
@XmlType(name="CEdge")
public class GuardedEdge extends AbstractEdge {

  @XmlElement(name="Condition")
  protected String mCondition = null;

  public GuardedEdge() {
  }

  public GuardedEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap,
          String condition) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
    mCondition = condition;
  }

  @XmlTransient
  public String getCondition() {
    return mCondition;
  }

  public void setCondition(String mOldCondition) {
    mCondition = mOldCondition;
  }

  @Override
  public EdgeType getEdgeType() {
    return EdgeType.GuardedEdge;
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
  public GuardedEdge getCopy() {
    return new GuardedEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mArrow.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap(), new String(mCondition));
  }

}
