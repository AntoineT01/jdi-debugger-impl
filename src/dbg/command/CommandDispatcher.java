package dbg.command;

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
    // Décomposer la ligne : le premier token est le nom de la commande, le reste sont les arguments.
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
    return command.execute(args, context);
  }
}
