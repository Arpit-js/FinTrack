package com.fynance.analytics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ChartUtils {

    public static void exportComponentAsPNG(Component component, String fileName) {
        Dimension size = component.getSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        component.paint(g2);
        g2.dispose();
        try {
            ImageIO.write(image, "png", new File(fileName));
            JOptionPane.showMessageDialog(null, "Chart exported to " + fileName, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error exporting chart.", "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
