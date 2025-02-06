package dbg.graphic.view.panels;

import javax.swing.*;
import java.awt.*;

public abstract class DebuggerPanel extends JPanel {
  public DebuggerPanel() {
    setBorder(BorderFactory.createTitledBorder(getTitle()));
    setLayout(new BorderLayout());
  }

  abstract String getTitle();
}