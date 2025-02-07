package dbg.graphic.view.panels;

import dbg.graphic.controller.DebuggerController;
import dbg.graphic.util.IconFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {
  private final DebuggerController controller;
  private static final Color BUTTON_BACKGROUND = Color.WHITE;
  private static final Color SEPARATOR_COLOR = new Color(200, 200, 200);

  public ControlPanel(DebuggerController controller) {
    this.controller = controller;
    setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setBackground(Color.WHITE);

    // Navigation buttons
    addButton(null, IconFactory.createStepIcon(), "Step (F8)", e -> controller.executeStep());
    addButton(null, IconFactory.createStepOverIcon(), "Step Over (F7)", e -> controller.executeStepOver());
    addButton(null, IconFactory.createPlayIcon(), "Continue (F9)", e -> controller.executeContinue());
    addSeparator();

    // Debug info buttons
    addButton("Frame", IconFactory.createFrameIcon(), "Show Current Frame", e -> controller.executeFrame());
    addButton("Temp", IconFactory.createVariablesIcon(), "Show Temporary Variables", e -> controller.executeTemporaries());
    addButton("Stack", IconFactory.createStackIcon(), "Show Call Stack", e -> controller.executeStack());
    addSeparator();

    addButton("this", IconFactory.createThisIcon(), "Show Current Receiver", e -> controller.executeReceiver());
    addButton("Sender", IconFactory.createSenderIcon(), "Show Sender", e -> controller.executeSender());
    addButton("Fields", IconFactory.createFieldsIcon(), "Show Receiver Variables", e -> controller.executeReceiverVariables());
    addSeparator();

    addButton("M", IconFactory.createMethodIcon(), "Show Current Method", e -> controller.executeMethod());
    addButton("Args", IconFactory.createArgumentsIcon(), "Show Method Arguments", e -> controller.executeArguments());
    addButton("Print", IconFactory.createPrintIcon(), "Print Variable Value", e -> showPrintVarDialog());
    addSeparator();

    // Breakpoint buttons
    addButton(null, IconFactory.createBreakpointIcon(), "Set Breakpoint", e -> showBreakpointDialog());
    addButton("List", IconFactory.createBreakpointsListIcon(), "List Breakpoints", e -> controller.executeBreakpoints());
    addButton(null, IconFactory.createBreakpointOnceIcon(), "Set One-time Breakpoint", e -> showBreakOnceDialog());
    addButton(null, IconFactory.createBreakpointCountIcon(), "Set Count Breakpoint", e -> showBreakCountDialog());
    addButton("Method", IconFactory.createBreakpointMethodIcon(), "Break Before Method", e -> showBreakMethodDialog());
  }

  private void addSeparator() {
    JSeparator separator = new JSeparator(JSeparator.VERTICAL);
    separator.setPreferredSize(new Dimension(1, 20));
    separator.setForeground(SEPARATOR_COLOR);
    add(separator);
    add(Box.createHorizontalStrut(5));
  }
  private void addButton(String text, Icon icon, String tooltip, ActionListener listener) {
    JButton button = new JButton(text);
    button.setIcon(icon);
    if (text == null) {
      button.setText("");
    }
    button.setToolTipText(tooltip);
    button.addActionListener(listener);
    styleButton(button);
    add(button);
  }

  private void styleButton(JButton button) {
    button.setBackground(BUTTON_BACKGROUND);
    button.setFocusPainted(false);
    button.setBorderPainted(true);
    button.setFont(new Font("Dialog", Font.PLAIN, 12));
    button.setMargin(new Insets(4, 4, 4, 4));
  }

  private void showPrintVarDialog() {
    String varName = JOptionPane.showInputDialog(
      this,
      "Enter variable name:",
      "Print Variable",
      JOptionPane.QUESTION_MESSAGE
    );
    if (varName != null && !varName.trim().isEmpty()) {
      controller.executePrintVar(varName.trim());
    }
  }

  private void showBreakpointDialog() {
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new GridLayout(2, 2, 5, 5));

    JTextField fileField = new JTextField(20);
    fileField.setText("dbg.JDISimpleDebuggee");
    JTextField lineField = new JTextField(10);

    inputPanel.add(new JLabel("File name:"));
    inputPanel.add(fileField);
    inputPanel.add(new JLabel("Line number:"));
    inputPanel.add(lineField);

    int result = JOptionPane.showConfirmDialog(
      this,
      inputPanel,
      "Set Breakpoint",
      JOptionPane.OK_CANCEL_OPTION
    );

    if (result == JOptionPane.OK_OPTION) {
      try {
        int line = Integer.parseInt(lineField.getText().trim());
        controller.executeBreak(fileField.getText().trim(), line);
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
          this,
          "Invalid line number",
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  private void showBreakOnceDialog() {
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new GridLayout(2, 2, 5, 5));

    JTextField fileField = new JTextField(20);
    JTextField lineField = new JTextField(10);

    inputPanel.add(new JLabel("File name:"));
    inputPanel.add(fileField);
    inputPanel.add(new JLabel("Line number:"));
    inputPanel.add(lineField);

    int result = JOptionPane.showConfirmDialog(
      this,
      inputPanel,
      "Set One-time Breakpoint",
      JOptionPane.OK_CANCEL_OPTION
    );

    if (result == JOptionPane.OK_OPTION) {
      try {
        int line = Integer.parseInt(lineField.getText().trim());
        controller.executeBreakOnce(fileField.getText().trim(), line);
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
          this,
          "Invalid line number",
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  private void showBreakCountDialog() {
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new GridLayout(3, 2, 5, 5));

    JTextField fileField = new JTextField(20);
    JTextField lineField = new JTextField(10);
    JTextField countField = new JTextField(10);

    inputPanel.add(new JLabel("File name:"));
    inputPanel.add(fileField);
    inputPanel.add(new JLabel("Line number:"));
    inputPanel.add(lineField);
    inputPanel.add(new JLabel("Count:"));
    inputPanel.add(countField);

    int result = JOptionPane.showConfirmDialog(
      this,
      inputPanel,
      "Set Count Breakpoint",
      JOptionPane.OK_CANCEL_OPTION
    );

    if (result == JOptionPane.OK_OPTION) {
      try {
        int line = Integer.parseInt(lineField.getText().trim());
        int count = Integer.parseInt(countField.getText().trim());
        controller.executeBreakOnCount(fileField.getText().trim(), line, count);
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
          this,
          "Invalid number format",
          "Error",
          JOptionPane.ERROR_MESSAGE
        );
      }
    }
  }

  private void showBreakMethodDialog() {
    String methodName = JOptionPane.showInputDialog(
      this,
      "Enter method name:",
      "Break Before Method",
      JOptionPane.QUESTION_MESSAGE
    );
    if (methodName != null && !methodName.trim().isEmpty()) {
      controller.executeBreakBeforeMethod(methodName.trim());
    }
  }

  private void addButton(JPanel panel, String text, Icon icon, String tooltip, ActionListener listener) {
    JButton button = new JButton(text);
    if (icon != null) {
      button.setIcon(icon);
      if (text == null) {
        button.setText("");
      }
    }
    button.setToolTipText(tooltip);
    button.addActionListener(listener);
    styleButton(button);
    panel.add(button);
  }
}