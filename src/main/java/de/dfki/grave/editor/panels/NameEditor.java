package de.dfki.grave.editor.panels;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.util.evt.EventDispatcher;
import de.dfki.grave.util.evt.EventListener;

/**
 *
 * @author Gregor Mehlmann
 *
 *
 */
@SuppressWarnings("serial")
public class NameEditor extends JPanel implements EventListener {

  private JTextField mNameField;
  private Node mNode = null;

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
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
          mNode.issueChangeName(sanitizeString(mNameField.getText()));
        }
        EditorInstance.getInstance().refresh();
      }
    });

    add(mNameField);
  }

  @Override
  public void update(Object event) {
    if (event instanceof ElementSelectedEvent) {
      Object elt = ((ElementSelectedEvent) event).getElement();
      if (elt instanceof Node) {
        // Update the selected node
        mNode = (Node)elt;
        if (mNode == null) {
          mNameField.setText("");
          mNameField.setEditable(false);
        } else {
          // Reload the node name
          mNameField.setText(mNode.getDataNode().getName());
          mNameField.setEditable(true);
        }
      }
    } else {
      mNameField.setText("");
      mNameField.setEditable(false);
    }
  }

  // remove all illegal characters
  private String sanitizeString(String st) {
    return st.replaceAll("[^-a-zA-Z0-9_]", "");
  }
}