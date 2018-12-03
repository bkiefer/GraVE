package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.dfki.vsm.editor.project.WorkSpace;

/**
 *
 * @author Patrick Gebhard
 */
public class StraightenEdgeAction {

  private de.dfki.vsm.editor.Edge mGUIEdge = null;
  private WorkSpace mWorkSpace;

  public StraightenEdgeAction(WorkSpace workSpace, de.dfki.vsm.editor.Edge edge) {
    mWorkSpace = workSpace;
    mGUIEdge = edge;
  }

  public ActionListener getActionListener() {
    return new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        mGUIEdge.straightenEdge();

        // renew graphical representation on work space
        mWorkSpace.revalidate();
        mWorkSpace.repaint(100);
      }
    };
  }
}
