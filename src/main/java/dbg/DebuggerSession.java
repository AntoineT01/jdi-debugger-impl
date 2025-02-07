package dbg;

import com.sun.jdi.event.EventSet;
import dbg.command.DebuggerContext;
import dbg.state.ExecutionState;

public class DebuggerSession {
  // État actuel du débogueur
  private static DebuggerContext debuggerContext;
  private static EventSet currentEventSet;

  // Indicateur pour le redémarrage
  private static boolean restartRequired = false;
  private static boolean replayInProgress = false;  // Nouveau: indique si nous sommes en replay
  private static ExecutionState replayTargetState = null;

  private DebuggerSession() {}


  /**
   * Récupère le contexte actuel du débogueur.
   */
  public static DebuggerContext getContext() {
    return debuggerContext;
  }
  public static void setReplayTargetState(ExecutionState state) {
    replayTargetState = state;
    replayInProgress = true;
  }

  public static ExecutionState getReplayTargetState() {
    return replayTargetState;
  }



  /**
   * Définit le contexte du débogueur.
   * Cette méthode est appelée lors des changements d'état importants,
   * comme le démarrage initial ou après un redémarrage.
   */
  public static void setContext(DebuggerContext context) {
    debuggerContext = context;
  }

  /**
   * Vérifie si un contexte de débogage existe.
   */
  public static boolean hasContext() {
    return debuggerContext != null;
  }

  /**
   * Récupère l'ensemble d'événements actuel.
   */
  public static EventSet getCurrentEventSet() {
    return currentEventSet;
  }

  /**
   * Définit l'ensemble d'événements actuel.
   */
  public static void setCurrentEventSet(EventSet eventSet) {
    currentEventSet = eventSet;
  }

  /**
   * Indique si un redémarrage du débogueur est nécessaire.
   * Cette méthode est utilisée principalement pour le time travelling,
   * quand nous devons redémarrer la VM pour revenir à un état précédent.
   */
  public static boolean isRestartRequired() {
    return restartRequired;
  }

  public static boolean isReplayInProgress() {
    return replayInProgress;
  }

  public static void resetReplay() {
    replayInProgress = false;
    replayTargetState = null;
    restartRequired = false;
  }

  /**
   * Définit si un redémarrage est nécessaire.
   * Cette méthode est appelée quand une commande de time travelling
   * nécessite un redémarrage de la VM.
   */
  public static void setRestartRequired(boolean required) {
    restartRequired = required;
    if (required) {
      replayInProgress = true;  // Activer le mode replay quand on demande un redémarrage
    }
  }

  /**
   * Réinitialise l'état de la session.
   * Cette méthode est utile après un redémarrage réussi.
   */
  public static void reset() {
    restartRequired = false;
    if (debuggerContext != null) {
      debuggerContext.setReplayMode(false);
    }
  }

  // Méthodes pour gérer le mode replay
  public static void startReplay(ExecutionState targetState) {
    replayTargetState = targetState;
    replayInProgress = true;
    System.out.println("Debug info - Starting replay to state " + targetState.getExecutionIndex());
  }
}