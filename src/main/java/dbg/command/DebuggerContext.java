package dbg.command;

import com.sun.jdi.*;
import dbg.state.ExecutionState;
import java.util.*;

/**
 * Classe centrale gérant le contexte du débogueur.
 * Cette classe maintient l'état d'exécution courant et gère l'historique pour le time travelling.
 * Elle coordonne également le mode replay pour revenir à des états précédents.
 */
public class DebuggerContext {
  // État d'exécution courant
  private final VirtualMachine vm;
  private ThreadReference currentThread;
  private StackFrame currentFrame;

  // Gestion de l'historique des états
  private final List<ExecutionState> executionHistory;
  private int currentStateIndex;
  private int executionCounter;

  // Gestion du time travelling
  private ExecutionState targetState;
  private boolean replayMode;
  private ExecutionState replayTargetState;
  private boolean ignoreBreakpoints;

  /**
   * Constructeur initialisant un nouveau contexte de débogage.
   * Initialise les structures de données pour l'historique et le time travelling.
   */
  public DebuggerContext(VirtualMachine vm, ThreadReference currentThread, StackFrame currentFrame) {
    this.vm = vm;
    this.currentThread = currentThread;
    this.currentFrame = currentFrame;
    this.executionHistory = new ArrayList<>();
    this.currentStateIndex = -1;
    this.executionCounter = 0;
    this.replayMode = false;
    this.ignoreBreakpoints = false;
    System.out.println("Debug info - New DebuggerContext created");
  }

  /**
   * Récupère l'historique complet des états d'exécution.
   * Retourne une copie pour préserver l'encapsulation.
   */
  public List<ExecutionState> getExecutionHistory() {
    return new ArrayList<>(executionHistory);
  }

  /**
   * Définit un nouvel historique d'exécution.
   * Utilisé lors du redémarrage pour préserver l'historique existant.
   */
  public void setExecutionHistory(List<ExecutionState> history) {
    this.executionHistory.clear();
    this.executionHistory.addAll(history);
    this.currentStateIndex = history.size() - 1;
    if (!history.isEmpty()) {
      this.executionCounter = history.get(history.size() - 1).getExecutionIndex() + 1;
    }
  }

  // Méthodes de gestion de l'état courant
  public void setCurrentThread(ThreadReference thread) {
    this.currentThread = thread;
  }

  public void setCurrentFrame(StackFrame frame) {
    this.currentFrame = frame;
    captureCurrentState();
  }

  // Méthodes de gestion du mode replay
  public void setReplayMode(boolean replayMode) {
    this.replayMode = replayMode;
    if (!replayMode) {
      this.ignoreBreakpoints = false;
      this.replayTargetState = null;
    }
  }

  public void setReplayTargetState(ExecutionState state) {
    this.replayTargetState = state;
    this.ignoreBreakpoints = true;
    this.replayMode = true;
  }

  public boolean isInReplayMode() {
    return replayMode;
  }

  public boolean shouldIgnoreBreakpoints() {
    return ignoreBreakpoints;
  }

  /**
   * Vérifie si nous avons atteint l'état cible pendant le replay.
   * Compare la location actuelle avec la location cible.
   */
  public boolean hasReachedReplayTarget(Location currentLocation) {
    if (!replayMode || replayTargetState == null) {
      return false;
    }

    Location targetLocation = replayTargetState.getLocation();
    boolean reached = currentLocation.lineNumber() == targetLocation.lineNumber() &&
      currentLocation.method().equals(targetLocation.method());

    if (reached) {
      // Réinitialiser le mode replay une fois la cible atteinte
      replayMode = false;
      ignoreBreakpoints = false;
      replayTargetState = null;
    }

    return reached;
  }

  /**
   * Configure l'état cible pour le time travelling.
   * Active automatiquement le mode replay.
   */
  public void setTargetState(ExecutionState state) {
    this.targetState = state;
    this.replayMode = true;
    this.ignoreBreakpoints = true;
  }

  public ExecutionState getTargetState() {
    return this.targetState;
  }

  /**
   * Vérifie si nous avons atteint l'état cible pendant le time travelling.
   */
  public boolean hasReachedTargetState(Location currentLocation) {
    if (targetState == null) {
      return false;
    }

    Location targetLocation = targetState.getLocation();
    boolean reached = currentLocation.lineNumber() == targetLocation.lineNumber() &&
      currentLocation.method().equals(targetLocation.method());

    if (reached) {
      targetState = null;
    }

    return reached;
  }

  /**
   * Capture l'état d'exécution actuel.
   * Cette méthode est cruciale pour le time travelling car elle maintient
   * l'historique des états qui peuvent être revisités.
   */
  public void captureCurrentState() {
    try {
      if (currentThread != null && currentFrame != null) {
        Location location = currentFrame.location();

        // Éviter les doublons d'état pour la même location
        if (!executionHistory.isEmpty()) {
          ExecutionState lastState = executionHistory.get(currentStateIndex);
          if (location.equals(lastState.getLocation())) {
            return;
          }
        }

        ExecutionState state = new ExecutionState(
          currentThread,
          location,
          currentFrame,
          executionCounter++
        );

        // Nettoyer l'historique futur lors d'un retour en arrière
        if (currentStateIndex < executionHistory.size() - 1) {
          executionHistory.subList(currentStateIndex + 1, executionHistory.size()).clear();
        }

        executionHistory.add(state);
        currentStateIndex = executionHistory.size() - 1;

        System.out.println("Debug info - State captured at " +
                             state.getLocation().method().name() + ":" +
                             state.getLocation().lineNumber() +
                             " (State #" + currentStateIndex + ")");
      }
    } catch (Exception e) {
      System.err.println("Debug info - Failed to capture state: " + e.getMessage());
    }
  }

  /**
   * Revient d'un pas en arrière dans l'historique.
   */
  public ExecutionState stepBack() {
    if (currentStateIndex > 0 && !executionHistory.isEmpty()) {
      System.out.println("Debug info - Stepping back from state " + currentStateIndex);
      currentStateIndex--;
      return executionHistory.get(currentStateIndex);
    }
    return null;
  }

  /**
   * Revient de plusieurs pas en arrière dans l'historique.
   */
  public ExecutionState stepBack(int steps) {
    int targetIndex = Math.max(0, currentStateIndex - steps);
    if (targetIndex < currentStateIndex && !executionHistory.isEmpty()) {
      currentStateIndex = targetIndex;
      return executionHistory.get(currentStateIndex);
    }
    return null;
  }

  // Getters pour l'accès aux informations du contexte
  public VirtualMachine getVm() { return vm; }
  public ThreadReference getCurrentThread() { return currentThread; }
  public StackFrame getCurrentFrame() { return currentFrame; }
}