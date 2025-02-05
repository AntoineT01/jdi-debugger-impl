package dbg.ui;

import dbg.command.DebuggerContext;

public interface DebuggerUI {
  /**
   * Affiche un message à l'utilisateur.
   */
  void showOutput(String message);

  /**
   * Demande à l'utilisateur de saisir une commande dans le contexte donné.
   * Renvoie la chaîne saisie.
   */
  String getCommand(DebuggerContext context);
}
