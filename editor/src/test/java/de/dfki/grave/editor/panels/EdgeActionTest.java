package de.dfki.grave.editor.panels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.junit.Test;

import de.dfki.grave.editor.ObserverDocument;
import de.dfki.grave.editor.action.CreateEdgeAction;
import de.dfki.grave.editor.action.EditContentAction;
import de.dfki.grave.editor.action.RemoveEdgeAction;
import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.EpsilonEdge;
import de.dfki.grave.model.ForkingEdge;
import de.dfki.grave.model.GuardedEdge;
import de.dfki.grave.model.InterruptEdge;
import de.dfki.grave.model.RandomEdge;
import de.dfki.grave.model.TimeoutEdge;

public class EdgeActionTest extends WorkSpaceTest {

  @Test
  public void createEdgeTest() {
    int[] edges = getEdgeNum();
    assertEquals(edges[1], edges[0]);

    List<BasicNode> l = getBasics();
    AbstractEdge[] edgeProtos = {
        new EpsilonEdge(),
        new ForkingEdge(),
        new InterruptEdge(),
        new GuardedEdge(),
        new TimeoutEdge(),
        new RandomEdge(),
    };

    for (AbstractEdge proto : edgeProtos) {
      AbstractEdge newEdge = AbstractEdge.getNewEdge(proto);
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

      act.undo();
      nedges = getEdgeNum();
      assertEquals(edges[0], nedges[0]);
    }
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
  public void removeRandomEdgeTest() {
    int[] edges = getEdgeNum();

    List<BasicNode> l = getBasicSuper();
    BasicNode src = l.get(0);
    AbstractEdge e = null;
    Iterator<AbstractEdge> it = src.getEdgeList().iterator();
    while (it.hasNext() && (! ((e = it.next()) instanceof RandomEdge))) {
    }
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
