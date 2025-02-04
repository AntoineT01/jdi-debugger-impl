package dbg.command;

import com.sun.jdi.*;

import java.util.List;

public class TemporariesCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StringBuilder sb = new StringBuilder();
    try {
      StackFrame frame = context.getCurrentFrame();
      if (frame == null) {
        return "No current frame available.";
      }
      List<LocalVariable> vars = frame.visibleVariables();
      for (LocalVariable var : vars) {
        Value value = frame.getValue(var);
        sb.append(var.name()).append(" -> ").append(value).append("\n");
      }
      return sb.toString();
    } catch (AbsentInformationException e) {
      return "Local variable information is not available.";
    }
  }
}
