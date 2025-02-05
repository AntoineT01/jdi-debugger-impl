package dbg.graphic.controller;

import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;
import dbg.graphic.model.DebuggerModel;

public class DebuggerController {
  private final DebuggerModel model;
  private final CommandDispatcher dispatcher;

  public DebuggerController(DebuggerModel model) {
    this.model = model;
    this.dispatcher = new CommandDispatcher();
  }

  // Méthode utilitaire pour récupérer le contexte courant depuis le modèle
  private DebuggerContext getContext() {
    return model.getCurrentDebuggerContext();
  }

  // Commandes de base de contrôle d'exécution
  public void executeStep() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "step");
    System.out.println(result);
  }

  public void executeStepOver() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "step-over");
    System.out.println(result);
  }

  public void executeContinue() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "continue");
    System.out.println(result);
  }

  // Commandes d'information
  public void executeFrame() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "frame");
    System.out.println(result);
  }

  public void executeTemporaries() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "temporaries");
    System.out.println(result);
  }

  public void executeStack() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "stack");
    System.out.println(result);
  }

  public void executeReceiver() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "receiver");
    System.out.println(result);
  }

  public void executeSender() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "sender");
    System.out.println(result);
  }

  public void executeReceiverVariables() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "receiver-variables");
    System.out.println(result);
  }

  public void executeMethod() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "method");
    System.out.println(result);
  }

  public void executeArguments() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "arguments");
    System.out.println(result);
  }

  public void executePrintVar(String varName) {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "print-var " + varName);
    System.out.println(result);
  }

  // Commandes de breakpoint
  public void executeBreak(String filename, int lineNumber) {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "break " + filename + " " + lineNumber);
    System.out.println(result);
  }

  public void executeBreakpoints() {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "breakpoints");
    System.out.println(result);
  }

  public void executeBreakOnce(String filename, int lineNumber) {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "break-once " + filename + " " + lineNumber);
    System.out.println(result);
  }

  public void executeBreakOnCount(String filename, int lineNumber, int count) {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "break-on-count " + filename + " " + lineNumber + " " + count);
    System.out.println(result);
  }

  public void executeBreakBeforeMethod(String methodName) {
    DebuggerContext context = getContext();
    Object result = dispatcher.dispatchCommand(context, "break-before-method-call " + methodName);
    System.out.println(result);
  }
}
