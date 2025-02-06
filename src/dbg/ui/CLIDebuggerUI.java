package dbg.ui;

import dbg.command.DebuggerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implémentation en mode CLI de l'interface DebuggerUI.
 */
public class CLIDebuggerUI implements DebuggerUI {

  private final BufferedReader reader;

  public CLIDebuggerUI() {
    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  @Override
  public void showOutput(String output) {
    System.out.println(output);
  }

  @Override
  public String getCommand(DebuggerContext context) {
    System.out.print("Commande > ");
    try {
      return reader.readLine();
    } catch (IOException e) {
      showOutput("Erreur de lecture de commande: " + e.getMessage());
      return null;
    }
  }

  @Override
  public boolean isBlocking() {
    return true;
  }
}
