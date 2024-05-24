package de.dfki.introduction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.dfki.introduction.IntroAgent.Signal;
import de.dfki.introduction.IntroAgent.StatE;

public class TestIntroduction extends TestUtils {
  @Before
  public void fireUp() {
    init();
    //client._agent.resetLogging();
  }

  @After
  public void shutDown() { shutdown(); }

  @Test
  public void test() throws InterruptedException {
    sendSignal("%start");
    assertEquals(1, waitForDA("salutation(greeting)"));
    assertEquals(1, waitForDA("request(name)"));
    sendDA("inform(givenname, value=Bernd)");
    int res = waitForDA("acknowledgement(name)", 2000);
    int hits = 0;
    int tries = 0;
    int maxTries = 2;
    while (hits < 3) {
      res = waitForDA("request(color)");
      if (res == 1) {
        ++hits;
        tries = 0;
        client._agent.logger.error("Hit {}", hits);
        sendDA("inform(color, value=yellow)");
        waitForDA("acknowledgement(color)");
        eatAwayDA("acknowledgement(eyecolorset)");
      } else if (res == 2) {
        res = waitForDA("request(hobby)");
        if (res == 1) {
          ++hits;
          tries = 0;
          client._agent.logger.error("Hit {}", hits);
          sendDA("inform(hobby, value=zocken)");
          waitForDA("acknowledgement(hobby)");
        } else if (res == 2) {
          res = waitForDA("request(age)");
          if (res == 1) {
            ++hits;
            tries = 0;
            client._agent.logger.error("Hit {}", hits);
            sendDA("inform(age, value=69)");
            waitForDA("acknowledgement(age)");
          } else {
            if (++tries > maxTries)
              assertTrue("Wrong or missing response", false);
          }
        } else {
          if (++tries > maxTries)
            assertTrue("Missing response", false);
        }
      } else {
        if (++tries > maxTries)
          assertTrue("Missing response", false);
      }
    }

    assertEquals(1, waitForDA("request(play)"));
    sendDA("provide(yes)");
    waitNow(1000);
    assertEquals(StatE.idle, client._agent.status);
    sendSignal("%shutDown");
    //waitNow(2000);
    //assertTrue(checkStatus(StatE.stopped));
    //checkSysCall("flowended", "introduction");
    //sendSignal(Signal.stop);
    /*
    sendSignal("%start");
    waitForDA("salutation(greeting)");
    waitForDA("request(name)");
    sendDA("@inform(givenname, value=Bernd)");
    oneOfOrNil(waitForDA("acknowledge(name)"));

    allOf(
        seq(
            waitForDA("request(color)");
            sendDA("@inform(color, value=yellow)");
            oneOfOrNil(waitForDA("acknowledge(color)"));
            eatAwayDA("acknowledgement(eyecolorset");
            );
        seq(
            waitForDA("request(hobby)");
            sendDA("@inform(hobby, value=zocken)");
            oneOfOrNil(waitForDA("acknowledge(color)"));
            );
        seq(
            waitForDA("request(age)");
            sendDA("@inform(age, value=69)");
            oneOfOrNil(waitForDA("acknowledge(color)"));
            ););

    waitForDA("request(play)");
    sendDA("@provide(yes)");
    checkStatus(StatE.stopped);
    checkSysCall("flowended", "introduction");
    //sendSignal("%shutDown");
    */
  }

}
