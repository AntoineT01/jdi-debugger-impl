package dbg.state;

import com.sun.jdi.*;
import java.util.*;

/**
 * Représente un instantané complet de l'état d'exécution à un moment donné.
 * Cette classe capture et stocke toutes les informations nécessaires pour
 * restaurer l'état du programme lors d'un step-back.
 */
public class ExecutionState {
  private final ThreadReference thread;
  private final Location location;
  private final StackFrame frame;
  private final Map<String, Value> variables;
  private final int executionIndex;

  public ExecutionState(ThreadReference thread, Location location,
                        StackFrame frame, int executionIndex)
  throws IncompatibleThreadStateException {
    this.thread = thread;
    this.location = location;
    this.frame = frame;
    this.executionIndex = executionIndex;
    this.variables = new HashMap<>();
    // Capture les variables seulement si nous avons une frame valide
    if (frame != null) {
      try {
        List<LocalVariable> visibleVars = frame.visibleVariables();
        if (visibleVars != null && !visibleVars.isEmpty()) {
          Map<LocalVariable, Value> frameVars = frame.getValues(visibleVars);
          for (Map.Entry<LocalVariable, Value> entry : frameVars.entrySet()) {
            variables.put(entry.getKey().name(), entry.getValue());
          }
        }
      } catch (AbsentInformationException e) {
        System.out.println("Debug info - No variables available at this point");
      }
    }
  }
  private Map<String, Value> captureVariables(StackFrame frame)
    throws IncompatibleThreadStateException {
    Map<String, Value> vars = new HashMap<>();
    try {
      Map<LocalVariable, Value> frameVars = frame.getValues(frame.visibleVariables());
      for (Map.Entry<LocalVariable, Value> entry : frameVars.entrySet()) {
        vars.put(entry.getKey().name(), entry.getValue());
      }
    } catch (AbsentInformationException e) {
      System.err.println("Unable to capture variables: " + e.getMessage());
    }
    return vars;
  }

  public ThreadReference getThread() {
    return thread;
  }

  public Location getLocation() {
    return location;
  }

  public StackFrame getFrame() {
    return frame;
  }

  public Map<String, Value> getVariables() {
    return variables;
  }

  public int getExecutionIndex() {
    return executionIndex;
  }
}