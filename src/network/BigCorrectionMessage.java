package network;

import com.mygdx.game.MovingPlatform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hermann on 30.06.16.
 */
public class BigCorrectionMessage {

    float posX;
    float posY;
    int lifes;
    int health;
    int bossLife;
    public BigCorrectionMessage()
    {

    }

    /**
     * Nachrichtenklasse für große Updates, falls es tatsächlich große Abweichungen geben sollte
     * @param x X-Position des Hero-Bodys
     * @param y Y-Position des Hero-Bodys
     * @param lifes Anzahl der Leben
     * @param health Lebensleiste
     * @param bl Falls Bossfight, auch sein leben nachkorrigieren
     */
    public BigCorrectionMessage(float x, float y, int lifes, int health,int bl)
    {
        posX=x;
        posY=y;
        this.lifes=lifes;
        this.health=health;
        bossLife=bl;


    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public int getHealth() {
        return health;
    }

    public int getLifes() {
        return lifes;
    }

    public int getBossLife() {
        return bossLife;
    }
}
