package de.dfki.vsm.model.sceneflow.chart.edge;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.sceneflow.chart.BasicNode;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.model.sceneflow.chart.graphics.edge.EdgeGraphics;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.tpl.TPLTuple;
import de.dfki.vsm.util.xml.XMLWriteError;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author Gregor Mehlmann
 */
public abstract class AbstractEdge implements ModelObject {

    protected String mTargetUnid = new String();
    protected String mSourceUnid = new String();
    protected BasicNode mTargetNode = null;
    protected BasicNode mSourceNode = null;
    protected EdgeGraphics mGraphics = null;
    protected ArrayList<Command> mCmdList = new ArrayList();
    protected HashMap<
            TPLTuple<String, BasicNode>, TPLTuple<String, BasicNode>> mAltMap = new HashMap();

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
        TPLTuple<String, BasicNode>, TPLTuple<String, BasicNode>> getAltMap() {
        return mAltMap;
    }

    public final void setAltMap(final HashMap value) {
        mAltMap = value;
    }

    // TODO: do this over the list of strings
    public String getAltStartNodesAsString() {
        String result = "";
        Iterator it = mAltMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            TPLTuple<String, BasicNode> start = (TPLTuple<String, BasicNode>) pairs.getKey();
            TPLTuple<String, BasicNode> alt = (TPLTuple<String, BasicNode>) pairs.getValue();

            result += start.getFirst() + "/" + alt.getFirst() + ";";
        }

        return result;
    }

    public abstract EdgeType getEdgeType();

    public void writeXML(IOSIndentWriter out) throws XMLWriteError {
      if (mGraphics != null) {
          mGraphics.writeXML(out);
      }

      if (!mCmdList.isEmpty()) {
        out.println("<Commands>").push();

        Command.writeListXML(out, mCmdList);

        out.pop().println("</Commands>");
      }
    }

}
