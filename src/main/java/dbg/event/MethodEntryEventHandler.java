package dbg.event;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import dbg.ScriptableDebugger;
import dbg.ui.DebuggerUI;

public class MethodEntryEventHandler implements DebuggerEventHandler {
  private final DebuggerUI ui;

  public MethodEntryEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    MethodEntryEvent meEvent = (MethodEntryEvent) event;
    Object targetMethodObj = meEvent.request().getProperty("targetMethod");
    if (targetMethodObj != null) {
      String targetMethod = targetMethodObj.toString();
      String currentMethod = meEvent.location().method().name();
      if (currentMethod.equals(targetMethod)) {
        ui.showOutput("MethodEntryEvent: Arrêt avant l'appel de la méthode : " + currentMethod);
        return debugger.waitForUser(meEvent.thread(), eventSet);
      } else {
        return true;
      }
    }
    return true;
  }
}

