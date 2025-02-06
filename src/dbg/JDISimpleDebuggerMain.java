package dbg;

import dbg.graphic.controller.DebuggerController;
import dbg.graphic.model.DebuggerModel;
import dbg.graphic.view.DebuggerGUI;

import javax.swing.*;

public class JDISimpleDebuggerMain {
  public static void main(String[] args) throws Exception {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        e.printStackTrace();
      }

      DebuggerModel model = new DebuggerModel();
      DebuggerController controller = new DebuggerController(model);
      DebuggerGUI gui = new DebuggerGUI(model, controller);

      ScriptableDebugger debuggerInstance = new ScriptableDebugger(controller);
      debuggerInstance.attachTo(JDISimpleDebuggee.class);
      gui.setVisible(true);
    });

  }
}

