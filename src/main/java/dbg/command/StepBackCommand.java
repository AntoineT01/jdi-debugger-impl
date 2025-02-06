package dbg.command;

import com.sun.jdi.*;
import dbg.state.ExecutionState;

public class StepBackCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      ExecutionState previousState = context.stepBack();
      if (previousState == null) {
        return "Cannot step back - at the beginning of execution history";
      }

      // Au lieu d'essayer de restaurer l'état directement,
      // on signale qu'on veut revenir à cet état
      context.setTargetState(previousState);

      // Demander au débugger de redémarrer l'exécution
      return "RESTART: Stepping back to previous state";
    } catch (Exception e) {
      return "Error executing step back: " + e.getMessage();
    }
  }
}