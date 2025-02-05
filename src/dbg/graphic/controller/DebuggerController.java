package dbg.graphic.controller;

import dbg.graphic.model.DebuggerModel;

public class DebuggerController {
  private final DebuggerModel model;

  public DebuggerController(DebuggerModel model) {
    this.model = model;
  }

  // Commandes de base de contrôle d'exécution
  public void executeStep() {
    // Implémentation de step
  }

  public void executeStepOver() {
    // Implémentation de step-over
  }

  public void executeContinue() {
    // Implémentation de continue
  }

  // Commandes d'information
  public void executeFrame() {
    // Implémentation de frame
  }

  public void executeTemporaries() {
    // Implémentation de temporaries
  }

  public void executeStack() {
    // Implémentation de stack
  }

  public void executeReceiver() {
    // Implémentation de receiver
  }

  public void executeSender() {
    // Implémentation de sender
  }

  public void executeReceiverVariables() {
    // Implémentation de receiver-variables
  }

  public void executeMethod() {
    // Implémentation de method
  }

  public void executeArguments() {
    // Implémentation de arguments
  }

  public void executePrintVar(String varName) {
    // Implémentation de print-var
  }

  // Commandes de breakpoint
  public void executeBreak(String filename, int lineNumber) {
    // Implémentation de break
  }

  public void executeBreakpoints() {
    // Implémentation de breakpoints
  }

  public void executeBreakOnce(String filename, int lineNumber) {
    // Implémentation de break-once
  }

  public void executeBreakOnCount(String filename, int lineNumber, int count) {
    // Implémentation de break-on-count
  }

  public void executeBreakBeforeMethod(String methodName) {
    // Implémentation de break-before-method-call
  }
}