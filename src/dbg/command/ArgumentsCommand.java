package dbg.command;

import com.sun.jdi.*;

import java.util.List;

public class ArgumentsCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StringBuilder sb = new StringBuilder();
    try {
      StackFrame frame = context.getCurrentFrame();
      if (frame == null) return "No current frame available.";
      List<LocalVariable> argsList = frame.visibleVariables();
      for (LocalVariable var : argsList) {
        // Vous pouvez filtrer les arguments si besoin (ex : en fonction du nom ou d'une propriété)
        Value value = frame.getValue(var);
        sb.append(var.name()).append(" -> ").append(value).append("\n");
      }
      return sb.toString();
    } catch (AbsentInformationException e) {
      return "Arguments information is not available.";
    }
  }
}
