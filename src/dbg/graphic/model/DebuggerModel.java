package dbg.graphic.model;

import java.util.*;

public class DebuggerModel extends Observable {
  private String currentSourceCode;
  private List<String> callStack;
  private Map<String, String> variables;
  private List<Breakpoint> breakpoints;
  private int currentLine;
  private String lastCommandResult;
  private boolean isRunning;

  public DebuggerModel() {
    callStack = new ArrayList<>();
    variables = new HashMap<>();
    breakpoints = new ArrayList<>();
    lastCommandResult = "";
    isRunning = false;
  }

  public void updateState(String sourceCode, int line, List<String> stack, Map<String, String> vars) {
    this.currentSourceCode = sourceCode;
    this.currentLine = line;
    this.callStack = stack != null ? stack : new ArrayList<>();
    this.variables = vars != null ? vars : new HashMap<>();
    setChanged();
    notifyObservers();
  }

  public void updateCommandResult(String result) {
    this.lastCommandResult = result;
    setChanged();
    notifyObservers("command");
  }

  public void setRunning(boolean running) {
    this.isRunning = running;
    setChanged();
    notifyObservers("running");
  }

  public void addBreakpoint(Breakpoint breakpoint) {
    breakpoints.add(breakpoint);
    setChanged();
    notifyObservers("breakpoints");
  }

  public void removeBreakpoint(Breakpoint breakpoint) {
    breakpoints.remove(breakpoint);
    setChanged();
    notifyObservers("breakpoints");
  }

  public String getCurrentSourceCode() { return currentSourceCode; }
  public int getCurrentLine() { return currentLine; }
  public List<String> getCallStack() { return callStack; }
  public Map<String, String> getVariables() { return variables; }
  public List<Breakpoint> getBreakpoints() { return breakpoints; }
  public String getLastCommandResult() { return lastCommandResult; }
  public boolean isRunning() { return isRunning; }
}