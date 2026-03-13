package chaosGame;

import java.awt.Color;

/**
 * Definiuje preset Chaos Game:
 *  - liste wierzcholkow wielokata (w przestrzeni [-1,1])
 *  - domyslny wspolczynnik r
 *  - palete kolorow (jeden kolor na wierzcholek)
 *  - nazwe
 *
 * Specjalny typ BARNSLEY nie uzywa wierzcholkow – generuje paprot IFS.
 */
public class ChaosPreset {

    public enum Type { POLYGON, BARNSLEY }

    public final String  name;
    public final Type    type;
    public final Point2D[] vertices;   // null dla BARNSLEY
    public final double  defaultRatio;
    public final Color[] palette;      // kolor per wierzcholek (lub 4 dla Barnsley)

    // --- konstruktor polygon ---
    private ChaosPreset(String name, Point2D[] vertices,
                        double defaultRatio, Color[] palette) {
        this.name         = name;
        this.type         = Type.POLYGON;
        this.vertices     = vertices;
        this.defaultRatio = defaultRatio;
        this.palette      = palette;
    }

    // --- konstruktor Barnsley ---
    private ChaosPreset(String name) {
        this.name         = name;
        this.type         = Type.BARNSLEY;
        this.vertices     = null;
        this.defaultRatio = 0.0;
        this.palette = new Color[]{
            new Color(20,  160,  60),
            new Color(50,  200,  90),
            new Color(100, 230, 130),
            new Color(160, 250, 180)
        };
    }

    // =========================================================
    //  FABRYKI PRESETOW
    // =========================================================

    /** Trojkat Sierpinskiego – 3 wierzcholki, r = 0.5 */
    public static ChaosPreset sierpinski() {
        Point2D[] v = new Point2D[3];
        for (int i = 0; i < 3; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / 3;
            v[i] = new Point2D(Math.cos(angle), Math.sin(angle));
        }
        Color[] pal = {
            new Color(140, 110, 255),
            new Color(180, 150, 255),
            new Color(220, 200, 255)
        };
        return new ChaosPreset("Trojkat Sierpinskiego", v, 0.5, pal);
    }

    /** Pieciocat – 5 wierzcholkow, r = 0.618 (zloty podzial) */
    public static ChaosPreset pentagon() {
        Point2D[] v = new Point2D[5];
        for (int i = 0; i < 5; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / 5;
            v[i] = new Point2D(Math.cos(angle), Math.sin(angle));
        }
        Color[] pal = {
            new Color(255, 160,  50),
            new Color(255, 190,  80),
            new Color(255, 215, 120),
            new Color(255, 235, 160),
            new Color(255, 248, 200)
        };
        return new ChaosPreset("Pieciocat (r=0.618)", v, 0.618, pal);
    }

    /** Szesciocat – 6 wierzcholkow, r = 0.667 */
    public static ChaosPreset hexagon() {
        Point2D[] v = new Point2D[6];
        for (int i = 0; i < 6; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i / 6;
            v[i] = new Point2D(Math.cos(angle), Math.sin(angle));
        }
        Color[] pal = {
            new Color( 50, 190, 220),
            new Color( 90, 210, 235),
            new Color(130, 228, 245),
            new Color(170, 240, 252),
            new Color(200, 248, 255),
            new Color(225, 252, 255)
        };
        return new ChaosPreset("Szesciocat (r=0.667)", v, 0.667, pal);
    }

    /** Kwadrat – 4 wierzcholki, r = 0.5 */
    public static ChaosPreset square() {
        Point2D[] v = new Point2D[4];
        for (int i = 0; i < 4; i++) {
            double angle = -Math.PI / 4 + Math.PI / 2 * i;
            v[i] = new Point2D(Math.cos(angle), Math.sin(angle));
        }
        Color[] pal = {
            new Color( 60, 180, 255),
            new Color(110, 210, 255),
            new Color(160, 230, 255),
            new Color(210, 245, 255)
        };
        return new ChaosPreset("Kwadrat (r=0.5)", v, 0.5, pal);
    }

    /** Paprot Barnsleya – IFS, 4 transformacje afiniczne */
    public static ChaosPreset barnsley() {
        return new ChaosPreset("Paprot Barnsleya (IFS)");
    }

    /** Zwraca tablice wszystkich presetow w kolejnosci combo-boxa */
    public static ChaosPreset[] all() {
        return new ChaosPreset[]{
            sierpinski(),
            pentagon(),
            hexagon(),
            square(),
            barnsley()
        };
    }
}
