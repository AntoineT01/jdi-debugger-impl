package dbg.command;

import com.sun.jdi.*;

import java.util.List;
import java.util.Map;

public class ReceiverVariablesCommand implements DebugCommand {
  @Override
  public Object execute(String[] args, DebuggerContext context) {
    try {
      StackFrame frame = context.getCurrentFrame();
      if (frame == null) return "No current frame available.";
      ObjectReference receiver = frame.thisObject();
      if (receiver == null) return "No receiver (static method?)";
      ReferenceType refType = receiver.referenceType();
      List<Field> fields = refType.visibleFields();
      StringBuilder sb = new StringBuilder();
      for (Field field : fields) {
        Value val = receiver.getValue(field);
        sb.append(field.name()).append(" -> ").append(val).append("\n");
      }
      return sb.toString();
    } catch (Exception e) {
      return "Error retrieving receiver variables: " + e.getMessage();
    }
  }
}
