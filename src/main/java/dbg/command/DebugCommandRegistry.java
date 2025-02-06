package dbg.command;

import java.util.HashMap;
import java.util.Map;

public class DebugCommandRegistry {
  private final Map<String, DebugCommand> commands = new HashMap<>();

  public DebugCommandRegistry() {
    // Enregistrer toutes les commandes ici :
    commands.put("step", new StepCommand());
    commands.put("step-over", new StepOverCommand());
    commands.put("continue", new ContinueCommand());
    commands.put("frame", new FrameCommand());
    commands.put("temporaries", new TemporariesCommand());
    commands.put("stack", new StackCommand());
    commands.put("receiver", new ReceiverCommand());
    commands.put("sender", new SenderCommand());
    commands.put("receiver-variables", new ReceiverVariablesCommand());
    commands.put("method", new MethodCommand());
    commands.put("arguments", new ArgumentsCommand());
    commands.put("print-var", new PrintVarCommand());
    commands.put("break", new BreakCommand());
    commands.put("breakpoints", new BreakpointsCommand());
    commands.put("break-once", new BreakOnceCommand());
    commands.put("break-on-count", new BreakOnCountCommand());
    commands.put("break-before-method-call", new BreakBeforeMethodCallCommand());
    // Time Travel
    commands.put("step-back", new StepBackCommand());
    commands.put("step-back-n", new StepBackNCommand());
  }

  public DebugCommand getCommand(String name) {
    return commands.get(name);
  }
}
