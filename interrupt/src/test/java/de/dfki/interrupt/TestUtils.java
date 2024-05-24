package de.dfki.interrupt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.lt.tr.dialogue.cplan.DagNode;
import de.dfki.mlt.rudimant.agent.Behaviour;
import de.dfki.mlt.rudimant.agent.nlp.DialogueAct;

public class TestUtils {

  public static long MAX_WAIT_TIME = 10000;

  public static TestUtils tu = null;

  protected StubClient client;

  TestListener l;

  Deque<DialogueAct> dialActs = new ArrayDeque<>();

  public class TestAgent extends interrupt {
    @Override
    protected Behaviour createBehaviour(int delay, DialogueAct da) {
      dialActs.add(da);
      return super.createBehaviour(delay, da);
    }
  }

  public class TestClient extends StubClient {
    @Override
    public void init(File configDir, Map<String, Object> configs)
        throws IOException, WrongFormatException {
      //RdfProxy proxy = startClient(configDir, configs);
      _agent = new TestAgent();
      _agent.init(configDir, configs);
      _agent.setCommunicationHub(this);
    }

    @Override
    public void sendBehaviour(Behaviour b) {
      l.bevs.add(b);
    }

    @Override
    protected void runReceiveSendCycle() {
      try {
        super.runReceiveSendCycle();
      } catch (Throwable ex) {
        l.duringRun = ex;
      }
    }
  }

  public class TestListener implements Listener<Behaviour> {
    List<Behaviour> bevs = new ArrayList<>();

    Throwable duringRun = null;

    public void reset() {
      bevs.clear();
      duringRun = null;
    }

    @Override
    public void listen(Behaviour q) {
      l.bevs.add(q);
    }
  }

  protected boolean waitForResult(long maxTime) throws InterruptedException {
    long now = System.currentTimeMillis();
    while (l.bevs.isEmpty() && System.currentTimeMillis() - now < maxTime) {
      Thread.sleep(50);
    }
    return l.bevs.isEmpty();
  }

  //
  public boolean containsAll(String s, String... keywords) {
    for (String word : keywords) {
      if (!s.toLowerCase().contains(word.toLowerCase())) {
        return false;
      }
    }
    return true;
  }

  private boolean containsAll(String... expected) {
    for (Behaviour b : l.bevs) {
      String input = b.getText().toLowerCase().replaceAll("\n", " ");
      if (input.contains(expected[0].toLowerCase())) {
        if (containsAll(input, expected)) {
          return true;
        }
      }
    }
    return false;
  }

  public void checkReactionOnInput(String input, String... expected)
      throws InterruptedException {
    if (tu != null && this != tu) {
      tu.checkReactionOnInput(input, expected);
      return;
    }
    synchronized (client) {
      try {
        l.reset();
        client.getAgent().lastDAprocessed();
        client.getAgent().clearBehavioursAndProposals();
        client.sendEvent(input);
        assertFalse("No reaction on " + input, waitForResult(MAX_WAIT_TIME));
        boolean result = containsAll(expected);
        assertTrue("Wrong reaction: " + l.bevs.get(0).getText(), result);
      } finally {
        if (l.duringRun != null) {
          l.duringRun.printStackTrace();
        }
        l.reset();
        client.getAgent().lastDAprocessed();
        client.getAgent().clearBehavioursAndProposals();
      }
    }
  }

  public void checkReactionOnIntent(String input, String... expected)
      throws InterruptedException {
    if (tu != null && this != tu) {
      tu.checkReactionOnIntent(input, expected);
      return;
    }
    synchronized (client) {
      try {
        l.reset();
        client.getAgent().lastDAprocessed();
        client.getAgent().clearBehavioursAndProposals();
        client.sendEvent(new DialogueAct(DagNode.parseLfString(input)));
        assertFalse("No reaction on " + input, waitForResult(MAX_WAIT_TIME));
        List<Behaviour> results = new ArrayList<>(l.bevs);
        for (Behaviour b : results) {
          client.getAgent().setBehaviourFinished(b.getId());
        }
        boolean result = containsAll(expected);
        StringBuilder s = null;
        if (!result) {
          s = new StringBuilder();
          for (Behaviour b : results)
            s.append(b.getText()).append(',').append(b.getMotion()).append("|");
        }
        assertTrue("Wrong reaction: " + (s == null ? "" : s.toString()), result);
      } finally {
        if (l.duringRun != null) {
          l.duringRun.printStackTrace();
        }
        l.reset();
        client.getAgent().lastDAprocessed();
        client.getAgent().clearBehavioursAndProposals();
      }
    }
  }

  // @Before
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void init(String config_name) {
    try {
      l = new TestListener();
      client = new TestClient();
      DagNode.reset();
      File confDir = new File(".");
      String confName = config_name;
      Map configs = Main.readConfig(confName);
      client.init(confDir, configs);
      client.registerBehaviourListener(l);
      client.startListening();
      // l.reset();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  // @After
  public void shutdown() {
    client.shutdown();
  }

  //@BeforeClass
  public static void start(String config_name) {
    tu = new TestUtils();
    tu.init(config_name);
  }

  //@AfterClass
  public static void end() {
    tu.shutdown();
    tu = null;
  }

  protected void sendSignal(String s) {
    client.sendEvent(s);
  }

  boolean waitForDA(long maxTime) {
    long now = System.currentTimeMillis();
    while (dialActs.isEmpty() && System.currentTimeMillis() - now < maxTime) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        return false;
      }
    }
    return dialActs.isEmpty();
  }

  int waitForDA(String exp, long maxtime) {
    if (! waitForDA(maxtime)) {
      DialogueAct expected = new DialogueAct(exp);
      // TODO: THIS MIGHT REQUIRE MORE LENIENT TREATMENT
      boolean result = expected.subsumes(dialActs.peek());
      if (result) dialActs.poll();
      return result ? 1 : 2;
    }
    return 0;
  }

  protected int waitForDA(String darep) {
    client._agent.logger.info("Wait for DA {}", darep);
    return waitForDA(darep, MAX_WAIT_TIME);
  }

  protected void sendDA(String darep) {
    DialogueAct toSend = new DialogueAct(darep);
    client.sendEvent(toSend);
  }

  protected int eatAwayDA(String darep) {
    return waitForDA(darep);
  }

  protected void waitNow(long maxTime) {
    long now = System.currentTimeMillis();
    while (dialActs.isEmpty() && System.currentTimeMillis() - now < maxTime) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
  }
}