package dbg.ui;

import dbg.command.DebuggerContext;

/**
 * Interface permettant d'abstraire l'interface utilisateur (CLI, GUI, etc.).
 */
public interface DebuggerUI {
  /**
   * Affiche un message à l'utilisateur.
   */
  void showOutput(String output);

  /**
   * Récupère une commande saisie par l'utilisateur.
   * Pour une UI non bloquante (GUI), cette méthode pourra être adaptée.
   */
  String getCommand(DebuggerContext context);

  /**
   * Indique si cette interface utilisateur nécessite d'attendre une commande.
   * En mode CLI, c'est vrai (commande saisie par l'utilisateur via la console).
   * En mode GUI, c'est généralement faux puisque l'action est déclenchée par un bouton.
   */
  boolean isBlocking();
}
