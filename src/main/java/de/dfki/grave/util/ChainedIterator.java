package de.dfki.grave.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChainedIterator<T> implements Iterator<T> {

  private final Iterator<Iterator<T>> chain;
  private Iterator<T> currentIterator;
  private Iterator<T> lastIterator;

  @SafeVarargs
  public ChainedIterator(Iterator<T> ... iterators) {
    this.chain = Arrays.asList(iterators).iterator();
  }

  @Override
  public boolean hasNext() {
    while (currentIterator == null || !currentIterator.hasNext()) {
      if (!chain.hasNext())
        return false;
      currentIterator = chain.next();
    }
    return true;
  }

  @Override
  public T next() {
    if (!this.hasNext()) {
      this.lastIterator = null; // to disallow remove()
      throw new NoSuchElementException();
    }
    this.lastIterator = currentIterator; // to support remove()
    return currentIterator.next();
  }

  @Override
  public void remove() {
    if (this.lastIterator == null) {
      throw new IllegalStateException();
    }
    this.lastIterator.remove();
  }
}