package dbg.graphic.model;

import com.sun.jdi.*;
import java.util.*;

public class DebuggerContext {
  private VirtualMachine vm;
  private ThreadReference thread;  // Correction ici
  private StackFrame currentFrame;
  private Location currentLocation;
  private List<Breakpoint> breakpoints = new ArrayList<>();

  public DebuggerContext(VirtualMachine vm, ThreadReference thread, StackFrame currentFrame) {
    this.vm = vm;
    this.thread = thread;  // Correction ici
    this.currentFrame = currentFrame;
    try {
      this.currentLocation = currentFrame != null ? currentFrame.location() : null;
    } catch (Exception e) {
      this.currentLocation = null;
    }
  }

  // Ajout des getters
  public VirtualMachine getVm() {
    return vm;
  }

  public ThreadReference getThread() {
    return thread;
  }

  public StackFrame getCurrentFrame() {
    return currentFrame;
  }

  public Location getCurrentLocation() {
    return currentLocation;
  }

  // Méthodes pour obtenir les variables locales
  public List<LocalVariable> getLocalVariables() throws AbsentInformationException {
    return currentFrame != null ? currentFrame.visibleVariables() : new ArrayList<>();
  }

  public Value getLocalVariableValue(LocalVariable var) {
    try {
      return currentFrame != null ? currentFrame.getValue(var) : null;
    } catch (Exception e) {
      return null;
    }
  }
}