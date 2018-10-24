package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.dialog.ModifyCEdgeDialog;
import de.dfki.vsm.editor.event.CEdgeDialogModifiedEvent;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.model.flow.Code;
import de.dfki.vsm.model.flow.edge.GuardedEdge;
import de.dfki.vsm.model.flow.edge.AbstractEdge.EdgeType;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 *
 *
 * @author Sergio Soto
 *
 *
 */
class ConditionEditor extends JPanel implements EventListener {

  private GuardedEdge mDataCEdge;
  private ModifyCEdgeDialog mCEdgeDialog;

  public ConditionEditor() {
    initComponents();
    EventDispatcher.getInstance().register(this);
  }

  private void initComponents() {
    setBackground(Color.white);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof EdgeSelectedEvent) {
      if (event instanceof EdgeSelectedEvent) {
        if (((EdgeSelectedEvent) event).getEdge().getEdgeType().equals(EdgeType.GuardedEdge)) {
          mDataCEdge = (GuardedEdge) ((EdgeSelectedEvent) event).getEdge();
          createNewDialog();
          removeAll();
          mCEdgeDialog.getInputPanel().setMinimumSize(new Dimension(200, 40));
          mCEdgeDialog.getInputPanel().setMaximumSize(new Dimension(1000, 40));
          mCEdgeDialog.getInputPanel().setPreferredSize(new Dimension(200, 40));
          mCEdgeDialog.getInputPanel().setAlignmentX(RIGHT_ALIGNMENT);
          mCEdgeDialog.getAltStartNodePanel().setMinimumSize(new Dimension(200, 150));
          mCEdgeDialog.getAltStartNodePanel().setMaximumSize(new Dimension(1000, 150));
          mCEdgeDialog.getAltStartNodePanel().setPreferredSize(new Dimension(200, 150));
          mCEdgeDialog.getAltStartNodePanel().setAlignmentX(RIGHT_ALIGNMENT);
          add(mCEdgeDialog.getInputPanel());
          add(mCEdgeDialog.getAltStartNodePanel());
          mCEdgeDialog.getInputTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
              save();
              EditorInstance.getInstance().refresh();
            }
          });
        }
      }
    } else {

      if (!(event instanceof CEdgeDialogModifiedEvent)) {
        int a = 0;
      }
    }
  }

  private void createNewDialog() {
    if (mCEdgeDialog != null) {
      mCEdgeDialog.removeListener();
    }
    mCEdgeDialog = new ModifyCEdgeDialog(mDataCEdge);
  }

  private void save() {
    String inputString = mCEdgeDialog.getInputTextField().getText().trim();

    try {
      //ChartParser.parseResultType = ChartParser.LOG;
      //ChartParser.parseResultType = ChartParser.EXP;
      Code log = new Code(inputString);

      //LogicalCond log = ChartParser.logResult;
      //Expression log = ChartParser.expResult;
      if (log != null) {
        mDataCEdge.setCondition(log);
      } else {

        // Do nothing
      }
    } catch (Exception e) {
    }
  }
}