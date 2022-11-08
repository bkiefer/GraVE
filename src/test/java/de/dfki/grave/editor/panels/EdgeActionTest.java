package de.dfki.grave.editor.panels;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.text.BadLocationException;

import org.junit.*;

import de.dfki.grave.editor.*;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.model.flow.*;

public class EdgeActionTest extends WorkSpaceTest {

  @Test
  public void createEdgeTest() {
    int[] edges = getEdgeNum();
    assertEquals(edges[1], edges[0]);

    List<BasicNode> l = getBasics();
    AbstractEdge newEdge = AbstractEdge.getNewEdge(new EpsilonEdge());
    CreateEdgeAction act = new CreateEdgeAction(ed, l.get(0), l.get(1), newEdge);
    act.run();

    int[] nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] + 1, nedges[0]);

    act.undo();
    nedges = getEdgeNum();
    assertEquals(edges[0], nedges[0]);

    act.redo();
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] + 1, nedges[0]);

  }

  @Test
  public void removeEdgeTest() {
    int[] edges = getEdgeNum();

    List<BasicNode> l = getBasicSuper();
    BasicNode src = l.get(0);
    AbstractEdge e = src.getEdgeList().iterator().next();
    RemoveEdgeAction act = new RemoveEdgeAction(ed, e);
    act.run();

    assertFalse(src.getEdgeList().iterator().hasNext());
    int[] nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[1] - 1, nedges[1]);

    act.undo();
    assertTrue(src.getEdgeList().iterator().hasNext());
    assertEquals(e, src.getEdgeList().iterator().next());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[1], nedges[1]);

    act.redo();
    assertFalse(src.getEdgeList().iterator().hasNext());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[1] - 1, nedges[1]);
  }

  @Test
  public void moveEdgeCtrTest() {

  }

  @Test
  public void moveEdgeEndPointTest() {

  }

  @Test
  public void normalizeEdgeTest() {

  }

  @Test
  public void editContentTest() throws BadLocationException {
    BasicNode toChange = null; // select the only supernode
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (n.isBasic()) { toChange = n; break; }
    GuardedEdge e = (GuardedEdge)toChange.getEdgeList().iterator().next();
    String before = e.getCondition();
    ObserverDocument doc = new ObserverDocument(e);
    doc.replace(doc.getLength() - 3 , 1, "bla", null);
    EditContentAction act = new EditContentAction(ed, doc);
    act.run();
    String after = before.substring(0, before.length() - 3)
        + "bla" + before.substring(before.length() - 2);
    assertEquals(after, e.getCondition().toString());

    act.undo();
    assertEquals(before, e.getCondition().toString());

    act.redo();
    assertEquals(after, e.getCondition().toString());
  }

}
