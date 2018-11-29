package de.dfki.vsm.editor.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;

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

  private List<Node> mNodes = new ArrayList<>();
  private List<Edge> mEdges = new ArrayList<>();

  private boolean needsCopy = true;
  private WorkSpacePanel origin;

  public void clear() {
    mNodes.clear(); mEdges.clear();
  }

  private void set(WorkSpacePanel wsp, Collection<Node> nodes,
      Collection<Edge> edges, boolean copy) {
    clear();
    origin = wsp;
    needsCopy = copy;
    mNodes.addAll(nodes);
    mEdges.addAll(edges);
  }

  public void setToCopy(WorkSpacePanel wsp, Collection<Node> nodes,
      Collection<Edge> edges) {
    set(wsp, nodes, edges, true);
  }

  public void set(WorkSpacePanel wsp, Collection<Node> nodes,
      Collection<Edge> edges) {
    set(wsp, nodes, edges, false);
  }

  public List<Node> getNodes() {
    return Collections.unmodifiableList(mNodes);
  }

  public List<Edge> getEdges() {
    return Collections.unmodifiableList(mEdges);
  }

  public boolean isEmpty() {
    return mNodes.isEmpty() && mEdges.isEmpty();
  }

  public boolean needsCopy(WorkSpacePanel wsp) {
    return needsCopy || origin != wsp;
  }

  public void forceCopy() {
    needsCopy = true;
  }

}
