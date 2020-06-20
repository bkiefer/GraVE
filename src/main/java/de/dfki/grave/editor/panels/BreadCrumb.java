package de.dfki.grave.editor.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import de.dfki.grave.model.flow.SuperNode;

/** A breadcrumb panel for the current path of supernodes */
@SuppressWarnings("serial")
public class BreadCrumb extends JPanel {
  
  private final ProjectEditor mEditor;
  
  // The supernodes of the path display
  private final LinkedList<JButton> mPathComponents = new LinkedList<>();
  
  private JScrollBar mPathScrollBar;
  private JPanel inner;
  
  public BreadCrumb(ProjectEditor editor) {
    mEditor = editor;
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    inner = new JPanel();
    inner.setLayout(new BoxLayout(inner, BoxLayout.X_AXIS));
    inner.setMinimumSize(new Dimension(500, 22));

    JScrollPane pane = new JScrollPane(inner);
    pane.setViewportBorder(BorderFactory.createLineBorder(Color.gray));
    pane.setMaximumSize(new Dimension(7000, 40));
    pane.setMinimumSize(new Dimension(300, 30));
    pane.setPreferredSize(new Dimension(400, 30));
    pane.setBorder(BorderFactory.createEmptyBorder());
    mPathScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    mPathScrollBar.setPreferredSize(new Dimension(300, 10));
    mPathScrollBar.setOpaque(false);
    mPathScrollBar.setBorder(BorderFactory.createEmptyBorder());
    pane.setHorizontalScrollBar(mPathScrollBar);
    add(pane);
  }
  
  private JButton createPathButton(SuperNode supernode) {
    final Action action = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mEditor.selectNewWorkSpaceLevel(supernode);
      }
    };
    action.putValue(Action.NAME, supernode.getName());
    action.putValue(Action.SHORT_DESCRIPTION, supernode.getName());
    final JButton pathElement = new JButton(action);
    pathElement.setUI(new BasicButtonUI());
    pathElement.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    pathElement.setMinimumSize(new Dimension(80, 18));
    pathElement.setMaximumSize(new Dimension(80, 18));
    pathElement.setPreferredSize(new Dimension(80, 18));
    pathElement.setBackground(new Color(255, 255, 255));
    pathElement.addMouseMotionListener(new MouseMotionAdapter() {
      // TODO: Does not work smoothly, revise
      @Override
      public void mouseDragged(MouseEvent e) {
        int dir = e.getX();

        if (dir < 0) {
          mPathScrollBar.setValue(mPathScrollBar.getValue() + 10);
        } else {
          mPathScrollBar.setValue(mPathScrollBar.getValue() - 10);
        }
      }
    });
    return pathElement;
  }
  
  // Refresh the path display
  public void refreshDisplay() {
    // Remove all path components
    inner.removeAll();
    // For each supernode component in the path
    for (final JButton pathElement : mPathComponents) {
      // Compute color intensity
      int index = inner.getComponentCount();
      int intensity = 255 - 5 * index;
      intensity = (intensity < 0) ? 0 : intensity;
      // Create a button with the name
      pathElement.setBackground(new Color(intensity, intensity, intensity));
      // Create a label with an arrow
      final JLabel arrow = new JLabel("\u2192");
      if (index > 0) {
        inner.add(arrow);
      }
      inner.add(pathElement);
    }
    revalidate();
    repaint(100);
  }
  
  // TODO: adding not explicit but via refresh method
  public void addPathComponent(SuperNode supernode) {
    mPathComponents.addLast(createPathButton(supernode));
    refreshDisplay();
    int va = mPathScrollBar.getMaximum();
    mPathScrollBar.setValue(va);
  }

  public void removePathComponent() {
    mPathComponents.removeLast();
    refreshDisplay();
  }
}
