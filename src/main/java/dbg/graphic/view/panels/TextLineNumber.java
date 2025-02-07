package dbg.graphic.view.panels;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * Composant permettant d'afficher les numéros de lignes à gauche d'un JTextComponent.
 * Adapté depuis des exemples disponibles en ligne.
 */
public class TextLineNumber extends JPanel implements CaretListener, DocumentListener, PropertyChangeListener {
  private final JTextComponent component;
  private FontMetrics fontMetrics;
  private int lineHeight;
  private int currentDigits;
  private final int minimumDisplayDigits = 3;
  private final int leftMargin = 5;

  public TextLineNumber(JTextComponent component) {
    this.component = component;
    setFont(component.getFont());
    component.getDocument().addDocumentListener(this);
    component.addCaretListener(this);
    component.addPropertyChangeListener("font", this);
    setPreferredWidth();
  }

  private void setPreferredWidth() {
    int lines = component.getDocument().getDefaultRootElement().getElementCount();
    int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);
    if (currentDigits != digits) {
      currentDigits = digits;
      fontMetrics = getFontMetrics(getFont());
      int width = leftMargin * 2 + fontMetrics.charWidth('0') * digits;
      Dimension d = new Dimension(width, Integer.MAX_VALUE);
      setPreferredSize(d);
      setSize(d);
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    // Détermine la zone visible du composant texte
    Rectangle clip = g.getClipBounds();
    int startOffset = component.viewToModel2D(new Point(0, clip.y));
    int endOffset = component.viewToModel2D(new Point(0, clip.y + clip.height));

    while (startOffset <= endOffset) {
      try {
        Rectangle r = component.modelToView2D(startOffset).getBounds();
        int lineNumber = component.getDocument().getDefaultRootElement().getElementIndex(startOffset) + 1;
        String text = String.valueOf(lineNumber);
        int x = getWidth() - leftMargin - fontMetrics.stringWidth(text);
        int y = r.y + r.height - fontMetrics.getDescent();
        g.drawString(text, x, y);
        startOffset = Utilities.getRowEnd(component, startOffset) + 1;
      } catch (Exception e) {
        break;
      }
    }
  }

  // --- Implémentations des interfaces ---
  @Override
  public void caretUpdate(CaretEvent e) {
    repaint();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    setPreferredWidth();
    repaint();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    setPreferredWidth();
    repaint();
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    setPreferredWidth();
    repaint();
  }

  @Override
  public void propertyChange(java.beans.PropertyChangeEvent evt) {
    if (evt.getNewValue() instanceof Font) {
      setFont((Font) evt.getNewValue());
      setPreferredWidth();
    }
  }
}
