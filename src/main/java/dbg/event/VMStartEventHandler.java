package dbg.event;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import dbg.DebuggerSession;
import dbg.ScriptableDebugger;
import dbg.command.DebuggerContext;
import dbg.ui.DebuggerUI;

public class VMStartEventHandler implements DebuggerEventHandler {
  private DebuggerUI ui;

  public VMStartEventHandler(DebuggerUI ui) {
    this.ui = ui;
  }

  @Override
  public boolean handle(Event event, EventSet eventSet, ScriptableDebugger debugger) {
    ui.showOutput("La VM a démarré.");
    return true;
  }
}
