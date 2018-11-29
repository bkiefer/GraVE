/*
 * SceneflowEditor - GridManager
 */
package de.dfki.vsm.editor.util;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpacePanel;
import de.dfki.vsm.editor.util.grid.GridConstants;
import de.dfki.vsm.editor.util.grid.GridRectangle;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.project.EditorConfig;

/**
 * @author Patrick
 * This class manages the node placement on the workspace.
 * Additional methods are provided for an intelligent placement
 * of nodes
 *
 * BK: I think this class should consider three dimensions:
 * - node dimension
 * - grid size (as a factor of node dimension: max(height, width))
 * - zoom factor
 *
 * What's the purpose of this class?
 * a) return the "closest" free node position in the grid
 *    1. on the grid (snap to grid active)
 *    2. not covering another node (inactive)
 * b) provide routing hints for edges, either new or during straighten/normalize
 *
 * Positions of GUI elements should be mapped to abstract positions, so that
 * all positions and dimensions are divided by zoomFactor
 */
public class GridManager {

  private HashSet<Point> mPlacedNodes = new HashSet<>();

  // Subgrid for A* algorithm
  private GridRectangle[][] mTransitionArea = null;
  //private GridRectangle[][] mTempTransitions = null;
  private boolean isSubgridEstablished = false;
  //private boolean firstRoundOfComputeWasFinished = false;
  private int height = 0;
  private int width = 0;
  private int columns = 0;  // x
  private int rows = 0; // y
  private final WorkSpacePanel mWorkSpacePanel;
  private ArrayList<Rectangle> mNodeAreas;
  private boolean isDebug;

  public GridManager(WorkSpacePanel ws) {
    mWorkSpacePanel = ws;
    EditorConfig config = mWorkSpacePanel.getEditorConfig();
    isDebug = config.sSHOW_SMART_PATH_DEBUG;
    compute();
  }

  // TODO: MUST BE ADAPTED, THE SIZE OF THE COMMAND BADGES IS NOT TAKEN INTO
  // ACCOUNT, WHICH LEADS TO ERRORS
  private Dimension calculateWorkArea(int nodeWidth, int nodeHeight) {
    int width = mWorkSpacePanel.getSize().width;
    int height = mWorkSpacePanel.getSize().height;
    for (BasicNode n : mWorkSpacePanel.getSceneFlowEditor().getSceneFlow()) {
      if (n.getPosition().getYPos() > height) {
        height = n.getPosition().getYPos() + nodeHeight;
      }
      if (n.getPosition().getXPos() > width) {
        width = n.getPosition().getXPos() + nodeWidth;
      }
    }
    return new Dimension(width, height);
  }

  // TODO: UNFORTUNATELY, THIS WORKS ONLY WITH A GRID_SCALE OF 1
  // THIS WHOLE CLASS MUST BE RE-DONE TO ACHIEVE PROPER ZOOMING / GRID SCALE
  // AND: SNAPPING NODES TO GRID IF THEY CAME WITH OTHER POSITIONS IS QUESTIONABLE
  private final void compute() {
    EditorConfig config = mWorkSpacePanel.getEditorConfig();
    config.sGRID_SCALE = 1;
    int nodeSize = Math.max(config.sNODEWIDTH, config.sNODEHEIGHT);
    int gridWidth = (int)(nodeSize * config.sGRID_SCALE);
    int halfGridWidth = gridWidth / 2;
    int offset = gridWidth / 3;

    Dimension area = calculateWorkArea(gridWidth, gridWidth);
    height = area.height;
    width = area.width;

    int cs = width / gridWidth + 1;  // x
    int rs = height / gridWidth + 1; // y
    if (cs != columns || rs != rows) {
      columns = cs; rows = rs; isSubgridEstablished = false;
    }
    mNodeAreas = new ArrayList<>();

    if (!isSubgridEstablished) {
      mTransitionArea = new GridRectangle[(columns + 1) * 2][(rows + 1) * 2];
    }

    /*
    if (! firstRoundOfComputeWasFinished) {
      mTempTransitions =
        new GridRectangle[(columns + 1) * 2][(rows + 1) * 2];
    }
    */

    for (int row = 0; row <= rows; row++) { // y
      for (int col = 0; col <= columns; col++) { // x
        int colOff = offset + (col * gridWidth);
        int rowOff = offset + (row * gridWidth);
        Rectangle r = new Rectangle(colOff, rowOff, gridWidth, gridWidth);
        mNodeAreas.add(r);

        // Initiate subgrids, have a 2x2 border around the A* rectangles
        colOff += 2;
        rowOff += 2;
        if (columns > 0 && rows > 0) {
          for (int dcol = 0; dcol < 2; ++dcol) {
            for (int drow = 0; drow < 2; ++drow) {
              if (! isSubgridEstablished) {
                GridRectangle s = new GridRectangle(
                    colOff + dcol * halfGridWidth, rowOff + drow * halfGridWidth,
                    halfGridWidth - 4, halfGridWidth - 4);
                s.setColumnIndex(row * 2 + drow);  // ??
                s.setRowIndex(col * 2 + dcol); // ??
                mTransitionArea[col * 2 + dcol][row * 2 + drow] = s;

                /* Why is that there?
                if (! firstRoundOfComputeWasFinished) {
                  GridRectangle tmp = new GridRectangle(
                    colOff + dcol * halfGridWidth, rowOff + drow * halfGridWidth,
                    halfGridWidth - 4, halfGridWidth - 4);

                  tmp.setColumnIndex(row * 2 + drow);
                  tmp.setRowIndex(col * 2 + dcol);
                  mTempTransitions[col * 2 + dcol][row * 2 + drow] = tmp;
                } else {
                  mTempTransitions[col * 2 + dcol][row * 2 + drow] =
                    mTransitionArea[col * 2 + dcol][row * 2 + drow];
                  // ?? strange
                  mTempTransitions[col * 2 + dcol][row * 2 + drow]
                    .setaStarPath(mTransitionArea[col * 2 + dcol][row * 2 + drow].isaStarPath());
                  mTempTransitions[col * 2 + dcol][row * 2 + drow]
                    .setLocation(colOff + dcol * halfGridWidth, rowOff + drow * halfGridWidth);

                  mTempTransitions[col * 2+ dcol][row * 2 + drow]
                    .setSize(halfGridWidth - 4, halfGridWidth - 4);
                }
                */
              }
            }
          }
        }
      }
    }

    if ((columns) > 0 && (rows) > 0 && (isSubgridEstablished == false)) {
      isSubgridEstablished = true;
      //firstRoundOfComputeWasFinished = true;
    }

    /*
    if (!((height == rows) && (width == columns))) {
      mTransitionArea = mTempTransitions;
      firstRoundOfComputeWasFinished = true;
    }*/
  }

  public void update() {
    EditorConfig config = mWorkSpacePanel.getEditorConfig();
    isDebug = config.sSHOW_SMART_PATH_DEBUG;
    mPlacedNodes = new HashSet<>();
    compute();
  }

  public void drawGrid(Graphics2D g2d) {
    EditorConfig config = mWorkSpacePanel.getEditorConfig();
    compute();
    if (config.sSHOWGRID) {
      g2d.setStroke(new BasicStroke(1.0f));

      for (Rectangle r : mNodeAreas) {
        // draw a litte cross
        g2d.setColor(Color.GRAY.brighter());

        // g2d.setColor(new Color(230, 230, 230, 200).darker());
        g2d.drawLine(r.x + r.width / 2 - 2, r.y + r.height / 2, r.x + r.width / 2 + 2, r.y + r.height / 2);
        g2d.drawLine(r.x + r.width / 2, r.y + r.height / 2 - 2, r.x + r.width / 2, r.y + r.height / 2 + 2);

        // draw node areas
        // g2d.drawRect(r.x, r.y, r.width, r.height);
        // g2d.drawString("" + ai, r.x + 2, r.y + 12);
      }

      if (isDebug) {
        for (GridRectangle[] r : mTransitionArea) {
          for (GridRectangle s : r) {
            // draw a litte cross
            g2d.setColor(new Color(230, 230, 230, 200));
            g2d.drawLine(s.x + s.width / 2 - 2, s.y + s.height / 2, s.x + s.width / 2 + 2,
                    s.y + s.height / 2);
            g2d.drawLine(s.x + s.width / 2, s.y + s.height / 2 - 2, s.x + s.width / 2,
                    s.y + s.height / 2 + 2);

            // draw node areas
            if (s.getWeight() > 1) {
              g2d.setColor(Color.red);
              g2d.drawString("" + s.getWeight(), s.x + 2, s.y + s.height / 2 + 6);
            }

            if (s.isaStarPath()) {
              g2d.setColor(Color.blue);
            }

            g2d.drawRect(s.x, s.y, s.width, s.height);
            g2d.drawString("" + s.getColumnIndex() + "," + s.getRowIndex(), s.x + 2, s.y + 12);
          }
        }
      }
    }
  }

  public void setDebugMode(boolean status) {
    this.isDebug = status;
    update();
  }

  public Point getNodeLocation(Point inputPoint) {
    EditorConfig config = mWorkSpacePanel.getEditorConfig();
    int nodeSize = Math.max(config.sNODEWIDTH, config.sNODEHEIGHT);
    int gridWidth = (int)(nodeSize * config.sGRID_SCALE);
    Point p = new Point(inputPoint.x + gridWidth / 2,
        inputPoint.y + gridWidth / 2);

    for (Rectangle r : mNodeAreas) {
      if (r.contains(p)) {
        p = new Point(r.x, r.y);

        break;
      }
    }

    // check if p is already in set of grid points
    if (mPlacedNodes.contains(p)) {

      // System.out.println("point already in use!");
      p = findNextFreePosition(p);
    }

    mPlacedNodes.add(p);

    return p;
  }

  public void freeGridPosition(Point p) {
    if (mPlacedNodes.contains(p)) {

      // System.out.println("point is in use - delete in occupied positions");
      mPlacedNodes.remove(p);
    }
  }

  public GridRectangle[][] getmTransitionArea() {
    return mTransitionArea;
  }

  public void setNodeWeight(Node node) {
    for (GridRectangle[] gridParent : mTransitionArea) {
      for (GridRectangle gridRectangle : gridParent) {
        if (gridRectangle.isIntersectedbyNode(node)) {
          gridRectangle.setWeight(GridConstants.NODE_WEIGHT);

//                  System.out.println("Setting weight of " +
//                          GridConstants.NODE_WEIGHT + " to Grid <" +
//                          gridRectangle.getColumnIndex() + "," +
//                          gridRectangle.getRowIndex() + ">");
        }
      }
    }
  }

  public void setEdgeWeight(Edge edge) {
    for (GridRectangle[] gridParent : mTransitionArea) {
      for (GridRectangle gridRectangle : gridParent) {
        if (gridRectangle.isIntersectByRectangle(edge)) {
          gridRectangle.setWeight(GridConstants.EDGE_WEIGHT);

//                  System.out.println("Setting weight of " +
//                          GridConstants.EDGE_WEIGHT + " to Grid <" +
//                          gridRectangle.getColumnIndex() + "," +
//                          gridRectangle.getRowIndex() + ">");
        }
      }
    }
  }

  public void resetGridWeight(Edge edge) {
    for (GridRectangle[] gridParent : mTransitionArea) {
      for (GridRectangle gridRectangle : gridParent) {
        if (gridRectangle.isIntersectByRectangle(edge)) {
          gridRectangle.setWeight(GridConstants.INITIAL_WEIGHT);

//                  System.out.println("Setting weight of " +
//                          GridConstants.INITIAL_WEIGHT + " to Grid <" +
//                          gridRectangle.getColumnIndex() + "," +
//                          gridRectangle.getRowIndex() + ">");
        }
      }
    }
  }

  public void resetGridWeight(Node node) {
    for (GridRectangle[] gridParent : mTransitionArea) {
      for (GridRectangle gridRectangle : gridParent) {
        if (gridRectangle.isIntersectedbyNode(node)) {
          gridRectangle.setWeight(GridConstants.INITIAL_WEIGHT);

//                  System.out.println("Setting weight of " +
//                          GridConstants.INITIAL_WEIGHT + " to Grid <" +
//                          gridRectangle.getColumnIndex() + "," +
//                          gridRectangle.getRowIndex() + ">");
        }
      }
    }
  }

  public void normalizeGridWeight() {
    for (GridRectangle[] gridParent : mTransitionArea) {
      for (GridRectangle gridRectangle : gridParent) {
        boolean isGridInteresected = false;

        for (Edge edge : mWorkSpacePanel.getEdges()) {
          if (gridRectangle.isIntersectByRectangle(edge)) {
            gridRectangle.setWeight(GridConstants.EDGE_WEIGHT);
            isGridInteresected = true;
          }
        }

        for (Node node : mWorkSpacePanel.getNodes()) {
          if (gridRectangle.isIntersectedbyNode(node)) {
            gridRectangle.setWeight(GridConstants.NODE_WEIGHT);
            isGridInteresected = true;
          }
        }

        if (isGridInteresected == false) {
          gridRectangle.setWeight(GridConstants.INITIAL_WEIGHT);
        }

        if (gridRectangle.isaStarPath()) {
          gridRectangle.setaStarPath(false);
        }
      }
    }
  }

  public void resetAllGridWeight() {
    for (GridRectangle[] gridParent : mTransitionArea) {
      for (GridRectangle gridRectangle : gridParent) {
        gridRectangle.setWeight(GridConstants.INITIAL_WEIGHT);
      }
    }
  }

  /**
   * This method spirals around an occupied grid point in order to find a free
   * grid position for a new or moved node. It starts looking for a free grid
   * position left to the occupied grid place, then proceeds clockwise in a spiral
   * around that place.
   * Code used from: JHolta (http://stackoverflow.com/questions/398299/looping-in-a-spiral/10607084#10607084)
   */
  private Point findNextFreePosition(Point iPoint) {
    EditorConfig config = mWorkSpacePanel.getEditorConfig();
    int nodeSize = Math.max(config.sNODEWIDTH, config.sNODEHEIGHT);
    int gridWidth = (int)(nodeSize * config.sGRID_SCALE);
    int mNodesInRow = width / gridWidth;
    int mNodesInCol = height / gridWidth;

    int x = 0,
        y = 0,
        dx = 0,
        dy = -1;
    int t = Math.min(mNodesInRow, mNodesInCol);
    int maxI = t * t;

    for (int i = 0; i < maxI; i++) {
      if ((-mNodesInCol / 2 <= x) && (x <= mNodesInCol / 2)
          && (-mNodesInRow / 2 <= y) && (y <= mNodesInRow / 2)) {
        if (i > 0) {
          if ((iPoint.x - (x * gridWidth) > 0)
                  && (iPoint.y - (y * gridWidth) > 0)) {    // check if position is not outside the workspace on the left / top
            Point p = new Point(iPoint.x - (x * gridWidth),
                    iPoint.y - (y * gridWidth));

            if (!mPlacedNodes.contains(p)) {
              return p;
            }
          }
        }
      }

      if ((x == y) || ((x < 0) && (x == -y)) || ((x > 0) && (x == 1 - y))) {
        t = dx;
        dx = -dy;
        dy = t;
      }

      x += dx;
      y += dy;
    }

    return null;
  }
}
