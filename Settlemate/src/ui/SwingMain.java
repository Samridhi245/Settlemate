package ui;

import controllers.AppController;
import ui.navigation.ScreenIds;

import javax.swing.SwingUtilities;

public class SwingMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AppController controller = new AppController("settlemate-data.txt");
                AppFrame frame = new AppFrame(controller);
                frame.setVisible(true);
                frame.showScreen(ScreenIds.LOGIN);
            } catch (Exception e) {
                throw new RuntimeException("Failed to launch Swing UI: " + e.getMessage(), e);
            }
        });
    }
}
