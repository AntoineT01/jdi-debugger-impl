package dbg.graphic.controller;

import com.sun.jdi.event.*;
import dbg.ScriptableDebugger;
import dbg.command.*;
import dbg.graphic.model.DebuggerContext;
import dbg.graphic.model.DebuggerModel;
import dbg.graphic.util.ContextUpdater;
import dbg.graphic.util.ContextAdapter;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class DebuggerController {
  private final DebuggerModel model;
  private final ScriptableDebugger debugger;
  private DebuggerContext currentContext;
  private String sourceCode;

  public DebuggerController(DebuggerModel model) {
    this.model = model;
    this.debugger = new ScriptableDebugger();
    this.debugger.setController(this);
    this.currentContext = null;
  }

  public void startDebugging(Class<?> debuggeeClass) {
    loadSourceCode(debuggeeClass);
    new Thread(() -> debugger.attachTo(debuggeeClass)).start();
  }

  private void loadSourceCode(Class<?> debuggeeClass) {
    try {
      // Essaye d'abord dans src/
      String srcPath = "src/" + debuggeeClass.getName().replace('.', '/') + ".java";
      if (new File(srcPath).exists()) {
        sourceCode = new String(Files.readAllBytes(Paths.get(srcPath)));
        return;
      }

      // Essaye dans src/main/java/
      srcPath = "src/main/java/" + debuggeeClass.getName().replace('.', '/') + ".java";
      if (new File(srcPath).exists()) {
        sourceCode = new String(Files.readAllBytes(Paths.get(srcPath)));
        return;
      }

      // Essaye directement dans le package
      srcPath = debuggeeClass.getName().replace('.', '/') + ".java";
      if (new File(srcPath).exists()) {
        sourceCode = new String(Files.readAllBytes(Paths.get(srcPath)));
        return;
      }

      sourceCode = "// Source file not found: " + debuggeeClass.getName();
      System.out.println("Could not find source file for: " + debuggeeClass.getName());

    } catch (Exception e) {
      sourceCode = "// Error loading source code: " + e.getMessage();
      e.printStackTrace();
    }
  }

  public void handleEvent(Event event) {
    SwingUtilities.invokeLater(() -> {
      if (event instanceof VMStartEvent) {
        System.out.println("VM started");
        model.updateState(sourceCode, -1, new ArrayList<>(), new HashMap<>());
      }
      else if (event instanceof ClassPrepareEvent) {
        System.out.println("Class prepared");
        model.updateState(sourceCode, -1, new ArrayList<>(), new HashMap<>());
      }
      else if (event instanceof BreakpointEvent || event instanceof StepEvent) {
        currentContext = ContextUpdater.createContext(event);
        if (currentContext != null) {
          updateModelState();
        }
      }
      else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
        currentContext = null;
        model.updateState(sourceCode + "\n// Program terminated", -1, new ArrayList<>(), new HashMap<>());
      }
    });
  }

  private void updateModelState() {
    if (currentContext != null && currentContext.getCurrentLocation() != null) {
      model.updateState(
        sourceCode,
        currentContext.getCurrentLocation().lineNumber(),
        ContextUpdater.getCallStack(currentContext),
        ContextUpdater.getVariables(currentContext)
      );
    }
  }

  public void resumeExecution() {
    if (debugger.getVM() != null) {
      debugger.resume();
    }
  }

  private void executeCommand(DebugCommand command, String... args) {
    if (currentContext == null) {
      JOptionPane.showMessageDialog(null,
                                    "No debugging context available",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      Object result = command.execute(args, ContextAdapter.adapt(currentContext));
      if (result != null) {
        model.updateCommandResult(result.toString());
        if (result.toString().startsWith("RESUME:")) {
          resumeExecution();
        }
      }
      updateModelState();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null,
                                    e.getMessage(),
                                    "Command Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  public void executeStep() {
    executeCommand(new StepCommand());
  }

  public void executeStepOver() {
    executeCommand(new StepOverCommand());
  }

  public void executeContinue() {
    executeCommand(new ContinueCommand());
  }

  public void executeFrame() {
    executeCommand(new FrameCommand());
  }

  public void executeTemporaries() {
    executeCommand(new TemporariesCommand());
  }

  public void executeStack() {
    executeCommand(new StackCommand());
  }

  public void executeReceiver() {
    executeCommand(new ReceiverCommand());
  }

  public void executeSender() {
    executeCommand(new SenderCommand());
  }

  public void executeReceiverVariables() {
    executeCommand(new ReceiverVariablesCommand());
  }

  public void executeMethod() {
    executeCommand(new MethodCommand());
  }

  public void executeArguments() {
    executeCommand(new ArgumentsCommand());
  }

  public void executePrintVar(String varName) {
    executeCommand(new PrintVarCommand(), varName);
  }

  public void executeBreak(String filename, int lineNumber) {
    executeCommand(new BreakCommand(), filename, String.valueOf(lineNumber));
  }

  public void executeBreakpoints() {
    executeCommand(new BreakpointsCommand());
  }

  public void executeBreakOnce(String filename, int lineNumber) {
    executeCommand(new BreakOnceCommand(), filename, String.valueOf(lineNumber));
  }

  public void executeBreakOnCount(String filename, int lineNumber, int count) {
    executeCommand(new BreakOnCountCommand(), filename,
                   String.valueOf(lineNumber), String.valueOf(count));
  }

  public void executeBreakBeforeMethod(String methodName) {
    executeCommand(new BreakBeforeMethodCallCommand(), methodName);
  }
}