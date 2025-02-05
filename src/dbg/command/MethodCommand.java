package dbg.command;

import com.sun.jdi.*;

public class MethodCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StackFrame frame = context.getCurrentFrame();
    if (frame == null) return "No current frame available.";
    return frame.location().method().toString();
  }
}
