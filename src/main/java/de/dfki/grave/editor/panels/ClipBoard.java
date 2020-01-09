package de.dfki.grave.editor.panels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

/**
 * Created by alvaro on 7/18/16.
 * The clipboard should be shared among the different projects
 *
 * We need to know from which WorkSpace the nodes are, because if they
 * are pasted onto another WorkSpace, they *must* be copied.
 */
public class ClipBoard {

  static ClipBoard instance = new ClipBoard();

  private ClipBoard() {}

  public static ClipBoard getInstance() {
    return instance;
  }

  private List<BasicNode> mNodes = new ArrayList<>();
  private List<AbstractEdge> mEdges = new ArrayList<>();

  private boolean needsCopy = true;
  private WorkSpace origin;

  public void clear() {
    mNodes.clear(); mEdges.clear();
  }

  private void set(WorkSpace workSpace, Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges, boolean copy) {
    clear();
    origin = workSpace;
    needsCopy = copy;
    mNodes.addAll(nodes);
    mEdges.addAll(edges);
  }

  public void setToCopy(WorkSpace workSpace, Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges) {
    set(workSpace, nodes, edges, true);
  }

  public void set(WorkSpace workSpace, Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges) {
    set(workSpace, nodes, edges, false);
  }

  public List<BasicNode> getNodes() {
    return Collections.unmodifiableList(mNodes);
  }

  public List<AbstractEdge> getEdges() {
    return Collections.unmodifiableList(mEdges);
  }

  public boolean isEmpty() {
    return mNodes.isEmpty() && mEdges.isEmpty();
  }

  public boolean needsCopy(WorkSpace workSpace) {
    return needsCopy || origin != workSpace;
  }

  public void forceCopy() {
    needsCopy = true;
  }

}
