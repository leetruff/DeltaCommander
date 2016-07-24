package network;

/**
 * Created by hermann on 21.06.16.
 */
public class CorrectionMessage {
    float posX;
    float posY;

    /**
     * Nachrichtentyp für Korrektur der Hero-Position im MUltiplayer, wird alle 500ms gesendet
     * @param x X-Position des Heros
     * @param y Y-Position des Heros
     */
    public CorrectionMessage(float x, float y)
    {
        posX=x;
        posY=y;
    }
    public CorrectionMessage()
    {

    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }
}
