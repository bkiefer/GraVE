package de.dfki.interrupt;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import de.dfki.lt.hfc.db.HfcDbHandler;
import de.dfki.lt.hfc.db.rdfProxy.RdfProxy;
import de.dfki.mlt.rudimant.agent.Agent;


public abstract class InterAgent extends Agent {
  public static final String CFG_ONTOLOGY_FILE = "ontologyFile";

  private HfcDbHandler handler;
  //private HfcDbHandler server;
  private RdfProxy proxy;

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
    //robot = proxy.getRdf("<top:robot1>");
    //user = proxy.getRdf("<top:user_english_32>");
    //String language = user.getString("<dom:language>");
    super.init(configDir, "en_US", proxy, configs, "intro");
    logAllRules();
    //ruleLogger.filterUnchangedRules = false;
  }

}
