package de.dfki.grave.model;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
  public SceneFlow getSceneFlow() { return new SceneFlow(); }

  public BasicNode getNode() { return new BasicNode(); }

  public SuperNode getSuperNode() { return new SuperNode(); }

  public EpsilonEdge getEEdge() { return new EpsilonEdge(); }

  public GuardedEdge getCEdge() { return new GuardedEdge(); }

  public ForkingEdge getFEdge() { return new ForkingEdge(); }

  public RandomEdge getPEdge() { return new RandomEdge(); }

  public TimeoutEdge getTEdge() { return new TimeoutEdge(); }

  public CommentBadge getComment() { return new CommentBadge(); }
}
