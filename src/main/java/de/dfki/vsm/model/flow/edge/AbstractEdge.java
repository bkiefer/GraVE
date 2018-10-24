package de.dfki.vsm.model.flow.edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.*;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.graphics.edge.EdgeGraphics;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.Pair;

/**
 * @author Gregor Mehlmann
 */
public abstract class AbstractEdge implements ModelObject {

  @XmlAttribute(name="target")
  protected String mTargetUnid = new String();
  @XmlAttribute(name="start") // TODO: nonsense
  protected String mSourceUnid = new String();
  protected BasicNode mTargetNode = null;
  protected BasicNode mSourceNode = null;
  @XmlElement(name="Graphics")
  protected EdgeGraphics mGraphics = null;
  @XmlElementWrapper(name="Commands")
  @XmlElement(name="Command")
  protected ArrayList<Command> mCmdList = new ArrayList<>();
  protected HashMap<
            Pair<String, BasicNode>, Pair<String, BasicNode>> mAltMap = new HashMap();

  // The edge type
  public enum EdgeType {

    GuardedEdge,
    EpsilonEdge,
    InterruptEdge,
    RandomEdge,
    TimeoutEdge,
    ForkingEdge
  }

  public AbstractEdge() {
  }

  public AbstractEdge(
          final String targetUnid,
          final String sourceUnid,
          final BasicNode targetNode,
          final BasicNode sourceNode,
          final EdgeGraphics graphics,
          final ArrayList cmdList,
          final HashMap altMap) {
    mTargetUnid = targetUnid;
    mSourceUnid = sourceUnid;
    mTargetNode = targetNode;
    mSourceNode = sourceNode;
    mGraphics = graphics;
    mCmdList = cmdList;
    mAltMap = altMap;
  }

  public final String getTargetUnid() {
    return mTargetUnid;
  }

  public final void setTargetUnid(final String value) {
    mTargetUnid = value;
  }

  public final String getSourceUnid() {
    return mSourceUnid;
  }

  public final void setSourceUnid(final String value) {
    mSourceUnid = value;
  }

  public final BasicNode getTargetNode() {
    return mTargetNode;
  }

  public final void setTargetNode(final BasicNode value) {
    mTargetNode = value;
  }

  public final BasicNode getSourceNode() {
    return mSourceNode;
  }

  public final void setSourceNode(final BasicNode value) {
    mSourceNode = value;
  }

  public final EdgeGraphics getGraphics() {
    return mGraphics;
  }

  public final void setGraphics(final EdgeGraphics value) {
    mGraphics = value;
  }

  public final ArrayList<Command> getCmdList() {
    return mCmdList;
  }

  public final void setCmdList(final ArrayList<Command> value) {
    mCmdList = value;
  }

  public final ArrayList<Command> getCopyOfCmdList() {
    final ArrayList<Command> copy = new ArrayList();
    for (Command cmd : mCmdList) {
      copy.add(cmd.getCopy());
    }
    return copy;
  }

  /*
    public final ArrayList<BasicNode> getAltList() {
        final ArrayList<BasicNode> altList = new ArrayList();
        for (TPLTuple<String, BasicNode> pair : mAltMap.values()) {
            altList.add(pair.getSecond());
        }
        return altList;
    }
   */
  public final HashMap<
        Pair<String, BasicNode>, Pair<String, BasicNode>> getAltMap() {
    return mAltMap;
  }

  public final void setAltMap(final HashMap value) {
    mAltMap = value;
  }

  // TODO: This is not yet a deep copy
  public HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> getCopyOfAltStartNodeMap() {
    HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> copy = new HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>>();
    Iterator it = mAltMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      Pair<String, BasicNode> startNodePair = (Pair<String, BasicNode>) pairs.getKey();
      Pair<String, BasicNode> altStartNodePair = (Pair<String, BasicNode>) pairs.getValue();
      Pair<String, BasicNode> startNodePairCopy = new Pair<String, BasicNode>(startNodePair.getFirst(),
              startNodePair.getSecond());
      Pair<String, BasicNode> altStartNodePairCopy = new Pair<String, BasicNode>(altStartNodePair.getFirst(),
              altStartNodePair.getSecond());

      copy.put(startNodePairCopy, altStartNodePairCopy);
    }

    return copy;
  }

  // TODO: do this over the list of strings
  public String getAltStartNodesAsString() {
    String result = "";
    Iterator it = mAltMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry pairs = (Map.Entry) it.next();
      Pair<String, BasicNode> start = (Pair<String, BasicNode>) pairs.getKey();
      Pair<String, BasicNode> alt = (Pair<String, BasicNode>) pairs.getValue();

      result += start.getFirst() + "/" + alt.getFirst() + ";";
    }

    return result;
  }

  public abstract EdgeType getEdgeType();

  @Override
  public abstract AbstractEdge getCopy();
}
