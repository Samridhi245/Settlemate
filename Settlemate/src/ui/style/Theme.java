package ui.style;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

public final class Theme {
    public static final Color BG = new Color(246, 249, 255);
    public static final Color SIDEBAR = new Color(233, 241, 255);
    public static final Color CARD = new Color(255, 255, 255);
    public static final Color PRIMARY = new Color(56, 132, 255);
    public static final Color TEXT = new Color(34, 45, 62);
    public static final Color MUTED = new Color(115, 132, 154);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private Theme() {
    }

    public static void styleRootPanel(JPanel panel) {
        panel.setBackground(BG);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 234, 246), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
    }

    public static void styleButton(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(BODY_FONT.deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    public static void setStandardFieldSize(JComponent component) {
        component.setPreferredSize(new Dimension(260, 34));
        component.setFont(BODY_FONT);
    }

    public static void styleSidebarButton(JButton button) {
        button.setBackground(new Color(219, 232, 255));
        button.setForeground(TEXT);
        button.setFont(BODY_FONT.deriveFont(Font.BOLD, 13f));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    }
}
