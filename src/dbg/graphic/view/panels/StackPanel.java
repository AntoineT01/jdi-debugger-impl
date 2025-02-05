package dbg.graphic.view.panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StackPanel extends DebuggerPanel {
  private final JList<String> stackList;

  public StackPanel() {
    stackList = new JList<>();
    add(new JScrollPane(stackList), BorderLayout.CENTER);
  }

  public void updateContent(List<String> stack) {
    if (stack == null) {
      stackList.setListData(new String[0]);
    } else {
      stackList.setListData(stack.toArray(new String[0]));
    }
  }

  @Override
  String getTitle() { return "Call Stack"; }
}