package dbg.event;

import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.DebuggerSession;
import dbg.ScriptableDebugger;
import dbg.command.DebuggerContext;
import dbg.ui.DebuggerUI;

public class BreakpointEventHandler implements DebuggerEventHandler {
  private DebuggerUI ui;

  public BreakpointEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    BreakpointEvent bpEvent = (BreakpointEvent) event;
    ui.showOutput("Breakpoint atteint à: " + bpEvent.location());

    // Gestion du hit count
    Object targetObj = bpEvent.request().getProperty("breakOnCount");
    if (targetObj != null) {
      int targetCount = (Integer) targetObj;
      Object currentObj = bpEvent.request().getProperty("currentHitCount");
      int currentHit = (currentObj == null) ? 0 : (Integer) currentObj;
      currentHit++;
      bpEvent.request().putProperty("currentHitCount", currentHit);
      ui.showOutput("Hit count for this breakpoint: " + currentHit);
      if (currentHit % targetCount != 0) {
        ui.showOutput("Breakpoint atteint mais pas sur le nombre ciblé (" + targetCount + "). Reprise automatique.");
        return true;
      } else {
        ui.showOutput("Breakpoint atteint sur le nombre ciblé (" + targetCount + ").");
        bpEvent.request().putProperty("breakOnCount", null);
      }
    }

    // Vérification du breakpoint one-shot
    Object onceObj = bpEvent.request().getProperty("breakOnce");
    if (onceObj != null && (Boolean) onceObj) {
      bpEvent.request().disable();
      bpEvent.virtualMachine().eventRequestManager().deleteEventRequest(bpEvent.request());
      ui.showOutput("Breakpoint one-shot supprimé après exécution.");
      return debugger.waitForUser(bpEvent.thread(), eventSet);
    }

    return debugger.waitForUser(bpEvent.thread(), eventSet);
  }
}
