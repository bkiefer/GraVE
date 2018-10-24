package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.XmlRegistry;

import de.dfki.vsm.model.flow.edge.*;

@XmlRegistry
public class ObjectFactory {
  public SceneFlow getSceneFlow() { return new SceneFlow(); }

  public BasicNode getNode() { return new BasicNode(); }

  public SuperNode getSuperNode() { return new SuperNode(); }

  public Code getCommand() { return new Code(""); }

  public EpsilonEdge getEEdge() { return new EpsilonEdge(); }

  public GuardedEdge getCEdge() { return new GuardedEdge(); }

  public ForkingEdge getFEdge() { return new ForkingEdge(); }

  public RandomEdge getPEdge() { return new RandomEdge(); }

  public TimeoutEdge getTEdge() { return new TimeoutEdge(); }

}
