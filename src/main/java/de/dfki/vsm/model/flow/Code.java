package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
  public static class AdapterCDATA extends XmlAdapter<String, String> {
    @Override
    public String marshal(String arg0) throws Exception {
        return "<![CDATA[" + arg0 + "]]>";
    }
    @Override
    public String unmarshal(String arg0) throws Exception {
        return arg0;
    }
  }

  @XmlJavaTypeAdapter(AdapterCDATA.class)
  protected String content;

  public Code(String c) {
    content = c;
  }

  protected Code() {

  }

  @XmlTransient
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
