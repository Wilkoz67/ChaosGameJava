package chaosGame;

/**
 * Prosta klasa reprezentujaca punkt 2D (wspolrzedne double).
 */
public class Point2D {
    public double x, y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Zwraca nowy punkt bedacy interpolacja liniowa: this + r*(other-this) */
    public Point2D lerp(Point2D other, double r) {
        return new Point2D(
            this.x + r * (other.x - this.x),
            this.y + r * (other.y - this.y)
        );
    }
}
