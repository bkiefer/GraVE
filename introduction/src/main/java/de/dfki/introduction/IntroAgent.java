package de.dfki.introduction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import de.dfki.lt.hfc.db.HfcDbHandler;
import de.dfki.lt.hfc.db.QueryResult;
import de.dfki.lt.hfc.db.rdfProxy.Rdf;
import de.dfki.lt.hfc.db.rdfProxy.RdfProxy;
import de.dfki.mlt.rudimant.agent.Agent;
import de.dfki.mlt.rudimant.agent.Behaviour;
import de.dfki.mlt.rudimant.agent.nlp.DialogueAct;

public abstract class IntroAgent extends Agent implements Constants {

  public Rdf user;
  public Rdf robot;

  public enum SE { startIntro , askName, askHobby, askColor, askAge, introFinished, askPlay};
  public enum Signal { start, stop, continuation, shutDown };
  public enum StatE { started, cont, stopped, idle, shuttingDown };

  private HfcDbHandler handler;
  //private HfcDbHandler server;
  private RdfProxy proxy;

  String eyeColor = "blue";
  Deque<Signal> signals = new ArrayDeque<>();

  private void startClient(File configDir, Map<String, Object> configs)
      throws IOException {
    String ontoFileName = (String) configs.get(CFG_ONTOLOGY_FILE);
    if (ontoFileName == null) {
      throw new IOException("Ontology file is missing.");
    }
    //server = new HfcDbHandler(new File(configDir, ontoFileName).getPath());
    handler = new HfcDbHandler(ontoFileName);
    proxy = new RdfProxy(handler);
    handler.registerStreamingClient(proxy);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void init(File configDir, Map configs) throws IOException {
    startClient(configDir, configs);
    robot = proxy.getRdf("<top:robot1>");
    user = proxy.getRdf("<top:user_english_32>");
    String language = user.getString("<dom:language>");
    super.init(configDir, language, proxy, configs, "intro");
    logAllRules();
    //ruleLogger.filterUnchangedRules = false;
  }

  @Override
  public void shutdown() {
    handler.shutdown();
    //if (server != null) server.shutdown();
    super.shutdown();
  }

  @Override
  protected Behaviour createBehaviour(int delay, DialogueAct da) {
    System.out.println("Returned DA: " + da.toString());
    return super.createBehaviour(delay, da);
  }

  public Rdf getSuper(String s) {
    QueryResult r = proxy.selectQuery(
        "select ?super where ?super <autm:name> \"{}\" ?_", s);
    if (r.table.rows.isEmpty()) {
      return null;
    }
    return toRdf(r.table.rows.get(0).get(0));
  }

  /** TODO: FILL STUB */
  public Signal getSystemSignal() {
    return signals.poll();
  }

  /** TODO: FILL STUB */
  public boolean setEyeColor() {
    eyeColor = "red";
    return eyeColor == "red";
  }

  /** TODO: FILL STUB */
  public void sysCall(String what, String activity) {
    System.out.println("sysCall("+what+", "+activity+")");
  }
}