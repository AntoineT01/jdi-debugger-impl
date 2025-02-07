package dbg.command;

import dbg.DebuggerSession;
import dbg.state.ExecutionState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandDispatcher {
  private final DebugCommandRegistry registry = new DebugCommandRegistry();

  /**
   * Lit la commande utilisateur depuis System.in et l'exécute dans le contexte donné.
   */
  public Object dispatchCommand(DebuggerContext context) {
    System.out.print("dbg> ");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    try {
      String line = reader.readLine();
      if (line == null || line.trim().isEmpty()) {
        return null;
      }
      return dispatchCommand(context, line);
    } catch (IOException e) {
      return "Error reading command: " + e.getMessage();
    }
  }

  /**
   * Exécute la commande utilisateur fournie sous forme de chaîne dans le contexte spécifié.
   */
  public Object dispatchCommand(DebuggerContext context, String commandLine) {
    String[] tokens = commandLine.trim().split("\\s+");

    if (tokens.length == 0) {
      return null;
    }
    String cmdName = tokens[0];
    String[] args = new String[tokens.length - 1];
    System.arraycopy(tokens, 1, args, 0, args.length);

    DebugCommand command = registry.getCommand(cmdName);
    if (command == null) {
      return "Unknown command: " + cmdName;
    }

    // Exécuter la commande
    Object result = command.execute(args, context);

//    // Si la commande demande un redémarrage (comme step-back)
//    if (result != null && result.toString().startsWith("RESTART:")) {
//      // Récupérer l'état cible depuis le contexte
//      ExecutionState targetState = context.getTargetState();
//      if (targetState != null) {
//        // Configurer le mode "replay" avec l'état cible
//        context.setReplayMode(true);
//        context.setReplayTargetState(targetState);
//        // Signaler qu'il faut redémarrer jusqu'à cet état
//        return "RESUME:REPLAY:" + targetState.getExecutionIndex();
//      }
//    }

    if (result != null && result.toString().startsWith("RESTART:")) {
      ExecutionState targetState = context.getTargetState();
      if (targetState != null) {
        DebuggerSession.startReplay(targetState);  // Ajout: configurer le replay au niveau session
        return "RESUME:REPLAY:" + targetState.getExecutionIndex();
      }
    }

    return result;
  }
}