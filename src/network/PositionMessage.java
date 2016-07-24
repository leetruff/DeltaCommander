package network;

import com.mygdx.game.Hero;

/**
 * Created by hermann on 05.06.16.
 */
public class PositionMessage {

    float posX;
    float posY;
    String state;

    /**
     * Leerer Konstruktur für Kryonet bzw. zum Verbindungsaufbau
     */
    public PositionMessage()
    {
        posX=-1;
        posY=-1;
        state = "Falling";
    }

    /**
     * Konstruktur für Ingame-Kommunikation
     * @param posX X-Position des Hosts
     * @param posY Y-Position des Hosts
     * @State des Heros
     */
    public PositionMessage(float posX, float posY, Hero.State state)
    {
        this.posX= posX;
        this.posY= posY;
        this.state=state.name();
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public String getState() {
        return state;
    }
}
