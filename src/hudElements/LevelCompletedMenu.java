package hudElements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Created by hermann on 30.06.16.
 */
public class LevelCompletedMenu {

    /**
     * Menü, das erscheint wenn Spieler ein Level beendet hat.
     */

    public Texture levelCompleted;
    float labelX;
    public TextButton continueButton;
    BitmapFont font;
    TextureAtlas atlas;
    Skin skin;
    TextButton.TextButtonStyle buttonstyle;

    public LevelCompletedMenu()
    {
        //Schriftart
        Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font= new BitmapFont(Gdx.files.internal("fonts/futura.fnt"),new TextureRegion(fontTexture), false);

        //Buttonstyle
        atlas = new TextureAtlas("buttons/button.pack");
        skin = new Skin(atlas);
        buttonstyle = new TextButton.TextButtonStyle();
        buttonstyle.font=font;
        buttonstyle.fontColor= Color.LIGHT_GRAY;
        buttonstyle.up = skin.getDrawable("button.up");
        buttonstyle.down = skin.getDrawable("button.down");

        continueButton = new TextButton("Continue",buttonstyle);

        //Labels
        levelCompleted = new Texture(Gdx.files.internal("fonts/levelCompleted.png"));
        labelX= -50-levelCompleted.getWidth();
        continueButton.setPosition(Gdx.graphics.getWidth()-10+levelCompleted.getWidth()/2,Gdx.graphics.getHeight()/2-75);
        //gameOverLabel.setPosition(Gdx.graphics.getWidth()/2-gameOverLabel.getWidth(),Gdx.graphics.getHeight()+50);


    }

    public Texture getLevelCompleted() {
        return levelCompleted;
    }

    public TextButton getcontinueButton() {
        return continueButton;
    }

    public void setLabelX(float labelX) {
        this.labelX = labelX;
    }

    public float getLabelX() {
        return labelX;
    }
    public void enable()
    {
        continueButton.setDisabled(false);
    }
    public void disable()
    {
        continueButton.setDisabled(true);
    }
}
