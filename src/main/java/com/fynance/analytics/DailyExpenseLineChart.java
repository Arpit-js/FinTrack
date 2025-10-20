package com.fynance.analytics;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.swing.JPanel;

import com.fynance.UITheme;

public class DailyExpenseLineChart extends JPanel {

    private NavigableMap<LocalDate, Double> dailyData;

    public DailyExpenseLineChart() {
        this.dailyData = new TreeMap<>();
        setBackground(UITheme.PRIMARY_NAVY);
    }

    public void setData(Map<LocalDate, Double> data) {
        this.dailyData = new TreeMap<>(data);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dailyData == null || dailyData.isEmpty()) {
            g.setColor(UITheme.TEXT_LIGHT_GRAY);
            g.drawString("No daily expense data for this month.", 20, 40);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 30;
        int labelPadding = 25;
        int width = getWidth() - 2 * padding;
        int height = getHeight() - 2 * padding;

        // Draw axes
        g2.setColor(UITheme.ACCENT_BLUE_GRAY);
        g2.drawLine(padding, getHeight() - padding, padding, padding);
        g2.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding);

        double maxAmount = Collections.max(dailyData.values());
        if (maxAmount == 0) {
            maxAmount = 100; // Avoid division by zero
        }
        LocalDate firstDay = dailyData.firstKey().withDayOfMonth(1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        long totalDays = ChronoUnit.DAYS.between(firstDay, lastDay) + 1;

        // Draw Y-axis labels
        g2.setFont(UITheme.FONT_MAIN.deriveFont(10f));
        for (int i = 0; i <= 5; i++) {
            int y = getHeight() - padding - i * (height / 5);
            g2.setColor(UITheme.TEXT_LIGHT_GRAY);
            g2.drawLine(padding - 5, y, padding, y);
            String label = String.format("₹%.0f", (maxAmount / 5) * i);
            g2.drawString(label, padding - labelPadding - 5, y + 5);
        }

        // --- Create and draw the smooth line ---
        Path2D.Double path = new Path2D.Double();
        int[] xPoints = new int[dailyData.size()];
        int[] yPoints = new int[dailyData.size()];
        int i = 0;

        for (Map.Entry<LocalDate, Double> entry : dailyData.entrySet()) {
            long dayIndex = ChronoUnit.DAYS.between(firstDay, entry.getKey());
            xPoints[i] = (int) (padding + (double) dayIndex / (totalDays - 1) * width);
            yPoints[i] = (int) (getHeight() - padding - (entry.getValue() / maxAmount) * height);
            i++;
        }

        if (xPoints.length > 1) {
            path.moveTo(xPoints[0], yPoints[0]);
            CubicSpline spline = new CubicSpline(xPoints, yPoints);

            for (int j = xPoints[0]; j < xPoints[xPoints.length - 1]; j++) {
                path.lineTo(j, spline.interpolate(j));
            }
        } else if (xPoints.length == 1) {
            // Draw a single point if only one day of data
            g2.fillOval(xPoints[0] - 3, yPoints[0] - 3, 6, 6);
        }

        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(UITheme.ACCENT_PINK);
        g2.draw(path);

        // Draw X-axis labels (e.g., every 5 days)
        g2.setColor(UITheme.TEXT_LIGHT_GRAY);
        for (int day = 1; day <= totalDays; day += 5) {
            int x = (int) (padding + ((double) (day - 1) / (totalDays - 1)) * width);
            g2.drawLine(x, getHeight() - padding, x, getHeight() - padding + 5);
            g2.drawString(String.valueOf(day), x - 5, getHeight() - padding + 20);
        }
    }

    // Inner class to handle the cubic spline calculation for a smooth curve
    private static class CubicSpline {

        private final int[] x, y;
        private final double[] h, a, b, c, d;

        public CubicSpline(int[] x, int[] y) {
            this.x = x;
            this.y = y;
            int n = x.length - 1;
            h = new double[n];
            a = new double[n + 1];
            b = new double[n];
            c = new double[n + 1];
            d = new double[n];

            for (int i = 0; i < n; i++) {
                h[i] = x[i + 1] - x[i];
            }
            for (int i = 0; i < n + 1; i++) {
                a[i] = y[i];
            }

            double[] alpha = new double[n];
            for (int i = 1; i < n; i++) {
                alpha[i] = (3.0 / h[i]) * (a[i + 1] - a[i]) - (3.0 / h[i - 1]) * (a[i] - a[i - 1]);
            }

            double[] l = new double[n + 1];
            double[] mu = new double[n + 1];
            double[] z = new double[n + 1];
            l[0] = 1;

            for (int i = 1; i < n; i++) {
                l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];
                mu[i] = h[i] / l[i];
                z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
            }
            l[n] = 1;

            for (int j = n - 1; j >= 0; j--) {
                c[j] = z[j] - mu[j] * c[j + 1];
                b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3.0;
                d[j] = (c[j + 1] - c[j]) / (3.0 * h[j]);
            }
        }

        public double interpolate(double xi) {
            int j = 0;
            while (j < x.length - 2 && xi > x[j + 1]) {
                j++;
            }
            double dx = xi - x[j];
            return a[j] + b[j] * dx + c[j] * dx * dx + d[j] * dx * dx * dx;
        }
    }
}
