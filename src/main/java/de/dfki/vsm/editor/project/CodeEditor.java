package de.dfki.vsm.editor.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import de.dfki.vsm.editor.CmdBadge;
import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.model.project.EditorProject;
import de.dfki.vsm.util.ios.ResourceLoader;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneUI;

@SuppressWarnings("serial")
public class CodeEditor extends JPanel {

  private final EditCodeArea mRightTextArea;
  private final EditCodeArea mLeftTextArea;
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
    //b.setOpaque(false);

    //b.setContentAreaFilled(false);
    //b.setFocusable(false);
    b.setBorder(BorderFactory.createEmptyBorder());
  }

  public CodeEditor(final EditorProject project) {
    mEditorProject = project;

    setLayout(new BorderLayout());
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
    splitPane.setResizeWeight(1.0);

    splitPane.setUI(new BasicSplitPaneUI());
    splitPane.setDividerSize(10);
    splitPane.setDividerLocation(400);

    mRightTextArea = new EditCodeArea(
              mEditorProject.getEditorConfig().sCODE_DIVIDER_LOCATION);
    mLeftTextArea = new EditCodeArea(
              mEditorProject.getEditorConfig().sCODE_DIVIDER_LOCATION);

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

    splitPane.setRightComponent(mRightTextArea);
    splitPane.setLeftComponent(mLeftTextArea);
    //jsp.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    add(splitPane, BorderLayout.CENTER);
  }

  private class EditCodeArea extends JPanel {
    RSyntaxTextArea textArea;
    Object editedObject;
    JScrollPane s;

    // make this jpane, with scrollpane, and textarea
    public EditCodeArea(int dividerLocation) {
      super(new BorderLayout());
      textArea = new RSyntaxTextArea();
      textArea.setCodeFoldingEnabled(true);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      //textArea.setBounds(this.getBounds());
      //setBounds(0, 0, 5000, dividerLocation);
      textArea.setVisible(true);

      // Get rid of annoying yellow line
      textArea.setHighlightCurrentLine(false);
      textArea.setHighlightSecondaryLanguages(false);
      s = new JScrollPane();
      s.add(textArea);
      add(s, BorderLayout.CENTER);
    }

    public void setEditedObject(Object n) {
      if (n == null) {
        textArea.setText("");
        editedObject = null;
        return;
      }
      editedObject = n;
      if (n instanceof CmdBadge)
        textArea.setDocument(((CmdBadge)n).getDocument());
      else if (n instanceof Edge)
        textArea.setDocument(((Edge)n).getCodeDocument());
      else if (n instanceof Node)
        textArea.setDocument(((Node)n).getCodeDocument());
      //else
        // TODO: Might want to throw exception here
      //  return;
    }

    public void updateBorders(int x, int y, int w, int h) {
      //setBounds(x, y, w, h);
      //s.setBounds(x, y, w, h);
      textArea.setBounds(x, y, w, h);
    }
  }

  public void updateBorders(){
    mRightTextArea.updateBorders(0, 0, splitPane.getDividerLocation(), this.getSize().height);
    mLeftTextArea.updateBorders(0, 0, splitPane.getDividerLocation(), this.getSize().height);
  }

  // Set the pin pricked flag
  public final void setPin(boolean state) {
    pinPricked = state;
    mPinButton.setIcon(pinPricked ? ICON_PIN_ROLLOVER : ICON_PIN_STANDARD);
    mPinButton.setRolloverIcon(pinPricked ? ICON_PIN_STANDARD : ICON_PIN_ROLLOVER);
    mEditorProject.getEditorConfig().sAUTOHIDE_BOTTOMPANEL = pinPricked;
    if (state) {
      // pin was pricked, thus clone right code editor to the left
      mLeftTextArea.setEditedObject(mRightTextArea.editedObject);
    } else {
      // TODO: clear if pin unpricked?
    }
  }

  /** Get the pin pricked flag */
  public boolean isPinPricked() { return pinPricked; }

  /** Set the pin pricked flag */
  public void setPinPricked() {
    setPin(true); // true pricks the pin
  }

  public void setEditedNodeOrEdge(Object n) {
    if (mRightTextArea.editedObject == n) return; // may be due to FocusGained
    // update text area with current code & object
    mRightTextArea.setEditedObject(n);
  }

  /** If the TODO: revert button is pressed, put the original text back into
   *  the text area
   */
  public void revert() {
  }
}
