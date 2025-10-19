package com.fynance;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Central theme: modern dark palette inspired from the provided images.
 */
public class UITheme {

    // Backgrounds
    public static final Color BACKGROUND_DARK = Color.decode("#0F1416"); // deep almost-black (page background)
    public static final Color CARD_BG = Color.decode("#0C1113"); // slightly lighter card background
    public static final Color SURFACE = Color.decode("#111418"); // surfaces
    public static final Color PANEL_EDGE = Color.decode("#1A1F23");

    // Greys (text & subtle surfaces)
    public static final Color TEXT_LIGHT = Color.decode("#E6EDF3");
    public static final Color TEXT_LIGHT_GRAY = Color.decode("#C7D0D8");
    public static final Color TEXT_MUTED = Color.decode("#98A0A6");

    // Accents (from palette)
    public static final Color ACCENT_PINK = Color.decode("#A6FFCB"); // bright pastel greenish from palette (works as primary accent)
    public static final Color ACCENT_BLUE_GRAY = Color.decode("#6F7B8A"); // subtle blue-gray accent
    public static final Color ACCENT_MAROON = Color.decode("#E07A5F"); // negative/expense color

    // Nav colors
    public static final Color PRIMARY_NAVY = Color.decode("#0B1114"); // nav background
    public static final Color NAV_HOVER = Color.decode("#162027");

    // Soft shadows (just color for painting in components)
    public static final Color SHADOW = new Color(0, 0, 0, 90);

    // Fonts (fall back to SansSerif if the named font is not available)
    public static final Font FONT_MAIN = new Font("Inter", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Inter", Font.BOLD, 13);
    public static final Font FONT_HEADER = new Font("Inter", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 18);

    public static void applyTheme() {
        // Basic LAF tweaks - you can extend as needed
        UIManager.put("Label.font", new FontUIResource(FONT_MAIN));
        UIManager.put("Button.font", new FontUIResource(FONT_MAIN));
        UIManager.put("TextField.font", new FontUIResource(FONT_MAIN));
        UIManager.put("TextArea.font", new FontUIResource(FONT_MAIN));
    }
}
