package de.dfki.vsm.editor.util;

//~--- JDK imports ------------------------------------------------------------
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SceneFlow;
import de.dfki.vsm.model.flow.SuperNode;

/**
 * IDManager provides unique ids for nodes and supernodes
 */
public class IDManager {
  // We could try to be more clever, but why?
  //private HashMap<String, BasicNode> mNodeIds, mSuperNodeIds;

  private int mNextSuperNodeID = 0;
  private int mNextNodeID = 0;

  /** Call this with the root node of the project only! */
  public IDManager(SceneFlow rootNode) {
    if (rootNode == null) return;
    getIDs(rootNode);
    ++mNextNodeID;
    ++mNextSuperNodeID;
  }

  private int getInt(BasicNode n) {
    return Integer.parseInt(n.getId().substring(1));
  }

  private void getIDs(SuperNode sn) {
    for (BasicNode n : sn.getNodeList()) {
      mNextNodeID = Math.max(mNextNodeID, getInt(n));
    }

    for (SuperNode sub : sn.getSuperNodeList()) {
      mNextSuperNodeID = Math.max(mNextSuperNodeID, getInt(sub));
      getIDs(sub);
    }
  }

  public String getNextFreeID(BasicNode b) {
    return "N" + mNextNodeID++;
  }

  public String getNextFreeID(SuperNode b) {
    return "S" + mNextSuperNodeID++;
  }
}
