package dbg;

import dbg.ui.GUIDebuggerUI;

public class DebuggerMain {
  public static void main(String[] args) {
    // Instanciation de l'UI GUI
    GUIDebuggerUI ui = new GUIDebuggerUI();
    // Création du débogueur avec cette UI
    ScriptableDebugger debugger = new ScriptableDebugger(ui);
    // Attache le débogueur à la classe cible (votre debuggee)
    debugger.attachTo(JDISimpleDebuggee.class);
  }
}
