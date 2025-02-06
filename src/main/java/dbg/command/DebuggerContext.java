package dbg.command;

import com.sun.jdi.*;
import dbg.state.ExecutionState;
import java.util.*;

public class DebuggerContext {
  private final VirtualMachine vm;
  private ThreadReference currentThread;
  private StackFrame currentFrame;
  private final List<ExecutionState> executionHistory;
  private int currentStateIndex;
  private int executionCounter;
  private ExecutionState targetState; // Time-travel target state


  public DebuggerContext(VirtualMachine vm, ThreadReference currentThread, StackFrame currentFrame) {
    this.vm = vm;
    this.currentThread = currentThread;
    this.currentFrame = currentFrame;
    this.executionHistory = new ArrayList<>();
    this.currentStateIndex = -1;
    this.executionCounter = 0;
    System.out.println("Debug info - New DebuggerContext created");
  }

  public void setCurrentThread(ThreadReference thread) {
    this.currentThread = thread;
  }

  public void setCurrentFrame(StackFrame frame) {
    this.currentFrame = frame;
    // Capturer l'état quand on change de frame
    captureCurrentState();
  }

  public void setTargetState(ExecutionState state) {
    this.targetState = state;
  }

  public boolean hasReachedTargetState(Location currentLocation) {
    if (targetState == null) {
      return false;
    }

    Location targetLocation = targetState.getLocation();
    boolean reached = currentLocation.lineNumber() == targetLocation.lineNumber() &&
      currentLocation.method().equals(targetLocation.method());

    if (reached) {
      targetState = null; // Réinitialiser pour les prochaines opérations
    }

    return reached;
  }

  public void captureCurrentState() {
    try {
      if (currentThread != null && currentFrame != null) {
        Location location = currentFrame.location();

        // Ne pas capturer si nous sommes en train de rejouer
        if (targetState != null &&
          !hasReachedTargetState(location)) {
          return;
        }

        ExecutionState state = new ExecutionState(
          currentThread,
          location,
          currentFrame,
          executionCounter++
        );

        if (currentStateIndex < executionHistory.size() - 1) {
          executionHistory.subList(currentStateIndex + 1, executionHistory.size()).clear();
        }

        executionHistory.add(state);
        currentStateIndex = executionHistory.size() - 1;

        System.out.println("Debug info - State captured at " +
                             state.getLocation().method().name() +
                             ":" + state.getLocation().lineNumber() +
                             " (State #" + currentStateIndex + ")");
      }
    } catch (Exception e) {
      System.err.println("Debug info - Failed to capture state: " + e.getMessage());
    }
  }


  public ExecutionState stepBack() {
    if (currentStateIndex > 0 && !executionHistory.isEmpty()) {
      System.out.println("Debug info - Stepping back from state " + currentStateIndex);
      currentStateIndex--;
      return executionHistory.get(currentStateIndex);
    }
    return null;
  }

  public ExecutionState stepBack(int steps) {
    int targetIndex = Math.max(0, currentStateIndex - steps);
    if (targetIndex < currentStateIndex && !executionHistory.isEmpty()) {
      currentStateIndex = targetIndex;
      return executionHistory.get(currentStateIndex);
    }
    return null;
  }

  // Getters existants
  public VirtualMachine getVm() { return vm; }
  public ThreadReference getCurrentThread() { return currentThread; }
  public StackFrame getCurrentFrame() { return currentFrame; }
}