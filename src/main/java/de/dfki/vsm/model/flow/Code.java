package de.dfki.vsm.model.flow;

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
public class Code implements Copyable, XMLParseable, XMLWriteable {

  protected String content;

  public Code(String c) {
    content = c;
  }

  protected Code() {

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
  public Code getCopy() {
    Code result = null;
    try {
      result = this.getClass().newInstance();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    result.content = this.content;
    return result;
  }

  public static Code parse(final Element element) throws XMLParseError {
    // The command to parse
    return new Code(element.getTextContent());
  }

  @Override
  public void parseXML(Element element) throws XMLParseError {
    content = element.getTextContent();
  }

  @Override
  public void writeXML(IOSIndentWriter writer) throws XMLWriteError {
    writer.print("<Command><![CDATA[").printPlain(content).println("]]></Command>");
  }
}
