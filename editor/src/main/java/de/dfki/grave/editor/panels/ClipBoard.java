package de.dfki.grave.editor.panels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.CommentBadge;

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
  private List<CommentBadge> mComments = new ArrayList<>();

  private boolean needsCopy = true;
  private ProjectEditor origin;

  public void clear() {
    mNodes.clear(); mEdges.clear(); mComments.clear();
  }

  private void set(ProjectEditor editor, Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges, Collection<CommentBadge> comments, 
      boolean copy) {
    clear();
    origin = editor;
    needsCopy = copy;
    mNodes.addAll(nodes);
    mEdges.addAll(edges);
    mComments.addAll(comments);
  }

  public void setToCopy(ProjectEditor editor, Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges, Collection<CommentBadge> comments) {
    set(editor, nodes, edges, comments, true);
  }

  public void set(ProjectEditor editor, Collection<BasicNode> nodes,
      Collection<AbstractEdge> edges, Collection<CommentBadge> comments) {
    set(editor, nodes, edges, comments, false);
  }

  public List<BasicNode> getNodes() {
    return Collections.unmodifiableList(mNodes);
  }

  public List<AbstractEdge> getEdges() {
    return Collections.unmodifiableList(mEdges);
  }

  public List<CommentBadge> getComments() {
    return Collections.unmodifiableList(mComments);
  }

  public boolean isEmpty() {
    return mNodes.isEmpty() && mEdges.isEmpty() && mComments.isEmpty();
  }
  
  public boolean needsCopy(ProjectEditor editor) {
    return needsCopy || origin != editor;
  }

  public void forceCopy() {
    needsCopy = true;
  }

}
