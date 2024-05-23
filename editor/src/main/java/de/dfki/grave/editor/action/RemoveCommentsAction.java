package de.dfki.grave.editor.action;

import java.util.ArrayList;
import java.util.Collection;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.CommentBadge;

/**
 * @author Patrick Gebhard
 */
public class RemoveCommentsAction extends EditorAction {
  private Collection<CommentBadge> mComments;

  public RemoveCommentsAction(ProjectEditor editor,
      Collection<CommentBadge> c, boolean isCutOperation) {
    super(editor);
    mComments = c;
  }
  
  public RemoveCommentsAction(ProjectEditor editor, CommentBadge c) {
    super(editor);
    mComments = new ArrayList<>(1);
    mComments.add(c);
  }

  protected void doIt() {
    for (CommentBadge c : mComments)
      mSuperNode.removeComment(c);
    
    if (onActiveWorkSpace())
      for (CommentBadge c : mComments)
        mEditor.getWorkSpace().removeComment(c);
  }

  protected void undoIt() {
    for (CommentBadge c : mComments)
      mSuperNode.addComment(c);
    
    if (onActiveWorkSpace())
      for (CommentBadge c : mComments)
        mEditor.getWorkSpace().addComment(c);
  }

  public String msg() { return "Deletion of Comment"; }
}
