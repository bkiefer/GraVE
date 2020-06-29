package de.dfki.grave.editor.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import de.dfki.grave.editor.EditorComponent;
import de.dfki.grave.model.project.EditorProject;
import de.dfki.grave.util.ResourceLoader;

/** This class provides the code editing regions in the bottom part of the
 *  window
 */
@SuppressWarnings("serial")
public class CodeEditor extends JPanel {

  private final CodeEditPanel mRightTextArea;
  private final CodeEditPanel mLeftTextArea;
  private final JButton mPinButton;
  private JSplitPane splitPane;
  //PIN icons
  private final ImageIcon ICON_PIN_STANDARD = ResourceLoader.loadImageIcon("img/pin.png");
  private final ImageIcon ICON_PIN_ROLLOVER = ResourceLoader.loadImageIcon("img/pin_blue.png");
  //PIN status
  private boolean pinPricked = false;

  private final EditorProject mEditorProject;

  private static void sanitizeTinyButton(JButton b) {
    Dimension bDim = new Dimension(30, 30);
    b.setMinimumSize(bDim);
    b.setMaximumSize(bDim);
    b.setPreferredSize(bDim);
    b.setBorder(BorderFactory.createEmptyBorder());
  }
  
  /**
   * Makes a split pane invisible. Only contained components are shown.
   *
   * @param splitPane
   */
  public static void flattenJSplitPane(JSplitPane splitPane) {
      splitPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      BasicSplitPaneUI flatDividerSplitPaneUI = new BasicSplitPaneUI() {
          @Override
          public BasicSplitPaneDivider createDefaultDivider() {
              return new BasicSplitPaneDivider(this) {
                  @Override
                  public void setBorder(Border b) {
                  }
              };
          }
      };
      splitPane.setUI(flatDividerSplitPaneUI);
      splitPane.setBorder(null);
  }
  
  public CodeEditor(final EditorProject project) {
    mEditorProject = project;
    setLayout(new BorderLayout());

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    splitPane.setResizeWeight(1.0);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setUI(new BasicSplitPaneUI());
    splitPane.setDividerSize(10);
    splitPane.setDividerLocation(.5);
    flattenJSplitPane(splitPane);
    Font font = project.getEditorConfig().sCODE_FONT.getFont();
    mRightTextArea = new CodeEditPanel(font);
    mRightTextArea.setBorder(BorderFactory.createLineBorder(Color.gray));
    mLeftTextArea = new CodeEditPanel(font);
    mLeftTextArea.setBorder(BorderFactory.createLineBorder(Color.gray));
    
    mPinButton = new JButton();
    mPinButton.setVisible(true);
    /*
    pinPricked = mEditorProject.getEditorConfig().sAUTOHIDE_BOTTOMPANEL;
    setPin(pinPricked);
    mPinButton.setContentAreaFilled(false);
    mPinButton.setFocusable(false);
    mPinButton.addActionListener((ActionEvent e) -> { setPin(!pinPricked); });
    sanitizeTinyButton(mPinButton);
    */
    Box VpinBox = Box.createVerticalBox();
    VpinBox.add(mPinButton);
    VpinBox.add(Box.createVerticalGlue());
    add(VpinBox, BorderLayout.EAST);

    splitPane.setRightComponent(mRightTextArea);
    splitPane.setLeftComponent(mLeftTextArea);
    add(splitPane, BorderLayout.CENTER);
  }

  public void updateBorders(){
    mRightTextArea.updateBorders(0, 0, splitPane.getDividerLocation(), this.getSize().height);
    mLeftTextArea.updateBorders(0, 0, splitPane.getDividerLocation(), this.getSize().height);
  }

  /** Set the pin pricked flag
  public final void setPin(boolean state) {
    pinPricked = state;
    mPinButton.setIcon(pinPricked ? ICON_PIN_ROLLOVER : ICON_PIN_STANDARD);
    mPinButton.setRolloverIcon(pinPricked ? ICON_PIN_STANDARD : ICON_PIN_ROLLOVER);
    mEditorProject.getEditorConfig().sAUTOHIDE_BOTTOMPANEL = pinPricked;
    if (state) {
      // pin was pricked, thus clone right code editor to the left
      mLeftTextArea.setEditedObject(mRightTextArea.getEditedObject());
    } else {
      // TODO: clear if pin unpricked?
    }
  }

  /** Get the pin pricked flag *
  public boolean isPinPricked() { return pinPricked; }

  /** Set the pin pricked flag *
  public void setPinPricked() {
    setPin(true); // true pricks the pin
  }

  /*
  public void setEditedNodeOrEdge(EditorComponent n) {
    if (mRightTextArea.getEditedObject() == n) return; // may be due to FocusGained
    // update text area with current code & object
    mRightTextArea.setEditedObject(n);
  }*/

  /** If the TODO: revert button is pressed, put the original text back into
   *  the text area
   */
  public void revert() {
  }
}
