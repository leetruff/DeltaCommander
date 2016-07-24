package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Hero.State;

import hudElements.JumpButton;
import network.MultiMenu;

/**
 * Screen: Haupmenue - Navigation zu allen Bereichen des Spiels. <br>
 * Auswahl von Menuepunkten durch Schiessen auf den jeweiligen Button. <br>
 * Beenden des Spiels durch Verlassen des Bildschirms mit dem Hero
 * @author Tim
 */
public class MainMenuScreen implements Screen {

	MyGdxGame game;
	OrthographicCamera gamecam;
	OrthographicCamera hud;
	Viewport gameport;
	Viewport hudport;
	SpriteBatch batch;
	ShapeRenderer shape;
	Stage hudStage;
	
	//Objekte um die map zu laden, welche mit TILED erstellt wurde
	TmxMapLoader maploader;
	TiledMap map;
	OrthogonalTiledMapRenderer renderer;
	
	//Box2d Kram
	World world;
	Box2DDebugRenderer b2dr;
	B2WorldCreator b2creator;
	
	//Player
	Hero hero;
	
	//Hintergrund
	Sprite background;

	//Pads
	hudElements.Joystick runningStick;
	hudElements.Joystick aimStick;
	JumpButton jumpButton;
	
	//Bodies fuer Menuepunkte
	Body highscoreButtonBody;
	Body singlePlayerButtonBody;
	Body loadButtonBody;
	Body multiPlayerButtonBody;
	Body settingsButtonBody;
	
	//Button + Schrift
	Texture button;
	Texture logo;
	BitmapFont font;
	
	/**
	 * Konstruktor zum Erzeugen eines MainMenuScreen-Objekts
	 * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen, haelt aktuellen Screen
	 */
	public MainMenuScreen(MyGdxGame game){
		this.game = game;
		this.game.playMenuTheme();
	}
	

	@Override
	public void show() {
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		
		gamecam = new OrthographicCamera();
		hud= new OrthographicCamera();
		gameport = new StretchViewport(MyGdxGame.V_WIDTH / MyGdxGame.PPM, MyGdxGame.V_HEIGHT / MyGdxGame.PPM, gamecam);
		hudport= new FitViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		hudport.setCamera(hud);

		maploader = new TmxMapLoader();
		map = maploader.load("level/menuLevel.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / MyGdxGame.PPM);
		
		gamecam.position.set(gameport.getWorldWidth() / 2 + 10/MyGdxGame.PPM, gameport.getWorldHeight() / 2, 0);
		
		world = new World(new Vector2(0, -10), true);
		b2dr = new Box2DDebugRenderer();
		
		hero = new Hero(world, game, gamecam);
		
		button =  new Texture("buttons/mmButton.png");
		logo = new Texture("fonts/dcLogo.png");
		Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font= new BitmapFont(Gdx.files.internal("fonts/futura.fnt"),new TextureRegion(fontTexture), false);
		font.getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
		font.setColor(Color.WHITE);
		
		b2creator = new B2WorldCreator(world, map);
		
		background = new Sprite(new Texture("level/baseBackground.png"));
		background.setBounds(0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		hudStage = new Stage(hudport);
		
		//für Android Steuerung
		if(Application.ApplicationType.Android==Gdx.app.getType()) {
			//Sticks initialisiert
			runningStick = new hudElements.Joystick(0, 0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
			aimStick = new hudElements.Joystick(Gdx.graphics.getWidth() - 200*Gdx.graphics.getWidth()/ MyGdxGame.V_WIDTH, 1, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
			//JumpButton init
			jumpButton = new JumpButton();
			jumpButton.jumpButton.addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					if (hero.getState() != Hero.State.JUMPING && hero.getState() != Hero.State.FALLING)
						hero.b2body.applyLinearImpulse(new Vector2(0, 5.5f), hero.b2body.getWorldCenter(), true);
					return true;

				}
			});
			
			//Stage für HUD-Elemente
			//hudStage = new Stage(hudport);
			hudStage.addActor(runningStick.touchpad);
			hudStage.addActor(aimStick.touchpad);
			hudStage.addActor(jumpButton.jumpButton);
			Gdx.input.setInputProcessor(hudStage);
			
		}
		
		world.setContactListener(new WorldContactListener());
		
		//Menuepunkte als Box2D-Bodies reealisieren
		BodyDef buttonBodyDef = new BodyDef();
		buttonBodyDef.type = BodyType.StaticBody;
		
		//Highscores Button
		//40: Rest von der Wand, 36 Abstand zw. Buttons, 100: halbe Breite
		buttonBodyDef.position.set(new Vector2( (40+36+100)/MyGdxGame.PPM, 390/MyGdxGame.PPM));

		highscoreButtonBody = world.createBody(buttonBodyDef);

		PolygonShape buttonShape = new PolygonShape();
		buttonShape.setAsBox(100/MyGdxGame.PPM, 30/MyGdxGame.PPM);

		Fixture highscoreButtonFixture = highscoreButtonBody.createFixture(buttonShape, 0.0f);
		highscoreButtonFixture.setUserData("highscores");
		
		//SinglePlayer Button
		buttonBodyDef.position.set(new Vector2((40+(2*36)+(3*100))/MyGdxGame.PPM, 480/MyGdxGame.PPM));
		singlePlayerButtonBody = world.createBody(buttonBodyDef);
		Fixture singlePlayerButtonFixture = singlePlayerButtonBody.createFixture(buttonShape, 0.0f);
		singlePlayerButtonFixture.setUserData("singleplayer");

		//Load Button
		buttonBodyDef.position.set(new Vector2((40+(3*36)+(5*100))/MyGdxGame.PPM, 480/MyGdxGame.PPM));
		loadButtonBody = world.createBody(buttonBodyDef);
		Fixture loadButtonFixture = loadButtonBody.createFixture(buttonShape, 0.0f);
		loadButtonFixture.setUserData("load");
		
		//MultiPlayer Button
		buttonBodyDef.position.set(new Vector2((40+(4*36)+(7*100))/MyGdxGame.PPM, 480/MyGdxGame.PPM));
		multiPlayerButtonBody = world.createBody(buttonBodyDef);
		Fixture multiPlayerButtonFixture = multiPlayerButtonBody.createFixture(buttonShape, 0.0f);
		multiPlayerButtonFixture.setUserData("multiplayer");
		
		//Settings Button
		buttonBodyDef.position.set(new Vector2((40+(5*36)+(9*100))/MyGdxGame.PPM, 390/MyGdxGame.PPM));
		settingsButtonBody = world.createBody(buttonBodyDef);
		Fixture settingsButtonFixture = settingsButtonBody.createFixture(buttonShape, 0.0f);
		settingsButtonFixture.setUserData("settings");

		//Labels - Beschriftung der Buttons
		LabelStyle buttonFont = new LabelStyle(font,Color.LIGHT_GRAY);	
		
		Label highscoreLabel = new Label("Highscores",buttonFont);
		highscoreLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(highscoreButtonBody.getPosition().x*MyGdxGame.PPM-70)),
								Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(highscoreButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-70,y-15

		Label singlePlayerLabel = new Label("Singleplayer",buttonFont);
		singlePlayerLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(singlePlayerButtonBody.getPosition().x*MyGdxGame.PPM-76)),
				Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(singlePlayerButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-76,480-15
		
		Label loadLabel = new Label("Load",buttonFont);
		loadLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(loadButtonBody.getPosition().x*MyGdxGame.PPM-35)),
				Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(loadButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-53,480-15
		
		Label multiPlayerLabel = new Label("Multiplayer",buttonFont);
		multiPlayerLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(multiPlayerButtonBody.getPosition().x*MyGdxGame.PPM-72)),
				Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(multiPlayerButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-72,480-15
		
		Label settingsLabel = new Label("Settings",buttonFont);
		settingsLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(settingsButtonBody.getPosition().x*MyGdxGame.PPM-53)),
				Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(settingsButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-38,390-15

		hudStage.addActor(highscoreLabel);
		hudStage.addActor(singlePlayerLabel);
		hudStage.addActor(loadLabel);
		hudStage.addActor(multiPlayerLabel);
		hudStage.addActor(settingsLabel);
		
	}
	
	/**
	 * Welt samt Positionen von Objekten (fuer das zeichnen von Sprites) aktualisieren und Kamera ausrichten
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void update(float delta){
		handleInput(delta);
		
			
		world.step(1/60f, 1, 1);
		hero.update(delta);	
		
		if(hero.b2body.getPosition().y < -2){
        	dispose();
			System.exit(0);
		}
		
		gamecam.update();
		renderer.setView(gamecam);
	}
	
	/**
	 * Nutzereingaben interpretieren -> Realisiert Steuerung des Helden
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void handleInput(float delta) {
		if(Application.ApplicationType.Desktop==Gdx.app.getType()) {
			
			//Sprung
			if ((Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && hero.getState() != State.JUMPING && hero.getState() != State.FALLING)
				hero.getBody().applyLinearImpulse(new Vector2(0, 5.5f), hero.getBody().getWorldCenter(), true);

			//Nach rechts laufen
			if (Gdx.input.isKeyPressed(Input.Keys.D) && hero.getBody().getLinearVelocity().x <= 3){
				hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), hero.getBody().getWorldCenter(), true);
				
				if(hero.getBody().getLinearVelocity().x < 0){
					//hero.getBody().setLinearVelocity(0, hero.getBody().getLinearVelocity().y);
					hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), hero.getBody().getWorldCenter(), true);
				}
			}

			//Nach links laufen
			if (Gdx.input.isKeyPressed(Input.Keys.A) && hero.getBody().getLinearVelocity().x >= -3){
				hero.getBody().applyLinearImpulse(new Vector2(-0.15f, 0), hero.getBody().getWorldCenter(), true);

				if(hero.getBody().getLinearVelocity().x > 0){
					//hero.getBody().setLinearVelocity(0, hero.getBody().getLinearVelocity().y);
					hero.getBody().applyLinearImpulse(new Vector2(-0.15f, 0), hero.getBody().getWorldCenter(), true);
				}
			}
	
			//Bremst den Spieler aus wenn er sich nicht bewegt
			if (!Gdx.input.isKeyPressed(Input.Keys.D) && !Gdx.input.isKeyPressed(Input.Keys.A) && hero.getState() == State.RUNNING)
				hero.getBody().applyLinearImpulse(new Vector2(hero.getBody().getLinearVelocity().x / 40 * (-1), 0), hero.getBody().getWorldCenter(), true);	
				
			//Schiessen in Richtung Mausposition bei Mausklick 
			if (Gdx.input.isTouched()) {
				hero.shoot(gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).x, gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).y, delta);
			}
		}
		else
		{	
			//Android Steuerung
			if(runningStick.touchpad.isTouched())
			{
				if(runningStick.touchpad.getKnobPercentX()>0.9 && hero.b2body.getLinearVelocity().x <= 3)
				{
					hero.b2body.applyLinearImpulse(new Vector2(0.15f, 0), hero.b2body.getWorldCenter(), true);
				}
				else if(runningStick.touchpad.getKnobPercentX()<-0.9 && hero.b2body.getLinearVelocity().x >= -3)
				{
						hero.b2body.applyLinearImpulse(new Vector2(-0.15f, 0), hero.b2body.getWorldCenter(), true);
				}
			}
			if(aimStick.touchpad.isTouched()&&(Math.abs(aimStick.touchpad.getKnobPercentX())>0.2||Math.abs(aimStick.touchpad.getKnobPercentY())>0.2))
			{
				hero.shoot((aimStick.touchpad.getKnobPercentX()>0)?(100f-Math.abs(aimStick.touchpad.getKnobPercentY()*100)):-(100f-Math.abs(aimStick.touchpad.getKnobPercentY()*100)),((aimStick.touchpad.getKnobPercentY()>0)? 20:-20),delta);
			}
		}
	}

	@Override
	public void render(float delta) {
		
		update(delta);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Hintergrund zeichnen
		batch.setProjectionMatrix(hud.combined);
		batch.begin();
		background.draw(batch);
		batch.end();
		
		batch.setProjectionMatrix(gamecam.combined);
		renderer.render();
		
		//DebugRenderer
		//b2dr.render(world, gamecam.combined);
		
		
		batch.begin();
		hero.draw(batch);
		
		//Alle Projektile malen
		for(int i = 0; i < hero.getBulletList().size(); i++){
			hero.getBulletList().get(i).draw(batch);
		}
		
		//Schriftzug und Buttons zeichnen
		batch.draw(logo, 300/MyGdxGame.PPM, 3.5f, logo.getWidth()/MyGdxGame.PPM,logo.getHeight()/MyGdxGame.PPM); 
		batch.draw(button, highscoreButtonBody.getPosition().x-(100/MyGdxGame.PPM), highscoreButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
		batch.draw(button, singlePlayerButtonBody.getPosition().x-(100/MyGdxGame.PPM), singlePlayerButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
		batch.draw(button, loadButtonBody.getPosition().x-(100/MyGdxGame.PPM), loadButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
		batch.draw(button, multiPlayerButtonBody.getPosition().x-(100/MyGdxGame.PPM), multiPlayerButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
		batch.draw(button, settingsButtonBody.getPosition().x-(100/MyGdxGame.PPM), settingsButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
		
		batch.end();
		
		
		//Android-Steuerelemente 
		if(Application.ApplicationType.Android==Gdx.app.getType()) {
			hudStage.act(Gdx.graphics.getDeltaTime());
			hudStage.draw();
		}
		hudStage.draw();
		
		Gdx.graphics.setTitle("Delta Commander | " + Gdx.graphics.getFramesPerSecond() + " FPS");
		
		
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
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
		map.dispose();
		renderer.dispose();
		world.dispose();
		b2dr.dispose();
		button.dispose();;
		logo.dispose();;
		font.dispose();;

	}
	
	/**
	 * ContactListener, der alle Kollisionen zwischen Fixtures in der Box2D-Welt interpretiert.<br>
	 * Menuepunkt ausgewaehlt, wenn die entsprechende Fixture von einer Bullet-Fixture getroffen wurde.
	 */
  	public class WorldContactListener implements ContactListener {

  		// Contact listener for all fixtures in the world
  		@Override
  		public void beginContact(Contact contact) {
  			Fixture fixtureA = contact.getFixtureA();
  			Fixture fixtureB = contact.getFixtureB();
  			System.out.println("Contact between " + fixtureA.getUserData() + " and "
  					+ fixtureB.getUserData());

  			//Wenn Bullet beteiligt -> Sicherstellen BulletFixture immer B  
  			if(fixtureA.getUserData()=="bullet")
  			{
  				Fixture temp = fixtureA;
  				fixtureA = fixtureB;
  				fixtureB = temp;
  			}
  			
  			//Kollision von fixtureA mit Bullet
  			if(fixtureB.getUserData()=="bullet")
  			{
  				//Singleplayer-Fixture getroffen -> Zum Levelauswahl-Screen wechseln
                if(fixtureA.getUserData() == "singleplayer")
                {
                	game.playButtonClicked();
                	game.setScreen(new LevelSelect(game,false));
                }
                //Multiplayer-Fixture getroffen -> Zum Multimenu-Screen wechseln
                else if(fixtureA.getUserData() == "multiplayer")
                {
                	game.playButtonClicked();
					game.setScreen(new MultiMenu(game,hero.getX(),hero.getY()));
                }
                //Highscore-Fixture getroffen -> Zum Highscore-Screen wechseln
                else if(fixtureA.getUserData() == "highscores")
                {
                	game.playButtonClicked();
                	game.setScreen(new HighscoreScreen(game));
                }
                //Load-Fixture getroffen -> Letzten Speicherstand laden
                else if(fixtureA.getUserData() == "load")
                {
                	game.playButtonClicked();
                	XMLInstructions loaded = XMLInteraction.loadGame();
                	if(loaded.id.equals("NONE")){ //TODO
                		//Falls kein Savegame existiert (bisher passiert nichts) 
                	} else if(loaded.id.equals("level1")){
                		game.setScreen(new Level1(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("level2")){
                		game.setScreen(new Level2(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("level3")){
                		game.setScreen(new Level3(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("levelice1")){
                		game.setScreen(new LevelIce1(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("levelice2")){
                		game.setScreen(new LevelIce2(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("levelice3")){
                		game.setScreen(new LevelIce3(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("levelscifi1")){
                		game.setScreen(new LevelScifi1(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("levelscifi2")){
                		game.setScreen(new LevelScifi2(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	} else if(loaded.id.equals("levelscifi3")){
                		game.setScreen(new LevelScifi3(game, loaded.health, loaded.lives, loaded.ammo, loaded.score, loaded.laserAmmo, loaded.tripleAmmo));
                	}
                }
                //Settings-Fixture getroffen -> Zum Einstellungs-Screen wechseln
                else if(fixtureA.getUserData() == "settings")
                {
                	game.playButtonClicked();
                	game.setScreen(new SettingsScreen(game));
                }

  			}
  			 			
  			
  		}

  		@Override
  		public void endContact(Contact contact) {}
  		@Override
  		public void preSolve(Contact contact, Manifold oldManifold) {}
  		@Override
  		public void postSolve(Contact contact, ContactImpulse impulse) {}
  	}

}