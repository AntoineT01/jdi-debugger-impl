package dbg.graphic.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class IconFactory {
  private static final int ICON_SIZE = 16;
  private static final Color ICON_COLOR = new Color(187, 187, 187);
  private static final Color BREAKPOINT_COLOR = new Color(255, 128, 128);

  private static BufferedImage createEmptyImage() {
    BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.dispose();
    return image;
  }

  public static ImageIcon createStepIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    int[] xPoints = {4, 12, 4};
    int[] yPoints = {4, 8, 12};
    g2.fillPolygon(xPoints, yPoints, 3);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createStepOverIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.fillPolygon(new int[]{12, 4, 12}, new int[]{4, 8, 12}, 3);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createPlayIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    // Première flèche
    g2.fillPolygon(new int[]{4, 8, 4}, new int[]{4, 8, 12}, 3);
    // Deuxième flèche
    g2.fillPolygon(new int[]{8, 12, 8}, new int[]{4, 8, 12}, 3);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createFrameIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawRect(3, 3, 10, 10);
    g2.drawLine(3, 7, 13, 7);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createVariablesIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 12));
    g2.drawString("x", 5, 12);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createStackIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.fillRect(4, 4, 8, 2);
    g2.fillRect(4, 7, 8, 2);
    g2.fillRect(4, 10, 8, 2);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createThisIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 10));
    g2.drawString("this", 2, 12);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createSenderIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawLine(4, 8, 12, 8);
    g2.drawLine(8, 4, 12, 8);
    g2.drawLine(8, 12, 12, 8);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createFieldsIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawOval(3, 3, 10, 10);
    g2.drawLine(8, 3, 8, 13);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createMethodIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 12));
    g2.drawString("M", 4, 12);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createArgumentsIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 11));
    g2.drawString("()", 4, 12);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createPrintIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(ICON_COLOR);
    g2.setStroke(new BasicStroke(1.5f));
    g2.drawRect(3, 6, 10, 6);
    g2.drawLine(5, 4, 11, 4);
    g2.fillRect(6, 8, 4, 2);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createBreakpointIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(BREAKPOINT_COLOR);
    g2.fillOval(4, 4, 8, 8);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createBreakpointsListIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(BREAKPOINT_COLOR);
    g2.fillOval(4, 2, 6, 6);
    g2.fillOval(4, 9, 6, 6);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createBreakpointOnceIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(BREAKPOINT_COLOR);
    g2.fillOval(4, 4, 8, 8);
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 10));
    g2.drawString("1", 6, 11);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createBreakpointCountIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(BREAKPOINT_COLOR);
    g2.fillOval(4, 4, 8, 8);
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 10));
    g2.drawString("n", 6, 11);
    g2.dispose();
    return new ImageIcon(image);
  }

  public static ImageIcon createBreakpointMethodIcon() {
    BufferedImage image = createEmptyImage();
    Graphics2D g2 = image.createGraphics();
    g2.setColor(BREAKPOINT_COLOR);
    g2.fillOval(4, 4, 8, 8);
    g2.setColor(ICON_COLOR);
    g2.setFont(new Font("Dialog", Font.BOLD, 10));
    g2.drawString("M", 6, 11);
    g2.dispose();
    return new ImageIcon(image);
  }
}