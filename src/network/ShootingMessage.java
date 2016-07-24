package network;

/**
 * Created by hermann on 19.06.16.
 */
public class ShootingMessage {

    float x;
    float y;

    public ShootingMessage()
    {

    }

    /**
     * Nachrichtenklasse zur Benachrichtung von Schüssen.
     * @param x-Koordinate des Schusses
     * @param y-Koordinate des Schusses
     */
    public ShootingMessage(float x, float y)
    {
        this.x=x;
        this.y=y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
