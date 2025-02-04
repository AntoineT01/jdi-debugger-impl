package dbg.command;

public class ContinueCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    // Pour "continue", on ne crée pas de step et on laisse la VM reprendre son cours.
    return "Continue command executed: resuming execution.";
  }
}
