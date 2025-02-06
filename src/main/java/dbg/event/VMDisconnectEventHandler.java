package dbg.event;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.ScriptableDebugger;
import dbg.ui.DebuggerUI;

public class VMDisconnectEventHandler implements DebuggerEventHandler {
  private DebuggerUI ui;

  public VMDisconnectEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    ui.showOutput("La VM est déconnectée. Fin du débogage.");
    return true;
  }
}
