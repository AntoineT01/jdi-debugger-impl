package dbg.command;

import com.sun.jdi.*;

public class DebuggerContext {
  private final VirtualMachine vm;
  private final ThreadReference currentThread;
  private final StackFrame currentFrame;

  public DebuggerContext(VirtualMachine vm, ThreadReference currentThread, StackFrame currentFrame) {
    this.vm = vm;
    this.currentThread = currentThread;
    this.currentFrame = currentFrame;
  }

  public VirtualMachine getVm() {
    return vm;
  }

  public ThreadReference getCurrentThread() {
    return currentThread;
  }

  public StackFrame getCurrentFrame() {
    return currentFrame;
  }

  // Vous pouvez ajouter d'autres getters/setters au besoin
}
