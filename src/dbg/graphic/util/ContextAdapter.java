package dbg.graphic.util;

import dbg.command.DebuggerContext;
import com.sun.jdi.*;

public class ContextAdapter {
  public static DebuggerContext adapt(dbg.graphic.model.DebuggerContext guiContext) {
    if (guiContext == null) return null;
    return new DebuggerContext(
      guiContext.getVM(),
      guiContext.getThread(),
      guiContext.getCurrentFrame()
    );
  }
}