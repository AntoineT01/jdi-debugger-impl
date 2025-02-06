package dbg.command;

import dbg.state.ExecutionState;

public class StepBackNCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      // Vérifions d'abord que nous avons reçu un argument
      if (args.length < 1) {
        return "Usage: step-back-n <number_of_steps>";
      }

      // Convertissons et validons le nombre de pas
      int steps = Integer.parseInt(args[0]);
      if (steps <= 0) {
        return "Number of steps must be positive";
      }

      // Récupérons l'état cible en reculant du nombre de pas demandé
      ExecutionState targetState = context.stepBack(steps);
      if (targetState == null) {
        return "Cannot step back " + steps + " steps - not enough history";
      }

      // Au lieu de restaurer directement l'état, nous définissons l'état cible
      // et demandons un redémarrage, exactement comme dans StepBackCommand
      context.setTargetState(targetState);

      return "RESTART: Stepping back " + steps + " steps";
    } catch (NumberFormatException e) {
      return "Invalid number format: " + args[0];
    } catch (Exception e) {
      return "Error executing step back: " + e.getMessage();
    }
  }
}