package de.dfki.vsm.model.sceneflow.glue.command;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.dfki.vsm.util.cpy.Copyable;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLParseable;
import de.dfki.vsm.util.xml.XMLWriteError;
import de.dfki.vsm.util.xml.XMLWriteable;

/**
 * @author Gregor Mehlmann
 */
public class Command implements Copyable, XMLParseable, XMLWriteable {

  protected String content;

  public Command(String c) {
    content = c;
  }

  protected Command() {

  }

  public String getContent() {
    return content;
  }

  public void setContent(String s) {
    content = s;
  }

  public String getAbstractSyntax() {
    return content;
  }

  public String getConcreteSyntax() {
    return content;
  }

  public String getFormattedSyntax() {
    return content;
  }

  @Override
  public Command getCopy() {
    Command result = null;
    try {
      result = this.getClass().newInstance();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    result.content = this.content;
    return result;
  }

  public static Command parse(final Element element) throws XMLParseError {
    // The command to parse
    Command command = new Command(element.getTextContent());
    return command;
  }

  @Override
  public void parseXML(Element element) throws XMLParseError {
    content = "";
    NodeList l = element.getChildNodes();
  }

  @Override
  public void writeXML(IOSIndentWriter writer) throws XMLWriteError {
    // TODO Auto-generated method stub

  }
}
