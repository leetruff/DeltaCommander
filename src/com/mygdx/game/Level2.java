package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Hero.State;

import java.util.ArrayList;

import enemies.Boss;
import enemies.BossBullet;
import enemies.EnemyBullet;
import enemies.EnemyHoming;
import enemies.EnemySmallBullet;
import enemies.EnemySoldier;
import enemies.EnemyTower;
import hudElements.GameOverMenu;
import hudElements.JumpButton;
import hudElements.LevelCompletedMenu;
import hudElements.PauseMenu;
import network.JoinHeroBullet;
import powerups.HealthPU;
import powerups.LaserPU;
import powerups.LifePU;
import powerups.PickUp;
import powerups.ShieldPU;
import powerups.TripleshotPU;

/**
 * Screen: Level 2 der Base Welt
 * @Leveldesign Tim
 * @author Fabian, Lars, Hermann, Tim
 */
public class Level2 extends GameScreen {

	//Zeit die zu unterbieten ist in Sekunden:
	final int timeToBeat = 105;
	//Gewichtung Sekunden zu Punkten:
	final int timeFactor = 10;

	public MyGdxGame game;
	public OrthographicCamera gamecam;
	OrthographicCamera hud;
	Viewport gameport;
	Viewport hudport;
	public SpriteBatch batch;
	ShapeRenderer shape;
	public Stage hudStage;
	public Stage pauseStage;

	public boolean paused;

	//objekte um die map zu laden, welche mit TILED erstellt wurde
	TmxMapLoader maploader;
	TiledMap map;
	OrthogonalTiledMapRenderer renderer;

	//Box2d Kram
	public World world;
	Box2DDebugRenderer b2dr;
	B2WorldCreator b2creator;

	//Player
	public Hero hero;
	Shield shield;

	//Anzeige fuer Leben, Punkte, Munition
	Sprite statBar;
	Sprite healthBar;
	BitmapFont font;
	Vector2 scorePosition;
	Vector2 livesPosition;
	Vector2 ammoPosition;
	Vector2 timePosition;
	float levelTime;
	float barwidth;
	
	//Hintergrund
	Sprite background;
	
	//Pads
	public hudElements.Joystick runningStick;
	public hudElements.Joystick aimStick;
	public JumpButton jumpButton;

	//Pause-Menue
	public PauseMenu pauseMenu;


	//Checkpoints
	Checkpoint[] checkpoints = new Checkpoint[5]; 
	float nextCheckpoint;
	int checkpointCounter;
	int respawnTimer;

	//GameOver
	boolean gameOver;
	GameOverMenu gameOverMenu;
	Stage gameOverStage;
	float labelX;
	float buttonX;

	//Level Complete
	boolean levelCompleted;
	LevelCompletedMenu levelCompletedMenu;
	Stage levelCompleteStage;

	//Bewegende Platformen
	ArrayList<MovingPlatform> platformList;
	MovingPlatform platform1;
	MovingPlatform platform2;
	MovingPlatform platform3;
	MovingPlatform platform4;
	MovingPlatform platform5;
	MovingPlatform platform6;
	
	//PowerUps
	ArrayList<PickUp> powerUpList; 
	HealthPU healthPU1;
	LifePU lifePU1;
	LaserPU laserPU1;

	//Enemies
	ArrayList<EnemyTower> enemyTowerList;
	ArrayList<EnemySoldier> enemySoldierList;
	ArrayList<EnemyHoming> enemyHomingList;

	//Geschosse von bereits besiegten Gegnern
	ArrayList<EnemyBullet> remainingEnemyBulletList;
	ArrayList<EnemySmallBullet> remainingEnemySmallBulletList;

	/**
	 * Konstruktor zum Erzeugen eines Level2-Objekts
	 * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen, haelt aktuellen Screen
	 */
	public Level2(MyGdxGame game){
		this.game = game;
		this.game.playLevelThemeW1();
		gamecam = new OrthographicCamera();
		gameport = new StretchViewport(MyGdxGame.V_WIDTH / MyGdxGame.PPM, MyGdxGame.V_HEIGHT / MyGdxGame.PPM, gamecam);
		gamecam.position.set(gameport.getWorldWidth() / 2, gameport.getWorldHeight() / 2, 0);
		world = new World(new Vector2(0, -10), true);
		world.setContactListener(new WorldContactListener());
		hero = new Hero(world, this, game, gamecam);
		this.levelTime = 0;
	}
	/**
	 * Konstruktor der Stats des Heros uebergeben bekommt (Fuer Laden/Uebergang ins naechste Level)
	 * @param game Startklassen-Instanz: Zugriff auf globale Ressourcen, haelt aktuellen Screen
	 * @param health Verbleibende Lebenspunkte
     * @param lives Anzahl verbleibender Leben
     * @param ammo Verbleibende Munition
     * @param score Bisher erreichte Punktzahl
     * @param laserAmmo Verbleibende Laser-Munition
     * @param tripleAmmo Verbleibende Triple-Munition
	 */
	public Level2(MyGdxGame game, int health, int lives, int ammo, int score, int laserAmmo, int tripleAmmo){
		this(game);
		
		if(hero != null){
			world.destroyBody(hero.getBody());
		}
		
		hero = new Hero(world, this, game, gamecam, health, lives, ammo, score, laserAmmo, tripleAmmo);
	}


	@Override
	public void show() {

        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
		batch = new SpriteBatch();
		shape = new ShapeRenderer();

		hud= new OrthographicCamera();
		hudport= new StretchViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		hudport.setCamera(hud);

		paused = false;

		maploader = new TmxMapLoader();
		map = maploader.load("level/level2.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / MyGdxGame.PPM);

		b2dr = new Box2DDebugRenderer();

		hero.b2body.setTransform(150 / MyGdxGame.PPM, 100 / MyGdxGame.PPM, 0);
		hero.b2body.setLinearVelocity(0f, 0f);
		//hero.b2body.setTransform(1.5f,1f,0);
		
		shield = new Shield(hero);

		//Anzeigerahmen fuer Leben, Score, Munition
		statBar = new Sprite(new Texture("sprites/statbar.png"));
		System.out.println(statBar.getHeight());
		//statBar.setBounds(10, Gdx.graphics.getHeight()-statBar.getHeight()*1.5f-10, statBar.getWidth()*1.5f, statBar.getHeight()*1.5f);
		statBar.setBounds(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(10)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(720-statBar.getHeight()*1.5f-10)),
				Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(statBar.getWidth()*1.5f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(statBar.getHeight()*1.5f)));

		//Lebensleiste
		healthBar = new Sprite(new Texture("sprites/health.png"));
		//healthBar.setBounds(10+5*1.5f, Gdx.graphics.getHeight()-16-healthBar.getHeight()*1.5f, healthBar.getWidth()*1.5f*hero.getHealth(), healthBar.getHeight()*1.5f);
		healthBar.setBounds(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(10+5*1.5f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(720-10-(4+healthBar.getHeight())*1.5f)),
				Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(healthBar.getWidth()*1.5f*hero.getHealth())), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(healthBar.getHeight()*1.5f)));



		Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font= new BitmapFont(Gdx.files.internal("fonts/futura.fnt"),new TextureRegion(fontTexture), false);
		font.setColor(Color.SCARLET);
		//Schrift skalieren
		if(Gdx.graphics.getWidth()*Gdx.graphics.getHeight() < MyGdxGame.V_WIDTH*MyGdxGame.V_HEIGHT){
			font.getData().setScale((Gdx.graphics.getWidth()*Gdx.graphics.getHeight())/((MyGdxGame.V_WIDTH*MyGdxGame.V_HEIGHT)/0.9f));
		}
		else{
			font.getData().setScale(0.9f);
		}
		scorePosition =  new Vector2(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(10+5*1.5f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(720-10-(20*1.5f))));
		livesPosition =  new Vector2(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(11+128*1.5f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(720-10-(4f*1.5f))));
		ammoPosition =  new Vector2(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(10+121*1.5f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(720-10-(20*1.5f))));
		timePosition =  new Vector2(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(10+741*1.5f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(720-10-(4*1.5f))));

		barwidth = (Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100*1.5f)));
		
		background = new Sprite(new Texture("level/baseBackground.png"));
		background.setBounds(0,0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		b2creator = new B2WorldCreator(world, map);

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
					if (hero.getState() != State.JUMPING && hero.getState() != State.FALLING)
						hero.b2body.applyLinearImpulse(new Vector2(0, 5.5f), hero.b2body.getWorldCenter(), true);
					return true;

				}
			});

			//Stage für HUD-Elemente
			hudStage = new Stage(hudport);
			hudStage.addActor(runningStick.touchpad);
			hudStage.addActor(aimStick.touchpad);
			hudStage.addActor(jumpButton.jumpButton);
			Gdx.input.setInputProcessor(hudStage);
		}
		
		//Pausemenue: Tabelle samt Buttons
		pauseMenu = new PauseMenu();
			
		pauseMenu.continueButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.playButtonClicked();
				paused = false;
				Gdx.input.setInputProcessor(hudStage);
				pauseMenu.disableButtons();			
				}});
	   pauseMenu.saveButton.addListener(new ChangeListener() {
		   @Override
	       public void changed(ChangeEvent event, Actor actor) {
			   game.playButtonClicked();
				XMLInteraction.saveGame(hero, "levelice1");
	       }});
	   pauseMenu.menuButton.addListener(new ChangeListener() {
		   @Override
		   public void changed(ChangeEvent event, Actor actor) {
			   pauseMenu.disableButtons();
			   game.playButtonClicked();
			   game.setScreen(new MainMenuScreen(game));}});
	        
		pauseMenu.quitButton.addListener(new ChangeListener() {
	     	@Override
	      	public void changed(ChangeEvent event, Actor actor) {
	     		game.playButtonClicked();
	          	game.dispose();
	          	System.exit(0);}});
		
		
		pauseStage = new Stage(hudport);
		pauseStage.addActor(pauseMenu.pauseTable);
		
		//world.setContactListener(new WorldContactListener());
		
		
		
		
		
		
		
		
		/*
		 * BEREICH FUER GEGNER ERSTELLUNG
		 */

		//Enemies
		enemyTowerList = new ArrayList<EnemyTower>();
		enemyTowerList.add(new EnemyTower(world,hero,(6f+0.18f)*MyGdxGame.PPM,(2.66f+0.1f)*MyGdxGame.PPM,false,game));
		enemyTowerList.add(new EnemyTower(world,hero,(8.261f+0.22f)*MyGdxGame.PPM,(2.66f+0.1f)*MyGdxGame.PPM,false,game));
		//enemyTowerList.add(new EnemyTower(world,hero,6726,529,false,game));
		 

		enemySoldierList = new ArrayList<EnemySoldier>();
		enemySoldierList.add(new EnemySoldier(world,hero,15.15f*MyGdxGame.PPM,4.64f*MyGdxGame.PPM,game));
		enemySoldierList.add(new EnemySoldier(world,hero,23f*MyGdxGame.PPM,0.325f*MyGdxGame.PPM,game));
		enemySoldierList.add(new EnemySoldier(world,hero,28f*MyGdxGame.PPM,0.32f*MyGdxGame.PPM,game));
		enemySoldierList.add(new EnemySoldier(world,hero,52.72f*MyGdxGame.PPM,3f*MyGdxGame.PPM,game));
		enemySoldierList.add(new EnemySoldier(world,hero,25.48f*MyGdxGame.PPM,3.32f*MyGdxGame.PPM,game));
		enemySoldierList.add(new EnemySoldier(world,hero,58.75f*MyGdxGame.PPM,1.66f*MyGdxGame.PPM,game));
		
		
		enemyHomingList = new ArrayList<EnemyHoming>();	
		enemyHomingList.add(new EnemyHoming(world, hero, 16f * MyGdxGame.PPM, 4f * MyGdxGame.PPM, game));
		enemyHomingList.add(new EnemyHoming(world, hero, 33f * MyGdxGame.PPM, 4f * MyGdxGame.PPM, game));
		enemyHomingList.add(new EnemyHoming(world, hero, 38f * MyGdxGame.PPM, 1.6f * MyGdxGame.PPM, game));
		enemyHomingList.add(new EnemyHoming(world, hero, 43f * MyGdxGame.PPM, 1.7f * MyGdxGame.PPM, game));
		enemyHomingList.add(new EnemyHoming(world, hero, 57f * MyGdxGame.PPM, 4f * MyGdxGame.PPM, game));
		
		remainingEnemyBulletList = new ArrayList<EnemyBullet>();
		remainingEnemySmallBulletList = new ArrayList<EnemySmallBullet>();
		
		
		
		
		/*
		 * BEREICH FUER POWERUP ERSTELLUNG
		 */
		//PickUps 
		powerUpList = new ArrayList<PickUp>();
		healthPU1 = new HealthPU(world, 20.65f*MyGdxGame.PPM, 4.64f*MyGdxGame.PPM);
		powerUpList.add(healthPU1);
		
		lifePU1 = new LifePU(world, 20.65f*MyGdxGame.PPM, 0.75f*MyGdxGame.PPM);
		powerUpList.add(lifePU1);
		
		laserPU1 = new LaserPU(world, 0.4f*MyGdxGame.PPM, 4.7f*MyGdxGame.PPM);
		powerUpList.add(laserPU1);
		
		
		/*
		 * BEREICH FUER PLATFORM ERSTELLUNG
		 */
		platformList = new ArrayList<MovingPlatform>();
		
		//platform1 = new MovingPlatform(hero, world, new Vector2(17.828794f * MyGdxGame.PPM, 1.9633348f * MyGdxGame.PPM), new Vector2(20.268723f * MyGdxGame.PPM, 1.9633348f * MyGdxGame.PPM), new Vector2(1, 0));
		platform1 = new MovingPlatform(hero, world, new Vector2(5.5f * MyGdxGame.PPM, 0.675f * MyGdxGame.PPM), new Vector2(7.5f * MyGdxGame.PPM, 0.675f * MyGdxGame.PPM), new Vector2(1, 0));
		platformList.add(platform1);
		
		platform2 = new MovingPlatform(hero, world, new Vector2(7f * MyGdxGame.PPM, 1f * MyGdxGame.PPM), new Vector2(9f * MyGdxGame.PPM, 1f * MyGdxGame.PPM), new Vector2(1, 0));
		platformList.add(platform2);
		
		platform3 = new MovingPlatform(hero, world, new Vector2(22.5f * MyGdxGame.PPM, 3f * MyGdxGame.PPM), new Vector2(24f * MyGdxGame.PPM, 3f * MyGdxGame.PPM), new Vector2(1, 0));
		platformList.add(platform3);
		
		platform4 = new MovingPlatform(hero, world, new Vector2(36f * MyGdxGame.PPM, 3.75f * MyGdxGame.PPM), new Vector2(38f * MyGdxGame.PPM, 3.75f * MyGdxGame.PPM), new Vector2(1.0f, 0));
		platformList.add(platform4);
		
		platform5 = new MovingPlatform(hero, world, new Vector2(39f * MyGdxGame.PPM, 2.5f * MyGdxGame.PPM), new Vector2(41f * MyGdxGame.PPM, 2.5f * MyGdxGame.PPM), new Vector2(1.5f, 0));
		platformList.add(platform5);
		
		platform6 = new MovingPlatform(hero, world, new Vector2(42f * MyGdxGame.PPM, 3f * MyGdxGame.PPM), new Vector2(45.5f * MyGdxGame.PPM, 3f * MyGdxGame.PPM), new Vector2(1.25f, 0));
		platformList.add(platform6);
		
		initCheckpoints();

		//GameOver
		gameOverStage= new Stage(hudport);
		gameOverMenu = new GameOverMenu();
		gameOverMenu.retry.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.playButtonClicked();
				gameOverMenu.disable();
				restart();			}});
		gameOverMenu.mainMenu.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gameOverMenu.disable();
				game.playButtonClicked();
				game.setScreen(new MainMenuScreen(game));
			}});
		gameOverStage.addActor(gameOverMenu.mainMenu);
		gameOverStage.addActor(gameOverMenu.retry);
		gameOver=false;
		labelX = Gdx.graphics.getWidth()/2-gameOverMenu.gameOverLabel.getWidth()/2;
		buttonX = -50;

		//Level complete
		levelCompleteStage= new Stage(hudport);
		levelCompletedMenu= new LevelCompletedMenu();
		levelCompletedMenu.continueButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				game.playButtonClicked();
				levelCompletedMenu.disable();
				game.setScreen(new MainMenuScreen(game));
			}});
		levelCompleteStage.addActor(levelCompletedMenu.continueButton);
		levelCompleted=false;



	}
	
	/**
	 * Welt samt Positionen von Objekten (fuer das zeichnen von Sprites) aktualisieren und Kamera ausrichten
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void update(float delta){

		handleInput(delta);
		handleCheckpoints();
		
		if(!paused){
			levelTime += delta;
			world.step(delta, 1, 1);
			if(hero.b2body.getPosition().y<-2.5 || hero.getHealth() == 0)
			{
				if(!gameOver){
					game.playDeath();
					hero.setDamageEffect(1);
				}
				respawn();	
			}
			hero.update(delta);
			shield.update(delta, hero);


			for(int i = 0; i < powerUpList.size(); i++){
				powerUpList.get(i).update();
			}
			for(int i = 0; i < enemyTowerList.size(); i++){
				enemyTowerList.get(i).update(delta);
			}
			for(int i = 0; i < enemySoldierList.size(); i++){
				enemySoldierList.get(i).update(delta);
			}
			for(int i = 0; i < enemyHomingList.size(); i++){
				enemyHomingList.get(i).update(delta);
			}
			for(int i = 0; i < platformList.size(); i++){
				platformList.get(i).update(delta);
			}
			
			
		}		
		
		if(hero.getHealth()>0){
			healthBar.setSize(barwidth*((float)hero.getHealth()/100), healthBar.getHeight());
			//healthBar.setSize(1.5f*hero.getHealth(), healthBar.getHeight());
		}
		else{
			healthBar.setSize(0, healthBar.getHeight());
		}
		
		if(hero.getBody().getPosition().x * MyGdxGame.PPM > MyGdxGame.V_WIDTH / 2)
		gamecam.position.x = hero.getBody().getPosition().x;
		
		//PowerUp aus der Liste entfernen, wenn es aufgesammelt wurde und den zugehoerigen Body zerstoeren
		for(int i = 0; i < powerUpList.size(); i++){
			if(powerUpList.get(i).isCollected() && powerUpList.get(i).getBody().getFixtureList().size != 0)
			{
				powerUpList.get(i).getBody().destroyFixture(powerUpList.get(i).getBody().getFixtureList().get(0));
				powerUpList.remove(i);
			}
		}
		
		//Projektile (EnemyBullet) von bereits besiegten Gegnern manuell updaten (passiert sonst in jeweiliger Gegner-Update-Methode
		for(int i = 0; i < remainingEnemyBulletList.size(); i++){
			remainingEnemyBulletList.get(i).update(delta);
			//Wenn Objekt getroffen Fixture loeschen und aus Liste entfernen
			if(remainingEnemyBulletList.get(i).isHitObject()){
				remainingEnemyBulletList.get(i).getBody().destroyFixture(remainingEnemyBulletList.get(i).getBody().getFixtureList().get(0));
				remainingEnemyBulletList.remove(i);
			}
		}
		
		//Projektile (EnemySmallBullet) von bereits besiegten Gegnern manuell updaten (passiert sonst in jeweiliger Gegner-Update-Methode
		for(int i = 0; i < remainingEnemySmallBulletList.size(); i++){
			remainingEnemySmallBulletList.get(i).update(delta);
			//Wenn Objekt getroffen aus Liste entfernen
			if(remainingEnemySmallBulletList.get(i).isHitObject()){
				remainingEnemySmallBulletList.get(i).getBody().destroyFixture(remainingEnemySmallBulletList.get(i).getBody().getFixtureList().get(0));
				remainingEnemySmallBulletList.remove(i);
			}
		}
		//Homing-Gegner entfernen wenn getoetet
		for(int i = 0; i < enemyHomingList.size(); i++){
			if(enemyHomingList.get(i).getLife() <= 0 && enemyHomingList.get(i).getBody().getFixtureList().size != 0)
			{
				hero.setScore(hero.getScore()+enemyHomingList.get(i).getPoints());
				enemyHomingList.get(i).getBody().destroyFixture(enemyHomingList.get(i).getBody().getFixtureList().get(0));
				enemyHomingList.remove(i);
			}
		}

		//Soldaten-Gegner, die besiegt wurden aus der Liste entfernen und den zugehoerigen Body zerstoeren
		for(int i = 0; i < enemySoldierList.size(); i++){
			if(enemySoldierList.get(i).getLife() <= 0 && enemySoldierList.get(i).getBody().getFixtureList().size != 0)
			{
				hero.setScore(hero.getScore()+enemySoldierList.get(i).getPoints());
				enemySoldierList.get(i).getBody().destroyFixture(enemySoldierList.get(i).getBody().getFixtureList().get(0));

				//Verbleibende Projektile in Liste sammeln
				for(int j = 0; j < enemySoldierList.get(i).getBulletList().size(); j++){
					if(enemySoldierList.get(i).getBulletList().get(j).isHitObject()){
						enemySoldierList.get(i).getBulletList().get(j).getBody().destroyFixture(enemySoldierList.get(i).getBulletList().get(j).getBody().getFixtureList().get(0));
					}
					else{
						remainingEnemyBulletList.add(enemySoldierList.get(i).getBulletList().get(j));
					}
					
				}
				
				enemySoldierList.remove(i);
			}
		}
	
		//Tower-Gegner, die besiegt wurden aus der Liste entfernen und den zugehoerigen Body zerstoeren
		for(int i = 0; i < enemyTowerList.size(); i++){
			if(enemyTowerList.get(i).getLife() <= 0 && enemyTowerList.get(i).getBody().getFixtureList().size != 0)
			{
				hero.setScore(hero.getScore()+enemyTowerList.get(i).getPoints());
				enemyTowerList.get(i).getBody().destroyFixture(enemyTowerList.get(i).getBody().getFixtureList().get(0));

				//Verbleibende Projektile in Liste sammeln
				for(int j = 0; j < enemyTowerList.get(i).getBulletList().size(); j++){
					if(enemyTowerList.get(i).getBulletList().get(j).isHitObject()){
						enemyTowerList.get(i).getBulletList().get(j).getBody().destroyFixture(enemyTowerList.get(i).getBulletList().get(j).getBody().getFixtureList().get(0));
					}
					else{
						remainingEnemySmallBulletList.add(enemyTowerList.get(i).getBulletList().get(j));
					}
					
				}
				
				enemyTowerList.remove(i);
			}
		}
		
		
		gamecam.update();
		renderer.setView(gamecam);
	}
	
	/**
	 * Nutzereingaben interpretieren -> Realisiert Steuerung des Helden
	 * @param delta Deltatime (Zeit in Sekunden seit dem letzten render Aufruf)
	 */
	public void handleInput(float delta) {
		if (!gameOver&&!levelCompleted){
			if (Application.ApplicationType.Desktop == Gdx.app.getType()) {

				if (!paused) {

					//Sprung
					if ((Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && hero.getState() != State.JUMPING && hero.getState() != State.FALLING)
						hero.getBody().applyLinearImpulse(new Vector2(0, 5.5f), hero.getBody().getWorldCenter(), true);

					//Nach rechts laufen
					if (Gdx.input.isKeyPressed(Input.Keys.D) && hero.getBody().getLinearVelocity().x <= 3) {
						hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), hero.getBody().getWorldCenter(), true);

						if (hero.getBody().getLinearVelocity().x < 0) {
							//hero.getBody().setLinearVelocity(0, hero.getBody().getLinearVelocity().y);
							hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), hero.getBody().getWorldCenter(), true);
						}
					}

					//Nach links laufen
					if (Gdx.input.isKeyPressed(Input.Keys.A) && hero.getBody().getLinearVelocity().x >= -3) {
						hero.getBody().applyLinearImpulse(new Vector2(-0.15f, 0), hero.getBody().getWorldCenter(), true);

						if (hero.getBody().getLinearVelocity().x > 0) {
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
					if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
						System.out.println("X: " + hero.b2body.getPosition().x*150 + " Y: " + hero.b2body.getPosition().y*150);
					}
				}
				
				//Pausieren
				if(Gdx.input.isKeyJustPressed(Keys.ESCAPE)){

					if (paused) {
						Gdx.input.setInputProcessor(hudStage);
						pauseMenu.disableButtons();
					} else {
						Gdx.input.setInputProcessor(pauseStage);
						pauseMenu.enableButtons();
					}

					paused = !paused;


				}
			} else {
				//Android Steuerung
				//Pausieren
				if(Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.MENU)){

					if (paused) {
						Gdx.input.setInputProcessor(hudStage);
						pauseMenu.disableButtons();
					} else {
						Gdx.input.setInputProcessor(pauseStage);
						pauseMenu.enableButtons();
					}

					paused = !paused;
				}
				//Hero Steuerung
				if (!paused) {
					
					if (runningStick.touchpad.isTouched()) {
						if (runningStick.touchpad.getKnobPercentX() > 0.3 && hero.b2body.getLinearVelocity().x <= 3) {
							hero.b2body.applyLinearImpulse(new Vector2(0.15f, 0), hero.b2body.getWorldCenter(), true);
						} else if (runningStick.touchpad.getKnobPercentX() < -0.3 && hero.b2body.getLinearVelocity().x >= -3) {
							hero.b2body.applyLinearImpulse(new Vector2(-0.15f, 0), hero.b2body.getWorldCenter(), true);
						}
					}
					if (aimStick.touchpad.isTouched() && (Math.abs(aimStick.touchpad.getKnobPercentX()) > 0.2 || Math.abs(aimStick.touchpad.getKnobPercentY()) > 0.2)) {
						hero.shoot((aimStick.touchpad.getKnobPercentX() > 0) ? (100f - Math.abs(aimStick.touchpad.getKnobPercentY() * 100)) : -(100f - Math.abs(aimStick.touchpad.getKnobPercentY() * 100)), ((aimStick.touchpad.getKnobPercentY() > 0) ? Math.abs(aimStick.touchpad.getKnobPercentY()) * 30 : Math.abs(aimStick.touchpad.getKnobPercentY()) * -30), delta);
					}
				}
			}
	}
		else{
			if(gameOver) {
				if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
					gameOverMenu.retry.setY(Gdx.graphics.getHeight() / 2 - 75);
					gameOverMenu.setLabelY(Gdx.graphics.getHeight() / 2 + 75);
				}
				Gdx.input.setInputProcessor(gameOverStage);
			}
			else
			{
				if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
				{
					levelCompletedMenu.continueButton.setX(Gdx.graphics.getWidth()/2-levelCompletedMenu.continueButton.getWidth()/2);
					levelCompletedMenu.setLabelX(Gdx.graphics.getWidth()/2-levelCompletedMenu.levelCompleted.getWidth()/2);
				}
				Gdx.input.setInputProcessor(levelCompleteStage);
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


		
		//Checkpoints zeichnen
		for(int i=0;i<checkpoints.length;i++)
		{
			checkpoints[i].draw(batch);
		}
		//Alle Platformen malen
		for(int i = 0; i < platformList.size(); i++){
			platformList.get(i).draw(batch);
		}
		
		//Alle PowerUps zeichnen
		for(int i = 0; i < powerUpList.size(); i++){
			((Sprite)powerUpList.get(i)).draw(batch);
		}
		//Alle Gegner + Projektile malen - Tower
		for(int i = 0; i < enemyTowerList.size(); i++){
			enemyTowerList.get(i).draw(batch);

			for(int j = 0; j < enemyTowerList.get(i).getBulletList().size(); j++){
				if(!enemyTowerList.get(i).getBulletList().get(j).isHitObject()){
					enemyTowerList.get(i).getBulletList().get(j).draw(batch);
				}
			}
		}
		
		//Alle Gegner malen - Drohnen
		for(int i = 0; i < enemyHomingList.size(); i++){
			enemyHomingList.get(i).draw(batch);
		}
		
		//Alle Gegner + Projektile malen - Soldaten
		for(int i = 0; i < enemySoldierList.size(); i++){
			enemySoldierList.get(i).draw(batch);

			for(int j = 0; j < enemySoldierList.get(i).getBulletList().size(); j++){
				if(!enemySoldierList.get(i).getBulletList().get(j).isHitObject()){
					enemySoldierList.get(i).getBulletList().get(j).draw(batch);
				}
			}
		}
		
		//Projektile (EnemyBullet) von bereits besiegten Gegnern malen
		for(int i = 0; i < remainingEnemyBulletList.size(); i++){
			if(!remainingEnemyBulletList.get(i).isHitObject()){
				remainingEnemyBulletList.get(i).draw(batch);
			}
		}
		
		//Projektile (EnemySmallBullet) von bereits besiegten Gegnern malen
		for(int i = 0; i < remainingEnemySmallBulletList.size(); i++){
			if(!remainingEnemySmallBulletList.get(i).isHitObject()){
				remainingEnemySmallBulletList.get(i).draw(batch);
			}
		}

		//Respawn "Animation"
		if(respawnTimer<10)
		{
			hero.draw(batch);
			respawnTimer++;
		}
		else if(respawnTimer<20)
		{
			respawnTimer++;
		}
		else if(respawnTimer<30)
		{
			hero.draw(batch);
			respawnTimer++;
		}
		else if(respawnTimer<40)
		{
			respawnTimer++;
		}
		else if(respawnTimer<50)
		{
			hero.draw(batch);
			respawnTimer++;
		}
		else if(respawnTimer<60) {
			respawnTimer++;
		}
		else if(respawnTimer==100)
		{
			//Falls erschossen, nicht zeichnen
		}
		else
		{
			hero.draw(batch);
		}
		
		if(shield.isActive())
		{
			shield.draw(batch);
		}	
		
		//Alle Projektile malen, die noch nichts getroffen haben
		for(int i = 0; i < hero.getBulletList().size(); i++){
			if(!hero.getBulletList().get(i).hitObject)
			{
				hero.getBulletList().get(i).draw(batch);
			}
		}

		for(int i = 0; i < hero.getLaserBulletList().size(); i++){
			if(!hero.getLaserBulletList().get(i).hitObject)
			{
				hero.getLaserBulletList().get(i).draw(batch);
			}
		}


		batch.end();
		
		batch.setProjectionMatrix(hud.combined);
		batch.begin();
		//Anzeige fuer Leben, Score, Munition
		statBar.draw(batch);
		//Lebensleiste
		healthBar.draw(batch);
		//Beschriftung Anzeige
		font.draw(batch, getTimeFormatted(), timePosition.x, timePosition.y);
		font.draw(batch, "Score: " + hero.getScore(), scorePosition.x, scorePosition.y);
		font.draw(batch, Integer.toString(hero.getLives()), livesPosition.x, livesPosition.y);
		font.draw(batch, Integer.toString(hero.getCurrentAmmo()), ammoPosition.x, ammoPosition.y);
		batch.end();
		
		//Android-Steuerelemente 
		if(Application.ApplicationType.Android==Gdx.app.getType()) {
			hudStage.act(Gdx.graphics.getDeltaTime());
			hudStage.draw();
		}

		
		if(paused||gameOver||levelCompleted){
			//Hintergrund dunkler
        	Gdx.gl.glEnable(GL20.GL_BLEND);
        	Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        	shape.setProjectionMatrix(gamecam.combined);
        	shape.begin(ShapeType.Filled);
        	shape.setColor(new Color(0, 0, 0, 0.5f));
        	shape.rect(0, 0, MyGdxGame.V_WIDTH, MyGdxGame.V_HEIGHT);
        	shape.end();
        	Gdx.gl.glDisable(GL20.GL_BLEND);
        	//Menu
			if(paused) {
				pauseStage.act(Gdx.graphics.getDeltaTime());
				pauseStage.draw();
			}
			else if(gameOver)
			{
				if(gameOverMenu.getLabelY()>Gdx.graphics.getHeight()/2+75)
				{
					gameOverMenu.setLabelY(gameOverMenu.getLabelY()-3f);
				}
				batch.begin();
				batch.draw(gameOverMenu.gameOverLabel,labelX,gameOverMenu.getLabelY());
				batch.end();
				if(gameOverMenu.retry.getY()<Gdx.graphics.getHeight()/2-75)
				{
					gameOverMenu.retry.setY(gameOverMenu.retry.getY()+3f);
				}
				if(gameOverMenu.mainMenu.getY()<Gdx.graphics.getHeight()/2-150)
				{
					gameOverMenu.mainMenu.setY(gameOverMenu.mainMenu.getY()+3f);
				}
				gameOverStage.act(Gdx.graphics.getDeltaTime());
				gameOverStage.draw();
			}
			else
			{
				if(levelCompletedMenu.getLabelX()<Gdx.graphics.getWidth()/2-levelCompletedMenu.getLevelCompleted().getWidth()/2)
				{
					levelCompletedMenu.setLabelX(levelCompletedMenu.getLabelX()+6f);
				}
				batch.begin();
				batch.draw(levelCompletedMenu.getLevelCompleted(),levelCompletedMenu.getLabelX(),Gdx.graphics.getHeight()/2+75);
				batch.end();
				if(levelCompletedMenu.continueButton.getX()>Gdx.graphics.getWidth()/2-levelCompletedMenu.continueButton.getWidth()/2)
				{
					levelCompletedMenu.continueButton.setX(levelCompletedMenu.continueButton.getX()-6f);
				}
				levelCompleteStage.act(delta);
				levelCompleteStage.draw();
				stageCompleted();

			}
		}


		Gdx.graphics.setTitle("Delta Commander | " + Gdx.graphics.getFramesPerSecond() + " FPS");
	}

	@Override
	public void resize(int width, int height) {
		gameport.update(width, height);
		hudport.update(width, height);
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
		shape.dispose();
		batch.dispose();
		gameOverStage.dispose();
		levelCompleteStage.dispose();

	}
	
	
	
	/**
	 * ContactListener, der alle Kollisionen zwischen Fixtures in der Box2D-Welt interpretiert
	 */
  	public class WorldContactListener implements ContactListener {

  		@Override
  		public void beginContact(Contact contact) {
  			Fixture fixA = contact.getFixtureA();
  			Fixture fixB = contact.getFixtureB();
  			
  			//Kollisionen von Hero-Fixture (Spielercharakter) mit anderen Objekten
  			if(fixA.getUserData() == "hero" || fixB.getUserData() == "hero"){
  				Fixture heroFix = fixA.getUserData() == "hero" ? fixA : fixB;
  				Fixture object = heroFix == fixA ? fixB : fixA;
  				
  				//Kollision mit MovingPlatform
  				if(object.getUserData() instanceof MovingPlatform){
  					((MovingPlatform) object.getUserData()).onHit();
  				}
  				//Kollision mit Health-PowerUp
  				else if(object.getUserData() == "health"){
  					game.playPowerUp();
  					hero.setHealth(hero.getHealth()+25);
  					((HealthPU)object.getBody().getUserData()).setCollected(true);
  				}
  				//Kollision mit Life-PowerUp
  				else if(object.getUserData() == "life"){
  					game.playPowerUp();
  					hero.setLives(hero.getLives()+1);
  					((LifePU)object.getBody().getUserData()).setCollected(true);
  				}
  				//Kollision mit Shield-PowerUp
  				else if(object.getUserData() == "shield"){
  					game.playPowerUp();
  					shield.activate();
  					((ShieldPU)object.getBody().getUserData()).setCollected(true);  			
  				}
  				//Kollision mit Tripleshot-PowerUp
  				else if(object.getUserData() == "triple"){
  					game.playPowerUp();
  					hero.setTripleAmmo(hero.getTripleAmmo() + 15);
  					((TripleshotPU)object.getBody().getUserData()).setCollected(true);
  				}
  				//Kollision mit Laser-PowerUp
  				else if(object.getUserData() == "laser"){
  					game.playPowerUp();
  					hero.setLaserAmmo(hero.getLaserAmmo() + 30);
  					((LaserPU)object.getBody().getUserData()).setCollected(true);
  				}

  			}
  			
  			//Kollision von Bullet-Fixture (Projektil, das von Hero geschossen wird) mit anderen Objekten
  			if(fixA.getUserData() == "bullet" || fixB.getUserData() == "bullet" && fixA.getUserData() != fixB.getUserData())
  			{
  				Fixture bullet = fixA.getUserData() == "bullet" ? fixA : fixB;
  				Fixture object = bullet == fixA ? fixB : fixA;
  			 	
  				if((!((Bullet)bullet.getBody().getUserData()).isHitObject()) && object.getUserData() != "hero" && object.getUserData() != null)
  				{
  					//Kollision mit EnemySoldier-Gegner
  					if(object.getUserData() == "enemysoldier")
  					{
  						((EnemySoldier)object.getBody().getUserData()).setLife(((EnemySoldier)object.getBody().getUserData()).getLife()-((Bullet)bullet.getBody().getUserData()).getDamage());
  						((Bullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision mit EnemyTower-Gegner
  					else if(object.getUserData() == "enemytower")
  					{
  						((EnemyTower)object.getBody().getUserData()).setLife(((EnemyTower)object.getBody().getUserData()).getLife()-((Bullet)bullet.getBody().getUserData()).getDamage());
  						((Bullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision mit EnemyHoming-Gegner
  					else if(object.getUserData() == "enemyhoming")
  					{
  						((EnemyHoming)object.getBody().getUserData()).damage(((Bullet)bullet.getBody().getUserData()).getDamage());
  						((Bullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision anderem Objekt, das keine Projektile durchlassen soll
  					else if(object.getUserData() == "map" || object.getUserData() instanceof MovingPlatform)
  					{
  						((Bullet)bullet.getBody().getUserData()).setHitObject(true);
  					}

  					//Andere Bullets, JoinHero nicht betrachtet	
  				}	
  			}
  			
  			//Kollision von LaserBullet-Fixture (Projektil, das von Hero geschossen wird) mit anderen Objekten
  			if(fixA.getUserData() == "laserbullet" || fixB.getUserData() == "laserbullet" && fixA.getUserData() != fixB.getUserData())
  			{
  				Fixture bullet = fixA.getUserData() == "laserbullet" ? fixA : fixB;
  				Fixture object = bullet == fixA ? fixB : fixA;
  			 	
  				if((!((Laserbullet)bullet.getBody().getUserData()).isHitObject()) && object.getUserData() != "hero" && object.getUserData() != null)
  				{
  					//Kollision mit EnemySoldier-Gegner
  					if(object.getUserData() == "enemysoldier")
  					{
  						((EnemySoldier)object.getBody().getUserData()).setLife(((EnemySoldier)object.getBody().getUserData()).getLife()-((Laserbullet)bullet.getBody().getUserData()).getDamage());
  						((Laserbullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision mit EnemyTower-Gegner
  					else if(object.getUserData() == "enemytower")
  					{
  						((EnemyTower)object.getBody().getUserData()).setLife(((EnemyTower)object.getBody().getUserData()).getLife()-((Laserbullet)bullet.getBody().getUserData()).getDamage());
  						((Laserbullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision mit EnemyHoming-Gegner
  					else if(object.getUserData() == "enemyhoming")
  					{
  						((EnemyHoming)object.getBody().getUserData()).damage(((Laserbullet)bullet.getBody().getUserData()).getDamage());
  						((Laserbullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision anderem Objekt, das keine Projektile durchlassen soll
  					else if(object.getUserData() == "map" || object.getUserData() instanceof MovingPlatform)
  					{
  						((Laserbullet)bullet.getBody().getUserData()).setHitObject(true);
  					}			
  					
  				}	
  			}
  			
  			//Kollision von EnemyBullet-Fixture (Projektil, das von EnemySoldier geschossen wird) mit anderen Objekten
  			if(fixA.getUserData() == "enemybullet" || fixB.getUserData() == "enemybullet" && fixA.getUserData() != fixB.getUserData())
  			{
  				Fixture bullet = fixA.getUserData() == "enemybullet" ? fixA : fixB;
  				Fixture object = bullet == fixA ? fixB : fixA;
  			 	
  				if((!((EnemyBullet)bullet.getBody().getUserData()).isHitObject()) && object.getUserData() != "enemysoldier" && object.getUserData() != null)
  				{
  					//Kollision mit Hero
  					if(object.getUserData() == "hero")
  					{
  						//Leben nur abziehen, wenn kein Schild
  						if(!shield.isActive() && !hero.isInvulnerable())
  						{
  							hero.setHealth(hero.getHealth()-((EnemyBullet)bullet.getBody().getUserData()).getDamage());
  							hero.setDamageEffect(0.25f);
  						}
  						((EnemyBullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision anderem Objekt, das keine Projektile durchlassen soll
  					else if(object.getUserData() == "map" || object.getUserData() instanceof MovingPlatform)
  					{
  						((EnemyBullet)bullet.getBody().getUserData()).setHitObject(true);
  					}			
  					
  					//Andere Bullets, JoinHero nicht betrachtet	
  				}	
  			}
  			
  			//Kollision von EnemySmallBullet-Fixture (Projektil, das von EnemyTower geschossen wird) mit anderen Objekten
  			if(fixA.getUserData() == "enemysmallbullet" || fixB.getUserData() == "enemysmallbullet" && fixA.getUserData() != fixB.getUserData())
  			{
  				Fixture bullet = fixA.getUserData() == "enemysmallbullet" ? fixA : fixB;
  				Fixture object = bullet == fixA ? fixB : fixA;
  			 	
  				if((!((EnemySmallBullet)bullet.getBody().getUserData()).isHitObject()) && object.getUserData() != "enemytower" && object.getUserData() != null)
  				{
  					//Kollision mit Hero
  					if(object.getUserData() == "hero")
  					{
  						//Leben nur abziehen, wenn kein Schild
  						if(!shield.isActive() && !hero.isInvulnerable())
  						{
  							hero.setHealth(hero.getHealth()-((EnemySmallBullet)bullet.getBody().getUserData()).getDamage());
  							hero.setDamageEffect(0.25f);
  						}
  						((EnemySmallBullet)bullet.getBody().getUserData()).setHitObject(true);
  					}
  					//Kollision anderem Objekt, das keine Projektile durchlassen soll
  					else if(object.getUserData() == "map" || object.getUserData() instanceof MovingPlatform)
  					{
  						((EnemySmallBullet)bullet.getBody().getUserData()).setHitObject(true);
  					}			
  					
  					//Andere Bullets, JoinHero nicht betrachtet	
  				}	
  			}
			//Kollision von bossBullet-Fixture (Projektil, das von Boss geschossen wird) mit anderen Objekten
			if(fixA.getUserData() == "bossBullet" || fixB.getUserData() == "bossBullet" && fixA.getUserData() != fixB.getUserData())
			{
				Fixture bullet = fixA.getUserData() == "bossBullet" ? fixA : fixB;
				Fixture object = bullet == fixA ? fixB : fixA;

				if((!((BossBullet)bullet.getBody().getUserData()).isHitObject()) && object.getUserData() != "boss" && object.getUserData() != null)
				{
					//Kollision mit Hero
					if(object.getUserData() == "hero")
					{
						//Leben nur abziehen, wenn kein Schild
						if(!shield.isActive() && !hero.isInvulnerable())
						{
							hero.setHealth(hero.getHealth()-((BossBullet)bullet.getBody().getUserData()).getDamage());
							hero.setDamageEffect(0.25f);
						}
						((BossBullet)bullet.getBody().getUserData()).setHitObject(true);
					}
					//Kollision anderem Objekt, das keine Projektile durchlassen soll
					else if(object.getUserData() == "map" || object.getUserData() instanceof MovingPlatform)
					{
						((BossBullet)bullet.getBody().getUserData()).setHitObject(true);
					}

					//Andere Bullets, JoinHero nicht betrachtet
				}
			}
			//Kollision von Bullet-Fixture (Projektil, das von Join-Hero geschossen wird) mit anderen Objekten
			if(fixA.getUserData() == "joinherobullet" || fixB.getUserData() == "joinherobullet" && fixA.getUserData() != fixB.getUserData())
			{
				Fixture bullet = fixA.getUserData() == "joinherobullet" ? fixA : fixB;
				Fixture object = bullet == fixA ? fixB : fixA;
				if(object.getUserData() != null&&(!((JoinHeroBullet)bullet.getBody().getUserData()).isHitObject()) && object.getUserData() != "joinhero" )
				{
					//Kollision mit EnemySoldier-Gegner
					if(object.getUserData() == "enemysoldier")
					{
						((EnemySoldier)object.getBody().getUserData()).setLife(((EnemySoldier)object.getBody().getUserData()).getLife()-((JoinHeroBullet)bullet.getBody().getUserData()).getDamage());
						((JoinHeroBullet)bullet.getBody().getUserData()).setHitObject(true);
					}
					//Kollision mit EnemyTower-Gegner
					else if(object.getUserData() == "enemytower")
					{
						((EnemyTower)object.getBody().getUserData()).setLife(((EnemyTower)object.getBody().getUserData()).getLife()-((JoinHeroBullet)bullet.getBody().getUserData()).getDamage());
						((JoinHeroBullet)bullet.getBody().getUserData()).setHitObject(true);
					}
					//Kollision mit EnemyHoming-Gegner
					else if(object.getUserData() == "enemyhoming")
					{
						((EnemyHoming)object.getBody().getUserData()).damage(((JoinHeroBullet)bullet.getBody().getUserData()).getDamage());
						((JoinHeroBullet)bullet.getBody().getUserData()).setHitObject(true);
					}
					//Kollision mit Boss Gegner
					else if(object.getUserData() == "boss")
					{
						((Boss)object.getBody().getUserData()).setLife(((Boss)object.getBody().getUserData()).getLife()-((JoinHeroBullet)bullet.getBody().getUserData()).getDamage());
						((JoinHeroBullet)bullet.getBody().getUserData()).setHitObject(true);

					}
					//Kollision anderem Objekt, das keine Projektile durchlassen soll
					else if(object.getUserData() == "map" || object.getUserData() instanceof MovingPlatform)
					{
						((JoinHeroBullet)bullet.getBody().getUserData()).setHitObject(true);
					}

				}
			}
  			
  			
  		}

  		@Override
  		public void endContact(Contact contact) {
  			Fixture fixA = contact.getFixtureA();
  			Fixture fixB = contact.getFixtureB();
  			
  			if(fixA.getUserData() == "hero" || fixB.getUserData() == "hero"){
  				Fixture heroFix = fixA.getUserData() == "hero" ? fixA : fixB;
  				Fixture object = heroFix == fixA ? fixB : fixA;
  				
  				if(object.getUserData() instanceof MovingPlatform){
  					((MovingPlatform) object.getUserData()).onEnd();
  				}
  			}
  		}
  		@Override
  		public void preSolve(Contact contact, Manifold oldManifold) {}
  		@Override
  		public void postSolve(Contact contact, ContactImpulse impulse) {}
  	}
  	
  	/**
  	 * Hero am letzten erreichten Checkpoint respawnen, wenn er gestorben ist. <br>
  	 * Lebenspunkte wiederherstellen und Anzahl der Versuche um 1 verringern. <br>
  	 * Alte Projektile entfernen.
  	 */
	public void respawn()
	{
		hero.setLives(hero.getLives()-1);
		hero.setHealth(100);
		if(hero.lives==0 )
		{
			if(!gameOver) {
				hero.b2body.setLinearVelocity(0, 0);
				gameOver = true;
				game.playGameOver();
				gameOverMenu.enable();
			}
		}
		else {
			hero.b2body.setLinearVelocity(0,0);
			hero.b2body.setTransform(checkpoints[checkpointCounter].getX(), checkpoints[checkpointCounter].getY(), 0);
			System.out.println("Respawn");
			for (EnemySoldier e : enemySoldierList) {

				for (EnemyBullet b : e.getBulletList()) {
					world.destroyBody(b.getBody());
					b.getBody().setUserData(null);
					b.setBody(null);
				}
				e.setBulletList(new ArrayList<EnemyBullet>());

			}
			for (Bullet b : hero.getBulletList()) {
				world.destroyBody(b.getBody());
				b.getBody().setUserData(null);
				b.setBody(null);
			}
			hero.setBulletList(new ArrayList<Bullet>());
			for (EnemyTower e : enemyTowerList) {
				for (EnemySmallBullet b : e.getBulletList()) {
					world.destroyBody(b.getBody());
					b.getBody().setUserData(null);
					b.setBody(null);
				}
				e.setBulletList(new ArrayList<EnemySmallBullet>());
			}


			shield.deactivate();
				if (hero.getBody().getPosition().x * MyGdxGame.PPM > MyGdxGame.V_WIDTH / 2)
					gamecam.position.x = hero.getBody().getPosition().x;
				else {
					gamecam.position.set(gameport.getWorldWidth() / 2, gameport.getWorldHeight() / 2, 0);
				}

			respawnTimer = 0;
		}
	}
	/**
	 * Checkpoints initialisieren: Position setzen und ersten aktivieren.
	 */
	public void initCheckpoints()
	{
		//Checkpoints initialisieren 
		checkpoints[0]= new Checkpoint(0.5f, 1 - hero.getHeight());
		checkpoints[1]= new Checkpoint(10.32f,2f);
		checkpoints[2]= new Checkpoint(34f,3.67f);
		checkpoints[3]= new Checkpoint(48.34f,3.67f);
		checkpoints[4]= new Checkpoint(61.66f,1.98f);
		checkpoints[0].switchOn();
		nextCheckpoint= checkpoints[1].getPosX();
		checkpointCounter=0;
		respawnTimer =80;

	}
	/**
	 * Checkpoints verwalten: Aktivieren, wenn Hero einen Checkpoint erreicht
	 */
	public void handleCheckpoints()
	{
		if(checkpointCounter!=checkpoints.length-1) {
			if (hero.b2body.getPosition().x > nextCheckpoint) {
				checkpointCounter++;
				checkpoints[checkpointCounter].switchOn();
				game.playCheckpoint();
				if (checkpointCounter < checkpoints.length - 1) {
					nextCheckpoint = checkpoints[checkpointCounter + 1].posX;
				}
			}

		}
		else if(levelCompleted == false)
		{
			hero.b2body.setLinearVelocity(0,0);
			levelCompleted=true;
			game.playLevelUp();
		}

	}
	/**
	 * Levelzeit formatieren
	 * @return formatierte Levelzeit
	 */
	private String getTimeFormatted(){
		String minutes = "00";
		String seconds = "00";
		if(levelTime > 600){
			minutes = Integer.toString((int) (levelTime/60));
		} else if (levelTime > 60){
			minutes = "0" + Integer.toString((int) (levelTime/60));
		}
		if((levelTime%60) > 10){
			seconds = Integer.toString((int) (levelTime%60));
		} else {
			seconds = "0" + Integer.toString((int) (levelTime%60));
		}
		return minutes + ":" + seconds;
	}
	/**
	 *	Score berechnen und speichern -> Beim Beenden des Levels aufrufen. <br>
	 *	Gegebenenfalls ins naechste Level wechseln.
	 */
	public void stageCompleted(){
		if(levelTime < timeToBeat){
			hero.setScore((int) (hero.getScore() + timeFactor * (timeToBeat - levelTime)));
		}
		XMLInteraction.saveScore("level2", game.getUsername(), hero.getScore());
		hero.updateSavedScore();
		game.setScreen(new Level3(game, hero.getHealth(), hero.getLives(), hero.getAmmo(), hero.getScore(), hero.getLaserAmmo(), hero.getTripleAmmo()));
	}
	/**
	 * seperate Methode macht das erneute Starten im Multiplayer einfacher
	 */
	public void restart()
	{
		game.setScreen(new Level2(game));
	}

}