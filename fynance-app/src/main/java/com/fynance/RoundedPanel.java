package com.fynance;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * RoundedPanel: draws rounded rectangle with optional shadow. Works as a
 * container with custom corner radius.
 */
public class RoundedPanel extends JPanel {

    private final int radius;
    private boolean drawShadow = true;
    private int shadowOffset = 6;

    public RoundedPanel(int radius) {
        this(radius, true);
    }

    public RoundedPanel(int radius, boolean drawShadow) {
        super();
        this.radius = radius;
        this.drawShadow = drawShadow;
        setOpaque(false);
    }

    public void setDrawShadow(boolean drawShadow) {
        this.drawShadow = drawShadow;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (drawShadow) {
                // subtle shadow
                g2.setColor(UITheme.SHADOW);
                g2.fillRoundRect(shadowOffset, shadowOffset, w - shadowOffset - 1, h - shadowOffset - 1, radius, radius);
            }

            // panel background
            g2.setColor(getBackground() != null ? getBackground() : UITheme.CARD_BG);
            g2.fillRoundRect(0, 0, w - shadowOffset, h - shadowOffset, radius, radius);

            // border (thin)
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(UITheme.PANEL_EDGE);
            g2.drawRoundRect(0, 0, w - shadowOffset - 1, h - shadowOffset - 1, radius, radius);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }

    @Override
    public Insets getInsets() {
        // keep inner padding so content doesn't touch edges
        return new Insets(12, 12, 12, 12);
    }
}
