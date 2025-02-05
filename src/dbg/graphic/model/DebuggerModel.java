package dbg.graphic.model;

import java.util.*;

public class DebuggerModel extends Observable {
  private String currentSourceCode;
  private List<String> callStack;
  private Map<String, String> variables;
  private List<Breakpoint> breakpoints;
  private int currentLine;

  public DebuggerModel() {
    callStack = new ArrayList<>();
    variables = new HashMap<>();
    breakpoints = new ArrayList<>();
  }

  public void updateState(String sourceCode, int line, List<String> stack, Map<String, String> vars) {
    this.currentSourceCode = sourceCode;
    this.currentLine = line;
    this.callStack = stack;
    this.variables = vars;
    setChanged();
    notifyObservers();
  }

  public String getCurrentSourceCode() { return currentSourceCode; }
  public int getCurrentLine() { return currentLine; }
  public List<String> getCallStack() { return callStack; }
  public Map<String, String> getVariables() { return variables; }
  public List<Breakpoint> getBreakpoints() { return breakpoints; }
}