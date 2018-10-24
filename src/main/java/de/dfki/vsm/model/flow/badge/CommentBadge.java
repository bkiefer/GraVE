package de.dfki.vsm.model.flow.badge;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.flow.geom.Boundary;
import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@XmlType(name="Comment")
public class CommentBadge implements Copyable {

  protected SuperNode mParentNode = null;
  protected String mHTMLText = "";
  protected Boundary mBoundary;
  protected int mFontSize;
  JEditorPane mTextEditor;

  public void setParentNode(SuperNode value) {
    mParentNode = value;
  }

  @XmlElement(name="Boundary")
  public Boundary getBoundary() {
    return mBoundary;
  }

  public void setBoundary(Boundary value) {
    mBoundary = value;
  }

  public void setFontSize(int value) {
    mFontSize = value;
  }

  @XmlElement(name="Text")
  public String getHTMLText() {
    return mHTMLText.trim();
  }

  public void setHTMLText(String text) {
    mHTMLText = text.trim();
  }

  private void formatHTML() {
    // PG 30.7.2016 Da fuck? This is not allowed in the model!
    //mFontSize = EditorInstance.getInstance().getSelectedProjectEditor().getEditorProject().getEditorConfig().sWORKSPACEFONTSIZE;
    mFontSize = 16;

    if (mTextEditor == null) {
      mTextEditor = new JEditorPane();
    }

    //mTextEditor.setContentType(new HTMLEditorKit().getContentType());
    // now use the same font than the label!
    Font mFont = new Font("SansSerif", Font.PLAIN, mFontSize);
    String bodyRule = "body { font-family: " + mFont.getFamily() + "; " + "font-size: " + mFont.getSize() + "pt; }";

    // ((HTMLDocument) mTextEditor.getDocument()).getStyleSheet().addRule(bodyRule);
    mTextEditor.setText(mHTMLText);
    mHTMLText = mTextEditor.getText();
  }

  @Override
  public ModelObject getCopy() {
    // TODO: REACTIVATE
    return null;
  }
}
