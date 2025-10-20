package com.fynance;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * Backwards-compatible UI theme tokens. - Provides both the new light palette
 * and the original dark tokens your code expects. - Methods: applyTheme()
 * (default light), applyLightTheme(), applyDarkTheme(),
 * updateRootPane(Component).
 */
public class UITheme {

    // -------- Light Theme (modern, default) --------
    public static final Color BACKGROUND_LIGHT = new Color(0xF5F7FA);    // app background
    public static final Color SIDEBAR_BG = new Color(0xFFFFFF);
    public static final Color CARD_LIGHT = new Color(0xFFFFFF);
    public static final Color BORDER_LIGHT = new Color(0xE5E7EB);
    public static final Color TEXT_DARK = new Color(0x111827);
    public static final Color TEXT_SECONDARY = new Color(0x6B7280);
    public static final Color ACCENT_BLUE = new Color(0x2563EB);
    public static final Color ACCENT_BLUE_LIGHT = new Color(0x3B82F6);
    public static final Color BUTTON_HOVER = new Color(0xEFF6FF);
    public static final Color ACCENT_SKY = new Color(0x38BDF8);
    public static final Color BACKGROUND_PANE = new Color(0xF9FAFB);

    // -------- Dark Theme (original tokens preserved) --------
    public static final Color BACKGROUND_DARK = new Color(0x0A0E15);
    public static final Color PRIMARY_NAVY = new Color(0x212631);
    public static final Color ACCENT_PINK = new Color(0xFF7597);
    public static final Color ACCENT_BLUE_GRAY = new Color(0xA7B5C5);
    public static final Color TEXT_LIGHT_GRAY = new Color(0xE5E7EB);
    public static final Color ACCENT_MAROON = new Color(0xB91C1C); // used by transaction negative amounts

    // Legacy names mapped to light theme equivalents when in light mode (for code that reads these)
    // These will be used by UI components (ExpenseAppGUI) regardless of active theme,
    // so their meaning will change depending on active theme.
    public static Color CURRENT_BACKGROUND = BACKGROUND_LIGHT;
    public static Color CURRENT_PRIMARY = ACCENT_BLUE;
    public static Color CURRENT_TEXT = TEXT_DARK;

    // -------- Fonts (kept names used by project) --------
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 16);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 16);

    // -------- Theme application methods --------
    /**
     * applyTheme(): kept for backward compatibility with code that calls
     * UITheme.applyTheme() Defaults to light theme. If you prefer dark by
     * default, change to applyDarkTheme().
     */
    public static void applyTheme() {
        applyLightTheme();
    }

    /**
     * Apply the modern light theme and set UIManager tokens for common Swing
     * components.
     */
    public static void applyLightTheme() {
        // Setup FlatLightLaf (makes Swing controls look modern)
        FlatLightLaf.setup();

        // Map UIManager values for common components
        UIManager.put("Panel.background", BACKGROUND_LIGHT);
        UIManager.put("Viewport.background", BACKGROUND_LIGHT);
        UIManager.put("ToolBar.background", SIDEBAR_BG);
        UIManager.put("Button.background", CARD_LIGHT);
        UIManager.put("ToggleButton.background", CARD_LIGHT);
        UIManager.put("Button.foreground", TEXT_DARK);
        UIManager.put("Label.foreground", TEXT_DARK);
        UIManager.put("TextField.background", CARD_LIGHT);
        UIManager.put("TextField.foreground", TEXT_DARK);
        UIManager.put("TextArea.background", CARD_LIGHT);
        UIManager.put("TextArea.foreground", TEXT_DARK);
        UIManager.put("List.background", CARD_LIGHT);
        UIManager.put("List.foreground", TEXT_DARK);
        UIManager.put("Table.background", CARD_LIGHT);
        UIManager.put("Table.foreground", TEXT_DARK);
        UIManager.put("Table.gridColor", BORDER_LIGHT);
        UIManager.put("ScrollPane.border", BORDER_LIGHT);
        UIManager.put("ToolTip.background", CARD_LIGHT);
        UIManager.put("ToolTip.foreground", TEXT_DARK);

        // set legacy tokens to light equivalents so existing code keeps working visually
        setLegacyToLight();
    }

    /**
     * Apply the original dark theme (keeps original color tokens).
     */
    public static void applyDarkTheme() {
        FlatDarkLaf.setup();

        UIManager.put("Panel.background", BACKGROUND_DARK);
        UIManager.put("Viewport.background", PRIMARY_NAVY);
        UIManager.put("ToolBar.background", PRIMARY_NAVY);
        UIManager.put("Button.background", PRIMARY_NAVY);
        UIManager.put("Button.foreground", TEXT_LIGHT_GRAY);
        UIManager.put("Label.foreground", TEXT_LIGHT_GRAY);
        UIManager.put("TextField.background", PRIMARY_NAVY);
        UIManager.put("TextField.foreground", TEXT_LIGHT_GRAY);
        UIManager.put("List.background", PRIMARY_NAVY);
        UIManager.put("List.foreground", TEXT_LIGHT_GRAY);
        UIManager.put("Table.background", PRIMARY_NAVY);
        UIManager.put("Table.foreground", TEXT_LIGHT_GRAY);
        UIManager.put("Table.gridColor", new Color(0x2B2F36));
        UIManager.put("ScrollPane.border", new Color(0x2B2F36));
        UIManager.put("ToolTip.background", PRIMARY_NAVY);
        UIManager.put("ToolTip.foreground", TEXT_LIGHT_GRAY);

        // set legacy tokens to dark equivalents
        setLegacyToDark();
    }

    private static void setLegacyToLight() {
        CURRENT_BACKGROUND = BACKGROUND_LIGHT;
        CURRENT_PRIMARY = ACCENT_BLUE;
        CURRENT_TEXT = TEXT_DARK;
        // Keep older names used around code to avoid compilation changes:
        // For example code referencing UITheme.PRIMARY_NAVY should still compile:
        // We'll leave the constant PRIMARY_NAVY (dark) defined, but components should use these "mapped" colors
        // where appropriate (ExpenseAppGUI references both PRIMARY_NAVY and BACKGROUND_DARK; those remain defined).
    }

    private static void setLegacyToDark() {
        CURRENT_BACKGROUND = BACKGROUND_DARK;
        CURRENT_PRIMARY = PRIMARY_NAVY;
        CURRENT_TEXT = TEXT_LIGHT_GRAY;
    }

    /**
     * Call this if you change theme at runtime to refresh component UIs.
     */
    public static void updateRootPane(Component root) {
        javax.swing.SwingUtilities.updateComponentTreeUI(root);
    }
}
