package dbg.command;

import com.sun.jdi.*;
import java.util.List;

public class ArgumentsCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StringBuilder sb = new StringBuilder();
    try {
      StackFrame frame = context.getCurrentFrame();
      if (frame == null)
        return "No current frame available.";
      // Récupère toutes les variables visibles
      List<LocalVariable> vars = frame.visibleVariables();
      boolean foundArgument = false;
      for (LocalVariable var : vars) {
        // Ne conserver que les variables qui sont des arguments de la méthode
        if (var.isArgument()) {
          Value value = frame.getValue(var);
          sb.append(var.name()).append(" -> ").append(value).append("\n");
          foundArgument = true;
        }
      }
      if (!foundArgument) {
        return "No arguments available.";
      }
      return sb.toString();
    } catch (AbsentInformationException e) {
      return "Argument information not available: " + e.getMessage();
    }
  }
}
