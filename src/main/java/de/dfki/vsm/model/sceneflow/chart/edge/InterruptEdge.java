package de.dfki.vsm.model.sceneflow.chart.edge;

import java.util.ArrayList;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.*;

import org.w3c.dom.Element;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.model.sceneflow.chart.BasicNode;
import de.dfki.vsm.model.sceneflow.chart.graphics.edge.EdgeGraphics;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.tpl.TPLTuple;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="IEdge")
public class InterruptEdge extends AbstractEdge {

  @XmlElement
  protected Command mCondition = null;

  public InterruptEdge() {
  }

  public InterruptEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeGraphics graphics,
          ArrayList<Command> cmdList, HashMap<TPLTuple<String, BasicNode>, TPLTuple<String, BasicNode>> altStartNodeMap,
          Command condition) {
    super(target, source, targetNode, sourceNode, graphics, cmdList, altStartNodeMap);
    mCondition = condition;
  }

  public void setCondition(Command value) {
    mCondition = value;
  }

  public Command getCondition() {
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
    return new InterruptEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mGraphics.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap(), mCondition.getCopy());
  }

  @Override
  public void writeXML(IOSIndentWriter out) throws XMLWriteError {
    String start = "";
    Iterator it = mAltMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      TPLTuple<String, BasicNode> startNodeData = (TPLTuple<String, BasicNode>) pairs.getKey();
      TPLTuple<String, BasicNode> altStartNodeData = (TPLTuple<String, BasicNode>) pairs.getValue();

      start += startNodeData.getFirst() + "/" + altStartNodeData.getFirst() + ";";
    }

    out.println("<IEdge target=\"" + mTargetUnid + "\" start=\"" + start + "\">").push();

    if (mGraphics != null) {
      mGraphics.writeXML(out);
    }

    if (mCondition != null) {
      mCondition.writeXML(out);
    }

    if (!mCmdList.isEmpty()) {
      out.println("<Commands>").push();

      for (int i = 0; i < mCmdList.size(); i++) {
        mCmdList.get(i).writeXML(out);
      }

      out.pop().println("</Commands>");
    }

    out.pop().println("</IEdge>");
  }

  public void parseXML(Element element) throws XMLParseError {
    mTargetUnid = element.getAttribute("target");

    String[] altStartNodes = element.getAttribute("start").split(";");

    for (String idPair : altStartNodes) {
      if (!idPair.isEmpty()) {
        String[] ids = idPair.split("/");
        String startId = ids[0];
        String altStartId = ids[1];
        TPLTuple<String, BasicNode> startPair = new TPLTuple<String, BasicNode>(startId, null);
        TPLTuple<String, BasicNode> altStartPair = new TPLTuple<String, BasicNode>(altStartId, null);

        mAltMap.put(startPair, altStartPair);
      }
    }

    XMLParseAction.processChildNodes(element, new XMLParseAction() {
      @Override
      public void run(Element element) throws XMLParseError {
        String tag = element.getTagName();

        if (tag.equals("Graphics")) {
          mGraphics = new EdgeGraphics();
          mGraphics.parseXML(element);
        } else if (tag.equals("Commands")) {
          XMLParseAction.processChildNodes(element, new XMLParseAction() {
            @Override
            public void run(Element element) throws XMLParseError {
              mCmdList.add(Command.parse(element));
            }
          });
        } else {
          mCondition = Command.parse(element);
        }
      }
    });
  }
}
