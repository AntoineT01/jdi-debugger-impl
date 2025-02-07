package dbg.graphic.controller;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import dbg.DebuggerSession;
import dbg.command.CommandDispatcher;
import dbg.command.DebuggerContext;
import dbg.graphic.model.Breakpoint;
import dbg.graphic.model.DebuggerModel;
import dbg.ui.DebuggerUI;

import java.util.List;

public class DebuggerController implements DebuggerUI {
  private final DebuggerModel model;
  private final CommandDispatcher dispatcher;
  private static DebuggerController instance;

  public DebuggerController(DebuggerModel model) {
    this.model = model;
    this.dispatcher = new CommandDispatcher();
    instance = this;
  }

  public static DebuggerController getInstance() {
    return instance;
  }

  public DebuggerModel getModel() {
    return model;
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
  public synchronized String executeCommand(String command, boolean resume) {
    if (DebuggerSession.getContext() == null || DebuggerSession.getCurrentEventSet() == null) {
      return "Aucun contexte suspendu.";
    }
    String result = dispatcher.dispatchCommand(DebuggerSession.getContext(), command).toString();
    // Reprise de l'exécution de la VM
    if (resume) {
      DebuggerSession.getCurrentEventSet().resume();
    }

    getAndDisplayCurrentBreakpoints();

    getModel().updateState();
    return result;
  }

  // Toutes les autres méthodes d'exécution délèguent à executeCommand :

  public void executeStep() {
    String res = executeCommand("step", true);
    System.out.println(res);
  }

  public void executeStepOver() {
    String res = executeCommand("step-over", true);
    System.out.println(res);
  }

  public void executeContinue() {
    String res = executeCommand("continue", true);
    System.out.println(res);
  }

  public void executeFrame() {
    String res = executeCommand("frame", false);
    System.out.println(res);
  }

  public void executeTemporaries() {
    String res = executeCommand("temporaries", false);
    System.out.println(res);
  }

  public void executeStack() {
    String res = executeCommand("stack", false);
    System.out.println(res);
  }

  public void executeReceiver() {
    String res = executeCommand("receiver", false);
    System.out.println(res);
  }

  public void executeSender() {
    String res = executeCommand("sender", false);
    System.out.println(res);
  }

  public void executeReceiverVariables() {
    String res = executeCommand("receiver-variables", false);
    System.out.println(res);
  }

  public void executeMethod() {
    String res = executeCommand("method", false);
    System.out.println(res);
  }

  public void executeArguments() {
    String res = executeCommand("arguments", false);
    System.out.println(res);
  }

  public void executePrintVar(String varName) {
    String res = executeCommand("print-var " + varName, false);
    System.out.println(res);
  }

  public void executeBreak(String filename, int lineNumber) {
    String res = executeCommand("break " + filename + " " + lineNumber, false);
    System.out.println(res);
  }

  public void executeBreakpoints() {
    String res = executeCommand("breakpoints", false);
    System.out.println(res);
  }

  public void executeBreakOnce(String filename, int lineNumber) {
    String res = executeCommand("break-once " + filename + " " + lineNumber, false);
    System.out.println(res);
  }

  public void executeBreakOnCount(String filename, int lineNumber, int count) {
    String res = executeCommand("break-on-count " + filename + " " + lineNumber + " " + count, false);
    System.out.println(res);
  }

  public void executeBreakBeforeMethod(String methodName) {
    String res = executeCommand("break-before-method-call " + methodName, false);
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

  private void getAndDisplayCurrentBreakpoints() {
    String bpResult = dispatcher.dispatchCommand(DebuggerSession.getContext(), "breakpoints").toString();
    if (bpResult != null && !bpResult.isEmpty()) {
      String[] bpArray = bpResult.split("\\n");
      getModel().resetBreakpointsStored();
      for (String bpString : bpArray) {
        bpString = bpString.trim();
        if (bpString.isEmpty()) {
          continue;
        }
        // On s'attend à un format "dbg.JDISimpleDebuggee:8"
        String[] parts = bpString.split(":");
        if (parts.length == 2) {
          String className = parts[0].trim();
          try {
            int lineNumber = Integer.parseInt(parts[1].trim());
            // Vérifier si un breakpoint identique existe déjà dans le modèle
            getModel().addBreakpoint(new Breakpoint(className, lineNumber));
          } catch (NumberFormatException e) {
            System.err.println("Erreur de conversion pour la ligne: " + bpString);
          }
        } else {
          System.err.println("Format inattendu pour le breakpoint: " + bpString);
        }
      }
    }
  }
}
