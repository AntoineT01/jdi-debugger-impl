package dbg.event;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import dbg.DebuggerSession;
import dbg.ScriptableDebugger;
import dbg.command.DebuggerContext;
import dbg.ui.DebuggerUI;

public class StepEventHandler implements DebuggerEventHandler {
  private final DebuggerUI ui;

  public StepEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    StepEvent stepEvent = (StepEvent) event;
    DebuggerContext context = DebuggerSession.getContext();

    // Mise à jour du contexte avec le thread et la frame actuels
    try {
      context.setCurrentThread(stepEvent.thread());
      context.setCurrentFrame(stepEvent.thread().frame(0));

      ui.showOutput("StepEvent reached at: " + stepEvent.location());

      // Si nous sommes en mode replay, vérifier si nous avons atteint notre cible
      if (context.isInReplayMode()) {
        // Capture quand même l'état pour maintenir l'historique cohérent
        context.captureCurrentState();

        if (context.hasReachedReplayTarget(stepEvent.location())) {
          ui.showOutput("Target state reached at: " + stepEvent.location());
          context.setReplayMode(false); // Désactive le mode replay
          return debugger.waitForUser(stepEvent.thread(), eventSet);
        }
        return true; // Continue l'exécution automatiquement en mode replay
      }

      // En mode normal, capture l'état pour le time travelling
      context.captureCurrentState();

      // Attend l'entrée utilisateur si l'UI est en mode bloquant
      if (ui.isBlocking()) {
        return debugger.waitForUser(stepEvent.thread(), eventSet);
      }

      return false;

    } catch (Exception e) {
      ui.showOutput("Error in StepEventHandler: " + e.getMessage());
      return false;
    }
  }
}