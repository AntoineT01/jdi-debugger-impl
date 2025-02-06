package dbg;

import dbg.command.DebuggerContext;
import dbg.ui.DebuggerUI;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
   * Test dédié à la commande step.
   * On simule deux commandes "step" successives, puis une commande "continue".
   * On s'attend à voir dans la sortie :
   * - Le message de confirmation du StepCommand ("RESUME: Step into command executed.")
   * - Au moins deux occurrences de "StepEvent reached at:" indiquant que des StepEvent ont été interceptés.
   */
  @Test
  public void testStepCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    // Simuler deux commandes step, puis continue pour terminer la session
    fakeUI.addCommand("step");
    fakeUI.addCommand("step");
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000); // timeout de 30 secondes

    String output = fakeUI.getOutput();
    // Vérifier que le message de step est présent
    assertTrue(output.toLowerCase().contains("resume: step into command executed"),
      "La sortie doit contenir le message de reprise du step.");

    // Compter le nombre d'occurrences de "StepEvent reached at:" dans la sortie
    int stepEventCount = countOccurrences(output, "StepEvent reached at:");
    if (stepEventCount < 2) {
      fail("Expected at least 2 step events, but found " + stepEventCount);
    }
  }

  // Méthode utilitaire pour compter les occurrences d'une sous-chaîne dans une chaîne donnée
  private int countOccurrences(String str, String subStr) {
    int count = 0;
    int idx = 0;
    while ((idx = str.indexOf(subStr, idx)) != -1) {
      count++;
      idx += subStr.length();
    }
    return count;
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
   * Test pour la commande break-on-count.
   * On simule la saisie de la commande "break-on-count" sur la ligne 28 avec un hit count de 3,
   * puis "continue". On s'attend à voir dans la sortie un message indiquant que,
   * lors du 3ᵉ hit, le breakpoint est effectivement pris.
   */
  @Test
  public void testBreakOnCountCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    // La commande doit préciser la classe, la ligne et le hit count.
    fakeUI.addCommand("break-on-count dbg.JDISimpleDebuggee 28 8");
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000); // timeout 30 secondes

    String output = fakeUI.getOutput();
    // On s'attend à ce que lors du 3ᵉ hit, un message indiquant "Hit count for this breakpoint: 3" et
    // "Breakpoint reached on target count (3)." apparaisse.
    assertTrue(output.contains("Hit count for this breakpoint: 8"),
      "La sortie doit contenir le hit count 8 pour break-on-count");
    assertTrue(output.contains("Breakpoint atteint sur le nombre ciblé (8)."),
      "La sortie doit indiquer que le breakpoint est atteint sur le multiple ciblé 8.");
  }

  /**
   * Test pour la commande break-before-method-call.
   * On simule la saisie de la commande "break-before-method-call" pour la méthode hello,
   * puis "continue". On s'attend à ce que le debugger s'arrête avant l'appel de la méthode hello.
   */
  @Test
  public void testBreakBeforeMethodCallCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    // La commande doit spécifier la classe et la méthode cible.
    fakeUI.addCommand("break dbg.JDISimpleDebuggee 8");
    fakeUI.addCommand("continue");
    fakeUI.addCommand("break-before-method-call hello");
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000);

    String output = fakeUI.getOutput();
    // On s'attend à voir un message indiquant un arrêt avant l'appel de la méthode hello.
    assertTrue(output.contains("MethodEntryEvent: Arrêt avant l'appel de la méthode : hello"),
      "La sortie doit indiquer un break avant l'appel de la méthode hello.");
  }

  /**
   * Test dédié à la commande "step over".
   * La séquence simulée est la suivante :
   *   1. On installe un breakpoint à la ligne 8.
   *   2. On envoie "continue" pour atteindre ce breakpoint.
   *   3. On envoie "step" pour s'arrêter sur la ligne 8.
   *   4. On envoie "step-over" pour exécuter l'instruction courante et se positionner sur la ligne suivante (par exemple, la ligne 9).
   *   5. On envoie "continue" pour terminer le debug.
   * On vérifie que le message de reprise du step over est présent dans la sortie
   * et que la sortie indique un StepEvent sur la ligne 9.
   */
  @Test
  public void testStepOverCommand() throws Exception {
    FakeCLIUI fakeUI = new FakeCLIUI();
    // 1. Installer un breakpoint à la ligne 8
    fakeUI.addCommand("break dbg.JDISimpleDebuggee 8");
    // 2. Continuer jusqu'à atteindre le breakpoint
    fakeUI.addCommand("continue");
    // 3. Exécuter une commande step pour se positionner sur la ligne 8
    fakeUI.addCommand("step");
    // 4. Exécuter la commande step-over pour passer à la ligne suivante (par exemple, la ligne 9)
    fakeUI.addCommand("step-over");
    // 5. Envoyer "continue" pour terminer
    fakeUI.addCommand("continue");

    ScriptableDebugger debugger = new ScriptableDebugger(fakeUI);
    Thread debuggerThread = new Thread(() -> debugger.attachTo(JDISimpleDebuggee.class));
    debuggerThread.start();
    debuggerThread.join(30000); // timeout 30 secondes

    String output = fakeUI.getOutput();

    // Vérifier que le message de reprise pour le step over est présent.
    // Par exemple, si votre StepOverCommand renvoie "RESUME: Step over command executed."
    assertTrue(output.toLowerCase().contains("resume: step over command executed"),
      "La sortie doit contenir le message de reprise du step over.");

    // Vérifier que la sortie contient un StepEvent indiquant que l'exécution s'est positionnée sur la ligne suivante (par ex. ":9")
    if (!output.contains("dbg.JDISimpleDebuggee:9")) {
      fail("La sortie doit indiquer un StepEvent sur la ligne 9.");
    }
  }
}
