package de.dfki.grave.editor.panels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.editor.panels.WorkSpacePanel;
import de.dfki.grave.model.flow.*;

public class WorkSpaceTest {
  
  protected static String RESOURCE_DIR = "src/test/resources";

  protected ProjectEditor ed;
  protected WorkSpacePanel ws;
  
  @BeforeClass
  public static void init() {
    Geom.initialize(32);
  }

  @Before
  public void loadTestGraph() {
    AppFrame.getInstance().openProject(new File(RESOURCE_DIR, "test"));
    ed = AppFrame.getInstance().getSelectedProjectEditor();
    ws = ed.getWorkSpace();
  }
  
  protected int[] getEdgeNum() {
    final int[] edges = { 0, 0 };
    ws.getEdges().forEach((e) -> { ++edges[0]; });
    ed.getSceneFlow().getNodes().forEach(
        (n) -> { n.getEdgeList().forEach((e) -> { ++edges[1]; }); });
    return edges; // view, model
  }
  
  protected int getNodeNum() {
    return ed.getSceneFlow().getNodeSize();
  }
  
  protected List<BasicNode> getBasics() {
    List<BasicNode> l = new ArrayList<>();
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (n.isBasic()) { l.add(n); }
    return l;
  }
  
  protected List<BasicNode> getFirstBasic() {
    List<BasicNode> l = new ArrayList<>();
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (n.isBasic()) { l.add(n); break; }
    return l;
  }
  
  protected List<BasicNode> getSuper() {
    List<BasicNode> l = new ArrayList<>();
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (! n.isBasic()) { l.add(n); break; }
    return l;
  }
  
  protected List<BasicNode> getBasicSuper() {
    List<BasicNode> l = new ArrayList<>();
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (n.isBasic()) { l.add(n); break; }
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (! n.isBasic()) {  l.add(n); break; }
    return l;
  }
  
}
