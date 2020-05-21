package de.dfki.grave.model.project;

import java.awt.Font;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FontConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class FontConfig {
  @XmlAttribute
  public String family;
  @XmlAttribute
  public float size;
  @XmlAttribute
  public boolean bold;
  @XmlAttribute
  public boolean italic;
  
  private Font f = null;

  public Font getFont() {
    if (f == null) {
      int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
      f = new Font(family, style, (int)size);
    }
    return f;
  }
}
