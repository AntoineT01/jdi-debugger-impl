package dbg.graphic.controller;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import dbg.DebuggerSession;
import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;
import dbg.graphic.model.DebuggerModel;
import dbg.ui.DebuggerUI;

public class DebuggerController implements DebuggerUI {
  private final DebuggerModel model;
  private final CommandDispatcher dispatcher;

  public DebuggerController(DebuggerModel model) {
    this.model = model;
    this.dispatcher = new CommandDispatcher();
  }

  /**
   * Récupère le contexte courant.
   * Si DebuggerSession contient un contexte suspendu, on le retourne ;
   * sinon, on retourne celui provenant du modèle.
   */
  private DebuggerContext getContext() {
    DebuggerContext ctx = dbg.DebuggerSession.getContext();
    if (ctx != null) {
      return ctx;
    }
    return model.getCurrentDebuggerContext();
  }

  /**
   * Méthode unifiée appelée par l'UI pour exécuter une commande (ex : "continue", "step", "step-over").
   * Elle utilise le contexte et l'EventSet stockés dans DebuggerSession, appelle le CommandDispatcher,
   * puis reprend l'exécution de la VM.
   */
  public synchronized String executeCommand(String command) {
    if (DebuggerSession.getContext() == null || DebuggerSession.getCurrentEventSet() == null) {
      return "Aucun contexte suspendu.";
    }
    String result = dispatcher.dispatchCommand(DebuggerSession.getContext(), command).toString();
    // Reprise de l'exécution de la VM
    DebuggerSession.getCurrentEventSet().resume();
    // Réinitialisation du contexte et de l'EventSet
//    DebuggerSession.setContext(null);
//    DebuggerSession.setCurrentEventSet(null);
    return result;
  }

  // Toutes les autres méthodes d'exécution délèguent à executeCommand :

  public void executeStep() {
    String res = executeCommand("step");
    System.out.println(res);
  }

  public void executeStepOver() {
    String res = executeCommand("step-over");
    System.out.println(res);
  }

  public void executeContinue() {
    String res = executeCommand("continue");
    System.out.println(res);
  }

  public void executeFrame() {
    String res = executeCommand("frame");
    System.out.println(res);
  }

  public void executeTemporaries() {
    String res = executeCommand("temporaries");
    System.out.println(res);
  }

  public void executeStack() {
    String res = executeCommand("stack");
    System.out.println(res);
  }

  public void executeReceiver() {
    String res = executeCommand("receiver");
    System.out.println(res);
  }

  public void executeSender() {
    String res = executeCommand("sender");
    System.out.println(res);
  }

  public void executeReceiverVariables() {
    String res = executeCommand("receiver-variables");
    System.out.println(res);
  }

  public void executeMethod() {
    String res = executeCommand("method");
    System.out.println(res);
  }

  public void executeArguments() {
    String res = executeCommand("arguments");
    System.out.println(res);
  }

  public void executePrintVar(String varName) {
    String res = executeCommand("print-var " + varName);
    System.out.println(res);
  }

  public void executeBreak(String filename, int lineNumber) {
    String res = executeCommand("break " + filename + " " + lineNumber);
    System.out.println(res);
  }

  public void executeBreakpoints() {
    String res = executeCommand("breakpoints");
    System.out.println(res);
  }

  public void executeBreakOnce(String filename, int lineNumber) {
    String res = executeCommand("break-once " + filename + " " + lineNumber);
    System.out.println(res);
  }

  public void executeBreakOnCount(String filename, int lineNumber, int count) {
    String res = executeCommand("break-on-count " + filename + " " + lineNumber + " " + count);
    System.out.println(res);
  }

  public void executeBreakBeforeMethod(String methodName) {
    String res = executeCommand("break-before-method-call " + methodName);
    System.out.println(res);
  }

  @Override
  public void showOutput(String message) {
    System.out.println("Controller Output: " + message);
  }

  @Override
  public String getCommand(DebuggerContext context) {
    return "";
  }

  @Override
  public boolean isBlocking() {
    // Ici, on considère que le contrôleur est utilisé en mode GUI asynchrone.
    return false;
  }
}
