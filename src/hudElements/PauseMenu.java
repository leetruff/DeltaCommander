package hudElements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.mygdx.game.MyGdxGame;


/**
 * Tabelle mit Buttons, die das Pause-Menue darstellen. <br>
 * Wird im Spiel (Level Screens) ueber das Level gezeichet, falls das Spiel pausiert wurde.
 * 
 * @author Tim
 */
public class PauseMenu{

	public Table pauseTable;
    public TextButton continueButton;
    public TextButton saveButton;
    public TextButton menuButton;
    public TextButton quitButton;
    TextureAtlas atlas;
	Skin skin;
	TextButton.TextButtonStyle buttonstyle;
	BitmapFont font;
	
	/**
	 * Konstruktor: Erstellt eine Tabelle und fuegt 4 Buttons mit eigenem "Buttonstyle" hinzu
	 */
    public PauseMenu (){
    	
    	//Schriftart fuer Buttonbeschriftung
    	Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font= new BitmapFont(Gdx.files.internal("fonts/futura.fnt"),new TextureRegion(fontTexture), false);
		font.getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
		
    	//Erstellen des "Buttonstyle" (Texturen, Schrift)
    	atlas = new TextureAtlas("buttons/button.pack");
		skin = new Skin(atlas);
		buttonstyle = new TextButton.TextButtonStyle();
		buttonstyle.font=font;
		buttonstyle.fontColor= Color.LIGHT_GRAY;
		buttonstyle.up = skin.getDrawable("button.up");
		buttonstyle.down = skin.getDrawable("button.down");
		buttonstyle.pressedOffsetX = 1;
		buttonstyle.checkedOffsetY = -1;

		//Tabelle erstellen
    	pauseTable = new Table();
    	System.out.println(Gdx.graphics.getWidth());
    	pauseTable.setBounds(Gdx.graphics.getWidth()/2-(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(170))), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/40),
    			Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/340), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/500));
        pauseTable.setDebug(false);
        
        //Buttons erstellen
        continueButton = new TextButton("Continue",buttonstyle);
        continueButton.pad(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(20)));
        saveButton = new TextButton("Save",buttonstyle);
        saveButton.pad(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(20)));
        menuButton = new TextButton("Main Menu",buttonstyle);
        menuButton.pad(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(20)));
        quitButton = new TextButton("Quit",buttonstyle);
        quitButton.pad(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(20)));
        
        //Zu Spielbeginn nicht pausiert -> Buttons deaktivieren
        disableButtons();
        
        //Buttons zur Tabelle hinzufuegen 300
        pauseTable.add(continueButton).width(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(300))).spaceBottom(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50)));
        pauseTable.row();
        pauseTable.add(saveButton).width(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(300))).spaceBottom(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50)));
        pauseTable.row();
        pauseTable.add(menuButton).width(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(300))).spaceBottom(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50)));
        pauseTable.row();
        pauseTable.add(quitButton).width(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(300))).spaceBottom(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50)));
        
    }
    
    /**
     * Aktiviert Buttons damit diese klickbar sind, wenn Spiel pausiert ist.
     */
    public void enableButtons()
    {
    	continueButton.setTouchable(Touchable.enabled);
        saveButton.setTouchable(Touchable.enabled);
        menuButton.setTouchable(Touchable.enabled);
        quitButton.setTouchable(Touchable.enabled);   	
    }

    /**
     * Deaktiviert Buttons damit diese nicht klickbar sind, wenn Spiel laeuft.
     */
    public void disableButtons()
    {
    	continueButton.setTouchable(Touchable.disabled);
        saveButton.setTouchable(Touchable.disabled);
        menuButton.setTouchable(Touchable.disabled);
        quitButton.setTouchable(Touchable.disabled);	
    }


}


