package dbg.command;

import com.sun.jdi.*;

import java.util.List;

public class SenderCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      List<StackFrame> frames = context.getCurrentThread().frames();
      if (frames.size() >= 2) {
        // La deuxième frame correspond au caller (sender)
        StackFrame senderFrame = frames.get(1);
        return senderFrame.location().toString();
      } else {
        return "No sender found (insufficient stack frames).";
      }
    } catch (IncompatibleThreadStateException e) {
      return "Error retrieving sender: " + e.getMessage();
    }
  }
}
