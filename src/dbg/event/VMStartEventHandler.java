package dbg.event;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.ScriptableDebugger;
import dbg.ui.DebuggerUI;

public class VMStartEventHandler implements DebuggerEventHandler {
  private DebuggerUI ui;

  public VMStartEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    ui.showOutput("La VM a démarré.");
    eventSet.resume();
    return true;
  }
}
