package dbg.graphic.view.panels;

import javax.swing.*;
import java.awt.*;

public class SourcePanel extends DebuggerPanel {
  private final JTextArea sourceArea;
  private final JScrollPane scrollPane;

  public SourcePanel() {
    sourceArea = new JTextArea();
    sourceArea.setEditable(false);
    scrollPane = new JScrollPane(sourceArea);
    add(scrollPane, BorderLayout.CENTER);
  }

  public void updateContent(String sourceCode, int currentLine) {
    sourceArea.setText(sourceCode);
    highlightLine(currentLine);
  }

  private void highlightLine(int line) {
    // Highlight implementation
  }

  @Override
  String getTitle() { return "Source Code"; }
}