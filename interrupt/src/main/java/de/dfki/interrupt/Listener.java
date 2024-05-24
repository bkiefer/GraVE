package de.dfki.interrupt;

public interface Listener<T> {
  public void listen(T q);
}