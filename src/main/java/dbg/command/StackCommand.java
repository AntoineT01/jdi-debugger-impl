package dbg.command;

import com.sun.jdi.*;
import java.util.List;

public class StackCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    StringBuilder sb = new StringBuilder();
    try {
      // On récupère toutes les frames du thread courant.
      List<StackFrame> frames = context.getCurrentThread().frames();
      for (StackFrame frame : frames) {
        Location loc = frame.location();
        // On affiche le nom complet de la classe, le nom de la méthode et le numéro de ligne.
        sb.append(loc.declaringType().name())
          .append(".")
          .append(loc.method().name())
          .append(" : line ")
          .append(loc.lineNumber())
          .append("\n");
      }
      return sb.toString();
    } catch (IncompatibleThreadStateException e) {
      return "Erreur lors de la récupération de la pile d'appel : " + e.getMessage();
    }
  }
}
