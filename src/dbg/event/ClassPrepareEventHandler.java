package dbg.event;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.ScriptableDebugger;
import dbg.ui.DebuggerUI;

public class ClassPrepareEventHandler implements DebuggerEventHandler {
  private DebuggerUI ui;

  public ClassPrepareEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    ClassPrepareEvent cpEvent = (ClassPrepareEvent) event;
    ReferenceType refType = cpEvent.referenceType();
    ui.showOutput("Classe préparée: " + refType.name());
    // En mode CLI, on attend une commande. En GUI, le comportement peut être différent.
    return debugger.waitForUser(cpEvent.thread(), eventSet);
  }
}
