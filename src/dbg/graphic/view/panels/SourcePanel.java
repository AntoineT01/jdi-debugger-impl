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
    sourceArea.setText(sourceCode != null ? sourceCode : "");
    if (currentLine >= 0) {
      highlightLine(currentLine);
    }
  }

  private void highlightLine(int line) {
    try {
      String[] lines = sourceArea.getText().split("\n");
      int pos = 0;
      for (int i = 0; i < lines.length; i++) {
        if (i == line - 1) {
          sourceArea.setCaretPosition(pos);
          break;
        }
        pos += lines[i].length() + 1;
      }
    } catch (Exception e) {
      // Ignore highlighting errors
    }
  }

  @Override
  String getTitle() { return "Source Code"; }
}