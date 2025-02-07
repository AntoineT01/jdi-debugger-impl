package dbg.ui;

import dbg.command.DebuggerContext;
import dbg.graphic.controller.DebuggerController;
import dbg.graphic.model.DebuggerModel;
import dbg.graphic.view.DebuggerGUI;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GUIDebuggerUI implements DebuggerUI {
  private DebuggerGUI gui;

  public GUIDebuggerUI() {
    DebuggerModel model = new DebuggerModel();
    DebuggerController controller = new DebuggerController(model);
    getAndSetSourceCode(model);
    SwingUtilities.invokeLater(() -> {
      gui = new DebuggerGUI(model, controller);
      gui.setVisible(true);
    });
  }

  private void getAndSetSourceCode(DebuggerModel model) {
    File currentDir = new File(System.getProperty("user.dir"));
    File projectRoot = currentDir.getParentFile().getParentFile().getParentFile().getParentFile();
    File sourceFile = new File(projectRoot + File.separator + "jdi-debugger-impl" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "dbg" + File.separator + "JDISimpleDebuggee.java");

    try {
      String sourceCode = Files.readString(sourceFile.toPath());
      model.setCurrentSourceCode(sourceCode);
    } catch (IOException e) {
      e.printStackTrace();
      model.setCurrentSourceCode("Erreur lors du chargement du code source : " + e.getMessage());
    }
  }

  @Override
  public void showOutput(String output) {
    System.out.println("GUI Output: " + output);
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
