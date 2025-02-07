package dbg.graphic.view;

import dbg.graphic.controller.DebuggerController;
import dbg.graphic.model.DebuggerModel;
import dbg.graphic.view.panels.BreakpointsPanel;
import dbg.graphic.view.panels.ControlPanel;
import dbg.graphic.view.panels.SourcePanel;
import dbg.graphic.view.panels.StackPanel;
import dbg.graphic.view.panels.VariablesPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class DebuggerGUI extends JFrame implements Observer {
  private final DebuggerController controller;
  private final DebuggerModel model;

  private SourcePanel sourcePanel;
  private StackPanel stackPanel;
  private VariablesPanel variablesPanel;
  private BreakpointsPanel breakpointsPanel;
  private ControlPanel controlPanel;

  public DebuggerGUI(DebuggerModel model, DebuggerController controller) {
    this.model = model;
    this.controller = controller;
    model.addObserver(this);

    setupWindow();
    initComponents();
    pack();
    setLocationRelativeTo(null);
    sourcePanel.updateContent(model.getCurrentSourceCode(), model.getCurrentLine());
  }

  private void setupWindow() {
    setTitle("Java Debugger");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    setBackground(Color.WHITE);
  }

  private void initComponents() {
    // Contrôle en haut
    controlPanel = new ControlPanel(controller);
    add(controlPanel, BorderLayout.NORTH);

    // Panel principal avec séparation verticale
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setDividerLocation(800);
    mainSplitPane.setDividerSize(4);
    mainSplitPane.setBorder(null);
    add(mainSplitPane, BorderLayout.CENTER);

    // Zone de code source (gauche)
    sourcePanel = new SourcePanel();
    JPanel sourceWrapper = new JPanel(new BorderLayout());
    sourceWrapper.add(sourcePanel, BorderLayout.CENTER);
    mainSplitPane.setLeftComponent(sourceWrapper);

    // Zone d'information (droite)
    JPanel rightPanel = new JPanel(new GridLayout(3, 1, 0, 1));
    rightPanel.setBackground(Color.LIGHT_GRAY);

    // Panel pour la pile d'appels
    stackPanel = new StackPanel();
    JPanel stackWrapper = new JPanel(new BorderLayout());
    stackWrapper.add(stackPanel, BorderLayout.CENTER);
    rightPanel.add(stackWrapper);

    // Panel pour les variables
    variablesPanel = new VariablesPanel();
    JPanel variablesWrapper = new JPanel(new BorderLayout());
    variablesWrapper.add(variablesPanel, BorderLayout.CENTER);
    rightPanel.add(variablesWrapper);

    // Panel pour les points d'arrêt
    breakpointsPanel = new BreakpointsPanel();
    JPanel breakpointsWrapper = new JPanel(new BorderLayout());
    breakpointsWrapper.add(breakpointsPanel, BorderLayout.CENTER);
    rightPanel.add(breakpointsWrapper);

    mainSplitPane.setRightComponent(rightPanel);

    setPreferredSize(new Dimension(1200, 800));
  }

  @Override
  public void update(Observable o, Object arg) {
    sourcePanel.updateContent(model.getCurrentSourceCode(), model.getCurrentLine());
    stackPanel.updateContent(model.getCallStack());
    variablesPanel.updateContent(model.getVariables());
    breakpointsPanel.updateContent(model.getBreakpoints());
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        e.printStackTrace();
      }

      DebuggerModel model = new DebuggerModel();
      DebuggerController controller = new DebuggerController(model);
      DebuggerGUI gui = new DebuggerGUI(model, controller);

      gui.setVisible(true);
    });
  }
}