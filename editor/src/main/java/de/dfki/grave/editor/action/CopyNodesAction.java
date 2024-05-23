package de.dfki.grave.editor.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import de.dfki.grave.app.AppFrame;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Bernd Kiefer
 *
 * doesn't make a lot of sense to me to undo copy, so this is just an 
 * ActionListener
 */
public class CopyNodesAction implements ActionListener {

  private final ProjectEditor mEditor;
  private final Collection<BasicNode> mNodes;
  
  public CopyNodesAction(ProjectEditor editor, Collection<BasicNode> selected) {
    mEditor = editor;
    mNodes = selected;
  }
  
  public CopyNodesAction(ProjectEditor editor, BasicNode n) {
    mEditor = editor;
    mNodes = new ArrayList<BasicNode>(); mNodes.add(n);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (! mNodes.isEmpty()) {
      mEditor.copyNodes(mNodes);
    }
    AppFrame.getInstance().refreshMenuBar(); // reflect change of clipboard
  }
}
