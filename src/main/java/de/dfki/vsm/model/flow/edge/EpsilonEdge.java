package de.dfki.vsm.model.flow.edge;

import java.util.ArrayList;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.Code;
import de.dfki.vsm.model.flow.graphics.edge.EdgeGraphics;
import de.dfki.vsm.util.Pair;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="EEdge")
public class EpsilonEdge extends AbstractEdge {

  public EpsilonEdge() {
  }

  public EpsilonEdge(String target, String source, BasicNode targetNode, BasicNode sourceNode, EdgeGraphics graphics,
          ArrayList<Code> cmdList, HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> altStartNodeMap) {
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
    return new EpsilonEdge(mTargetUnid, mSourceUnid, mTargetNode, mSourceNode, mGraphics.getCopy(), getCopyOfCmdList(),
            getCopyOfAltStartNodeMap());
  }

  public void writeXML(IOSIndentWriter out) throws XMLWriteError {
    String start = "";
    Iterator it = mAltMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      Pair<String, BasicNode> startNodeData = (Pair<String, BasicNode>) pairs.getKey();
      Pair<String, BasicNode> altStartNodeData = (Pair<String, BasicNode>) pairs.getValue();

      start += startNodeData.getFirst() + "/" + altStartNodeData.getFirst() + ";";
    }

    out.println("<EEdge target=\"" + mTargetUnid + "\" start =\"" + start + "\">");

    if (mGraphics != null) {
      mGraphics.writeXML(out);
    }

    if (!mCmdList.isEmpty()) {
      out.println("<Commands>").push();

      for (int i = 0; i < mCmdList.size(); i++) {
        mCmdList.get(i).writeXML(out);
      }

      out.pop().println("</Commands>");
    }

    out.println("</EEdge>");
  }

  public void parseXML(Element element) throws XMLParseError {
    mTargetUnid = element.getAttribute("target");

    String[] altStartNodes = element.getAttribute("start").split(";");

    for (String idPair : altStartNodes) {
      if (!idPair.isEmpty()) {
        String[] ids = idPair.split("/");
        String startId = ids[0];
        String altStartId = ids[1];
        Pair<String, BasicNode> startPair = new Pair<String, BasicNode>(startId, null);
        Pair<String, BasicNode> altStartPair = new Pair<String, BasicNode>(altStartId, null);

        mAltMap.put(startPair, altStartPair);
      }
    }

    XMLParseAction.processChildNodes(element, new XMLParseAction() {
      public void run(Element element) throws XMLParseError {
        String tag = element.getTagName();

        if (tag.equals("Graphics")) {
          mGraphics = new EdgeGraphics();
          mGraphics.parseXML(element);
        } else if (tag.equals("Commands")) {
          XMLParseAction.processChildNodes(element, new XMLParseAction() {
            public void run(Element element) throws XMLParseError {
              mCmdList.add(Code.parse(element));
            }
          });
        } else {
          throw new XMLParseError(null,
                  "Cannot parse an element with tag \"" + tag
                  + "\" into a child of an EEdge!");
        }
      }
    });
  }
}
