package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.dialog.ModifyTEdgeDialog;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.model.sceneflow.chart.edge.TimeoutEdge;
import de.dfki.vsm.model.sceneflow.chart.edge.AbstractEdge.EdgeType;
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
class TimeOutEditor extends JPanel implements EventListener {

  private TimeoutEdge mDataTEdge;
  private ModifyTEdgeDialog mTEdgeDialog;

  public TimeOutEditor() {
    initComponents();
    EventDispatcher.getInstance().register(this);
  }

  private void initComponents() {
    setBackground(Color.white);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setAlignmentX(RIGHT_ALIGNMENT);
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof EdgeSelectedEvent) {
      if (((EdgeSelectedEvent) event).getEdge().getEdgeType().equals(EdgeType.TimeoutEdge)) {
        mDataTEdge = (TimeoutEdge) ((EdgeSelectedEvent) event).getEdge();
        mTEdgeDialog = new ModifyTEdgeDialog(mDataTEdge);
        removeAll();
        mTEdgeDialog.getInputPanel().setMinimumSize(new Dimension(200, 40));
        mTEdgeDialog.getInputPanel().setMaximumSize(new Dimension(1000, 40));
        mTEdgeDialog.getInputPanel().setPreferredSize(new Dimension(200, 40));
        mTEdgeDialog.getInputPanel().setAlignmentX(RIGHT_ALIGNMENT);
        mTEdgeDialog.getAltStartNodePanel().setMinimumSize(new Dimension(200, 150));
        mTEdgeDialog.getAltStartNodePanel().setMaximumSize(new Dimension(1000, 150));
        mTEdgeDialog.getAltStartNodePanel().setPreferredSize(new Dimension(200, 150));
        mTEdgeDialog.getAltStartNodePanel().setAlignmentX(RIGHT_ALIGNMENT);
        add(mTEdgeDialog.getInputPanel());
        add(mTEdgeDialog.getAltStartNodePanel());
        mTEdgeDialog.getInputTextField().addKeyListener(new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent event) {
            save();
            EditorInstance.getInstance().refresh();
          }
        });
      }
    } else {

      // Do nothing
    }
  }

  private void save() {
    mDataTEdge.setTimeout(Integer.parseInt(mTEdgeDialog.getInputTextField().getText()));
  }
}