package de.dfki.vsm.model.flow.edge;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.graphics.edge.EdgeArrow;
import de.dfki.vsm.util.Pair;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="PEdge")
public class RandomEdge extends AbstractEdge {

  @XmlAttribute(name="probability")
  protected int mProbability = Integer.MIN_VALUE;

  public RandomEdge() {
  }

  public RandomEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap,
          int probability) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
    mProbability = probability;
  }

  @XmlTransient
  public int getProbability() {
    return mProbability;
  }

  public void setProbability(int value) {
    mProbability = value;
  }

  public EdgeType getEdgeType() {
    return EdgeType.RandomEdge;
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
  public RandomEdge getCopy() {
    return new RandomEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mArrow.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap(), mProbability);
  }
}
