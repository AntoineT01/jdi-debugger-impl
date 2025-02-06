package dbg.command;

public class ContinueCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    return "RESUME: Continue command executed: resuming execution.";
  }
}
