package dbg;

import com.sun.jdi.event.EventSet;
import dbg.command.DebuggerContext;

public class DebuggerSession {
  private static DebuggerContext debuggerContext;
  private static EventSet currentEventSet;

  public static DebuggerContext getContext() {
    return debuggerContext;
  }

  public static void setContext(DebuggerContext context) {
    debuggerContext = context;
  }

  public static boolean hasContext() {
    return debuggerContext != null;
  }

  public static EventSet getCurrentEventSet() {
    return currentEventSet;
  }

  public static void setCurrentEventSet(EventSet eventSet) {
    currentEventSet = eventSet;
  }
}