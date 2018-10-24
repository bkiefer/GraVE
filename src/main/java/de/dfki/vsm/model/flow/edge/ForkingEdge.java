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
@XmlType(name="FEdge")
public class ForkingEdge extends AbstractEdge {

  public ForkingEdge() {
  }

  public ForkingEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeArrow graphics,
          String cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
  }

  public EdgeType getEdgeType() {
    return EdgeType.ForkingEdge;
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
  public ForkingEdge getCopy() {
    return new ForkingEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mArrow.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap());
  }

}
