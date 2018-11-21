package de.dfki.vsm.editor.util;

//~--- JDK imports ------------------------------------------------------------
import de.dfki.vsm.model.flow.*;

/**
 * IDManager provides unique ids for nodes and supernodes
 */
public class IDManager {
  private int mNextSuperNodeID = 0;
  private int mNextNodeID = 0;

  /** Call this with the root node of the project only! */
  public IDManager(SuperNode rootNode) {
    if (rootNode != null) {
      if (!(rootNode instanceof SceneFlow)) {
        throw new IllegalArgumentException("May only be called with root node!");
      }
      getIDs(rootNode);
      ++mNextNodeID;
      ++mNextSuperNodeID;
    }
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

  public String getNextFreeSuperNodeID() {
    return "S" + mNextSuperNodeID++;
  }

  public String getNextFreeNodeID() {
    return "N" + mNextNodeID++;
  }

}
