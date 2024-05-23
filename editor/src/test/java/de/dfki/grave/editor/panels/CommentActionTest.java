package de.dfki.grave.editor.panels;

import static org.junit.Assert.*;

import java.awt.Point;
import java.io.File;

import org.junit.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.*;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.model.flow.*;

public class CommentActionTest {

  private static String RESOURCE_DIR = "src/test/resources";

  private ProjectEditor ed;
  private WorkSpace ws;

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

  void commentAfterCreate(CommentBadge res, int cmtModels, int cmtViews) {
    assertEquals(cmtViews + 1, ws.getComments().values().size());
    assertEquals(cmtModels + 1, ed.getSceneFlow().getCommentList().size());
    assertNotNull(res);
    Comment view = ws.getComments().get(res);
    assertNotNull(view);
    assertEquals(ws.toModelBoundary(view.getBounds()), res.getBoundary());
  }

  @Test
  public void createCommentTest() {
    int cmtModels = ed.getSceneFlow().getCommentList().size();
    int cmtViews = ws.getComments().values().size();
    assertEquals(cmtModels, cmtViews);
    CommentBadge res = ws.createComment(new Point(200,200));
    CreateCommentAction act = new CreateCommentAction(ed, res);
    act.run();
    commentAfterCreate(res, cmtModels, cmtViews);

    act.undo();
    assertEquals(cmtViews, ws.getComments().values().size());
    assertEquals(cmtModels, ed.getSceneFlow().getCommentList().size());
    assertNull(ws.getComments().get(res));
    assertFalse(ed.getSceneFlow().getCommentList().contains(res));

    act.redo();
    commentAfterCreate(res, cmtModels, cmtViews);

  }

  void commentAfterRemove(CommentBadge res, int cmtModels, int cmtViews) {
    assertEquals(cmtModels - 1, ed.getSceneFlow().getCommentList().size());
    assertEquals(cmtViews - 1, ws.getComments().values().size());
    assertNull(ws.getComments().get(res));
    assertFalse(ed.getSceneFlow().getCommentList().contains(res));
  }

  @Test
  public void removeCommentTest() {
    CommentBadge res = ws.createComment(new Point(200,200));
    new CreateCommentAction(ed, res).run();
    int cmtModels = ed.getSceneFlow().getCommentList().size();
    int cmtViews = ws.getComments().values().size();
    RemoveCommentsAction act = new RemoveCommentsAction(ed, res);
    act.run();
    commentAfterRemove(res, cmtModels, cmtViews);

    act.undo();
    assertEquals(cmtViews, ws.getComments().values().size());
    assertEquals(cmtModels, ed.getSceneFlow().getCommentList().size());
    assertNotNull(ws.getComments().get(res));
    assertTrue(ed.getSceneFlow().getCommentList().contains(res));

    act.redo();
    commentAfterRemove(res, cmtModels, cmtViews);

  }

  void commentAfterMove(CommentBadge res, Boundary b) {
    assertEquals(res.getBoundary(), b);
    Comment view = ws.getComments().get(res);
    assertEquals(ws.toModelBoundary(view.getBounds()), b);
  }

  @Test
  public void moveCommentTest() {
    CommentBadge res = ws.createComment(new Point(200,200));
    new CreateCommentAction(ed, res).run();
    // Move has already happened! Pass the *old* location to the action
    Boundary old = res.getBoundary();
    Boundary b = new Boundary(150, 150, 150, 150);
    res.setBoundary(b);
    MoveCommentAction act = new MoveCommentAction(ed, res, old);
    act.run();
    commentAfterMove(res, b);

    act.undo();
    assertEquals(res.getBoundary(), old);
    Comment view = ws.getComments().get(res);
    assertEquals(ws.toModelBoundary(view.getBounds()), old);

    act.redo();
    commentAfterMove(res, b);
  }
}
