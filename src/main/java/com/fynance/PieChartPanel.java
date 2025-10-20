package com.fynance;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

public class PieChartPanel extends JPanel {

    private Map<String, Double> data;
    private final Map<String, Color> categoryColors;
    private final List<Color> colorPalette;

    public PieChartPanel() {
        this.data = new HashMap<>();
        this.categoryColors = new HashMap<>();
        colorPalette = List.of(
                new Color(0x3B82F6), // Blue
                new Color(0x10B981), // Green
                new Color(0xF97316), // Orange
                new Color(0x8B5CF6), // Violet
                new Color(0xEF4444) // Red
        );
        setBackground(UITheme.PRIMARY_NAVY);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        assignColorsToCategories();
        repaint();
    }

    private void assignColorsToCategories() {
        int colorIndex = 0;
        for (String category : data.keySet()) {
            if (!categoryColors.containsKey(category)) {
                categoryColors.put(category, colorPalette.get(colorIndex % colorPalette.size()));
                colorIndex++;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g2d.setColor(UITheme.TEXT_LIGHT_GRAY);
            g2d.drawString("No expense data to display.", 20, 40);
            g2d.dispose();
            return;
        }

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height) / 2;
        int x = (width / 2) - diameter;
        int y = (height - diameter) / 2;

        double startAngle = 90.0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double arcAngle = (entry.getValue() / total) * 360.0;
            g2d.setColor(categoryColors.get(entry.getKey()));
            g2d.fill(new Arc2D.Double(x, y, diameter, diameter, startAngle, -arcAngle, Arc2D.PIE));
            startAngle -= arcAngle;
        }

        g2d.setColor(getBackground());
        g2d.fill(new Ellipse2D.Double(x + diameter / 4, y + diameter / 4, diameter / 2, diameter / 2));

        g2d.setColor(UITheme.ACCENT_PINK);
        g2d.setFont(UITheme.FONT_HEADER);
        String totalText = String.format("₹%.0f", total);
        int textWidth = g2d.getFontMetrics().stringWidth(totalText);
        g2d.drawString(totalText, (x + diameter / 2) - textWidth / 2, y + diameter / 2 + 5);

        int legendX = width / 2 + 40;
        int legendY = (height - (data.size() * 25)) / 2;
        g2d.setFont(UITheme.FONT_MAIN);

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            g2d.setColor(categoryColors.get(entry.getKey()));
            g2d.fillRect(legendX, legendY, 15, 10);
            g2d.setColor(UITheme.TEXT_LIGHT_GRAY);
            String legendText = String.format("%s (%.0f%%)", entry.getKey(), (entry.getValue() / total) * 100);
            g2d.drawString(legendText, legendX + 25, legendY + 10);
            legendY += 25;
        }

        g2d.dispose();
    }
}
