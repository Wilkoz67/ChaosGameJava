package chaosGame;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Glowne okno aplikacji Chaos Game 2D.
 *
 * Zawiera:
 *  - ChaosGamePanel  (canvas po lewej)
 *  - Panel sterowania (po prawej): presety, suwaki, przyciski
 *  - Pasek statusu    (na dole)
 */
public class ChaosGameFrame extends JFrame {

    // ---- kolory motywu ----
    private static final Color C_BG       = new Color(28, 28, 40);
    private static final Color C_PANEL    = new Color(36, 36, 52);
    private static final Color C_SECTION  = new Color(110, 110, 160);
    private static final Color C_TEXT     = new Color(200, 210, 255);
    private static final Color C_MUTED    = new Color(100, 100, 140);
    private static final Color C_BTN_PLAY = new Color(70, 130, 200);
    private static final Color C_BTN_STEP = new Color(60, 160, 110);
    private static final Color C_BTN_RST  = new Color(180, 70,  70);

    // ---- komponenty ----
    private ChaosGamePanel canvas;

    private JComboBox<String> presetBox;
    private JSlider           ratioSlider;
    private JLabel            ratioValueLabel;
    private JSlider           speedSlider;
    private JLabel            speedValueLabel;
    private JSlider           maxPtsSlider;
    private JLabel            maxPtsValueLabel;
    private JButton           btnPlayPause;
    private JButton           btnStep;
    private JButton           btnReset;
    private JLabel            statusLabel;
    private JLabel            pointCountLabel;

    private Timer refreshTimer;

    // =========================================================
    public ChaosGameFrame() {
        super("Chaos Game 2D – Wizualizacja Atraktora");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BG);
        setLayout(new BorderLayout(6, 6));

        // --- canvas ---
        canvas = new ChaosGamePanel();
        canvas.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 80), 1));
        add(canvas, BorderLayout.CENTER);

        // --- panel sterowania ---
        add(buildControlPanel(), BorderLayout.EAST);

        // --- pasek statusu ---
        add(buildStatusBar(), BorderLayout.SOUTH);

        // Odswiezanie etykiet 10x na sekunde
        refreshTimer = new Timer(100, e -> refreshStatus());
        refreshTimer.start();

        pack();
        setMinimumSize(new Dimension(920, 680));
        setLocationRelativeTo(null);
    }

    // =========================================================
    //  BUDOWANIE UI
    // =========================================================

    private JPanel buildControlPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));
        p.setPreferredSize(new Dimension(230, 600));

        // --- Preset ---
        p.add(sectionLabel("PRESET"));
        p.add(vgap(5));

        String[] names = {"Trojkat Sierpinskiego", "Pieciocat (r=0.618)",
                          "Szesciocat (r=0.667)",  "Kwadrat (r=0.5)",
                          "Paprot Barnsleya"};
        presetBox = new JComboBox<>(names);
        styleCombo(presetBox);
        presetBox.addActionListener(e -> onPresetChanged());
        p.add(presetBox);
        p.add(vgap(16));

        // --- Wspolczynnik r ---
        p.add(sectionLabel("WSPOLCZYNNIK r"));
        p.add(vgap(4));
        ratioValueLabel = valueLabel("r = 0.500");
        p.add(ratioValueLabel);
        ratioSlider = slider(300, 900, 500);
        ratioSlider.addChangeListener(e -> onRatioChanged());
        p.add(ratioSlider);
        p.add(tickRow("0.30", "0.60", "0.90"));
        p.add(vgap(16));

        // --- Szybkosc ---
        p.add(sectionLabel("SZYBKOSC ANIMACJI"));
        p.add(vgap(4));
        speedValueLabel = valueLabel("100 pkt / klatke");
        p.add(speedValueLabel);
        speedSlider = slider(1, 10, 5);
        speedSlider.addChangeListener(e -> onSpeedChanged());
        p.add(speedSlider);
        p.add(tickRow("wolno", "srednio", "szybko"));
        p.add(vgap(16));

        // --- Limit punktow ---
        p.add(sectionLabel("LIMIT PUNKTOW"));
        p.add(vgap(4));
        maxPtsValueLabel = valueLabel("max: 100 000");
        p.add(maxPtsValueLabel);
        maxPtsSlider = slider(1, 20, 10);
        maxPtsSlider.addChangeListener(e -> onMaxPtsChanged());
        p.add(maxPtsSlider);
        p.add(tickRow("10k", "100k", "200k"));
        p.add(vgap(22));

        // --- Przyciski sterowania ---
        p.add(sectionLabel("STEROWANIE"));
        p.add(vgap(8));

        btnPlayPause = makeButton("||  Pauza", C_BTN_PLAY);
        btnPlayPause.addActionListener(e -> onPlayPause());
        p.add(btnPlayPause);
        p.add(vgap(8));

        btnStep = makeButton(">|  Krok (+500 pkt)", C_BTN_STEP);
        btnStep.addActionListener(e -> canvas.stepPoints(500));
        p.add(btnStep);
        p.add(vgap(8));

        btnReset = makeButton("<<  Reset", C_BTN_RST);
        btnReset.addActionListener(e -> onReset());
        p.add(btnReset);
        p.add(vgap(22));

        // --- Legenda algorytmu ---
        p.add(buildAlgoBox());
        p.add(Box.createVerticalGlue());

        return p;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        bar.setBackground(new Color(20, 20, 30));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                       new Color(55, 55, 80)));

        statusLabel = new JLabel("Status: uruchomiona");
        statusLabel.setForeground(new Color(120, 210, 130));
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        pointCountLabel = new JLabel("Punkty: 0");
        pointCountLabel.setForeground(new Color(170, 180, 230));
        pointCountLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JLabel hint = new JLabel("Zoom: kolko myszy   |   Pan: przeciagnij");
        hint.setForeground(C_MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));

        bar.add(statusLabel);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        bar.add(pointCountLabel);
        bar.add(Box.createHorizontalStrut(24));
        bar.add(hint);
        return bar;
    }

    private JPanel buildAlgoBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(24, 24, 38));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 85), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel title = new JLabel("Jak dziala algorytm:");
        title.setForeground(C_SECTION);
        title.setFont(new Font("SansSerif", Font.BOLD, 11));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(title);
        box.add(vgap(5));

        String[] steps = {
            "1. Losuj wierzcholek V_i",
            "2. P = P + r*(V_i - P)",
            "3. Narysuj punkt P",
            "4. Powtorz -> atraktor"
        };
        for (String s : steps) {
            JLabel l = new JLabel(s);
            l.setForeground(new Color(160, 165, 200));
            l.setFont(new Font("Monospaced", Font.PLAIN, 11));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(l);
        }
        return box;
    }

    // =========================================================
    //  HANDLERY ZDARZEN
    // =========================================================

    private void onPresetChanged() {
        int idx = presetBox.getSelectedIndex();
        canvas.setPreset(idx);
        // Dostosuj suwak r do domyslnej wartosci presetu
        double r = canvas.getRatio();
        ratioSlider.setValue((int)(r * 1000));
        ratioValueLabel.setText(String.format("r = %.3f", r));
        btnPlayPause.setText("||  Pauza");
    }

    private void onRatioChanged() {
        double r = ratioSlider.getValue() / 1000.0;
        ratioValueLabel.setText(String.format("r = %.3f", r));
        canvas.setRatio(r);
    }

    private void onSpeedChanged() {
        int ppf = speedSlider.getValue() * 20;
        speedValueLabel.setText(ppf + " pkt / klatke");
        canvas.setPointsPerFrame(ppf);
    }

    private void onMaxPtsChanged() {
        int max = maxPtsSlider.getValue() * 10_000;
        String txt = String.format("%,d", max).replace(",", " ");
        maxPtsValueLabel.setText("max: " + txt);
        canvas.setMaxPoints(max);
    }

    private void onPlayPause() {
        canvas.toggleAnimation();
        btnPlayPause.setText(canvas.isRunning() ? "||  Pauza" : ">  Start");
    }

    private void onReset() {
        canvas.reset();
        btnPlayPause.setText("||  Pauza");
    }

    private void refreshStatus() {
        int n = canvas.getPointCount();
        String cnt = String.format("%,d", n).replace(",", " ");
        pointCountLabel.setText("Punkty: " + cnt);
        statusLabel.setText("Status: " + (canvas.isRunning()
                            ? "uruchomiona" : "zatrzymana"));
    }

    // =========================================================
    //  POMOCNICZE METODY UI
    // =========================================================

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(C_SECTION);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(C_TEXT);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JSlider slider(int min, int max, int val) {
        JSlider s = new JSlider(min, max, val);
        s.setBackground(C_PANEL);
        s.setForeground(new Color(130, 155, 220));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        return s;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(new Color(45, 45, 68));
        c.setForeground(C_TEXT);
        c.setFont(new Font("SansSerif", Font.PLAIN, 12));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hover = bg.brighter();
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { b.setBackground(bg);   }
        });
        return b;
    }

    private Component vgap(int h) {
        return Box.createVerticalStrut(h);
    }

    private JPanel tickRow(String left, String mid, String right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(C_PANEL);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        row.add(tick(left,  SwingConstants.LEFT),   BorderLayout.WEST);
        row.add(tick(mid,   SwingConstants.CENTER), BorderLayout.CENTER);
        row.add(tick(right, SwingConstants.RIGHT),  BorderLayout.EAST);
        return row;
    }

    private JLabel tick(String text, int align) {
        JLabel l = new JLabel(text, align);
        l.setForeground(C_MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 9));
        return l;
    }
}
