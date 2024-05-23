/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.grave.editor;

import java.util.Observable;
import java.util.Observer;

import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.ContentHolder;

/**
 *
 * @author Anna Welker
 */
@SuppressWarnings("serial")
public class ObserverDocument extends RSyntaxDocument implements Observer {

  private ContentHolder mModel;
  private String mInitialContent;

  private static final Logger logger =
      LoggerFactory.getLogger(ObserverDocument.class);


  public ObserverDocument(String syntaxStyle) {
    super(syntaxStyle);
  }

  public ObserverDocument(ContentHolder h) {
    super(SyntaxConstants.SYNTAX_STYLE_JAVA);
    mModel = h;
    init();
  }

  private void init() {
    mInitialContent = mModel.getContent();
    if (mInitialContent == null) {
      mInitialContent = "";
    }
    try {
      replace(0, getLength(), mInitialContent, null);
    } catch (BadLocationException e) {
      throw new RuntimeException(e);      // can not happen
    }
  }

  /** Write the document content back to the model */
  public void updateModel(String newContent) {
    mModel.setContent(newContent);
    init();
  }

  /** Get the content before the last editing session started */
  public String getInitialContent() {
    return mInitialContent;
  }

  /** Did the content change from init or last explicit update? */
  public String getCurrentContent() {
    try {
      return getText(0, getLength());
    } catch (BadLocationException e) {
      throw new RuntimeException(e);      // can not happen
    }
  }

  /** Did the content change from init or last explicit update? */
  public boolean contentChanged() {
    try {
      return ! mInitialContent.equals(getText(0, getLength()));
    } catch (BadLocationException e) {
      // can not happen
      throw new RuntimeException(e);
    }
  }

  public void discardChanges() {
    if (contentChanged()) {
      updateModel(getInitialContent());
    }
  }

  @Override
  public void update(Observable o, Object o1) {
    try {
      if (o instanceof ContentHolder) {
        insertString(0, ((ContentHolder)o).getContent(), null);
      }
    } catch (BadLocationException ex) {
      logger.error("bad loc: {}", ex);
    }
  }

  @Override
  public String toString() {
    try {
      return getText(0, getLength());
    } catch (BadLocationException e) {
      // can not happen
      throw new RuntimeException(e);
    }
  }

}
