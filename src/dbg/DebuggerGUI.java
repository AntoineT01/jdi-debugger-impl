package dbg;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;

public class DebuggerGUI extends JFrame {
  // Couleurs personnalisées
  private static final Color BACKGROUND_COLOR = new Color(43, 43, 43);
  private static final Color DARKER_BACKGROUND = new Color(38, 38, 38);
  private static final Color FOREGROUND_COLOR = new Color(187, 187, 187);
  private static final Color SELECTION_COLOR = new Color(44, 86, 160);
  private static final Color HIGHLIGHT_COLOR = new Color(50, 50, 50);
  private static final Color BORDER_COLOR = new Color(61, 61, 61);
  private static final Color BUTTON_HOVER = new Color(70, 70, 70);
  private static final Color ACCENT_COLOR = new Color(99, 147, 198);

  // Couleurs pour la coloration syntaxique
  private static final Color KEYWORD_COLOR = new Color(204, 120, 50);
  private static final Color STRING_COLOR = new Color(106, 135, 89);
  private static final Color NUMBER_COLOR = new Color(104, 151, 187);
  private static final Color COMMENT_COLOR = new Color(98, 114, 164);

  private final JTextPane codeArea;
  private final JList<String> variablesList;
  private final JList<String> callStackList;
  private final JList<String> breakpointsList;
  private final DefaultListModel<String> variablesModel;
  private final DefaultListModel<String> callStackModel;
  private final DefaultListModel<String> breakpointsModel;
  private final ScriptableDebugger debugger;

  public DebuggerGUI(ScriptableDebugger debugger) {
    this.debugger = debugger;
    setTitle("Debugger Java");
    setSize(1400, 900);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBackground(DARKER_BACKGROUND);

    // Initialisation des modèles
    variablesModel = new DefaultListModel<>();
    callStackModel = new DefaultListModel<>();
    breakpointsModel = new DefaultListModel<>();

    // Création des composants
    codeArea = createCodeArea();
    variablesList = createList(variablesModel);
    callStackList = createList(callStackModel);
    breakpointsList = createList(breakpointsModel);

    // Layout principal
    JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
    mainPanel.setBackground(BACKGROUND_COLOR);
    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));


    mainPanel.add(createToolbar(), BorderLayout.NORTH);
    mainPanel.add(createMainContent(), BorderLayout.CENTER);

    setContentPane(mainPanel);
    applyCustomStyle();
  }

  private JTextPane createCodeArea() {
    JTextPane textPane = new JTextPane() {
      @Override
      public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g);
      }
    };
    textPane.setEditable(false);
    textPane.setBackground(DARKER_BACKGROUND);
    textPane.setForeground(FOREGROUND_COLOR);
    textPane.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
    if (!textPane.getFont().getFamily().equals("JetBrains Mono")) {
      textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    }

    // Style du code
    StyleContext sc = StyleContext.getDefaultStyleContext();
    AttributeSet keywordAttr = sc.addAttribute(SimpleAttributeSet.EMPTY,
                                               StyleConstants.Foreground, new Color(204, 120, 50));
    AttributeSet stringAttr = sc.addAttribute(SimpleAttributeSet.EMPTY,
                                              StyleConstants.Foreground, new Color(106, 135, 89));

    // Marges intérieures pour le code
    textPane.setBorder(new EmptyBorder(10, 10, 10, 10));
    return textPane;
  }

  private JList<String> createList(DefaultListModel<String> model) {
    JList<String> list = new JList<>(model);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setBackground(BACKGROUND_COLOR);
    list.setForeground(FOREGROUND_COLOR);
    list.setSelectionBackground(SELECTION_COLOR);
    list.setSelectionForeground(FOREGROUND_COLOR);
    list.setBorder(new EmptyBorder(5, 5, 5, 5));
    return list;
  }

  private JToolBar createToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.setBackground(BACKGROUND_COLOR);
    toolbar.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
      new EmptyBorder(5, 5, 5, 5)));

    // Boutons personnalisés
    toolbar.add(createButton("⇲", "Step Into", e -> onStepInto()));
    toolbar.add(createButton("↷", "Step Over", e -> onStepOver()));
    toolbar.add(createButton("⇱", "Step Out", e -> onStepOut()));
    toolbar.add(createButton("▶", "Continue", e -> onContinue()));
    toolbar.addSeparator();

    // Zone de breakpoint avec style moderne
    JTextField lineField = createStyledTextField();
    lineField.setMaximumSize(new Dimension(100, 30));
    toolbar.add(new JLabel("Ligne: ") {{ setForeground(FOREGROUND_COLOR); }});
    toolbar.add(lineField);
    toolbar.add(createButton("◉", "Set Breakpoint",
                             e -> onSetBreakpoint(Integer.parseInt(lineField.getText()))));

    return toolbar;
  }

  private JButton createButton(String text, String tooltip, java.awt.event.ActionListener action) {
    JButton button = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isPressed()) {
          g2.setColor(SELECTION_COLOR);
        } else if (getModel().isRollover()) {
          g2.setColor(BUTTON_HOVER);
        } else {
          g2.setColor(BACKGROUND_COLOR);
        }
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);

        // Dessiner le texte
        g2.setColor(FOREGROUND_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(getText(), x, y);
      }
    };
    button.setFont(new Font("Dialog", Font.BOLD, 14));
    button.setForeground(FOREGROUND_COLOR);
    button.setBackground(BACKGROUND_COLOR);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setToolTipText(tooltip);
    button.addActionListener(action);
    button.setPreferredSize(new Dimension(40, 30));
    return button;
  }

  private JTextField createStyledTextField() {
    JTextField field = new JTextField() {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(HIGHLIGHT_COLOR);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
        super.paintComponent(g);
      }
    };
    field.setBackground(HIGHLIGHT_COLOR);
    field.setForeground(FOREGROUND_COLOR);
    field.setCaretColor(FOREGROUND_COLOR);
    field.setBorder(new EmptyBorder(5, 5, 5, 5));
    return field;
  }

  private JSplitPane createMainContent() {
    // Panel gauche : code source
    JScrollPane codeScrollPane = new JScrollPane(codeArea);
    styleScrollPane(codeScrollPane);

    // Panel droit : variables, stack, breakpoints
    JTabbedPane rightTabs = new JTabbedPane();
    styleTab(rightTabs);

    rightTabs.addTab("Variables", wrapInScrollPane(variablesList));
    rightTabs.addTab("Call Stack", wrapInScrollPane(callStackList));
    rightTabs.addTab("Breakpoints", wrapInScrollPane(breakpointsList));

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                          codeScrollPane, rightTabs);
    styleSplitPane(splitPane);

    splitPane.setDividerLocation(900);
    splitPane.setResizeWeight(0.7);
    return splitPane;
  }

  private void styleScrollPane(JScrollPane scrollPane) {
    scrollPane.setBackground(BACKGROUND_COLOR);
    scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
    scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
  }

  private void styleTab(JTabbedPane tabbedPane) {
    tabbedPane.setBackground(BACKGROUND_COLOR);
    tabbedPane.setForeground(FOREGROUND_COLOR);
    tabbedPane.setBorder(null);
  }

  private void styleSplitPane(JSplitPane splitPane) {
    splitPane.setBackground(BACKGROUND_COLOR);
    splitPane.setBorder(null);
    splitPane.setDividerSize(4);
  }

  private JScrollPane wrapInScrollPane(JComponent component) {
    JScrollPane scrollPane = new JScrollPane(component);
    styleScrollPane(scrollPane);
    return scrollPane;
  }

  private void applyCustomStyle() {
    // Style global
    UIManager.put("Panel.background", BACKGROUND_COLOR);
    UIManager.put("ToolTip.background", HIGHLIGHT_COLOR);
    UIManager.put("ToolTip.foreground", FOREGROUND_COLOR);
    UIManager.put("ToolTip.border", new EmptyBorder(5, 5, 5, 5));
  }

  // Méthodes de mise à jour de l'interface
  public void updateSourceCode(String sourceCode) {
    codeArea.setText(applyBasicSyntaxHighlighting(sourceCode));
  }

  private String applyBasicSyntaxHighlighting(String sourceCode) {
    // Implémentation simple de coloration syntaxique
    // À améliorer selon vos besoins
    String highlighted = sourceCode;
    // Mots-clés Java
    String[] keywords = {"public", "private", "protected", "class", "interface",
      "void", "int", "double", "boolean", "if", "else", "while",
      "for", "return", "new", "static", "final"};

    for (String keyword : keywords) {
      highlighted = highlighted.replaceAll("\\b" + keyword + "\\b",
                                           "<font color='#CC7832'>" + keyword + "</font>");
    }

    return "<html>" + highlighted + "</html>";
  }

  public void highlightCurrentLine(int lineNumber) {
    // Implémentation de la mise en surbrillance
    try {
      // Logique de highlighting
      Element root = codeArea.getDocument().getDefaultRootElement();
      Element line = root.getElement(lineNumber - 1);
      codeArea.setCaretPosition(line.getStartOffset());

      // Highlight custom
      DefaultHighlighter.DefaultHighlightPainter painter =
        new DefaultHighlighter.DefaultHighlightPainter(SELECTION_COLOR);
      codeArea.getHighlighter().addHighlight(
        line.getStartOffset(),
        line.getEndOffset(),
        painter
      );
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  // Gestionnaires d'événements
  private void onStepInto() {
//    debugger.step();
  }

  private void onStepOver() {
//    debugger.stepOver();
  }

  private void onStepOut() {
//    debugger.stepOut();
  }

  private void onContinue() {
//    debugger.resume();
  }

  private void onSetBreakpoint(int line) {
//    debugger.setBreakpoint(line);
  }

  // Main pour test
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        // Test avec quelques données
        ScriptableDebugger debugger = new ScriptableDebugger();
        DebuggerGUI gui = new DebuggerGUI(debugger);

        // Exemple de code
        gui.updateSourceCode("""
                    public class Test {
                        public static void main(String[] args) {
                            int x = 42;
                            System.out.println("Hello, World!");
                        }
                    }
                    """);

        gui.setVisible(true);

      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}