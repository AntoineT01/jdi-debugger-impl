package dbg;

import dbg.command.DebuggerContext;
import dbg.ui.DebuggerUI;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests d’intégration pour vérifier que les commandes du débogueur
 * (continue, break, step, break-once) sont traitées correctement.
 */
public class ScriptableDebuggerTest {

  /**
   * Implémentation "fake" de DebuggerUI pour simuler l’entrée utilisateur en mode CLI.
   */
  static class FakeCLIUI implements DebuggerUI {
    private Queue<String> commands = new LinkedList<>();
    private StringBuilder output = new StringBuilder();

    public void addCommand(String cmd) {
      commands.add(cmd);
    }

    public String getOutput() {
      return output.toString();
    }

    @Override
    public void showOutput(String out) {
      System.out.println(out);
      output.append(out).append("\n");
    }

    @Override
    public String getCommand(DebuggerContext context) {
      String cmd = commands.poll();
      if (cmd == null) {
        cmd = "continue";
      }
      System.out.println("FakeCLIUI retourne la commande: " + cmd);
      return cmd;
    }

    @Override
    public boolean isBlocking() {
      return true;
    }
  }

  /**
   * Test simple qui vérifie que la commande "continue" reprend l’exécution.
   */
  @Test
  public void testContinueCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    // Ajoutez plusieurs commandes "continue" au cas où le débogueur en attendrait plus d'une
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000); // timeout 30 secondes

    String output = fakeUI.getOutput();
    assertTrue(output.toLowerCase().contains("resume: continue"),
      "La sortie doit contenir 'RESUME: continue'");
  }

  /**
   * Test pour la commande break-once.
   * On simule la saisie de la commande break-once sur la ligne 28 puis continue.
   */
  @Test
  public void testBreakOnceCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    fakeUI.addCommand("break-once dbg.JDISimpleDebuggee 28");
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000);

    String output = fakeUI.getOutput();
    // On vérifie que le message indiquant la suppression du breakpoint one-shot apparaisse.
    assertTrue(output.contains("Breakpoint one-shot supprimé après exécution") ||
               output.contains("Breakpoint one-shot removed after being hit"),
      "La sortie doit indiquer que le breakpoint one-shot a été supprimé.");
  }

  /**
   * Test pour la commande break (non one-shot).
   * On simule la saisie d'une commande break sur la ligne 28 puis continue.
   */
  @Test
  public void testBreakCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    fakeUI.addCommand("break dbg.JDISimpleDebuggee 28");
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000);

    String output = fakeUI.getOutput();
    // On s'attend à voir un message indiquant que le breakpoint a été installé ou atteint.
    assertTrue(output.contains("Breakpoint installé dans") || output.contains("Breakpoint atteint à"),
      "La sortie doit indiquer qu'un breakpoint a été installé/atteint.");
  }

  /**
   * Test pour la commande step.
   * On simule la saisie de la commande step puis continue.
   * Pour ce test, on suppose que le CommandDispatcher renvoie une chaîne commençant par "RESUME: step".
   */
  @Test
  public void testStepCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    fakeUI.addCommand("step");
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000);

    String output = fakeUI.getOutput();
    // On vérifie que la sortie contient bien le message de reprise lié à la commande step.
    assertTrue(output.contains("RESUME: step") || output.contains("Step"),
      "La sortie doit indiquer l'exécution de la commande step.");
  }
}
