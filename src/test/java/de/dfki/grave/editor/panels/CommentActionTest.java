package de.dfki.grave.editor.panels;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.*;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.*;
import de.dfki.grave.editor.action.*;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.editor.panels.WorkSpaceMouseHandler;
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
  
  @Test
  public void createCommentTest() {
    int cmtModels = ed.getSceneFlow().getCommentList().size();
    int cmtViews = ws.getComments().values().size();
    assertEquals(cmtModels, cmtViews);
    CreateCommentAction act = new CreateCommentAction(ed, new Position(200, 200));
    act.run();
    assertEquals(cmtViews + 1, ws.getComments().values().size());
    assertEquals(cmtModels + 1, ed.getSceneFlow().getCommentList().size());
    CommentBadge res = null;
    for (CommentBadge c : ed.getSceneFlow().getCommentList()) {
      if (c.getBoundary().getXPos() == 200 && c.getBoundary().getYPos() == 200) {
        res = c;
        break;
      }
    }
    assertNotNull(res);
    Comment view = ws.getComments().get(res);
    assertNotNull(view);
    assertEquals(ws.toModelBoundary(view.getBounds()), res.getBoundary());
    act.undo();
    assertEquals(cmtViews, ws.getComments().values().size());
    assertEquals(cmtModels, ed.getSceneFlow().getCommentList().size());
    assertNull(ws.getComments().get(res));
    assertFalse(ed.getSceneFlow().getCommentList().contains(res));
  }
  
  @Test
  public void removeCommentTest() {
    new CreateCommentAction(ed, new Position(200, 200)).run();
    CommentBadge res = null;
    for (CommentBadge c : ed.getSceneFlow().getCommentList()) {
      if (c.getBoundary().getXPos() == 200 && c.getBoundary().getYPos() == 200) {
        res = c;
        break;
      }
    }
    int cmtModels = ed.getSceneFlow().getCommentList().size();
    int cmtViews = ws.getComments().values().size();
    RemoveCommentsAction act = new RemoveCommentsAction(ed, res);
    act.run();
    assertEquals(cmtModels - 1, ed.getSceneFlow().getCommentList().size());
    assertEquals(cmtViews - 1, ws.getComments().values().size());
    assertNull(ws.getComments().get(res));
    assertFalse(ed.getSceneFlow().getCommentList().contains(res));
    act.undo();
    assertEquals(cmtViews, ws.getComments().values().size());
    assertEquals(cmtModels, ed.getSceneFlow().getCommentList().size());
    assertNotNull(ws.getComments().get(res));
    assertTrue(ed.getSceneFlow().getCommentList().contains(res));
  }
  
  @Test
  public void moveCommentTest() {
    new CreateCommentAction(ed, new Position(200, 200)).run();
    CommentBadge res = null;
    for (CommentBadge c : ed.getSceneFlow().getCommentList()) {
      if (c.getBoundary().getXPos() == 200 && c.getBoundary().getYPos() == 200) {
        res = c;
        break;
      }
    }
    // Move has already happened! Pass the *old* location to the action
    Boundary old = res.getBoundary();
    Boundary b = new Boundary(150, 150, 150, 150);
    res.setBoundary(b);
    MoveCommentAction act = new MoveCommentAction(ed, res, old);
    act.run();
    assertEquals(res.getBoundary(), b);
    Comment view = ws.getComments().get(res);
    assertEquals(ws.toModelBoundary(view.getBounds()), b);
    act.undo();
    assertEquals(res.getBoundary(), old);
    view = ws.getComments().get(res);
    assertEquals(ws.toModelBoundary(view.getBounds()), old);
  }
}
