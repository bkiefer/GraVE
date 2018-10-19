package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.dialog.ModifyIEdgeDialog;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.model.sceneflow.chart.edge.InterruptEdge;
import de.dfki.vsm.model.sceneflow.chart.edge.AbstractEdge.EdgeType;
import de.dfki.vsm.model.sceneflow.glue.GlueParser;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
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
class InterruptEditor extends JPanel implements EventListener {

  private InterruptEdge mDataIEdge;
  private ModifyIEdgeDialog mIEdgeDialog;

  public InterruptEditor() {
    initComponents();
    EventDispatcher.getInstance().register(this);
  }

  private void initComponents() {
    setBackground(Color.white);
    setPreferredSize(new Dimension(500, 270));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof EdgeSelectedEvent) {
      if (event instanceof EdgeSelectedEvent) {
        if (((EdgeSelectedEvent) event).getEdge().getEdgeType().equals(EdgeType.InterruptEdge)) {
          mDataIEdge = (InterruptEdge) ((EdgeSelectedEvent) event).getEdge();
          createNewDialog();
          removeAll();
          mIEdgeDialog.getInputPanel().setMinimumSize(new Dimension(200, 40));
          mIEdgeDialog.getInputPanel().setMaximumSize(new Dimension(1000, 40));
          mIEdgeDialog.getInputPanel().setPreferredSize(new Dimension(200, 40));
          mIEdgeDialog.getInputPanel().setAlignmentX(RIGHT_ALIGNMENT);
          mIEdgeDialog.getAltStartNodePanel().setMinimumSize(new Dimension(200, 150));
          mIEdgeDialog.getAltStartNodePanel().setMaximumSize(new Dimension(1000, 150));
          mIEdgeDialog.getAltStartNodePanel().setPreferredSize(new Dimension(200, 150));
          mIEdgeDialog.getAltStartNodePanel().setAlignmentX(RIGHT_ALIGNMENT);
          add(mIEdgeDialog.getInputPanel());
          add(mIEdgeDialog.getAltStartNodePanel());
          add(Box.createVerticalGlue());
          mIEdgeDialog.getInputTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
              save();
              EditorInstance.getInstance().refresh();
            }
          });
        }
      }
    } else {

      // Do nothing
    }
  }

  private void createNewDialog() {
    if (mIEdgeDialog != null) {
      mIEdgeDialog.removeListener();
    }
    mIEdgeDialog = new ModifyIEdgeDialog(mDataIEdge);
  }

  private void save() {
    String inputString = mIEdgeDialog.getInputTextField().getText().trim();

    try {
      final Command exp = (Command) GlueParser.run(inputString);
      if (exp != null) {
        mDataIEdge.setCondition(exp);
      } else {
        // Do nothing
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}