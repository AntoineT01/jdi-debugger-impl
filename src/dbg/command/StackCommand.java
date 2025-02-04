package dbg.command;

import com.sun.jdi.*;

import java.util.List;

public class StackCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StringBuilder sb = new StringBuilder();
    try {
      List<StackFrame> frames = context.getCurrentThread().frames();
      for (StackFrame frame : frames) {
        sb.append(frame.location().toString()).append("\n");
      }
      return sb.toString();
    } catch (IncompatibleThreadStateException e) {
      return "Error retrieving call stack: " + e.getMessage();
    }
  }
}
