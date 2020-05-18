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

import de.dfki.grave.model.flow.Code;
import de.dfki.grave.model.flow.ContentHolder;
import de.dfki.grave.model.flow.Expression;

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
    mInitialContent = h.getContent();
    try {
      insertString(0, mModel.getContent(), null);
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /** Write the document content back to the model */
  public void updateModel() {
    try {
      String newContent = getText(0, getLength()); 
      mModel.setContent(newContent);
      // explicitely changes, so make sure we consider this
      mInitialContent = mModel.getContent();
    } catch (BadLocationException e) {
      // can not happen
      throw new RuntimeException(e);
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
  
  @Override
  public void update(Observable o, Object o1) {
    try {
      if (o instanceof Code) {
        insertString(0, ((Code)o).getContent(), null);
      } else if (o instanceof Expression) {
        insertString(0, ((Expression)o).getContent(), null);
      }
    } catch (BadLocationException ex) {
      logger.error("bad loc: {}", ex);
    }
  }
  
  public String toString() {
    try {
      return getText(0, getLength());
    } catch (BadLocationException e) {
      // can not happen
      throw new RuntimeException(e);
    }
  }

}
