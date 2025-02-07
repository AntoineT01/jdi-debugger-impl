package dbg.graphic.view.panels;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;

public class SourcePanel extends DebuggerPanel {
  private final JTextArea sourceArea;
  private final JScrollPane scrollPane;

  public SourcePanel() {
    setLayout(new BorderLayout());
    sourceArea = new JTextArea();
    sourceArea.setEditable(false);
    scrollPane = new JScrollPane(sourceArea);
    add(scrollPane, BorderLayout.CENTER);
    scrollPane.setRowHeaderView(new TextLineNumber(sourceArea));
  }

  /**
   * Met à jour le contenu affiché et surligne la ligne courante s'il y en a une (> 0).
   */
  public void updateContent(String sourceCode, int currentLine) {
    sourceArea.setText(sourceCode);
    if (currentLine > 0) {
      highlightLine(currentLine);
    } else {
      // Aucun surlignage si currentLine <= 0
      sourceArea.getHighlighter().removeAllHighlights();
    }
  }

  /**
   * Surligne la ligne spécifiée dans le JTextArea.
   * @param line Le numéro de la ligne à surligner (1-indexé)
   */
  private void highlightLine(int line) {
    // Supprimer les surlignages précédents
    sourceArea.getHighlighter().removeAllHighlights();
    try {
      // Calculer les offsets de la ligne (le JTextArea est 0-indexé, d'où line - 1)
      int startOffset = sourceArea.getLineStartOffset(line - 1);
      int endOffset = sourceArea.getLineEndOffset(line - 1);
      // Ajouter le surlignage avec une couleur (ici jaune)
      sourceArea.getHighlighter().addHighlight(startOffset, endOffset,
        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
      // Optionnel : positionner le caret pour que la ligne soit visible
      sourceArea.setCaretPosition(startOffset);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  String getTitle() {
    return "Source Code";
  }
}
