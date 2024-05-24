package de.dfki.interrupt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestInterrupt extends TestUtils {
  interrupt agent;

  @Before
  public void fireUp() {
    init("config.yml");
    agent = client._agent;
    agent.resetLogging();
    MAX_WAIT_TIME = 5000000;
  }

  @After
  public void shutDown() { shutdown(); }

  @Test
  public void test() {
    waitNow(1000);
    assertEquals(6, agent.i);
    Integer[] exp = { 1, 2, 3 };
    assertEquals(3, agent.l.size());
    assertArrayEquals(exp, agent.l.toArray(new Integer[3]));
  }

}
