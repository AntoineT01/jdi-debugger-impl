package dbg.command;

import com.sun.jdi.*;

public class PrintVarCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    if (args.length < 1) return "Usage: print-var <variableName>";
    String varName = args[0];
    try {
      StackFrame frame = context.getCurrentFrame();
      if (frame == null) return "No current frame available.";
      LocalVariable var = frame.visibleVariableByName(varName);
      if (var == null) return "Variable " + varName + " not found.";
      Value value = frame.getValue(var);
      return var.name() + " = " + value;
    } catch (AbsentInformationException e) {
      return "Variable information is not available.";
    }
  }
}
