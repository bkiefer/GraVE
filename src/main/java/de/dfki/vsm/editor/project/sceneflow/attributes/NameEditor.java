package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.model.sceneflow.chart.BasicNode;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 *
 *
 * @author Gregor Mehlmann
 *
 *
 */
public class NameEditor extends JPanel implements EventListener {

  private JTextField mNameField;
  private BasicNode mDataNode;

  public NameEditor() {
    initComponents();
    EventDispatcher.getInstance().register(this);
  }

  private void initComponents() {
    // Init the node name panel
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(false);
    //setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Edit Node Name:"));
    JLabel jl = new JLabel("Name:");
    add(jl);
    add(Box.createRigidArea(new Dimension(4, 20)));
    int labelwidth = 48 + 4;
    // Init the node name text field
    mNameField = new JTextField();
    mNameField.setMinimumSize(new Dimension(230 - labelwidth, 20));
    mNameField.setMaximumSize(new Dimension(1000, 20));
    mNameField.setPreferredSize(new Dimension(230 - labelwidth, 20));
    mNameField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent event) {
        save();
        EditorInstance.getInstance().refresh();
      }
    });

    add(mNameField);
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof NodeSelectedEvent) {

      // Update the selected node
      mDataNode = ((NodeSelectedEvent) event).getNode();

      // Reload the node name
      mNameField.setText(mDataNode.getName());
    } else {

      // Do nothing
    }
  }

  private void save() {

    mDataNode.setName(sanitizeString(mNameField.getText().trim()));
  }

  //ESCAPES STRINGS
  private String sanitizeString(String st) {
    String output = st;
    output = output.replaceAll("'", "");
    output = output.replaceAll("\"", "");
    return output;
  }
}