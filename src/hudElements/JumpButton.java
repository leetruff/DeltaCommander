package hudElements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mygdx.game.Hero;
import com.mygdx.game.MyGdxGame;

/**
 * Created by hermann on 03.06.16.
 */
public class JumpButton {

    public ImageButton jumpButton;
    Drawable buttonImage;
    Skin buttonSkin;

    /**
     * Erstellt Image Button für Android Version
     */
    public JumpButton ()
    {
        //JumpButton
        buttonSkin = new Skin();
        buttonSkin.add("jumpButton", new Texture("sprites/JumpButton.png"));
        buttonImage = buttonSkin.getDrawable("jumpButton");
        jumpButton = new ImageButton(buttonImage);
        jumpButton.setBounds(Gdx.graphics.getWidth() - 150, 250*Gdx.graphics.getWidth()/ MyGdxGame.V_WIDTH, 100, 100);

    }
}
