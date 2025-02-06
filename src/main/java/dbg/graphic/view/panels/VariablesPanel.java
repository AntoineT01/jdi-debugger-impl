package dbg.graphic.view.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class VariablesPanel extends DebuggerPanel {
  private final JTable variablesTable;

  public VariablesPanel() {
    variablesTable = new JTable();
    add(new JScrollPane(variablesTable), BorderLayout.CENTER);
  }

  public void updateContent(Map<String, String> variables) {
    DefaultTableModel model = new DefaultTableModel(new String[]{"Name", "Value"}, 0);
    variables.forEach((key, value) -> model.addRow(new Object[]{key, value}));
    variablesTable.setModel(model);
  }

  @Override
  String getTitle() { return "Variables"; }
}