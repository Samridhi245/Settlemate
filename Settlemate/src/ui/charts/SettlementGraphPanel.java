package ui.charts;

import models.SettlementTransaction;
import ui.style.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettlementGraphPanel extends JPanel {
    private List<SettlementTransaction> settlements;
    private Map<String, String> userNames;

    public SettlementGraphPanel() {
        setPreferredSize(new Dimension(420, 260));
        setBackground(Theme.CARD);
        userNames = new HashMap<>();
    }

    public void setData(List<SettlementTransaction> settlements, Map<String, String> userNames) {
        this.settlements = settlements;
        this.userNames = userNames;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (settlements == null || settlements.isEmpty()) {
            g2.setColor(Theme.MUTED);
            g2.drawString("No settlement graph data", 20, 30);
            return;
        }

        int y = 30;
        int startX = 20;
        int endX = getWidth() - 20;
        for (SettlementTransaction tx : settlements) {
            String fromName = userNames.getOrDefault(tx.getFromUserId(), tx.getFromUserId());
            String toName = userNames.getOrDefault(tx.getToUserId(), tx.getToUserId());

            g2.setColor(new Color(220, 230, 244));
            g2.fillRoundRect(startX, y - 12, 120, 24, 12, 12);
            g2.setColor(Theme.TEXT);
            g2.drawString(fromName, startX + 8, y + 4);

            g2.setColor(Theme.PRIMARY);
            g2.drawLine(startX + 130, y, endX - 130, y);
            g2.fillPolygon(new int[]{endX - 130, endX - 140, endX - 140},
                    new int[]{y, y - 5, y + 5}, 3);
            g2.setColor(Theme.MUTED);
            g2.drawString(String.format("%.2f", tx.getAmount()), (startX + endX) / 2 - 20, y - 6);

            g2.setColor(new Color(220, 230, 244));
            g2.fillRoundRect(endX - 120, y - 12, 100, 24, 12, 12);
            g2.setColor(Theme.TEXT);
            g2.drawString(toName, endX - 112, y + 4);

            y += 35;
            if (y > getHeight() - 20) {
                break;
            }
        }
    }
}
