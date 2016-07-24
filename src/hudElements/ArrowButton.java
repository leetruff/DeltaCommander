package hudElements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by hermann on 01.07.16.
 */
public class ArrowButton {
    ImageButton arrow;
    Drawable drawable;
    Skin buttonSkin;

    /**
     * Button für Levelauswahl
     * @param right True, falls Button nach rechts zeigen soll.
     */
    public ArrowButton(boolean right)
    {
        buttonSkin=new Skin();
        TextureRegion buttonRegion = new TextureRegion(new Texture("buttons/arrow.png"));
        if(!right) {
            buttonRegion.flip(true, false);
        }
        buttonSkin.add("arrow",buttonRegion);
        drawable=buttonSkin.getDrawable("arrow");
        arrow = new ImageButton(drawable);
    }
    public void setPosition(float x,float y)
    {
        arrow.setPosition(x,y);
    }

    public ImageButton getArrow() {
        return arrow;
    }
    public void disable()
    {

    }
}
