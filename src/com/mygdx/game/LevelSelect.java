package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.net.InetAddress;
import java.util.HashMap;

import hudElements.ArrowButton;
import network.BuiltConnection;

/**
 * Created by hermann on 01.07.16.
 */
public class LevelSelect implements Screen {
    MyGdxGame game;
    boolean onlineGame;
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

    //Hashmap für Level
    HashMap<String,String[]> levelHashMap= new HashMap<String, String[]>();
    //Levelauswahl
    Texture baseTitle;
    Texture antTitle;
    Texture sciTitle;
    Texture preview;
    Texture previewBase;
    Texture previewAnt;
    Texture previewSci;
    Vector2 previewPosition;
    ArrowButton rightArrow;
    ArrowButton leftArrow;
    ArrowButton leftLevelArrow;
    ArrowButton rightLevelArrow;
    Label levelName;
    BitmapFont font;
    String currentWorld;
    int levelCounter;
    
    //StartButton
    TextureAtlas atlas;
    TextButton.TextButtonStyle buttonstyle;
    Skin skin;
    TextButton startButton;
    TextButton backButton;



    /**
     * Screen für Levelauswahl, sowohl für Single- als auch für Multiplayer
     * @param game Aktuelle Spielinstanz
     * @param isOnline true, wenn Multiplayer-Spiel
     */
    public LevelSelect(MyGdxGame game, boolean isOnline)
    {
        this.game=game;
        this.onlineGame = isOnline;
        //TODO: Neue Welten und Level hier eintragen, zusätzlichen unten Buttons aktualisieren
        levelHashMap.put("Base",new String[]{"Level 1","Level 2","Level 3"});
        levelHashMap.put("Antartica",new String[]{"Level 1","Level 2","Level 3"});
        levelHashMap.put("Scifi",new String[]{"Level 1","Level 2","Level 3"});
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

        b2creator = new B2WorldCreator(world, map);

        hudStage = new Stage(hudport);
        levelCounter=0;
        //TODO: Welttitel aktualisieren
        baseTitle = new Texture(Gdx.files.internal("fonts/base.png"));
        antTitle = new Texture(Gdx.files.internal("fonts/antartica.png"));
        sciTitle = new Texture(Gdx.files.internal("fonts/scifi.png"));
        //TODO: Preview Bilder aktualiseren
        previewAnt= new Texture(Gdx.files.internal("level/previewAnt.png"));
        previewBase = new Texture(Gdx.files.internal("level/previewBase.png"));
        previewSci = new Texture(Gdx.files.internal("level/previewSci.png"));
        preview=previewBase;
        previewPosition = new Vector2(2.93f,2.9f);
       
        //Pfeilbuttons
        leftArrow= new ArrowButton(false);
        rightArrow= new ArrowButton(true);
        leftLevelArrow = new ArrowButton(false);
        rightLevelArrow= new ArrowButton(true);
        
        rightArrow.getArrow().setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(rightArrow.getArrow().getWidth())), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(rightArrow.getArrow().getHeight())));
        leftArrow.getArrow().setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(leftArrow.getArrow().getWidth())), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(leftArrow.getArrow().getHeight())));
        rightArrow.setPosition((Gdx.graphics.getWidth()/2)+Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(baseTitle.getWidth())),(Gdx.graphics.getHeight()/2-Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(55))));
        leftArrow.setPosition((Gdx.graphics.getWidth()/2)-Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/baseTitle.getWidth())-leftArrow.getArrow().getWidth(),(Gdx.graphics.getHeight()/2-Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(55))));

        Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
        
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont(Gdx.files.internal("fonts/futura.fnt"), new TextureRegion(fontTexture), false);
        font.setColor(Color.WHITE);
        font.getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        Label.LabelStyle labelFont = new Label.LabelStyle(font, Color.BLUE);
        levelName= new Label(levelHashMap.get(currentWorld)[levelCounter],labelFont);
        levelName.setPosition(Gdx.graphics.getWidth()/2-levelName.getWidth()/1.5f,Gdx.graphics.getHeight()/4);
        leftLevelArrow.getArrow().setBounds(levelName.getX()-levelName.getWidth()-((Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50))-levelName.getWidth())/2),Gdx.graphics.getHeight()/4-levelName.getHeight()/2,Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50)),levelName.getHeight()*2);
        rightLevelArrow.getArrow().setBounds(levelName.getX()-((Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50))-levelName.getWidth())/2)+levelName.getWidth(),Gdx.graphics.getHeight()/4-levelName.getHeight()/2,Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50)),levelName.getHeight()*2);

        //Startbutton
        atlas = new TextureAtlas("buttons/button.pack");
        skin = new Skin(atlas);
        buttonstyle = new TextButton.TextButtonStyle();
        buttonstyle.font = font;
        buttonstyle.up = skin.getDrawable("button.up");
        buttonstyle.down= skin.getDrawable("button.down");
        buttonstyle.fontColor= Color.LIGHT_GRAY;
        
        startButton = new TextButton("Start!",buttonstyle);
        startButton.pad(20);
        startButton.setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50)));
        startButton.setPosition(Gdx.graphics.getWidth()/2+2*startButton.getWidth(),rightLevelArrow.getArrow().getY());
        
        backButton = new TextButton("Back",buttonstyle);
        backButton.pad(20);
        backButton.setSize(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50)));
        backButton.setPosition(Gdx.graphics.getWidth()/2-3*backButton.getWidth(),leftLevelArrow.getArrow().getY());
        
        initButtons();

        hudStage.addActor(backButton);
        hudStage.addActor(startButton);
        hudStage.addActor(leftLevelArrow.getArrow());
        hudStage.addActor(rightLevelArrow.getArrow());
        hudStage.addActor(leftArrow.getArrow());
        hudStage.addActor(levelName);
        hudStage.addActor(rightArrow.getArrow());
        Gdx.input.setInputProcessor(hudStage);



    }
    public void update(float delta) {

        world.step(delta, 1, 1);


        gamecam.update();
        renderer.setView(gamecam);
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

        batch.begin();
        if(currentWorld.equals("Base"))
        {
            batch.draw(baseTitle,(MyGdxGame.V_WIDTH/2-baseTitle.getWidth()/2)/MyGdxGame.PPM,(MyGdxGame.V_HEIGHT/2-40)/MyGdxGame.PPM,baseTitle.getWidth()/MyGdxGame.PPM,baseTitle.getHeight()/MyGdxGame.PPM);

        }
        if(currentWorld.equals("Antartica"))
        {
            batch.draw(antTitle,(MyGdxGame.V_WIDTH/2-antTitle.getWidth()/2)/MyGdxGame.PPM,(MyGdxGame.V_HEIGHT/2-40)/MyGdxGame.PPM,antTitle.getWidth()/MyGdxGame.PPM,antTitle.getHeight()/MyGdxGame.PPM);

        }
        
        if(currentWorld.equals("Scifi"))
        {
            batch.draw(sciTitle,(MyGdxGame.V_WIDTH/2-sciTitle.getWidth()/2)/MyGdxGame.PPM,(MyGdxGame.V_HEIGHT/2-40)/MyGdxGame.PPM,sciTitle.getWidth()/MyGdxGame.PPM,sciTitle.getHeight()/MyGdxGame.PPM);

        }
        
        batch.draw(preview,previewPosition.x,previewPosition.y,400/MyGdxGame.PPM,240/MyGdxGame.PPM);
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
        //map.dispose();
    	//renderer.dispose();
        //world.dispose();
        //b2dr.dispose();
        previewAnt.dispose();
        previewBase.dispose();
        previewSci.dispose();
        preview.dispose();
        antTitle.dispose();
        baseTitle.dispose();
        sciTitle.dispose();
        hudStage.dispose();


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
                    currentWorld="Antartica";
                    levelCounter=0;
                    levelName.setText(levelHashMap.get(currentWorld)[0]);
                    preview=previewAnt;
                }
                else if(currentWorld.equals("Antartica"))
                {
                    currentWorld="Scifi";
                    levelCounter=0;
                    levelName.setText(levelHashMap.get(currentWorld)[0]);
                    preview=previewSci;
                }
                
                else if(currentWorld.equals("Scifi"))
                {
                    currentWorld="Base";
                    levelCounter=0;
                    levelName.setText(levelHashMap.get(currentWorld)[0]);
                    preview=previewBase;
                }
                
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
                preview=previewSci;
            }
            else if(currentWorld.equals("Antartica"))
            {
                currentWorld="Base";
                levelCounter=0;
                levelName.setText(levelHashMap.get(currentWorld)[0]);
                preview=previewBase;
            }
            
            else if(currentWorld.equals("Scifi"))
            {
                currentWorld="Antartica";
                levelCounter=0;
                levelName.setText(levelHashMap.get(currentWorld)[0]);
                preview=previewAnt;
            }
            
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
                return true;

            }
        });
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	hudStage.clear();
            	game.playButtonClicked();
            	game.setScreen(new MainMenuScreen(game));
            }
        });
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	game.playButtonClicked();
            	if(!onlineGame)
            	{//TODO: Falls neue Level in Welt erstellt werden, hier aktualisieren
                   if(currentWorld.equals("Base"))
                   {
                       switch (levelCounter)
                       {
                           case 0: game.setScreen(new Level1(game));
                                    dispose();
                                    break;
                           case 1: game.setScreen(new Level2(game));
                           			dispose();
                           			break;    
                           case 2: game.setScreen(new Level3(game));
                                    dispose();
                                     break;
                       }
                   }
                   
                   else if (currentWorld.equals("Antartica"))
                   {
                       switch (levelCounter)
                       {
                           case 0: game.setScreen(new LevelIce1(game));
                               dispose();
                               break;
                           case 1: game.setScreen(new LevelIce2(game));
                               dispose();
                               break;
                           case 2: game.setScreen(new LevelIce3(game));
                               dispose();
                               break;

                       }

                   }
                   
                   else if (currentWorld.equals("Scifi"))
                   {
                       switch (levelCounter)
                       {
                       		case 0 : game.setScreen(new LevelScifi1(game));
                       		 		dispose();
                       		 		break;
                       		case 1 : game.setScreen(new LevelScifi2(game));
               		 				dispose();
               		 				break;
                       		case 2 : game.setScreen(new LevelScifi3(game));
                               		dispose();
                               		break;
                       }

                   }
               }
                else
                {if (currentWorld.equals("Antartica"))
                {
                    switch (levelCounter)
                    {
                        case 0: game.setScreen(new BuiltConnection(game,4));
                            dispose();
                            break;
                        case 1: game.setScreen(new BuiltConnection(game,5));
                            dispose();
                            break;
                        case 2: game.setScreen(new BuiltConnection(game,6));
                            dispose();
                            break;

                    }
                }
                    if (currentWorld.equals("Base"))
                    {
                        switch (levelCounter)
                        {
                            case 0: game.setScreen(new BuiltConnection(game,1));
                                dispose();
                                break;
                            case 1: game.setScreen(new BuiltConnection(game,2));
                                dispose();
                                break;
                            case 2: game.setScreen(new BuiltConnection(game,3));
                                dispose();
                                break;


                        }
                    }
                    if (currentWorld.equals("Scifi"))
                    {
                        switch (levelCounter)
                        {
                            case 0: game.setScreen(new BuiltConnection(game,7));
                                dispose();
                                break;
                            case 1: game.setScreen(new BuiltConnection(game,8));
                                dispose();
                                break;
                            case 2: game.setScreen(new BuiltConnection(game,9));
                                dispose();
                                break;


                        }
                    }

                }
            }});


    }


}
