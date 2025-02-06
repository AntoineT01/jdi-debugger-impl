package dbg.ui;

import dbg.command.DebuggerContext;
import dbg.graphic.controller.DebuggerController;
import dbg.graphic.model.DebuggerModel;
import dbg.graphic.view.DebuggerGUI;

import javax.swing.*;

public class GUIDebuggerUI implements DebuggerUI {
  private DebuggerGUI gui;

  public GUIDebuggerUI() {
    // Instanciez le modèle et le contrôleur, puis la GUI.
    DebuggerModel model = new DebuggerModel();
    DebuggerController controller = new DebuggerController(model);
    SwingUtilities.invokeLater(() -> {
      gui = new DebuggerGUI(model, controller);
      gui.setVisible(true);
    });
  }

  @Override
  public void showOutput(String output) {
    // Affichage dans une console dédiée ou dans la console système
    System.out.println("GUI Output: " + output);
    // Si votre GUI a une zone de console, vous pouvez l'actualiser ici.
  }

  @Override
  public String getCommand(DebuggerContext context) {
    // En mode GUI, la saisie se fait via des boutons, donc cette méthode ne sera pas utilisée.
    return null;
  }

  @Override
  public boolean isBlocking() {
    // En mode GUI, on ne bloque pas.
    return false;
  }
}
