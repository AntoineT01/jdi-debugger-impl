package dbg.event;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.DebuggerSession;
import dbg.ScriptableDebugger;
import dbg.command.DebuggerContext;
import dbg.graphic.controller.DebuggerController;
import dbg.graphic.model.Breakpoint;
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
    int newLine = bpEvent.location().lineNumber();
    if (DebuggerController.getInstance() != null) {
      DebuggerController.getInstance().getModel().setCurrentLine(newLine);
    } else {
      ui.showOutput("DebuggerController n'est pas initialisé.");
    }

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
        return returnInFunctionOfUI(bpEvent.thread(), eventSet, bpEvent, debugger);
      } else {
        ui.showOutput("Breakpoint atteint sur le nombre ciblé (" + targetCount + ").");
        bpEvent.request().putProperty("breakOnCount", null);
        return returnInFunctionOfUI(bpEvent.thread(), eventSet, bpEvent, debugger);
      }
    }

    // Vérification du breakpoint one-shot
    Object onceObj = bpEvent.request().getProperty("breakOnce");
    if (onceObj != null && (Boolean) onceObj) {
      bpEvent.request().disable();
      bpEvent.virtualMachine().eventRequestManager().deleteEventRequest(bpEvent.request());
      ui.showOutput("Breakpoint one-shot supprimé après exécution.");
      return returnInFunctionOfUI(bpEvent.thread(), eventSet, bpEvent, debugger);
    }

    return returnInFunctionOfUI(bpEvent.thread(), eventSet, bpEvent, debugger);
  }

  private boolean returnInFunctionOfUI(ThreadReference thread, EventSet eventSet, BreakpointEvent bpEvent, ScriptableDebugger debugger) {
    if (ui.isBlocking()) {
      return debugger.waitForUser(thread, eventSet);
    } else {
      return false;
    }
  }
}
