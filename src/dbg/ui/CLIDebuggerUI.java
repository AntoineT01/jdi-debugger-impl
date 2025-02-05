package dbg.ui;

import dbg.command.DebuggerContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLIDebuggerUI implements DebuggerUI {
  private final BufferedReader reader;

  public CLIDebuggerUI() {
    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  @Override
  public void showOutput(String message) {
    System.out.println(message);
  }

  @Override
  public String getCommand(DebuggerContext context) {
    System.out.print("dbg> ");
    System.out.flush();
    try {
      return reader.readLine();
    } catch (IOException e) {
      return "";
    }
  }
}
