package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.dialog.ModifyPEdgeDialog;
import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.model.flow.edge.RandomEdge;
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
class ProbabilityEditor extends JPanel implements EventListener {

  private final HashMap<RandomEdge, JTextField> mPEdgeMap = new HashMap<RandomEdge, JTextField>();
  private RandomEdge mDataPEdge;
  private ModifyPEdgeDialog mPEdgeDialog;
  private JPanel mButtonPanel;

  public ProbabilityEditor() {
    initComponents();
    EventDispatcher.getInstance().register(this);
  }

  private void initComponents() {
    setBackground(Color.white);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setAlignmentY(CENTER_ALIGNMENT);
    setAlignmentX(CENTER_ALIGNMENT);
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof EdgeSelectedEvent) {
      if (((EdgeSelectedEvent) event).getEdge().getEdgeType().equals(EdgeType.RandomEdge)) {
        mDataPEdge = (RandomEdge) ((EdgeSelectedEvent) event).getEdge();
        mPEdgeDialog = new ModifyPEdgeDialog(mDataPEdge);
        removeAll();
        mPEdgeDialog.getEdgeProbPanel().setMinimumSize(new Dimension(200, 140));
        mPEdgeDialog.getEdgeProbPanel().setMaximumSize(new Dimension(1000, 140));
        mPEdgeDialog.getEdgeProbPanel().setPreferredSize(new Dimension(200, 140));
        add(mPEdgeDialog.getEdgeProbPanel());
        mPEdgeDialog.getAltStartNodePanel().setMinimumSize(new Dimension(200, 150));
        mPEdgeDialog.getAltStartNodePanel().setMaximumSize(new Dimension(1000, 150));
        mPEdgeDialog.getAltStartNodePanel().setPreferredSize(new Dimension(200, 150));
        add(mPEdgeDialog.getAltStartNodePanel());

        for (JTextField textField : mPEdgeDialog.getPEdgeMap().values()) {
          textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
              save();
            }
          });
        }

        mPEdgeDialog.getNormButton().addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
            mPEdgeDialog.normalizeActionPerformed();
            save();
          }
        });
        mPEdgeDialog.getUniButton().addMouseListener(new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
            mPEdgeDialog.uniformActionPerformed();
            save();
          }
        });
        mButtonPanel = new JPanel();
        mButtonPanel.setOpaque(false);
        mButtonPanel.setMinimumSize(new Dimension(440, 40));
        mButtonPanel.setLayout(new BoxLayout(mButtonPanel, BoxLayout.X_AXIS));
        mButtonPanel.add(Box.createRigidArea(new Dimension(20, 10)));
        mButtonPanel.add(mPEdgeDialog.getUniButton());
        mButtonPanel.add(Box.createRigidArea(new Dimension(20, 10)));
        mButtonPanel.add(mPEdgeDialog.getNormButton());
        add(Box.createRigidArea(new Dimension(20, 20)));
        add(mButtonPanel);
      }
    } else {

      // Do nothing
    }
  }

  private void save() {
    mPEdgeDialog.okActionPerformed();
    EditorInstance.getInstance().refresh();

    // System.out.println("save");
  }
}