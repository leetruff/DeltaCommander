package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;

/**
 * Screen: Erster Screen, der nur aus einem kurzen Intro (Schriftzug wird 
 * ein- und ausgeblendet) besteht und dann zum MainMenuScreen wechselt.
 * 
 * @author Lars, Tim
 */
public class IntroScreen implements Screen {

	MyGdxGame game;
	BitmapFont font;
	FreeTypeFontGenerator generator;
	float fade = 0;
	Vector2 textPosition;
	SpriteBatch batch;
	boolean reverse = false;
	
	/**
	 * Konstruktor zum Erzeugen eines IntroScreens
	 * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen, haelt aktuellen Screen
	 */
	public IntroScreen(MyGdxGame game) {
		this.game = game;
	}
	
	@Override
	public void show() {
		batch = new SpriteBatch();
		
		//Schrift erstellen (Groesse und Position in Relation zur Fenster-/Bildschirmgroesse)
		generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ocraextended.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = (int) ((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/60f));
		font = generator.generateFont(parameter);
		textPosition = new Vector2((Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(200))), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(300)));
	}

	@Override
	public void render(float delta) {
		 Gdx.gl.glClearColor(0, 0, 0, 1);
		 Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		 
		 if(fade == 0){
			 game.playIntro();
		 }
		 
		 //Einblenden
		 if (!reverse){
			  fade += delta/4;
			    if(fade >= 0.8) {
			       reverse = true;
			    }
		 }
		 //Ausblenden
		 else{
			 fade -= delta/3;
			 
			 if(fade <= 0) {
				 game.setScreen(new MainMenuScreen(game));
			 }
		 }
		    
		 batch.begin();
		 //Schriftzug zeichnen
		 font.setColor(1, 1, 1, fade);
		 font.draw(batch, "A Team Delta Production", textPosition.x, textPosition.y);
		 batch.end();	
		
		 Gdx.graphics.setTitle("Delta Commander | " + Gdx.graphics.getFramesPerSecond() + " FPS");
	}



	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		generator.dispose();
	}
}
