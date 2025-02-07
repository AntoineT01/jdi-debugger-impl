package dbg.graphic.model;

import dbg.DebuggerSession;
import dbg.command.DebuggerContext;
import java.util.*;

public class DebuggerModel extends Observable {
  private String currentSourceCode;
  private List<String> callStack;
  private Map<String, String> variables;
  private final List<Breakpoint> breakpoints;
  private int currentLine;

  public int getCurrentLine() {
    return currentLine;
  }

  public void setCurrentLine(int currentLine) {
    this.currentLine = currentLine;
    setChanged();
    notifyObservers();
  }

  public DebuggerModel() {
    callStack = new ArrayList<>();
    variables = new HashMap<>();
    breakpoints = new ArrayList<>();
  }

  /**
   * Met à jour l'état global du débogueur (source, ligne courante, pile, variables)
   * et le contexte courant.
   */
  public void updateState(String sourceCode, List<String> stack, Map<String, String> vars) {
    this.currentSourceCode = sourceCode;
    this.callStack = stack;
    this.variables = vars;
    setChanged();
    notifyObservers();
  }

  public void updateState() {
    setChanged();
    notifyObservers();
  }

  public String getCurrentSourceCode() {
    return currentSourceCode;
  }

  public List<String> getCallStack() {
    return callStack;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public List<Breakpoint> getBreakpoints() {
    return breakpoints;
  }

  /**
   * Retourne le contexte courant (VM, thread, frame) utilisé pour l'exécution des commandes.
   */
  public DebuggerContext getCurrentDebuggerContext() {
    return DebuggerSession.getContext();
  }

  public void setCurrentDebuggerContext() {
    setChanged();
    notifyObservers();
  }

  public void resetBreakpointsStored() {
    breakpoints.clear();
  }

  public void addBreakpoint(Breakpoint bp) {
    breakpoints.add(bp);
    setChanged();
    notifyObservers();
  }

  public void removeBreakpoint(Breakpoint bp) {
    breakpoints.remove(bp);
    setChanged();
    notifyObservers();
  }

  public void setCurrentSourceCode(String currentSourceCode) {
    this.currentSourceCode = currentSourceCode;
    setChanged();
    notifyObservers();
  }
}
