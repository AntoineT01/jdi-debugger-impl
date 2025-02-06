package dbg.command;

import com.sun.jdi.*;
import java.util.List;

public class FrameCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StackFrame frame = context.getCurrentFrame();
    if (frame == null) {
      return "No current frame available.";
    }
    StringBuilder sb = new StringBuilder();

    try {
      sb.append("Location: ").append(frame.location().toString()).append("\n");
      sb.append("Method: ").append(frame.location().method().name()).append("\n");
    } catch (Exception e) {
      sb.append("Impossible d'extraire la location ou la méthode: ").append(e.getMessage()).append("\n");
    }

    // afficher les variables locales
    try {
      List<LocalVariable> vars = frame.visibleVariables();
      sb.append("Variables locales:\n");
      for (LocalVariable var : vars) {
        Value value = frame.getValue(var);
        sb.append("  ").append(var.name()).append(" = ").append(value).append("\n");
      }
    } catch (AbsentInformationException e) {
      sb.append("Informations sur les variables locales non disponibles.\n");
    }

    return sb.toString();
  }
}
