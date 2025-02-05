package dbg.graphic.model;

import com.sun.jdi.*;
import java.util.*;

public class DebuggerContext {
  private final VirtualMachine vm;
  private final ThreadReference thread;
  private final StackFrame currentFrame;

  public DebuggerContext(VirtualMachine vm, ThreadReference thread, StackFrame frame) {
    this.vm = vm;
    this.thread = thread;
    this.currentFrame = frame;
  }

  public VirtualMachine getVM() {
    return vm;
  }

  public ThreadReference getThread() {
    return thread;
  }

  public StackFrame getCurrentFrame() {
    return currentFrame;
  }

  public List<LocalVariable> getLocalVariables() throws AbsentInformationException {
    return currentFrame != null ? currentFrame.visibleVariables() : Collections.emptyList();
  }

  public Value getLocalVariableValue(LocalVariable var) {
    return currentFrame != null ? currentFrame.getValue(var) : null;
  }

  public ObjectReference getThisObject() {
    return currentFrame != null ? currentFrame.thisObject() : null;
  }

  public Location getCurrentLocation() {
    return currentFrame != null ? currentFrame.location() : null;
  }

  public Method getCurrentMethod() {
    return getCurrentLocation() != null ? getCurrentLocation().method() : null;
  }
}