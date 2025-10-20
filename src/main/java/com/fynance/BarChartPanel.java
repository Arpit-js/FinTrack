package com.fynance;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

public class BarChartPanel extends JPanel {

    private Map<String, Double> data; // Month Name -> Total Amount

    public BarChartPanel() {
        this.data = new LinkedHashMap<>();
        setBackground(UITheme.ACCENT_BLUE_GRAY);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double maxValue = 0;
        for (Double value : data.values()) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        int barWidth = getWidth() / (data.size() * 2);
        int spacing = barWidth;
        int x = spacing;

        g2.setColor(UITheme.TEXT_LIGHT_GRAY);
        g2.setFont(UITheme.FONT_MAIN);

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            int barHeight = (int) ((getHeight() - 40) * (value / maxValue));

            g2.setColor(UITheme.ACCENT_BLUE_GRAY);
            g2.fillRoundRect(x, getHeight() - barHeight - 20, barWidth, barHeight, 10, 10);

            g2.setColor(UITheme.ACCENT_BLUE_GRAY);
            g2.drawString(entry.getKey(), x + barWidth / 2 - 10, getHeight() - 5);

            x += 2 * spacing;
        }
    }
}
