package de.dfki.mlt.g2v;

import static de.dfki.mlt.g2v.Utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.AbstractEdge;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.GuardedEdge;
import de.dfki.grave.model.InterruptEdge;
import de.dfki.grave.model.SceneFlow;
import de.dfki.grave.model.SuperNode;


public class Generator implements AutoCloseable {
  private static Logger logger = LoggerFactory.getLogger(Main.class);

  private SuperNode automaton;
  private File outPath;
  private OntologyBuilder o;
  private CodeInfo blocks;

  private Formatter f;

  private Generator(SuperNode s, File outDir, OntologyBuilder ob, CodeInfo cb)
      throws IOException {
    if (!outDir.exists()) {
      outDir.mkdirs();
    }
    automaton = s;
    outPath = outDir;
    o = ob;
    f = new Formatter(outPath, o.getNodeName(automaton));
    cb.startNode(o.getNodeName(s));
    this.blocks = cb;
  }


  /** To implement AutoClosable */
  @Override
  public void close() throws Exception {
    f.close();
  }

  private String fetchNode(BasicNode n) {
    return "nodes[" + o.getId(n) + "]";
  }

  /* ******************** Edge code ******************* */

  /**
   * Creates the VOnDA-code fragment that imitates the functionality of the
   * {@code ConditionalEdge}.
   *
   * @return A String containing the VOnDA-code fragment that imitates the
   *         functionality of the {@code ConditionalEdge}
   */
  private <T extends AbstractEdge> void getConditionalEdgeCode(Iterable<T> edges) {
    for (AbstractEdge a : edges) {
      GuardedEdge e = (GuardedEdge)a;
      blocks.addBlock(o.getNodeName(e.getSourceNode()), f.getCurrentLineNo());
      f.ifOpen(e.getContent(), 0, 0);
      blocks.endBlock(f.getCurrentLineNo());
      getRudiEdgeCode(e);
      f.close(0, 1);
    }
  }

  /**
   * Creates the VOnDA-code fragment that imitates the functionality of the
   * {@code AbstractEdge}. For all edges except GuardedEdge (conditional).
   *
   * @return A String containing the VOnDA-code fragment that imitates the
   *         functionality of the {@code AbstractEdge} as a String
   */
  private void getRudiEdgeCode(AbstractEdge edge) {
    BasicNode source = edge.getSourceNode();
    BasicNode target = edge.getTargetNode();

    String transitionCode = "transition(";
    String thirdArg = "";
    int skip = 1;
    if (edge.isRandomEdge()) {
      transitionCode= "probabilty_transition(";
      thirdArg = ", " + edge.getContent();
    } else if (edge.isTimeoutEdge()) {
      transitionCode = "timeout_transition(";
      thirdArg = ", " + edge.getContent();
    } else if (edge.isInterruptEdge()) {
      transitionCode = "interruptive_transition(";
    } else {
      skip = 0; // don't generate new line for conditional edges
    }
    // add source and target node parameters, and possibly the third arg
    transitionCode += fetchNode(source) + ", " + fetchNode(target) +
        thirdArg + ")";
    f.statement(transitionCode, 0, skip);
  }

  /* ******************** Basic Node code ******************* */

  /** Helper function to initially activate a new node on entering it */
  private void initNode(String nodeName) {
    f.statement("initNode(" + nodeName + ")", 0, 0);
  }

  /**
   * @param edges A set of edges
   * @return A string containing the VOnDA-code fragment that imitates the
   *         functionality of the set of edges
   */
  private <T extends AbstractEdge> void getEdgeCode(Iterable<T> edges) {
    for (AbstractEdge e : edges) {
      getRudiEdgeCode(e);
    }
  }

  /** This is identical for SuperNode and Basicnode */
  private void getCommonEdgesCode(BasicNode node) {
    getEdgeCode(getTimeoutEdges(node));
    getConditionalEdgeCode(getConditionalEdges(node));
    getEdgeCode(getEpsilonEdges(node));
    getEdgeCode(getProbabilityEdges(node));

    boolean hasForking = false;
    for (AbstractEdge e : getForkingEdges(node)) {
      // we can not use the usual transition code here since it's a one to many
      // transition
      hasForking = true;
      BasicNode target = e.getTargetNode();
      initNode(fetchNode(target));
    }

    if (hasForking) {
      // we moved on to multiple other nodes, so we're not active anymore
      f.statement("setInactive(node)", 0, 0);
    }
  }

  /**
   * Creates the VOnDA-code fragment that imitates the functionality of the
   * {@code BasicNode} (NOT SuperNode)
   *
   * @return A String containing the VOnDA-code that imitates the functionality
   *         of the {@code Node}
   *
   */
  private void getBasicNodeCode(BasicNode node) {
    assert ! (node instanceof SuperNode);
    String name = o.getNodeName(node);

    f.ruleLabel(name, 0, 0);
    f.ifOpen("isActive(" + fetchNode(node) + ")", 0, 0);
    f.statement("node = " + fetchNode(node), 0, 0);

    if (!node.getContent().isEmpty()) {
      f.ifOpen("needsInit(node)", 0, 0);
      blocks.addBlock(o.getNodeName(node), f.getCurrentLineNo());
      f.raw(node.getContent(), 0, 0);
      blocks.endBlock(f.getCurrentLineNo());
      f.statement("setIsInitiated(node)", 0, 0);
      f.close(0, 1);
    }

    getCommonEdgesCode(node);

    // is this an end node?
    if (node.processCanDieHere()) {
      f.statement("super_out_transition(node, "
          + fetchNode(node.getParentNode()) + ")", 0, 0);
      //+ fetchNode(o.getOutNode(node.getParentNode())) + ")", 0, 0);    }
    }
    f.close(0, 1); // end basic node rule
  }

  /* ******************** Super Node code ******************* */

  private void getDefinitions(SuperNode s) {
    if (! s.getDefinitions().isBlank()) {
      f.raw(s.getDefinitions(), 0, 0);
    }
  }


  /**
   * Creates the VOnDA-code fragment that sets up the {@code SceneMakerAutomaton}.
   * @return A String containing the VOnDA-code fragment that sets up the {@code SceneMakerAutomaton}
   */
  private void getSceneFlowSetupCode(SceneFlow m) {
    blocks.addBlock(o.getNodeName(m), f.getCurrentLineNo());
    getDefinitions(m);
    blocks.endBlock(f.getCurrentLineNo());

    // add global transition etc. automaton functions
    f.transferResource("functions.rudi");

    blocks.addBlock(o.getNodeName(m), f.getCurrentLineNo());
    f.raw(m.getContent(), 0, 1);
    blocks.endBlock(f.getCurrentLineNo());

    f.defOpen("void start(){", 0, 0);
    f.statement("init_nodes()", 0, 0);
    f.ifOpen("!" + fetchNode(m) + ".children", 0, 0);

    f.statement(fetchNode(m) + ".children += " + o.getId(m), 0, 0);
    // now initialise the start node of the top-level node
    BasicNode start = m.getStartNode();
    initNode(fetchNode(start));

    f.close(0, 0); // if
    f.close(0, 1); // def
  }

  /**
   * Creates the VOnDA-code fragment that initializes the variables belonging to
   * this {@code Supernode}. THIS IS NOT FOR THE SCENEFLOW (TOPMOST NODE)
   *
   * @return A String containing the VOnDA-code fragment that initializes the
   *         variables of this {@code Supernode}.
   */
  private void getSuperSetupCode(SuperNode s) {
    blocks.addBlock(o.getNodeName(s), f.getCurrentLineNo());
    getDefinitions(s);
    blocks.endBlock(f.getCurrentLineNo());

    // first of all, print the code fragment that is associated with
    // this supernode, if any
    f.ruleLabel("setup_" + o.getNodeName(s), 1, 0);
    f.ifOpen("isActive(" + fetchNode(s) + ")", 0, 0);

    f.ifOpen("needsInit(" + fetchNode(s) + ")", 0, 0);
    // This is executed once we enter the super node, then, control is
    // passed to the inner nodes
    blocks.addBlock(o.getNodeName(s), f.getCurrentLineNo());
    f.raw(s.getContent(), 0, 0);
    blocks.endBlock(f.getCurrentLineNo());

    BasicNode start = s.getStartNode();
    // TODO: is this right, or is the `code' executed at the start node code
    //f.formatRaw(start.getContent(), 0, 0);
    initNode(fetchNode(start));
    f.statement("setIsInitiated(" + fetchNode(s) + ")", 0, 0);
    f.close(0, 0); // end needsInit

    // this must be executed before we set the current supernode inactive!
    getSuperOutCode(s);

    // check if SuperNode became inactive because running out of children
    f.ifOpen("! " + fetchNode(s) + ".children", 0, 0);
    f.statement("setInactive(" + fetchNode(s) + ")", 0, 0);
    f.close(0, 0);
    // else not active
    f.elseOpen(0, 0);
    f.statement("cancel", 0, 0);
    f.close(0, 1);
    // end else active
  }


  /**
   * Creates the VOnDA-code fragment that imitates the functionality of the
   * outgoing {@code InterruptiveEdges} of this {@code Supernode}.
   *
   * @return A String containing the VOnDA-code fragment that imitates the
   *         functionality of the outgoing {@code InterruptiveEdges} of this
   *         {@code Supernode}.
   */
  private void getSuperInterruptiveEdgesCode(SuperNode s) {
    String thisName = o.getNodeName(s);

    int index = 1;
    for (AbstractEdge aedge : getInterruptiveEdges(s)) {
      InterruptEdge edge = (InterruptEdge) aedge;
      BasicNode n = edge.getTargetNode();

      f.ruleLabel(thisName + "_interruptive_edge_" + index, 0, 0);
      blocks.addBlock(o.getNodeName(edge.getSourceNode()), f.getCurrentLineNo());
      f.ifOpen(edge.getContent(), 0, 0);
      blocks.endBlock(f.getCurrentLineNo());

      String trans = "interruptive_transition(" + fetchNode(s) + ", "
          + fetchNode(n) + ")";
      f.statement(trans, 0, 0);
      f.statement("cancel", 0, 0);
      f.close(0, 1);

      index += 1;
    }
  }

  /**
   * Creates the VOnDA-code fragment that imitates leaving the
   * {@code Supernode}.
   *
   * @return the VOnDA-code fragment that imitates leaving the {@code Supernode}
   *         as a String.
   *
   * This is very very very similar to getNodeCode(BasicNode), try to unify it
   * at some point.
   * This is obvious, since leaving a Supernode means considering the outgoing
   * transitions, as is the case in a Basicnode always.
   */
  private void getSuperOutCode(SuperNode s) {
    String name = o.getNodeName(s);
    // this parameterizes the SceneFlow and SuperNode in one out function!!

    f.ruleLabel(name + "_out", 1, 0);
    f.ifOpen("canBeLeft(" + fetchNode(s) + ")", 0, 0);

    if (s.getParentNode() != null) {
      // not the SceneFlow, ordinary Supernode
      getCommonEdgesCode(s);
    } else {
      f.statement("node = " + fetchNode(s), 0, 1);
      f.ifOpen("node.children.size() == 1", 0, 0);
      f.statement("setInactive(node)", 0, 0);
      f.statement("node.children -= node.id", 0, 0);
      f.close(0, 0); // end test no children and initiated
    }

    f.close(0, 0); // end rule if
  }

  /**
   * This generates all necessary code for a SuperNode, in its own file
   */
  private void getSuperNodeCode(SuperNode m) throws IOException {
    // If m is the top-level supernode, define utility functions (transitions
    // etc...) in this file
    if (m.getParentNode() == null) {
      getSceneFlowSetupCode((SceneFlow)m);
    } else {
      getSuperSetupCode(m);
    }

    // Post-process node and edge code and write it to the file
    getSuperInterruptiveEdgesCode(m);

    for (BasicNode n : m.getNodes()) {
      if (n.isBasic()) {
        getBasicNodeCode(n);
      } else {
        generateSupernodeFile(outPath, (SuperNode)n, o, blocks);
      }
    }

    // Add import statements to end of file (importing all sub-supernodes)
    for (BasicNode n : m.getNodes()) {
      String nodeName = o.getNodeName(n);
      if (!n.isBasic()) {
        f.statement("include " + nodeName, 0, 0);
      }
    }
  }

  private static void generateSupernodeFile(File path, SuperNode m,
      OntologyBuilder o, CodeInfo cb) {
    try (Generator g = new Generator(m, path, o, cb)) {
      g.getSuperNodeCode(m);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static CodeInfo generateAll(File sceneFlow, File outputRoot,
      Path ontologyDir) throws IOException {
    logger.info("Parsing Sceneflow...");
    SceneFlow sc = SceneFlow.load(sceneFlow);
    if (sc == null) {
      throw new RuntimeException("Sceneflow could not be loaded");
    }
    logger.info("Generating ontology files...");
    OntologyBuilder o = new OntologyBuilder();
    o.writeOntology(sc, ontologyDir);
    logger.info("Generating rudi files...");
    CodeInfo cb = new CodeInfo();
    Generator.generateSupernodeFile(outputRoot, sc, o, cb);
    logger.info("Done");
    return cb;
  }
}
