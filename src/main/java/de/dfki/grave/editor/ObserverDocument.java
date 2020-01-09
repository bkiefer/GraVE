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

  private static final Logger logger =
      LoggerFactory.getLogger(ObserverDocument.class);


  public ObserverDocument(String syntaxStyle) {
    super(syntaxStyle);
  }

  public ObserverDocument(ContentHolder h) {
    super(SyntaxConstants.SYNTAX_STYLE_JAVA);
    mModel = h;
    try {
      insertString(0, mModel.getContent(), null);
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void updateModel() {
    try {
      mModel.setContent(getText(0, getLength()));
    } catch (BadLocationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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

}
