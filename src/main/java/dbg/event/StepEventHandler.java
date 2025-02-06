package dbg.event;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import dbg.ScriptableDebugger;
import dbg.ui.DebuggerUI;

public class StepEventHandler implements DebuggerEventHandler {
  private final DebuggerUI ui;

  public StepEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    StepEvent stepEvent = (StepEvent) event;
    ui.showOutput("StepEvent reached at: " + stepEvent.location());
    // Attendre une commande de l'utilisateur (par exemple, pour afficher l'état ou pour continuer)
    return debugger.waitForUser(stepEvent.thread(), eventSet);
  }
}
