package chaosGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Silnik Chaos Game – generuje kolejne punkty atraktora.
 *
 * Algorytm (wielokat):
 *   1. Wybierz losowy wierzcholek V_i.
 *   2. P_nowy = P + r * (V_i - P)   [interpolacja liniowa]
 *   3. Zapisz P_nowy.
 *   4. Powtorz.
 *
 * Algorytm (Barnsley IFS):
 *   Cztery transformacje afiniczne z wagami 1%/85%/7%/7%.
 */
public class ChaosEngine {

    // ---- pojedynczy wygenerowany punkt ----
    public static class ChaosPoint {
        public final float x, y;
        public final int   colorIdx; // indeks wierzcholka -> kolor z palety

        public ChaosPoint(float x, float y, int colorIdx) {
            this.x        = x;
            this.y        = y;
            this.colorIdx = colorIdx;
        }
    }

    // ---- stan ----
    private final List<ChaosPoint> points = new ArrayList<>(150_000);
    private final Random           rng    = new Random();

    private ChaosPreset preset;
    private double      ratio;
    private int         maxPoints = 100_000;

    // biezacy punkt iteracji (polygon)
    private double curX = 0.0, curY = 0.0;

    // biezacy punkt iteracji (Barnsley)
    private double bX = 0.0, bY = 0.0;

    // =========================================================
    public ChaosEngine(ChaosPreset preset) {
        setPreset(preset);
    }

    // ---- konfiguracja ----

    public synchronized void setPreset(ChaosPreset p) {
        this.preset = p;
        this.ratio  = p.defaultRatio;
        reset();
    }

    public synchronized void setRatio(double r) {
        this.ratio = r;
        reset();
    }

    public synchronized void setMaxPoints(int max) {
        this.maxPoints = max;
        if (points.size() > max) {
            points.subList(max, points.size()).clear();
        }
    }

    public synchronized void reset() {
        points.clear();
        curX = 0.0;
        curY = 0.0;
        bX   = 0.0;
        bY   = 0.0;
    }

    // ---- generowanie ----

    /**
     * Generuje 'count' nowych punktow i dodaje do listy.
     * Wywolywane co klatke animacji.
     */
    public synchronized void generate(int count) {
        if (preset == null) return;

        if (preset.type == ChaosPreset.Type.BARNSLEY) {
            generateBarnsley(count);
        } else {
            generatePolygon(count);
        }

        // Przytnij liste do maksimum
        while (points.size() > maxPoints) {
            points.remove(0);
        }
    }

    // ---- algorytm wielokata ----
    private void generatePolygon(int count) {
        Point2D[] verts = preset.vertices;
        int       n     = verts.length;

        for (int i = 0; i < count; i++) {
            // losuj wierzcholek
            int vi = rng.nextInt(n);

            // interpolacja: P_nowy = P + r*(V - P)
            curX = curX + ratio * (verts[vi].x - curX);
            curY = curY + ratio * (verts[vi].y - curY);

            points.add(new ChaosPoint((float) curX, (float) curY, vi));
        }
    }

    // ---- algorytm Barnsley IFS ----
    private void generateBarnsley(int count) {
        for (int i = 0; i < count; i++) {
            double r = rng.nextDouble();
            double nx, ny;
            int    ci;

            if (r < 0.01) {
                // lodydze
                nx = 0.0;
                ny = 0.16 * bY;
                ci = 0;
            } else if (r < 0.86) {
                // liscie glowne
                nx = 0.85 * bX + 0.04 * bY;
                ny = -0.04 * bX + 0.85 * bY + 1.6;
                ci = 1;
            } else if (r < 0.93) {
                // lewy listek
                nx = 0.20 * bX - 0.26 * bY;
                ny = 0.23 * bX + 0.22 * bY + 1.6;
                ci = 2;
            } else {
                // prawy listek
                nx = -0.15 * bX + 0.28 * bY;
                ny =  0.26 * bX + 0.24 * bY + 0.44;
                ci = 3;
            }

            bX = nx;
            bY = ny;

            // normalizacja do [-1, 1] (paprot: x in [-2.5,2.5], y in [0,10])
            float px = (float) (bX / 2.5);
            float py = (float) (-(bY / 10.0 - 0.5)); // odwrocona os Y

            points.add(new ChaosPoint(px, py, ci));
        }
    }

    // ---- dostep do danych ----

    public synchronized List<ChaosPoint> getPoints() {
        return new ArrayList<>(points);
    }

    public synchronized int getPointCount() {
        return points.size();
    }

    public double getRatio()       { return ratio;  }
    public ChaosPreset getPreset() { return preset; }
}
