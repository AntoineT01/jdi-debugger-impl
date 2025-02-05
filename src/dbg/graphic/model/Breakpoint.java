package dbg.graphic.model;

public class Breakpoint {
  private final String fileName;
  private final int lineNumber;
  private boolean isEnabled;
  private int hitCount;

  public Breakpoint(String fileName, int lineNumber) {
    this.fileName = fileName;
    this.lineNumber = lineNumber;
    this.isEnabled = true;
    this.hitCount = 0;
  }

  public String getFileName() { return fileName; }
  public int getLineNumber() { return lineNumber; }
  public boolean isEnabled() { return isEnabled; }
  public void setEnabled(boolean enabled) { isEnabled = enabled; }
  public int getHitCount() { return hitCount; }
  public void incrementHitCount() { hitCount++; }

  @Override
  public String toString() {
    return fileName + ":" + lineNumber + (isEnabled ? "" : " (disabled)");
  }
}