package de.dfki.vsm.editor.util;

import java.util.ArrayList;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.dfki.vsm.model.flow.*;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class SceneFlowManager {

  private final SceneFlow mSceneFlow;
  private final IDManager mIDManager;
  private final LinkedList<SuperNode> mActiveSuperNodes;

  public SceneFlowManager(SceneFlow sceneFlow) {
    mSceneFlow = sceneFlow;
    mIDManager = new IDManager(mSceneFlow);
    mActiveSuperNodes = new LinkedList<SuperNode>();
    mActiveSuperNodes.addLast(mSceneFlow);
  }

  public SceneFlow getSceneFlow() {
    return mSceneFlow;
  }

  public IDManager getIDManager() {
    return mIDManager;
  }

  public SuperNode getCurrentActiveSuperNode() {
    return mActiveSuperNodes.getLast();
  }

  public LinkedList<SuperNode> getActiveSuperNodes() {
    return mActiveSuperNodes;
  }

  public void addActiveSuperNode(SuperNode value) {
    mActiveSuperNodes.addLast(value);
  }

  public SuperNode removeActiveSuperNode() {
    return mActiveSuperNodes.removeLast();
  }

  public boolean isRootSuperNode(BasicNode n) {
    return (n.equals((SuperNode) mSceneFlow));
  }

  /*
     * Returns a set of BasicNode IDs that are alternative Startnodes of a SuperNode
   */
  public Set<String> getAlternativeStartNode(SuperNode superNode) {
    Set<String> altStartNodeIDs = new HashSet<String>();

    if (!(superNode instanceof SceneFlow)) {
      SuperNode parentSuperNode = getParentSuperNode(superNode);
      ArrayList<BasicNode> parentNodeSet = parentSuperNode.getNodeList();
      Set<String> currentNodeIDs = new HashSet<String>();

      for (BasicNode cn : superNode.getNodeList()) {
        currentNodeIDs.add(cn.getId());
      }

      for (BasicNode node : parentNodeSet) {
        if (node.hasEdge()) {
          switch (node.getFlavour()) {
            case CNODE:
              ArrayList<GuardedEdge> ces = node.getCEdgeList();

              for (GuardedEdge c : ces) {

                // collectAltStartNodeIDs(processIDs(c.getStart()), currentNodeIDs, altStartNodeIDs);
              }

              break;

            case PNODE:
              ArrayList<RandomEdge> pes = node.getPEdgeList();

              for (RandomEdge p : pes) {

                // collectAltStartNodeIDs(processIDs(p.getStart()), currentNodeIDs, altStartNodeIDs);
              }

              break;

            case INODE:
              ArrayList<InterruptEdge> ies = node.getIEdgeList();

              for (InterruptEdge i : ies) {

                // collectAltStartNodeIDs(processIDs(i.getStart()), currentNodeIDs, altStartNodeIDs);
              }

              break;

            case NONE:
              if (node.hasDEdge()) {

                // collectAltStartNodeIDs(processIDs(node.getDedge().getStart()), currentNodeIDs, altStartNodeIDs);
              }

              break;
          }
        }
      }
    }

    return altStartNodeIDs;
  }

  /*
     * Returns the parent SuperNode to a given BasicNode n
   */
  public SuperNode getParentSuperNode(BasicNode n) {
    if (!isRootSuperNode(n)) {
      SuperNode parentSuperNode = (SuperNode) mSceneFlow;

      // checking if node is contained in the nodes of the root SuperNode
      for (BasicNode cn : parentSuperNode) {
        if (cn.equals(n)) {
          return parentSuperNode;
        } else {
          if (SuperNode.class.isInstance(cn)) {
            SuperNode sun = findParentSuperNode((SuperNode) cn, n);

            if (sun != null) {
              return sun;
            }
          }
        }
      }
    }

    // return null if no parent (super) node exists
    return null;
  }

  /*
     * Helper method for recursive traversion of supernodes to find Parent SuperNode to given BasicNode
   */
  private SuperNode findParentSuperNode(SuperNode currentSN, BasicNode n) {
    if (hasSuperNodes(currentSN)) {
      SuperNode parentSuperNode = currentSN;

      for (BasicNode cn : currentSN) {
        if (cn.equals(n)) {
          return parentSuperNode;
        } else {
          if (SuperNode.class.isInstance(cn)) {
            SuperNode sun = findParentSuperNode((SuperNode) cn, n);

            if (sun != null) {
              return sun;
            }
          }
        }
      }
    }

    return null;
  }

  /*
     * Checks if a given SuperNode contains an instance of SuperNode an returns a appropriate
     * boolean value
     *
     * @param SuperNode
   */
  private boolean hasSuperNodes(SuperNode sn) {

    // return (sn.getSuperNodeSet().size() > 0) ? true : false;
    return (sn.getSuperNodeList().size() > 0);
  }

}
