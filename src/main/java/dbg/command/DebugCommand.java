package dbg.command;

public interface DebugCommand {
  /**
   * Exécute la commande avec les arguments donnés et dans le contexte fourni.
   * @param args les arguments de la commande (éventuellement vides)
   * @param context le contexte courant (VM, thread, frame, etc.)
   * @return un objet résultat (ou null) qui pourra être affiché
   */
  Object execute(String[] args, dbg.command.DebuggerContext context);
}
