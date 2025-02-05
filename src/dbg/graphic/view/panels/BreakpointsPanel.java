package dbg.graphic.view.panels;

import dbg.graphic.model.Breakpoint;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BreakpointsPanel extends DebuggerPanel {
  private final JList<Breakpoint> breakpointsList;

  public BreakpointsPanel() {
    breakpointsList = new JList<>();
    add(new JScrollPane(breakpointsList), BorderLayout.CENTER);
  }

  public void updateContent(List<Breakpoint> breakpoints) {
    if (breakpoints == null) {
      breakpointsList.setListData(new Breakpoint[0]);
    } else {
      breakpointsList.setListData(breakpoints.toArray(new Breakpoint[0]));
    }
  }

  @Override
  String getTitle() { return "Breakpoints"; }
}