package dbg.command;

public class FrameCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if(context.getCurrentFrame() != null)
      return context.getCurrentFrame().toString();
    else
      return "No current frame available.";
  }
}
