package de.dfki.vsm.model.flow.edge;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.graphics.edge.EdgeArrow;
import de.dfki.vsm.util.Pair;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="EEdge")
public class EpsilonEdge extends AbstractEdge {

  public EpsilonEdge() {
  }

  public EpsilonEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
  }

  public EdgeType getEdgeType() {
    return EdgeType.EpsilonEdge;
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
  public EpsilonEdge getCopy() {
    return new EpsilonEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mArrow.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap());
  }

}
