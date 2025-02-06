package dbg.command;

import com.sun.jdi.*;

public class ReceiverCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      StackFrame frame = context.getCurrentFrame();
      if (frame == null) return "No current frame available.";
      ObjectReference receiver = frame.thisObject();
      return receiver != null ? receiver.toString() : "No receiver (static method?)";
    } catch (Exception e) {
      return "Error retrieving receiver: " + e.getMessage();
    }
  }
}
