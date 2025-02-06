package dbg.ui;

import dbg.command.DebuggerContext;

import javax.swing.*;
import java.awt.*;

/**
 * Implémentation en mode GUI de l'interface DebuggerUI.
 * Cette version utilise une fenêtre Swing avec une zone de texte pour l'affichage et un champ pour saisir les commandes.
 */
public class GUIDebuggerUI implements DebuggerUI {

  private JFrame frame;
  private JTextArea outputArea;
  private JTextField commandField;
  private final Object lock = new Object();
  private String commandResult = null;

  public GUIDebuggerUI() {
    frame = new JFrame("Debugger GUI");
    outputArea = new JTextArea(20, 50);
    outputArea.setEditable(false);
    commandField = new JTextField(50);

    // Dans cette version, l'action du bouton "continue" (ou autre) déclenche la reprise.
    commandField.addActionListener(e -> {
      synchronized (lock) {
        commandResult = commandField.getText();
        commandField.setText("");
        lock.notify();
      }
    });

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
    panel.add(commandField, BorderLayout.SOUTH);

    frame.setContentPane(panel);
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  @Override
  public void showOutput(String output) {
    SwingUtilities.invokeLater(() -> outputArea.append(output + "\n"));
  }

  @Override
  public String getCommand(DebuggerContext context) {
    // Ici, on peut implémenter une logique si besoin, mais en mode GUI,
    // on considère généralement que l'action de l'utilisateur via le bouton suffit.
    SwingUtilities.invokeLater(() -> commandField.setEnabled(true));
    synchronized (lock) {
      try {
        lock.wait();  // Attente que l'utilisateur saisisse une commande
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    SwingUtilities.invokeLater(() -> commandField.setEnabled(false));
    return commandResult;
  }

  @Override
  public boolean isBlocking() {
    return false; // Le mode GUI se base sur des boutons et des callbacks, pas sur la lecture bloquante.
  }
}
