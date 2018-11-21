package de.dfki.vsm.util.evt;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Gregor Mehlmann
 */
public final class EventDispatcher {

  // The Singelton Instance
  private static EventDispatcher sInstance = null;

  // The Listener List
  private final CopyOnWriteArrayList<EventListener> mListenerList;

  // Construct The Instance
  private EventDispatcher() {
    mListenerList = new CopyOnWriteArrayList<EventListener>();
  }

  // Get The Singelton Instance
  public final static synchronized EventDispatcher getInstance() {
    if (sInstance == null) {
      sInstance = new EventDispatcher();
    }
    return sInstance;
  }

  // Add an event listener
  public final void register(final EventListener listener) {
    mListenerList.add(listener);
  }

  // Remove an event listener
  public final void remove(final EventListener listener) {
    mListenerList.remove(listener);
  }

  // Dispatch an event object
  private void dispatch(final Object event) {
    for (final EventListener listener : mListenerList) {
      listener.update(event);
    }
  }

  // Immediately schedule an event
  public final void convey(final Object event) {
    //schedule(event, 1);
    dispatch(event);
  }
}
