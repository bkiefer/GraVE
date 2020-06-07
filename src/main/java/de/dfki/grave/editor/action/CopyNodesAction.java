package de.dfki.grave.editor.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import de.dfki.grave.editor.panels.WorkSpacePanel;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Bernd Kiefer
 *
 * doesn't make a lot of sense to me to undo copy, so this is just an 
 * ActionListener
 */
public class CopyNodesAction implements ActionListener {

  private final WorkSpacePanel mWorkSpace;
  private final BasicNode mNode;
  
  public CopyNodesAction(WorkSpacePanel workSpace) {
    mWorkSpace = workSpace;
    mNode = null;
  }
  
  public CopyNodesAction(WorkSpacePanel workSpace, BasicNode n) {
    mWorkSpace = workSpace;
    mNode = n;
  }

  @SuppressWarnings("serial")
  @Override
  public void actionPerformed(ActionEvent e) {
    if (mNode != null) {
      List<BasicNode> l = new ArrayList<BasicNode>() {{ add(mNode); }};
      mWorkSpace.copyNodes(l);
    } else {
      mWorkSpace.copySelected();    
    }
  }
}
