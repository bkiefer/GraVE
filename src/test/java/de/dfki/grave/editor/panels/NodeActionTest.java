package de.dfki.grave.editor.panels;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;

import org.junit.*;

import de.dfki.grave.editor.*;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.model.flow.*;

public class NodeActionTest extends WorkSpaceTest {

  @Test
  public void createNodeTest() {
    int nodes = getNodeNum();
    BasicNode n = new BasicNode().createNode(
        new Position(50, 50), ws.getSuperNode());
    CreateNodeAction act = new CreateNodeAction(ed, n);
    act.run();
    assertEquals(nodes + 1, getNodeNum());
    act.undo();
    assertEquals(nodes, getNodeNum());
    act.redo();
    assertEquals(nodes + 1, getNodeNum());
  }

  @Test
  public void removeNodesTest0() {
    // Remove node with incoming edges
    int nodes = getNodeNum();
    int[] edges = getEdgeNum();
    assertEquals(edges[1], edges[0]);
    List<BasicNode> l = getSuper();
    RemoveNodesAction act = new RemoveNodesAction(ed, l, false);
    act.run();
    assertEquals(nodes - 1, getNodeNum());
    int[] nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] - 2, nedges[0]);
    act.undo();
    assertEquals(nodes, getNodeNum());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0], nedges[0]);
    act.redo();
    assertEquals(nodes - 1, getNodeNum());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] - 2, nedges[0]);
  }

  @Test
  public void removeNodesTest1() {
    // remove node with outgoing edge
    int nodes = getNodeNum();
    final int[] edges = getEdgeNum();
    assertEquals(edges[1], edges[0]);
    List<BasicNode> l = getFirstBasic();
    RemoveNodesAction act = new RemoveNodesAction(ed, l, false);

    act.run();
    assertEquals(nodes - 1, getNodeNum());
    final int[] nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] - 1, nedges[0]);

    act.undo();
    assertEquals(nodes, getNodeNum());
    nedges[0] = nedges[1] = 0;
    ws.getEdges().forEach((e) -> { ++nedges[0];});
    ed.getSceneFlow().getNodes().forEach(
        (n) -> { n.getEdgeList().forEach((e) -> { ++nedges[1]; }); });
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0], nedges[0]);
    act.redo();
    assertEquals(nodes - 1, getNodeNum());
    int[] nedges2 = getEdgeNum();
    assertEquals(nedges2[1], nedges2[0]);
    assertEquals(edges[0] - 1, nedges2[0]);

  }

  @Test
  public void removeNodesTest2() {
    // Remove two nodes with internal edge
    int nodes = getNodeNum();
    final int[] edges = getEdgeNum();
    assertEquals(edges[1], edges[0]);
    List<BasicNode> l = getBasicSuper();
    RemoveNodesAction act = new RemoveNodesAction(ed, l, false);

    act.run();
    assertEquals(nodes - 2, getNodeNum());
    int[] nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] - 2, nedges[0]);

    act.undo();
    assertEquals(nodes, getNodeNum());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0], nedges[0]);
    act.redo();
    assertEquals(nodes - 2, getNodeNum());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] - 2, nedges[0]);
  }

  @Test
  public void changeNodeNameTest() {
    BasicNode n = ed.getSceneFlow().getNodes().iterator().next();
    String old = n.getName();
    ChangeNodeNameAction act = new ChangeNodeNameAction(ed, n, "MyNewName");
    act.run();
    assertEquals(n.getName(), "MyNewName");
    act.undo();
    assertEquals(n.getName(), old);
    act.redo();
    assertEquals(n.getName(), "MyNewName");
  }

  @Test
  public void changeNodeTypeTest() {
    int nodes = getNodeNum();
    int supers = 0;
    int basics = 0;
    BasicNode toChange = null;
    for (BasicNode n : ed.getSceneFlow().getNodes()) {
      if (! n.isBasic()) supers ++;
      else {
        basics++;
        if (toChange == null) toChange = n;
      }
    }
    ChangeNodeTypeAction act = new ChangeNodeTypeAction(ed, toChange);
    act.run();
    assertEquals(nodes, getNodeNum());
    int n_basics = 0;
    int n_supers = 0;
    for (BasicNode nn : ed.getSceneFlow().getNodes()) {
      if (! nn.isBasic()) n_supers ++;
      else n_basics++;
    }
    assertEquals(supers + 1, n_supers);
    assertEquals(basics - 1, n_basics);
    act.undo();
    assertEquals(nodes, getNodeNum());
    n_supers = n_basics = 0;
    for (BasicNode nn : ed.getSceneFlow().getNodes()) {
      if (! nn.isBasic()) n_supers ++;
      else n_basics++;
    }
    assertEquals(supers, n_supers);
    assertEquals(basics, n_basics);
    act.redo();
    assertEquals(nodes, getNodeNum());
    n_basics = 0;
    n_supers = 0;
    for (BasicNode nn : ed.getSceneFlow().getNodes()) {
      if (! nn.isBasic()) n_supers ++;
      else n_basics++;
    }
    assertEquals(supers + 1, n_supers);
    assertEquals(basics - 1, n_basics);
  }

  @Test
  public void editContentTest() throws BadLocationException {
    BasicNode toChange = null; // select the only supernode
    for (BasicNode n : ed.getSceneFlow().getNodes())
      if (n.isBasic() && n.getCode() != null
          && !n.getCode().toString().isEmpty()) { toChange = n; break; }
    String before = toChange.getCode().toString();
    ObserverDocument doc = new ObserverDocument(toChange);
    doc.replace(doc.getLength() - 3 , 1, "bla", null);
    EditContentAction act = new EditContentAction(ed, doc);
    act.run();
    String after = before.substring(0, before.length() - 3)
        + "bla" + before.substring(before.length() - 2);
    assertEquals(after, toChange.getCode().toString());

    act.undo();
    assertEquals(before, toChange.getCode().toString());
    act.redo();
    assertEquals(after, toChange.getCode().toString());
  }

  @Test
  public void copyPasteNodesTest() {
    // copy two nodes with internal edge
    int nodes = getNodeNum();
    final int[] edges = getEdgeNum();
    assertEquals(edges[1], edges[0]);
    List<BasicNode> l = getBasicSuper();
    CopyNodesAction act = new CopyNodesAction(ed, l);
    act.actionPerformed(new ActionEvent(1, 1, "1"));
    assertFalse(ed.mClipboard.isEmpty());

    PasteNodesAction paste = new PasteNodesAction(ed, new Position(300, 300));
    paste.run();

    assertEquals(nodes + l.size(), getNodeNum());
    int[] nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] + 1, nedges[0]);

    paste.undo();
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0], nedges[0]);

    paste.redo();
    assertEquals(nodes + l.size(), getNodeNum());
    nedges = getEdgeNum();
    assertEquals(nedges[1], nedges[0]);
    assertEquals(edges[0] + 1, nedges[0]);
  }

  @Test
  public void moveNodesTest() {
    int offset = 17;
    List<BasicNode> l = getBasics();
    Map<BasicNode, Position> orig = new IdentityHashMap<>();
    Map<BasicNode, Position> moved = new IdentityHashMap<>();
    for (BasicNode n : l) {
      orig.put(n, n.getPosition().deepCopy());
      Position newPos = n.getPosition().deepCopy();
      newPos.translate(offset, offset);
      moved.put(n, newPos);
    }

    MoveNodesAction act = new MoveNodesAction(ed, orig, moved);
    act.run();
    for (BasicNode n : l) {
      assertEquals(moved.get(n), n.getPosition());
    }

    act.undo();
    for (BasicNode n : l) {
      assertEquals(orig.get(n), n.getPosition());
    }

    act.redo();
    for (BasicNode n : l) {
      assertEquals(moved.get(n), n.getPosition());
    }
  }

  @Test
  public void toggleStartNodeTest() {
    List<BasicNode> l = getBasics();
    BasicNode n = l.get(0); BasicNode s = l.get(1);
    assertFalse(n.isStartNode());
    assertTrue(s.isStartNode());
    ToggleStartNodeAction act = new ToggleStartNodeAction(ed, n);
    act.run();

    assertTrue(n.isStartNode());
    assertFalse(s.isStartNode());

    act.undo();
    assertFalse(n.isStartNode());
    assertTrue(s.isStartNode());

    act.redo();
    assertTrue(n.isStartNode());
    assertFalse(s.isStartNode());
  }
}
