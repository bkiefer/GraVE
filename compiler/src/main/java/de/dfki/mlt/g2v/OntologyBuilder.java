package de.dfki.mlt.g2v;

import static de.dfki.lt.hfc.NamespaceManager.RDF;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.SceneFlow;
import de.dfki.grave.model.SuperNode;
import de.dfki.lt.hfc.types.XsdInt;

public class OntologyBuilder {
  private static Logger logger = LoggerFactory.getLogger(OntologyBuilder.class);
  public static final String NAMEDINDIVIDUAL_URI =
      " <http://www.w3.org/2002/07/owl#NamedIndividual>.";
  public static final String BASICNODE_URI =
      " <http://www.dfki.de/beki/ontologies/2023/7/automaton#Basicnode>.";
  public static final String SUPERNODE_URI =
      " <http://www.dfki.de/beki/ontologies/2023/7/automaton#Supernode>.";
  public static final String NAME_URI =
      " <http://www.dfki.de/beki/ontologies/2023/7/automaton#name> ";
  public static final String ID_URI =
      " <http://www.dfki.de/beki/ontologies/2023/7/automaton#id> ";
  public static final String STATE_URI =
      " <http://www.dfki.de/beki/ontologies/2023/7/automaton#state> ";
  public static final String PARENT_URI =
      " <http://www.dfki.de/beki/ontologies/2023/7/automaton#parent> ";
  public static final String TYPE_URI = " <" + RDF.getLong() + "type> ";


  private SceneFlow root;
  private String uriPrefix;
  private Map<BasicNode, Integer> node2int;
  private Map<BasicNode, String> node2name;
  private Map<String, BasicNode> name2node;

  private File getOntoFile(Path ontoPath) {
    return new File(ontoPath.toFile(), root.getName() + ".nt");
  }

  /**
   * @return a unique name
   */
  String getNodeName(BasicNode node) {
    if (! node2name.containsKey(node)) {
      String name = node.getName().replaceAll(" +", "_");
      if (node.getParentNode() != null) {
        name = node.getId() + "_" + name;
      }
      name2node.put(name, node);
      node2name.put(node, name);
    }
    return node2name.get(node);
  }

  /** s isa Supernode, s isa NamedIndividual, s.name = getNodeName(s) */
  private void writeNodeStatements(Writer out, BasicNode n) throws IOException {
    String s_uri = getNodeUri(n);
    out.append(s_uri).append(TYPE_URI).append(
        n instanceof SuperNode ? SUPERNODE_URI : BASICNODE_URI)
    .append(System.lineSeparator());
    out.append(s_uri).append(TYPE_URI).append(NAMEDINDIVIDUAL_URI)
    .append(System.lineSeparator());
    out.append(s_uri).append(NAME_URI).append('"').append(getNodeName(n))
    .append("\".").append(System.lineSeparator());
    int id = node2int.size();
    out.append(s_uri).append(ID_URI).append(new XsdInt(id).toString())
    .append(System.lineSeparator());
    SuperNode parent = n.getParentNode();
    if (parent != null) {
      out.append(s_uri).append(PARENT_URI).append(getNodeUri(parent))
      .append(System.lineSeparator());
    }
    node2int.put(n, id);
  }

  private void writeStatements(Writer out, SuperNode s) throws IOException {
    writeNodeStatements(out, s);
    for (BasicNode n : s.getNodes()) {
      if (n instanceof SuperNode) {
        writeStatements(out, (SuperNode) n);
      } else {
        writeNodeStatements(out, n);
      }
    }
  }

  public void writeOntology(SceneFlow flow, Path ontologyPath) {
    root = flow;
    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    uriPrefix = "<http://www.dfki.de/" + root.getName() +"#";
    // TODO: MAYBE LATER, WHEN THE CONFIG FILE IS ALSO GENERATED
    // + "/" + formatter.format(LocalTime.now()) +"#";
    node2int = new LinkedHashMap<>();
    node2name = new HashMap<>();
    name2node = new HashMap<>();
    try (FileWriter out = new FileWriter(getOntoFile(ontologyPath))) {
      writeStatements(out, root);
    } catch (IOException e) {
      logger.error("Error writing Ontology for {}: {}", root.getName(), e);
    }
  }

  public String getUriPrefix() {
    return uriPrefix;
  }

  public String getNodeUri(BasicNode s) {
    return uriPrefix + getNodeName(s) + ">";
  }

  public int getId(BasicNode n) {
    return node2int.get(n);
  }
}
