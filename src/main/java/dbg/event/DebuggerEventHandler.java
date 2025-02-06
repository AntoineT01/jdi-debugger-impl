package dbg.event;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.ScriptableDebugger;

/**
 * Interface pour le traitement des événements du débogueur.
 */
public interface DebuggerEventHandler {
  /**
   * Traite l'événement et détermine si l'exécution doit reprendre.
   *
   * @param event l'événement à traiter
   * @param eventSet l'ensemble d'événements contenant l'événement
   * @param debugger l'instance du débogueur, permettant d'appeler waitForUser() par exemple
   * @return true si l'exécution de la VM doit reprendre
   */
  boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger);
}
