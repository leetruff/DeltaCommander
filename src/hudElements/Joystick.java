package hudElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mygdx.game.MyGdxGame;

/**
 * Created by hermann on 01.06.16.
 */
public class Joystick{

    public Touchpad touchpad;
    Touchpad.TouchpadStyle touchpadStyle;
    Skin touchpadSkin;
    Drawable touchBackground;
    Drawable touchKnob;

    /**
     * Erstellt digitalen Joystick für Android Version
     * @param x X-Position, an der sich der Jostick befinden soll
     * @param y Y-Position, an der sich der Jostick befinden soll
     * @param width Breite des Bilschrims
     * @param height Höhe des Bildschrims
     */
    public Joystick (float x, float y,float width, float height){

        touchpadSkin = new Skin();
        touchpadSkin.add("stickBackground", new Texture("sprites/StickBackground.png"));
        touchpadSkin.add("stick", new Texture("sprites/Stick.png"));
        touchpadSkin.add("smallStick", new Texture("sprites/SmallStick.png"));
        touchpadSkin.add("bigStick", new Texture("sprites/BigStick.png"));
        touchpadStyle = new Touchpad.TouchpadStyle();
        touchBackground = touchpadSkin.getDrawable("stickBackground");
        if((width/ MyGdxGame.V_WIDTH)<1)
        {
            touchKnob = touchpadSkin.getDrawable("smallStick");
            System.out.println("Small One");
        }
        else if (width/ MyGdxGame.V_WIDTH>1.4)
        {
            System.out.println("Big One");
            touchKnob= touchpadSkin.getDrawable("bigStick");
        }
        else
        {
            System.out.println("Normal One");
            touchKnob = touchpadSkin.getDrawable("stick");
        }
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        touchpad = new Touchpad(10, touchpadStyle);
        touchpad.setBounds(x, y, 200f*(width/ MyGdxGame.V_WIDTH), 200f*(height/MyGdxGame.V_HEIGHT));
    }


}
