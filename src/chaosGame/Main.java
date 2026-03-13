package chaosGame;

import javax.swing.SwingUtilities;

/**
 * Chaos Game 2D – punkt wejscia aplikacji.
 * Uruchamia okno Swing w watku EDT.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChaosGameFrame frame = new ChaosGameFrame();
            frame.setVisible(true);
        });
    }
}
