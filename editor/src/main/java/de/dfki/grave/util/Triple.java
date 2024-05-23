package de.dfki.grave.util;

/**
 * @author Gregor Mehlmann
 */
public class Triple<F, S, T> {

  // The first component
  private F mFirst;
  // The second component
  private S mSecond;
  // The third component
  private T mThird;

  // Construct a tuple
  public Triple(final F first, final S second, final T third) {
    mFirst = first;
    mSecond = second;
    mThird = third;
  }

  // Get the first component
  public F getFirst() {
    return mFirst;
  }

  // Set the first component
  public void setFirst(final F value) {
    mFirst = value;
  }

  // Get the second component
  public S getSecond() {
    return mSecond;
  }

  // Set the second component
  public void setSecond(final S value) {
    mSecond = value;
  }

  // Get the third component
  public T getThird() {
    return mThird;
  }

  // Set the third component
  public void setThird(final T value) {
    mThird = value;
  }
}
