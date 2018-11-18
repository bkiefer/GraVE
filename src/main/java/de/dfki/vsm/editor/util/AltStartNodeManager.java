package de.dfki.vsm.editor.util;

import java.util.ArrayList;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.util.Pair;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
public class AltStartNodeManager {

  public AbstractEdge mEdge;
  public HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> mAltStartNodeMap;

  public AltStartNodeManager(AbstractEdge edge) {
    mEdge = edge;
    mAltStartNodeMap = mEdge.getCopyOfAltStartNodeMap();
  }

  public void saveAltStartNodeMap() {
    mEdge.setAltMap(mAltStartNodeMap);
  }

  public void loadAltStartNodeMap() {
    mAltStartNodeMap = mEdge.getCopyOfAltStartNodeMap();
  }

  public void removeAltStartNode(String id) {
    Pair<String, BasicNode> pair = null;

    for (Pair<String, BasicNode> p : mAltStartNodeMap.keySet()) {
      if (p.getFirst().equals(id)) {
        pair = p;

        break;
      }
    }

    if (pair != null) {
      mAltStartNodeMap.remove(pair);
    }
  }

  public ArrayList<BasicNode> getSubstitutableStartNodes() {
    ArrayList<BasicNode> substitutableStartNodeList = new ArrayList<BasicNode>();

    for (BasicNode node : ((SuperNode) mEdge.getTargetNode()).getStartNodeMap().values()) {
      substitutableStartNodeList.add(node);
    }

    //
    for (Pair<String, BasicNode> startNodePair : mAltStartNodeMap.keySet()) {
      if (!startNodePair.getFirst().equals("none")) {
        substitutableStartNodeList.remove(startNodePair.getSecond());
      }
    }

    return substitutableStartNodeList;
  }

  public ArrayList<BasicNode> getValidAltStartNodesFor(String id) {
    ArrayList<BasicNode> validAltStartNodeList = new ArrayList<BasicNode>();

    // /
    SuperNode targetNode = (SuperNode) mEdge.getTargetNode();
    BasicNode selectedNode = targetNode.getChildNodeById(id);

    System.err.println("Selected node=" + selectedNode);

    // /
    if (selectedNode == null) {

      // rwchability map
      HashMap<BasicNode, ArrayList<BasicNode>> reachableNodeMap = new HashMap<BasicNode, ArrayList<BasicNode>>();

      for (BasicNode node : ((SuperNode) mEdge.getTargetNode()).getStartNodeMap().values()) {
        reachableNodeMap.put(node, node.getReachableNodeList());
      }

      for (Pair<String, BasicNode> p : mAltStartNodeMap.values()) {
        reachableNodeMap.put(p.getSecond(), p.getSecond().getReachableNodeList());
      }

      // ArrayList<Node>
      ArrayList<BasicNode> finals = new ArrayList<BasicNode>();

      // /
      for (BasicNode node : ((SuperNode) mEdge.getTargetNode())) {
        System.err.println("looking if " + node.getId() + " is valid");

        boolean valid = true;
        Iterator it = reachableNodeMap.entrySet().iterator();

        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry) it.next();
          BasicNode n = (BasicNode) pairs.getKey();
          ArrayList<BasicNode> v = (ArrayList<BasicNode>) pairs.getValue();

          if (v.contains(node)) {
            valid = false;
          }
        }

        if (valid) {
          finals.add(node);
        }
      }

      /////
      for (BasicNode n : finals) {
        validAltStartNodeList.add(n);
      }

      /////
      for (BasicNode n : finals) {
        ArrayList<BasicNode> reverse = n.getReachableNodeList();

        ////
        Iterator it = mAltStartNodeMap.entrySet().iterator();

        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry) it.next();
          Pair<String, BasicNode> p1 = (Pair<String, BasicNode>) pairs.getKey();
          Pair<String, BasicNode> p2 = (Pair<String, BasicNode>) pairs.getValue();

          if (reverse.contains(p2.getSecond())) {

            // / remove p2 from finals
            validAltStartNodeList.remove(n);
          }
        }

        ////
      }

      // return finals;
    } else {
      ArrayList<BasicNode> reachableNodeList = selectedNode.getReachableNodeList();

      for (BasicNode node : reachableNodeList) {
        if (!node.getId().equals(id)) {
          validAltStartNodeList.add(node);
        }
      }
    }

    ////
    return validAltStartNodeList;
  }

  public void createAltStartNode(String s, String a) {
    String x = (s.equals("none")
            ? ""
            : s);
    BasicNode n1 = ((SuperNode) mEdge.getTargetNode()).getChildNodeById(x);
    BasicNode n2 = ((SuperNode) mEdge.getTargetNode()).getChildNodeById(a);
    Pair<String, BasicNode> pair1 = new Pair<String, BasicNode>(x, n1);
    Pair<String, BasicNode> pair2 = new Pair<String, BasicNode>(a, n2);

    mAltStartNodeMap.put(pair1, pair2);
  }
}
