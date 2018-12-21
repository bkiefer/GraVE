package de.dfki.vsm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;

import org.junit.BeforeClass;
import org.junit.Test;

import de.dfki.vsm.model.flow.geom.Geom;

public class GeomTest {
  @BeforeClass
  public static void init() {
    Geom.initialize(32);
  }

  @Test
  public void testDockAngle() {
    double[] expect = { 0, Math.PI, Math.PI /2, Math.PI *1.5,
        Math.PI * .25, Math.PI * 1.25, Math.PI * .75, Math.PI * 7/4};
    for (int i = 0; i < 8; ++i) {
      assertEquals(expect[i], Geom.dockToAngle(i), "" + i) ;
    }
  }

  @Test
  public void testDockSquare() {
    int[][] expect = { { 0, 50 }, { 0, -50 }, { 50, 0 } , { -50, 0 },
    { 50, 50 }, { -50, -50 }, { 50, -50 }, { -50, 50 },
    { 21, 50 }, { -21, -50 } };
    for (int i = 0; i < expect.length; ++i) {
      assertEquals(new Point(expect[i][0], expect[i][1]),
          Geom.getDockPointSquare(i, 100), "" + i) ;
    }
  }
}
