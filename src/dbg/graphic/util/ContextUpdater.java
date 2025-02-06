package dbg.graphic.util;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import dbg.graphic.model.DebuggerContext;

import java.util.*;
import java.util.List;

public class ContextUpdater {
  public static DebuggerContext createContext(Event event) {
    if (event instanceof LocatableEvent) {
      LocatableEvent locEvent = (LocatableEvent) event;
      try {
        return new DebuggerContext(
          event.virtualMachine(),
          locEvent.thread(),
          locEvent.thread().frame(0)
        );
      } catch (IncompatibleThreadStateException e) {
        return null;
      }
    }
    return null;
  }

  public static Map<String, String> getVariables(DebuggerContext context) {
    Map<String, String> variables = new HashMap<>();
    try {
      if (context.getCurrentFrame() != null) {
        for (LocalVariable var : context.getLocalVariables()) {
          Value value = context.getLocalVariableValue(var);
          variables.put(var.name(), value != null ? value.toString() : "null");
        }
      }
    } catch (AbsentInformationException e) {
      variables.put("error", "Variable information not available");
    }
    return variables;
  }

  public static List<String> getCallStack(DebuggerContext context) {
    List<String> stack = new ArrayList<>();
    try {
      ThreadReference thread = context.getThread();
      if (thread != null) {
        for (StackFrame frame : thread.frames()) {
          Location location = frame.location();
          String methodName = location.method().name();
          String className = location.declaringType().name();
          int lineNumber = location.lineNumber();
          stack.add(String.format("%s.%s (line %d)", className, methodName, lineNumber));
        }
      }
    } catch (IncompatibleThreadStateException e) {
      stack.add("Call stack not available");
    }
    return stack;
  }
}