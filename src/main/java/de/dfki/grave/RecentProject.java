package de.dfki.grave;

import javax.xml.bind.annotation.*;

@XmlType(name="recent")
@XmlAccessorType(XmlAccessType.NONE)
public class RecentProject {
  
  // Only for XML unmarshalling
  @SuppressWarnings("unused")
  private RecentProject() {}
  
  public RecentProject(String n, String p, String d) {
    name = n;
    path = p;
    date = d;
  }

  @XmlAttribute(name="name")
  public String name;
  
  @XmlElement(name="path")
  public String path;
  
  @XmlAttribute(name="date")
  public String date;
}
