package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SettingsScreen implements Screen {

	MyGdxGame game;
	OrthographicCamera cam;
	Viewport port;
	SpriteBatch batch;
	ShapeRenderer shape;
	Stage stage;
	
	TextureAtlas atlas;
	Skin skin, buttonSkin;
	TextButtonStyle textButtonStyle, chBlackStyle, chRedStyle, chRoundStyle;
	SliderStyle sliderStyle;
	BitmapFont font;
	private Slider volumeSlider;
	private Slider sfxSlider;
	private TextButton buttonMenu, buttonChBlack, buttonChRed, buttonChRound;
	private TextField username;
	private TextFieldStyle tfStyle;
	
	private Sprite background;
	
	public SettingsScreen(MyGdxGame game){
		this.game = game;
	}
	
	@Override
	public void show() {
		
		//Hintergrund
		background = new Sprite(new Texture(Gdx.files.internal("buttons/settings_background.png")));
		
		//Schrift
		Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("fonts/futura.fnt"),new TextureRegion(fontTexture), false);
		font.setColor(Color.SKY);
		
		//Camera - Port
		cam = new OrthographicCamera();
		port = new StretchViewport(MyGdxGame.V_WIDTH, MyGdxGame.V_HEIGHT, cam);
		cam.setToOrtho(false, port.getWorldWidth(), port.getWorldHeight());
		cam.update();
		
		batch = new SpriteBatch();
		batch.setProjectionMatrix(cam.combined);

		//Skin
		Texture button =  new Texture("buttons/mmButton.png");
		Texture crosshair_red = new Texture("sprites/crosshair_red.png");
		Texture crosshair_black = new Texture("sprites/crosshair_black.png");
		Texture crosshair_round = new Texture("sprites/crosshair_round.png");
		
		atlas = new TextureAtlas(Gdx.files.internal("buttons/ui-blue.atlas"));
		skin = new Skin();
		skin.add("myButton", button);
		skin.add("crosshair_black", crosshair_black);
		skin.add("crosshair_red", crosshair_red);
		skin.add("crosshair_round", crosshair_round);
		skin.addRegions(atlas);
		
		//Erstellen des "Buttonstyle" (Texturen, Schrift)
    	atlas = new TextureAtlas("buttons/button.pack");
		buttonSkin = new Skin(atlas);
		
		TextButtonStyle buttonstyle = new TextButton.TextButtonStyle();
		buttonstyle.font=font;
		buttonstyle.fontColor= Color.LIGHT_GRAY;
		buttonstyle.up = buttonSkin.getDrawable("button.up");
		buttonstyle.down = buttonSkin.getDrawable("button.down");
		buttonstyle.pressedOffsetX = 1;
		buttonstyle.checkedOffsetY = -1;
		
		//MainMenu Button
		buttonMenu = new TextButton("Main Menu",buttonstyle);
		buttonMenu.setSize(300, 75);
		buttonMenu.setPosition(cam.viewportWidth/2 - buttonMenu.getWidth() / 2, 50);
		
		//Styles
		sliderStyle = new SliderStyle(skin.getDrawable("slider_back_hor"), skin.getDrawable("knob_01"));
		tfStyle = new TextFieldStyle(font, font.getColor(), skin.getDrawable("textbox_cursor_02"), skin.getDrawable("color_widgettext"), skin.getDrawable("textbox_02"));

		chBlackStyle = new TextButtonStyle();
		chBlackStyle.over = skin.getDrawable("crosshair_black");
		chBlackStyle.up = skin.getDrawable("crosshair_black");
		chBlackStyle.font = font;
		
		chRedStyle = new TextButtonStyle();
		chRedStyle.over = skin.getDrawable("crosshair_red");
		chRedStyle.up = skin.getDrawable("crosshair_red");
		chRedStyle.font = font;
		
		chRoundStyle = new TextButtonStyle();
		chRoundStyle.over = skin.getDrawable("crosshair_round");
		chRoundStyle.up = skin.getDrawable("crosshair_round");
		chRoundStyle.font = font;
		
		buttonChBlack = new TextButton("", chBlackStyle);
		buttonChBlack.setSize(128, 128);
		buttonChBlack.setPosition(-100 + cam.viewportWidth/2 - buttonChBlack.getWidth() / 2, 200);
		
		buttonChRed = new TextButton("", chRedStyle);
		buttonChRed.setSize(128, 128);
		buttonChRed.setPosition(cam.viewportWidth/2 - buttonChRed.getWidth() / 2, 200);
		
		buttonChRound = new TextButton("", chRoundStyle);
		buttonChRound.setSize(128, 128);
		buttonChRound.setPosition(100 + cam.viewportWidth/2 - buttonChRound.getWidth() / 2, 200);
		

		volumeSlider = new Slider(0, 10, 1, false, sliderStyle);
		volumeSlider.setSize(300, 75);
		volumeSlider.setPosition(cam.viewportWidth/2 - volumeSlider.getWidth() / 2, 480);
		volumeSlider.setValue(game.getMusicVolume());
		
		sfxSlider = new Slider(0, 10, 1, false, sliderStyle);
		sfxSlider.setSize(300, 75);
		sfxSlider.setPosition(cam.viewportWidth/2 - sfxSlider.getWidth() / 2, 365);
		sfxSlider.setValue(game.getSFXVolume());
		
		username = new TextField(game.getUsername(), tfStyle);
		username.setSize(300, 40);
		username.setPosition(cam.viewportWidth/2 - username.getWidth()/2, 612);
		
		Gdx.input.setCatchBackKey(true);
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		stage.addActor(buttonMenu);
		stage.addActor(buttonChBlack);
		stage.addActor(buttonChRed);
		stage.addActor(buttonChRound);
		stage.addActor(volumeSlider);
		stage.addActor(sfxSlider);
		stage.addActor(username);
		stage.setViewport(port);
		
		username.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				game.setUsername(username.getText());
			}
		});
		
		sfxSlider.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	game.setSFXVolume((int) (sfxSlider.getValue()));
	        	//Hoertest
	        	game.playHeroShot();
	        }
	        
	    });

		volumeSlider.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	game.setMusicVolume((int) (volumeSlider.getValue()));
	        }
	    });
		
		buttonMenu.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	game.playButtonClicked();
	        	stage.dispose();
	        	game.setScreen(new MainMenuScreen(game));
	        }
	    });
		
		buttonChBlack.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_black.png"));
	    		Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
	    		pm.dispose();
	    		XMLInteraction.saveSettings("Crosshair", Float.toString(0f));
	        }
	    });
		
		buttonChRed.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_red.png"));
	    		Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
	    		pm.dispose();
	    		XMLInteraction.saveSettings("Crosshair", Float.toString(1f));
	        }
	    });
		
		buttonChRound.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_round.png"));
	    		Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
	    		pm.dispose();
	    		XMLInteraction.saveSettings("Crosshair", Float.toString(2f));
	        }
	    });
		
		
	}

	@Override
	public void render(float delta) {
		//BACK ruft das Multiplayer Men� auf
		if(Gdx.input.isKeyPressed(Keys.BACK)){
			game.setScreen(new MainMenuScreen(game));
		}
		
		Gdx.gl.glClearColor(0.086f, 0.086f, 0.098f, 1f);;
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		cam.update();
		
		batch.begin();
		background.draw(batch);
		GlyphLayout glyphLayout = new GlyphLayout();
		glyphLayout.setText(font, "Username");
		font.draw(batch, glyphLayout, cam.viewportWidth/2 - glyphLayout.width/2, 695);
		glyphLayout.setText(font, "Music Volume");
		font.draw(batch, glyphLayout, cam.viewportWidth/2 - glyphLayout.width/2, 580);
		glyphLayout.setText(font, "SFX Volume");
		font.draw(batch, glyphLayout, cam.viewportWidth/2 - glyphLayout.width/2, 465);
		glyphLayout.setText(font, "Crosshair");
		font.draw(batch, glyphLayout, cam.viewportWidth/2 - glyphLayout.width/2, 350);
		
		batch.end();
		
		stage.act();
		stage.draw();
		
		Gdx.graphics.setTitle("Delta Commander | " + Gdx.graphics.getFramesPerSecond() + " FPS");

	}

	@Override
	public void resize(int width, int height) {
		port.update(width, height);
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
