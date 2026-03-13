package chaosGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Panel rysujacy atraktor Chaos Game.
 *
 * Techniki renderingu:
 *  - Off-screen BufferedImage (bufor akumulacyjny) – rysujemy tylko nowe punkty
 *  - Addytywne mieszanie kolorow (jasniejsze = gestsza populacja)
 *  - Animowany "trail" – ostatnie 40 punktow widoczne jako swiecacy slad
 *  - Zoom (kolko myszy) i pan (przeciaganie)
 */
public class ChaosGamePanel extends JPanel {

    // ---- stale ----
    private static final int   TRAIL_LEN     = 40;
    private static final Color BG_COLOR      = new Color(10, 10, 20);

    // ---- dane ----
    private ChaosPreset[] allPresets;
    private int           presetIdx = 0;
    private ChaosEngine   engine;

    // ---- animacja ----
    private Timer   animTimer;
    private boolean running       = true;
    private int     pointsPerFrame = 100;

    // ---- widok ----
    private double viewScale = 1.0;
    private double viewOffX  = 0.0;
    private double viewOffY  = 0.0;

    // ---- pan ----
    private boolean panDragging = false;
    private int     panStartMouseX, panStartMouseY;
    private double  panStartOffX,   panStartOffY;

    // ---- bufor off-screen ----
    private BufferedImage buffer;
    private Graphics2D    bufG;
    private int           bufW = -1, bufH = -1;
    private int           lastDrawnCount = 0; // ile punktow jest juz w buforze

    // =========================================================
    public ChaosGamePanel() {
        setPreferredSize(new Dimension(700, 620));
        setBackground(BG_COLOR);

        allPresets = ChaosPreset.all();
        engine     = new ChaosEngine(allPresets[0]);

        setupMouseListeners();

        // ~60 fps
        animTimer = new Timer(16, e -> onTick());
        animTimer.start();
    }

    // =========================================================
    //  API PUBLICZNE (wywolywane z ChaosGameFrame)
    // =========================================================

    public void setPreset(int idx) {
        presetIdx = idx;
        engine.setPreset(allPresets[idx]);
        invalidateBuffer();
        resetView();
        repaint();
    }

    public void setRatio(double r) {
        engine.setRatio(r);
        invalidateBuffer();
        repaint();
    }

    public double getRatio()       { return engine.getRatio(); }
    public int    getPointCount()  { return engine.getPointCount(); }
    public boolean isRunning()     { return running; }

    public void setPointsPerFrame(int ppf) {
        pointsPerFrame = ppf;
    }

    public void setMaxPoints(int max) {
        engine.setMaxPoints(max);
    }

    /** Start / Pauza */
    public void toggleAnimation() {
        running = !running;
    }

    /** Generuje 'n' punktow natychmiast (tryb krokowy) */
    public void stepPoints(int n) {
        engine.generate(n);
        repaint();
    }

    /** Pelny reset – czysci punkty i bufor */
    public void reset() {
        engine.reset();
        invalidateBuffer();
        running = true;
        repaint();
    }

    // =========================================================
    //  ANIMACJA
    // =========================================================

    private void onTick() {
        if (running) {
            engine.generate(pointsPerFrame);
        }
        repaint();
    }

    // =========================================================
    //  RENDERING
    // =========================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        ensureBuffer();

        List<ChaosEngine.ChaosPoint> pts = engine.getPoints();

        // Rysuj nowe punkty do bufora
        paintNewPointsToBuffer(pts);

        // Blituj bufor na ekran
        g2.drawImage(buffer, 0, 0, null);

        // Nakладки: wierzcholki, trail, HUD
        paintOverlay(g2, pts);
    }

    // ---- bufor off-screen ----

    private void ensureBuffer() {
        int w = Math.max(getWidth(),  1);
        int h = Math.max(getHeight(), 1);
        if (buffer == null || bufW != w || bufH != h) {
            createBuffer(w, h);
        }
    }

    private void createBuffer(int w, int h) {
        bufW = w;
        bufH = h;
        buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        bufG   = buffer.createGraphics();
        clearBuffer();
        lastDrawnCount = 0;
    }

    private void clearBuffer() {
        bufG.setColor(BG_COLOR);
        bufG.fillRect(0, 0, bufW, bufH);
    }

    private void invalidateBuffer() {
        buffer         = null;
        lastDrawnCount = 0;
    }

    // ---- rysowanie punktow do bufora ----

    private void paintNewPointsToBuffer(List<ChaosEngine.ChaosPoint> pts) {
        if (pts == null || pts.isEmpty()) return;

        // Jesli lista sie zmniejszyla (po resecie) – wyczysc bufor i przerysuj wszystko
        if (pts.size() < lastDrawnCount) {
            clearBuffer();
            lastDrawnCount = 0;
        }

        ChaosPreset preset = allPresets[presetIdx];
        Color[]     pal    = preset.palette;

        double scale = computeScale();
        double cx    = bufW / 2.0 + viewOffX;
        double cy    = bufH / 2.0 + viewOffY;

        // Rysuj tylko nowe punkty (od lastDrawnCount)
        for (int i = lastDrawnCount; i < pts.size(); i++) {
            ChaosEngine.ChaosPoint cp = pts.get(i);

            int sx = (int) (cx + cp.x * scale);
            int sy = (int) (cy + cp.y * scale);

            if (sx < 0 || sx >= bufW || sy < 0 || sy >= bufH) continue;

            Color c     = pal[cp.colorIdx % pal.length];
            int   old   = buffer.getRGB(sx, sy);
            int   blended = additiveBlend(c.getRGB(), old);
            buffer.setRGB(sx, sy, blended);
        }

        lastDrawnCount = pts.size();
    }

    /**
     * Addytywne mieszanie: nowy kolor + stary kolor, nasycenie na 255.
     * Efekt: im wiecej punktow trafi w jedno miejsce, tym jasniejszy piksel.
     */
    private int additiveBlend(int newRGB, int oldRGB) {
        int r = Math.min(255, ((newRGB >> 16) & 0xFF) + ((oldRGB >> 16) & 0xFF));
        int g = Math.min(255, ((newRGB >>  8) & 0xFF) + ((oldRGB >>  8) & 0xFF));
        int b = Math.min(255, ( newRGB        & 0xFF) + ( oldRGB        & 0xFF));
        return (r << 16) | (g << 8) | b;
    }

    // ---- nakладки (wierzcholki, trail, HUD) ----

    private void paintOverlay(Graphics2D g2,
                               List<ChaosEngine.ChaosPoint> pts) {

        double scale = computeScale();
        double cx    = getWidth()  / 2.0 + viewOffX;
        double cy    = getHeight() / 2.0 + viewOffY;

        ChaosPreset preset = allPresets[presetIdx];

        // --- trail: ostatnie TRAIL_LEN punktow ---
        if (!pts.isEmpty()) {
            int from = Math.max(0, pts.size() - TRAIL_LEN);
            for (int i = from; i < pts.size(); i++) {
                ChaosEngine.ChaosPoint cp = pts.get(i);
                float alpha = (float)(i - from + 1) / (pts.size() - from);
                int   sx    = (int)(cx + cp.x * scale);
                int   sy    = (int)(cy + cp.y * scale);
                int   r     = Math.max(1, (int)(alpha * 4));
                g2.setColor(new Color(1f, 1f, 1f, alpha * 0.85f));
                g2.fillOval(sx - r, sy - r, r * 2, r * 2);
            }
        }

        // --- wierzcholki wielokata ---
        if (preset.type == ChaosPreset.Type.POLYGON
                && preset.vertices != null) {

            // obrys wielokata (przerywan, polprzezroczysty)
            g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 5f}, 0f));
            g2.setColor(new Color(255, 255, 255, 35));
            Point2D[] verts = preset.vertices;
            for (int i = 0; i < verts.length; i++) {
                int next = (i + 1) % verts.length;
                int ax = (int)(cx + verts[i].x    * scale);
                int ay = (int)(cy + verts[i].y    * scale);
                int bx = (int)(cx + verts[next].x * scale);
                int by = (int)(cy + verts[next].y * scale);
                g2.drawLine(ax, ay, bx, by);
            }

            // kropki wierzcholkow + etykiety
            g2.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < verts.length; i++) {
                int vx = (int)(cx + verts[i].x * scale);
                int vy = (int)(cy + verts[i].y * scale);

                Color vc = preset.palette[i % preset.palette.length].brighter();
                g2.setColor(vc);
                g2.fillOval(vx - 5, vy - 5, 10, 10);

                g2.setColor(Color.WHITE);
                g2.drawOval(vx - 5, vy - 5, 10, 10);

                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(220, 220, 255, 200));
                g2.drawString("V" + (i + 1), vx + 8, vy + 4);
            }
        }

        // --- HUD lewy dolny ---
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(140, 140, 190, 200));
        String hud = String.format("punkty: %,d   r = %.3f",
                engine.getPointCount(), engine.getRatio())
                .replace(",", " ");
        g2.drawString(hud, 10, getHeight() - 10);

        // --- nazwa presetu prawy gorny ---
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(170, 170, 220, 200));
        String name = preset.name;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(name, getWidth() - fm.stringWidth(name) - 10, 20);
    }

    // =========================================================
    //  WIDOK – skalowanie i reset
    // =========================================================

    /** Przelicza skale canvas -> piksele ekranu */
    private double computeScale() {
        return Math.min(getWidth(), getHeight()) * 0.43 * viewScale;
    }

    private void resetView() {
        viewScale = 1.0;
        viewOffX  = 0.0;
        viewOffY  = 0.0;
    }

    // =========================================================
    //  INTERAKCJA MYSZY
    // =========================================================

    private void setupMouseListeners() {

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panDragging    = true;
                panStartMouseX = e.getX();
                panStartMouseY = e.getY();
                panStartOffX   = viewOffX;
                panStartOffY   = viewOffY;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                panDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (panDragging) {
                    viewOffX = panStartOffX + (e.getX() - panStartMouseX);
                    viewOffY = panStartOffY + (e.getY() - panStartMouseY);
                    // Bufor trzeba przerysowac od nowa (zmienily sie wspolrzedne)
                    invalidateBuffer();
                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            double factor = (e.getWheelRotation() < 0) ? 1.15 : (1.0 / 1.15);
            // Zoom w kierunku kursora
            double mx = e.getX() - getWidth()  / 2.0;
            double my = e.getY() - getHeight() / 2.0;
            viewOffX = mx + (viewOffX - mx) * factor;
            viewOffY = my + (viewOffY - my) * factor;
            viewScale *= factor;
            invalidateBuffer();
            repaint();
        });
    }
}
