package hudElements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Created by hermann on 26.06.16.
 */
/**
 * Menü, das erscheint wenn der Spieler alle Leben verliert.
 */
public class GameOverMenu {



    public Texture gameOverLabel;
    float labelY;
    public TextButton retry;
    public TextButton mainMenu;
    BitmapFont font;
    TextureAtlas atlas;
    Skin skin;
    TextButton.TextButtonStyle buttonstyle;


    public GameOverMenu()
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

        retry = new TextButton("Retry",buttonstyle);
        retry.setPosition(Gdx.graphics.getWidth()/2-retry.getWidth()/2,-50);
        
        mainMenu = new TextButton("Main Menu", buttonstyle);
        mainMenu.setPosition(Gdx.graphics.getWidth()/2-mainMenu.getWidth()/2,-125);
        

        //Labels
        gameOverLabel = new Texture(Gdx.files.internal("fonts/gameOver.png"));
        labelY= Gdx.graphics.getHeight()+50;


    }

    public Texture getGameOverLabel() {
        return gameOverLabel;
    }

    public TextButton getRetry() {
        return retry;
    }
    
    public TextButton getMainMenu() {
        return mainMenu;
    }

    public void setLabelY(float labelY) {
        this.labelY = labelY;
    }

    public float getLabelY() {
        return labelY;
    }

    /**
     * Aktiviert die Buttons wenn Spieler verloren hat
     */
    public void enable()
    {
        retry.setDisabled(false);
        mainMenu.setDisabled(false);
    }

    /**
     * Deaktiviert die Buttons, wenn Spieler weiterspielt.
     */
    public void disable()
    {
        retry.setDisabled(true);
        mainMenu.setDisabled(true);
    }
}
