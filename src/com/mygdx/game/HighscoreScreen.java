package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import com.mygdx.game.B2WorldCreator;

import com.mygdx.game.MyGdxGame;

import hudElements.ArrowButton;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.regex.Pattern;


/**
 * Created by fabian on 20.06.16.
 */
public class HighscoreScreen implements Screen {
	TextureAtlas atlas;
    MyGdxGame game;
    OrthographicCamera gamecam;
    OrthographicCamera hud;
    Viewport gameport;
    Viewport hudport;
    SpriteBatch batch;
    ShapeRenderer shape;
    Stage hudStage;

    //objekte um die map zu laden, welche mit TILED erstellt wurde
    TmxMapLoader maploader;
    TiledMap map;
    OrthogonalTiledMapRenderer renderer;

    //Box2d Kram
    World world;
    Box2DDebugRenderer b2dr;
    B2WorldCreator b2creator;

    Texture button;
    Texture logo;
    BitmapFont font;

    //Liste
    ScrollPane scrollPane, namePane, scorePane;
    List<String> names, scores;
    Skin skin;
    ShapeRenderer shapeRenderer;
    TextButton back;

    ArrowButton rightArrow;
    ArrowButton leftArrow;
    ArrowButton leftLevelArrow;
    ArrowButton rightLevelArrow;

    Texture baseTitle;
    Texture antTitle;
    Texture sciTitle;

    //Hashmap für Level
    HashMap<String,String[]> levelHashMap= new HashMap<String, String[]>();
    
    Label levelName;
    String currentWorld;
    int levelCounter;
    

    public HighscoreScreen(MyGdxGame game) {
        this.game = game;
        //TODO: Neue Welten und Level hier eintragen, zusätzlichen unten Buttons aktualisieren
        levelHashMap.put("Base",new String[]{"Level 1", "Level 2", "Level 3"});
        levelHashMap.put("Antarctica",new String[]{"Level 1","Level 2", "Level 3"});
        levelHashMap.put("Scifi",new String[]{"Level 1", "Level 2", "Level 3"});
        currentWorld="Base";
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();

        gamecam = new OrthographicCamera();
        hud = new OrthographicCamera();
        gameport = new StretchViewport(MyGdxGame.V_WIDTH / MyGdxGame.PPM, MyGdxGame.V_HEIGHT / MyGdxGame.PPM, gamecam);
        hudport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudport.setCamera(hud);

        maploader = new TmxMapLoader();
        map = maploader.load("level/menuLevel.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MyGdxGame.PPM);

        gamecam.position.set(gameport.getWorldWidth() / 2 + 10 / MyGdxGame.PPM, gameport.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        button = new Texture("buttons/mmButton.png");
        logo = new Texture("fonts/dcLogo.png");
        
        //Schriftart
        Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont(Gdx.files.internal("fonts/futura.fnt"), new TextureRegion(fontTexture), false);
        font.getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        font.setColor(Color.LIGHT_GRAY);

        b2creator = new B2WorldCreator(world, map);

        hudStage = new Stage(hudport);

        levelCounter=0;
        //TODO: Welttitel aktualisieren
        baseTitle = new Texture(Gdx.files.internal("fonts/base.png"));
        antTitle = new Texture(Gdx.files.internal("fonts/antartica.png"));
        sciTitle = new Texture(Gdx.files.internal("fonts/scifi.png"));
        shapeRenderer = new ShapeRenderer();
        
        //Pfeilbuttons
        leftArrow= new ArrowButton(false);
        rightArrow= new ArrowButton(true);
        leftLevelArrow = new ArrowButton(false);
        rightLevelArrow= new ArrowButton(true);
        rightArrow.getArrow().setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(rightArrow.getArrow().getWidth())), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(rightArrow.getArrow().getHeight())));
        leftArrow.getArrow().setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(leftArrow.getArrow().getWidth())), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(leftArrow.getArrow().getHeight())));
        
        rightArrow.setPosition((2*Gdx.graphics.getWidth()/3)+Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/baseTitle.getWidth()),(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/448f)));
        leftArrow.setPosition((2*Gdx.graphics.getWidth()/3)-Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/baseTitle.getWidth())-leftArrow.getArrow().getWidth(),(Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/448f)));
        
        Label.LabelStyle labelFont = new Label.LabelStyle(font, Color.BLUE);
        
        levelName= new Label(levelHashMap.get(currentWorld)[levelCounter],labelFont);
        levelName.setPosition(Gdx.graphics.getWidth()/3-levelName.getWidth()/1.5f,11*Gdx.graphics.getHeight()/16);
        leftLevelArrow.getArrow().setBounds(levelName.getX()-levelName.getWidth()-((Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50))-levelName.getWidth())/2),11*Gdx.graphics.getHeight()/16-levelName.getHeight()/2,Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50)),levelName.getHeight()*2);
        rightLevelArrow.getArrow().setBounds(levelName.getX()-((Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50))-levelName.getWidth())/2)+levelName.getWidth(),11*Gdx.graphics.getHeight()/16-levelName.getHeight()/2,Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50)),levelName.getHeight()*2);
		
    	//Erstellen des "Buttonstyle" (Texturen, Schrift)
    	atlas = new TextureAtlas("buttons/button.pack");
		skin = new Skin(atlas);
		TextButtonStyle buttonstyle = new TextButton.TextButtonStyle();
		buttonstyle.font=font;
		buttonstyle.fontColor= Color.LIGHT_GRAY;
		buttonstyle.up = skin.getDrawable("button.up");
		buttonstyle.down = skin.getDrawable("button.down");
		buttonstyle.pressedOffsetX = 1;
		buttonstyle.checkedOffsetY = -1;
		
		//MainMenu Button
		back = new TextButton("Main Menu",buttonstyle);
		back.setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(200)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(60)));
		back.setPosition(Gdx.graphics.getWidth()/2 - back.getWidth()/2, Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(140)));	

        //Listen von Namen / Scores laden
        TextureAtlas selection =new TextureAtlas("ui/ListAtlas");
        skin = new Skin(Gdx.files.internal("ui/table.json"), selection);
        skin.getFont("white").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        
        names = new List<String>(skin);
        scores = new List<String>(skin);
        if(XMLInteraction.loadScores("level1") != null){
        	String[][] loaded = XMLInteraction.loadScores("level1");
        	String[] nameArray = new String[loaded.length];
        	String[] scoreArray = new String[loaded.length];
        	
        	for(int i = 0; i < loaded.length; i++){
        		nameArray[i] = loaded[i][0];
        		scoreArray[i] = loaded[i][1];
        	}
        	
        	names.setItems(nameArray);
        	scores.setItems(scoreArray);
        }
        names.clearListeners();
        scores.clearListeners();
        names.getSelection().clear();
        scores.getSelection().clear();
        
        namePane = new ScrollPane(names);
        namePane.clearListeners();
        scorePane = new ScrollPane(scores);
        scorePane.clearListeners();
        
        //Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100)
        //Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50))
        
        final Table scrollTable = new Table(skin);
        scrollTable.setBounds(0, 0, Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(900f)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(250f)));
        scrollTable.add(namePane).width(scrollTable.getWidth()/2);
        scrollTable.add(scorePane).width(scrollTable.getWidth()/2);
        
        ScrollPaneStyle sps = new ScrollPaneStyle();
        sps.background = new Image(new Texture("ui/gray.png")).getDrawable();
        
        scrollPane = new ScrollPane(scrollTable, sps);
        scrollPane.setBounds(0, 0, Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(900f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(250f)));
        scrollPane.setSmoothScrolling(false);
        scrollPane.setPosition(Gdx.graphics.getWidth()/6,Gdx.graphics.getHeight()/4 + Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(40f)));
        scrollPane.setTransform(true);
        //scrollPane.setScale(Gdx.graphics.getHeight()/720f);
        
        initButtons();
        
        hudStage.addActor(scrollPane);
        hudStage.addActor(back);
        hudStage.addActor(leftLevelArrow.getArrow());
        hudStage.addActor(rightLevelArrow.getArrow());
        hudStage.addActor(leftArrow.getArrow());
        hudStage.addActor(levelName);
        hudStage.addActor(rightArrow.getArrow());
        Gdx.input.setInputProcessor(hudStage);
        
        back.addListener(new ChangeListener() {
	        public void changed (ChangeEvent event, Actor actor) {
	        	game.playButtonClicked();
	        	hudStage.dispose();
	        	game.setScreen(new MainMenuScreen(game));
	        }
	        
	    });

    }


    public void update(float delta) {
        handleInput(delta);

        world.step(1 / 60f, 1, 1);

        gamecam.update();
        renderer.setView(gamecam);
    }

    public void handleInput(float delta) {
    	if(Gdx.input.isKeyPressed(Keys.ESCAPE)){
    		game.setScreen(new MainMenuScreen(game));
    	}
    }

    @Override
    public void render(float delta) {

        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.setProjectionMatrix(gamecam.combined);
        renderer.render();

        //DebugRenderer
        b2dr.render(world, gamecam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1,1,1,1);
        shapeRenderer.end();

        batch.begin();
        if(currentWorld.equals("Base"))
        {
            batch.draw(baseTitle,(2*MyGdxGame.V_WIDTH/3-baseTitle.getWidth()/2)/MyGdxGame.PPM,(MyGdxGame.V_HEIGHT/3+220)/MyGdxGame.PPM,baseTitle.getWidth()/MyGdxGame.PPM,baseTitle.getHeight()/MyGdxGame.PPM);

        }
        if(currentWorld.equals("Antarctica"))
        {
            batch.draw(antTitle,(2*MyGdxGame.V_WIDTH/3-antTitle.getWidth()/2)/MyGdxGame.PPM,(MyGdxGame.V_HEIGHT/3+220)/MyGdxGame.PPM,antTitle.getWidth()/MyGdxGame.PPM,antTitle.getHeight()/MyGdxGame.PPM);

        }
        
        if(currentWorld.equals("Scifi"))
        {
            batch.draw(sciTitle,(2*MyGdxGame.V_WIDTH/3-sciTitle.getWidth()/2)/MyGdxGame.PPM,(MyGdxGame.V_HEIGHT/3+220)/MyGdxGame.PPM,sciTitle.getWidth()/MyGdxGame.PPM,sciTitle.getHeight()/MyGdxGame.PPM);

        }
        
        batch.draw(logo, 300 / MyGdxGame.PPM, 3.5f, logo.getWidth() / MyGdxGame.PPM, logo.getHeight() / MyGdxGame.PPM);
        batch.end();

        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();

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
        button.dispose();
        logo.dispose();
        font.dispose();
    }



    public void initButtons()
    {
        rightArrow.getArrow().addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            	game.playButtonClicked();
            	//TODO: Wenn mehr Welten vorhanden, Liste füllen
                if(currentWorld.equals("Base"))
                {
                    currentWorld="Antarctica";
                    levelCounter=0;
                    levelName.setText(levelHashMap.get(currentWorld)[0]);
                }
                else if(currentWorld.equals("Antarctica"))
                {
                    currentWorld="Scifi";
                    levelCounter=0;
                    levelName.setText(levelHashMap.get(currentWorld)[0]);
                }
                
                else if(currentWorld.equals("Scifi"))
                {
                    currentWorld="Base";
                    levelCounter=0;
                    levelName.setText(levelHashMap.get(currentWorld)[0]);
                }
                
                if(XMLInteraction.loadScores(getCurrentLevel()) != null){
                	String[][] loaded = XMLInteraction.loadScores(getCurrentLevel());
                	String[] nameArray = new String[loaded.length];
                	String[] scoreArray = new String[loaded.length];
                	
                	for(int i = 0; i < loaded.length; i++){
                		nameArray[i] = loaded[i][0];
                		scoreArray[i] = loaded[i][1];
                	}
                	
                	names.setItems(nameArray);
                	scores.setItems(scoreArray);
                } else {
                	names.setItems("");
                	scores.setItems("");
                }
                names.getSelection().clear();
                scores.getSelection().clear();
                
                return true;

            }
        });
        leftArrow.getArrow().addListener(new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        	game.playButtonClicked();
        	//TODO Same Here
            if(currentWorld.equals("Base"))
            {
                currentWorld="Scifi";
                levelCounter=0;
                levelName.setText(levelHashMap.get(currentWorld)[0]);
            }
            else if(currentWorld.equals("Antarctica"))
            {
                currentWorld="Base";
                levelCounter=0;
                levelName.setText(levelHashMap.get(currentWorld)[0]);
            }
            
            else if(currentWorld.equals("Scifi"))
            {
                currentWorld="Antarctica";
                levelCounter=0;
                levelName.setText(levelHashMap.get(currentWorld)[0]);
            }
            
            if(XMLInteraction.loadScores(getCurrentLevel()) != null){
            	String[][] loaded = XMLInteraction.loadScores(getCurrentLevel());
            	String[] nameArray = new String[loaded.length];
            	String[] scoreArray = new String[loaded.length];
            	
            	for(int i = 0; i < loaded.length; i++){
            		nameArray[i] = loaded[i][0];
            		scoreArray[i] = loaded[i][1];
            	}
            	
            	names.setItems(nameArray);
            	scores.setItems(scoreArray);
            } else {
            	names.setItems("");
            	scores.setItems("");
            }
            names.getSelection().clear();
            scores.getSelection().clear();
            
            return true;

         }
        });
        rightLevelArrow.getArrow().addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            	game.playButtonClicked();
            	if (levelCounter==levelHashMap.get(currentWorld).length-1)
                {
                    levelCounter=0;
                }
                else
                {
                    levelCounter++;
                }
                levelName.setText(levelHashMap.get(currentWorld)[levelCounter]);
                System.out.println(getCurrentLevel());
                if(XMLInteraction.loadScores(getCurrentLevel()) != null){
                	String[][] loaded = XMLInteraction.loadScores(getCurrentLevel());
                	String[] nameArray = new String[loaded.length];
                	String[] scoreArray = new String[loaded.length];
                	
                	for(int i = 0; i < loaded.length; i++){
                		nameArray[i] = loaded[i][0];
                		scoreArray[i] = loaded[i][1];
                	}
                	
                	names.setItems(nameArray);
                	scores.setItems(scoreArray);
                } else {
                	names.setItems("");
                	scores.setItems("");
                }
                names.getSelection().clear();
                scores.getSelection().clear();
                
                return true;

            }
        });
        leftLevelArrow.getArrow().addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            	game.playButtonClicked();
            	if (levelCounter==0)
                {
                    levelCounter=levelHashMap.get(currentWorld).length-1;
                }
                else
                {
                    levelCounter--;
                }
                levelName.setText(levelHashMap.get(currentWorld)[levelCounter]);
                if(XMLInteraction.loadScores(getCurrentLevel()) != null){
                	String[][] loaded = XMLInteraction.loadScores(getCurrentLevel());
                	String[] nameArray = new String[loaded.length];
                	String[] scoreArray = new String[loaded.length];
                	
                	for(int i = 0; i < loaded.length; i++){
                		nameArray[i] = loaded[i][0];
                		scoreArray[i] = loaded[i][1];
                	}
                	
                	names.setItems(nameArray);
                	scores.setItems(scoreArray);
                } else {
                	names.setItems("");
                	scores.setItems("");
                }
                names.getSelection().clear();
                scores.getSelection().clear();
                return true;

            }
        });
        


    }
    
    private String getCurrentLevel(){
    	String lvl = "";
    	if(currentWorld.equals("Base")){
    		lvl = "level";
    	} else if(currentWorld.equals("Antarctica")){
    		lvl = "levelice";
    	} else if(currentWorld.equals("Scifi")){
    		lvl = "levelscifi";
    	} 
    	return lvl + (levelCounter+1);
    	
    }

    
}