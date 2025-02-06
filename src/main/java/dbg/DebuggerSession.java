package dbg;

import dbg.command.DebuggerContext;

public class DebuggerSession {
  private static DebuggerContext debuggerContext;

  public static DebuggerContext getContext() {
    return debuggerContext;
  }

  public static void setContext(DebuggerContext context) {
    debuggerContext = context;
  }

  public static boolean hasContext() {
    return debuggerContext != null;
  }
}