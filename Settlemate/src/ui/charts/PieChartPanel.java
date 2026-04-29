package ui.charts;

import ui.style.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PieChartPanel extends JPanel {
    private final List<Color> palette;
    private Map<String, Double> values;

    public PieChartPanel() {
        setPreferredSize(new Dimension(360, 260));
        setBackground(Theme.CARD);
        palette = new ArrayList<>();
        palette.add(new Color(56, 132, 255));
        palette.add(new Color(67, 214, 161));
        palette.add(new Color(255, 171, 64));
        palette.add(new Color(255, 99, 132));
        palette.add(new Color(153, 102, 255));
    }

    public void setValues(Map<String, Double> values) {
        this.values = values;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (values == null || values.isEmpty()) {
            g2.setColor(Theme.MUTED);
            g2.drawString("No category data", 20, 30);
            return;
        }

        double total = 0.0;
        for (double value : values.values()) {
            total += value;
        }
        if (total <= 0.0) {
            g2.setColor(Theme.MUTED);
            g2.drawString("No spending yet", 20, 30);
            return;
        }

        int x = 20;
        int y = 20;
        int diameter = Math.min(getWidth() - 180, getHeight() - 40);
        int legendX = x + diameter + 20;
        int legendY = 30;
        int startAngle = 0;
        int index = 0;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            int angle = (int) Math.round((entry.getValue() / total) * 360);
            Color color = palette.get(index % palette.size());
            g2.setColor(color);
            g2.fillArc(x, y, diameter, diameter, startAngle, angle);
            startAngle += angle;

            g2.fillRect(legendX, legendY - 10, 12, 12);
            g2.setColor(Theme.TEXT);
            g2.drawString(entry.getKey() + " (" + String.format("%.1f%%", (entry.getValue() / total) * 100) + ")",
                    legendX + 20, legendY);
            legendY += 20;
            index++;
        }
    }
}
