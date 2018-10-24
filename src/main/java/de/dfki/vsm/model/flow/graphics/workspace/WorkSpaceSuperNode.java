package de.dfki.vsm.model.flow.graphics.workspace;

import java.awt.Dimension;

import de.dfki.vsm.editor.project.sceneflow.workspace.WorkSpacePanel;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SuperNode;

/**
 * Created by alvaro on 1/24/17.
 * Compute workspace area inside super node (When changing level)
 */
public class WorkSpaceSuperNode extends WorkAreaSize {

  private SuperNode superNode = null;

  public WorkSpaceSuperNode(WorkSpacePanel workSpacePanel, int nodeWidth, int nodeHeight, SuperNode superNode) {
    super(workSpacePanel, nodeWidth, nodeHeight);
    this.superNode = superNode;
  }

  public Dimension calculate() {
    calculateDimensionForSuperNode();
    super.calculate();
    return new Dimension(width, height);
  }

  private void calculateDimensionForSuperNode() {
    for (BasicNode childNode : superNode.getNodeList()) {
      updateWidth(childNode);
      updateHeight(childNode);
    }

    for (BasicNode childNode : superNode.getSuperNodeList()) {
      updateWidth(childNode);
      updateHeight(childNode);
    }
  }

  private void updateHeight(BasicNode childNode) {
    if (childNode.getPosition().getYPos() > height) {
      height = childNode.getPosition().getYPos() + nodeHeight;
    }
  }

  private void updateWidth(BasicNode childNode) {
    if (childNode.getPosition().getXPos() > width) {
      width = childNode.getPosition().getXPos() + nodeWidth;
    }
  }

}
