package de.dfki.vsm.editor.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.util.ios.ResourceLoader;

@SuppressWarnings("serial")
public class CodeEditor extends JPanel {

  private final RSyntaxTextArea mTextArea;
  private final JButton mPinButton;
  //PIN icons
  private final ImageIcon ICON_PIN_STANDARD = ResourceLoader.loadImageIcon("img/pin.png");
  private final ImageIcon ICON_PIN_ROLLOVER = ResourceLoader.loadImageIcon("img/pin_blue.png");
  //PIN status
  private boolean pinPricked = false;

  private final EditorProject mEditorProject;

  // Can be Node or Edge
  private Object mEditedObject;

  private static void sanitizeTinyButton(JButton b) {
    Dimension bDim = new Dimension(30, 30);
    b.setMinimumSize(bDim);
    b.setMaximumSize(bDim);
    b.setPreferredSize(bDim);
    //b.setOpaque(false);

    //b.setContentAreaFilled(false);
    //b.setFocusable(false);
    b.setBorder(BorderFactory.createEmptyBorder());
  }

  public CodeEditor(final EditorProject project) {
    mEditorProject = project;

    setLayout(new BorderLayout());

    mTextArea = new RSyntaxTextArea();
    mTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    mTextArea.setCodeFoldingEnabled(true);
    mTextArea.setWrapStyleWord(true);
    mTextArea.setPreferredSize(new Dimension(10000, 230));
    mTextArea.setVisible(true);
    //setBackground(new Color(255, 255, 255, 90));
    //mTextArea.setBackground(new Color(175, 175, 175, 95));
    //this.setMaximumSize(new Dimension(maxWidth, maxHeight));

    mPinButton = new JButton();
    mPinButton.setVisible(true);
    pinPricked = mEditorProject.getEditorConfig().sAUTOHIDE_BOTTOMPANEL;
    setPin(pinPricked);
    mPinButton.setContentAreaFilled(false);
    //mPinButton.setMargin(new Insets(0, 10, 20, 10));
    mPinButton.setFocusable(false);
    mPinButton.addActionListener((ActionEvent e) -> { setPin(!pinPricked); });
    sanitizeTinyButton(mPinButton);
    Box VpinBox = Box.createVerticalBox();
    VpinBox.add(mPinButton);
    VpinBox.add(Box.createVerticalGlue());
    //add(Box.createHorizontalGlue());
    add(VpinBox, BorderLayout.EAST);

    // Get rid of annoying yellow line
    //mTextArea.setHighlighter(null);
    mTextArea.setHighlightCurrentLine(false);
    mTextArea.setHighlightSecondaryLanguages(false);

    JScrollPane jsp = new JScrollPane();
    jsp.add(mTextArea);
    //jsp.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    add(jsp, BorderLayout.CENTER);
  }

  // Set the pin pricked flag
  public final void setPin(boolean state) {
    pinPricked = state;
    mPinButton.setIcon(pinPricked ? ICON_PIN_ROLLOVER : ICON_PIN_STANDARD);
    mPinButton.setRolloverIcon(pinPricked ? ICON_PIN_STANDARD : ICON_PIN_ROLLOVER);
    mEditorProject.getEditorConfig().sAUTOHIDE_BOTTOMPANEL = pinPricked;
  }

  /** Get the pin pricked flag */
  public boolean isPinPricked() { return pinPricked; }

  /** Set the pin pricked flag */
  public void setPinPricked() {
    setPin(true); // true pricks the pin
  }

  public void setEditedNodeOrEdge(Object n) {
    if (n != null && !(n instanceof Node || n instanceof Edge)) return;
    // check if text was changed and write back

    // set new edited object
    mEditedObject = n;
    if (n == null) {
      // clear text area
    } else {
      // update text area with current code
    }
  }

  /** If the TODO: revert button is pressed, put the original text back into
   *  the text area
   */
  public void revert() {
  }
}
