package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by hermann on 23.06.16.
 */
public class Checkpoint extends Sprite{

    float posX;
    float posY;
    boolean passed;
    static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/flagoff.png"));

    public Checkpoint(float x, float y)
    {
        super(defaultTexture);
        posX=x;
        posY=y;
        passed=false;
        setBounds(posX,posY,51/MyGdxGame.PPM,52/MyGdxGame.PPM);
    }

    public float getPosX() {
        return posX;
    }

    public void switchOn()
    {
        setTexture(new Texture(Gdx.files.internal("sprites/flagon.png")));
    }

    public float getPosY() {
        return posY;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}
