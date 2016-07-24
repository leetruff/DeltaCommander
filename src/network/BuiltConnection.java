package network;

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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.mygdx.game.*;

import java.io.IOException;
import java.util.ArrayList;

import javafx.geometry.Pos;

/**
 * Created by hermann on 05.06.16.
 */
public class BuiltConnection implements Screen {
    MyGdxGame game;
    boolean isServer;
    float level;
    Server server;
    Client client;
    OrthographicCamera cam;
    SpriteBatch batch;
    Connection con;
    String ipAd;
    Boolean found=false;
    Stage hudstage;
    Viewport hudport;

    Label waitLabel;
    BitmapFont font;
    int changeText;
    Texture logo;



    //objekte um die map zu laden, welche mit TILED erstellt wurde
    TmxMapLoader maploader;
    TiledMap map;
    OrthogonalTiledMapRenderer renderer;

    //Box2d Kram
    World world;
    Box2DDebugRenderer b2dr;
    B2WorldCreator b2creator;

    OrthographicCamera gamecam;
    Viewport gameport;
    //Zurück-Button
    TextureAtlas atlas;
    TextButton.TextButtonStyle buttonstyle;
    TextButton backButton;
    Skin skin;




    /**
     * Konstruktur für Server, erstellt neuen Kryonet Server
     * @param gam   Das aktuelle Spiel
     * @param level ID des Levels
     */
    public BuiltConnection(MyGdxGame gam, final int level)
    {
        game=gam;
        this.level=level;
        isServer=true;

        cam = new OrthographicCamera();
        cam.setToOrtho(false,540,960);
        batch = new SpriteBatch();

        server = new Server();
        Kryo kryo = server.getKryo();

        kryo.register(PositionMessage.class);
        kryo.register(ShootingMessage.class);
        kryo.register(CorrectionMessage.class);
        kryo.register(BigCorrectionMessage.class);
        kryo.register(ArrayList.class);
        server.start();
        try {
            server.bind(54555,53012);
            System.out.println("Binding successful");
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        server.addListener(new Listener() {
                            @Override
                            public void received(Connection connection, Object object) {
                                if (object instanceof PositionMessage&&((PositionMessage) object).posY==-1) {
                                    con=connection;
                                    found=true;
                                    connection.sendTCP(new PositionMessage(level,level, Hero.State.FALLING));
                                }

                                if (object instanceof PositionMessage&&((PositionMessage) object).posY==-10) {
                                    PositionMessage request = (PositionMessage) object;
                                    System.out.println("Gegner gefunden");
                                    con=connection;
                                    found=true;
                                    connection.sendTCP(request);
                                }
                            }

                            public void connected(Connection connection) {
                                System.out.println("Server: Connected");
                            }
                        });

                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    /**
     * Konstruktor für Client
     * @param game
     * @param cl Client Instanz, die bereits bei der Server Discovery erzeugt wurde
     * @param ip Ausgewählte IP-Adresse aus dem Server Discovery
     */
    public BuiltConnection(MyGdxGame game, Client cl, String ip)
    {
        this.game=game;
        isServer=false;

        cam = new OrthographicCamera();
        cam.setToOrtho(false,540,960);
        batch = new SpriteBatch();
        client = cl;
        Kryo kryo = client.getKryo();
        kryo.register(PositionMessage.class);
        kryo.register(ShootingMessage.class);
        kryo.register(CorrectionMessage.class);
        kryo.register(BigCorrectionMessage.class);
        kryo.register(ArrayList.class);
        client.start();


        ipAd = ip;
        Gdx.app.postRunnable(new Runnable() {
            public void run() {
                client.addListener(new Listener() {
                    public void received (Connection connection, Object object) {

                        if (object instanceof PositionMessage&&((PositionMessage) object).posY!=-1) {
                            PositionMessage request = (PositionMessage) object;
                            level=request.getPosX();
                            found=true;
                        }
                    }

                });
                try {
                    System.out.println(ipAd);
                    client.connect(5000, ipAd.substring(1,ipAd.length()), 54555,53012);
                    if(client.isConnected()){
                        System.out.println("Connection successful!");
                        client.sendTCP(new PositionMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }                        }
        });

    }

    @Override
    public void show() {
        logo = new Texture("fonts/dcLogo.png");
        Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont(Gdx.files.internal("fonts/futura.fnt"), new TextureRegion(fontTexture), false);
        font.setColor(Color.WHITE);
        Label.LabelStyle labelFont = new Label.LabelStyle(font, Color.WHITE);
        waitLabel = new Label("Bitte warten...",labelFont);
        hudport= new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudport.setCamera(cam);
        hudstage=  new Stage(hudport);
        waitLabel.setPosition(Gdx.graphics.getWidth()/2-waitLabel.getWidth(),Gdx.graphics.getHeight()/2);
        waitLabel.setFontScale(1.5f);
        hudstage.addActor(waitLabel);
        changeText=0;

        gamecam = new OrthographicCamera();
        gameport = new StretchViewport(MyGdxGame.V_WIDTH / MyGdxGame.PPM, MyGdxGame.V_HEIGHT / MyGdxGame.PPM, gamecam);


        maploader = new TmxMapLoader();
        map = maploader.load("level/menuLevel.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MyGdxGame.PPM);

        gamecam.position.set(gameport.getWorldWidth() / 2 + 10/MyGdxGame.PPM, gameport.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();
        b2creator = new B2WorldCreator(world, map);
        //Back-Button
        atlas = new TextureAtlas("buttons/button.pack");
        skin = new Skin(atlas);
        buttonstyle = new TextButton.TextButtonStyle();

        buttonstyle.font = font;
        buttonstyle.up = skin.getDrawable("button.up");
        buttonstyle.down= skin.getDrawable("button.down");
        buttonstyle.fontColor= Color.BLACK;
        backButton = new TextButton("Back",buttonstyle);
        backButton.pad(20);
        backButton.setBounds(waitLabel.getX()+waitLabel.getWidth()/2+20,waitLabel.getY()-100,100,50);
        backButton.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	game.playButtonClicked();
            	if(isServer) {
                    server.close();
                }
                else
                {
                    client.close();
                }
            	hudstage.clear();
                game.setScreen(new MultiMenu(game,1,1));
                dispose();
            }});
        hudstage.addActor(backButton);
        Gdx.input.setInputProcessor(hudstage);


    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(found)
        {
            /**
             * Haben sich Server und Client gefunden, sendet der Server dem Client die Level-ID, woraufhin beide dann in das jeweilige Level springen
             */
            if(isServer)
            {
                waitLabel.setText("Found Opponent...");
                hudstage.clear();
                //Starte Server-Game
                switch ((int)level) {
                    case 1: game.setScreen(new Level1Multi(game,server,con));
                        dispose();
                        break;
                    case 2: game.setScreen(new Level2Multi(game,server,con));
                        dispose();
                        break;
                    case 3: game.setScreen(new Level3Multi(game,server,con));
                        dispose();
                        break;
                    case 4: game.setScreen(new LevelIce1Multi(game,server,con));
                            dispose();
                            break;
                    case 5: game.setScreen(new LevelIce2Multi(game,server,con));
                            dispose();
                            break;
                    case 6: game.setScreen(new LevelIce3Multi(game,server,con));
                        dispose();
                        break;
                    case 7: game.setScreen(new LevelScifi1Multi(game,server,con));
                        dispose();
                        break;
                    case 8: game.setScreen(new LevelScifi2Multi(game,server,con));
                        dispose();
                        break;
                    case 9: game.setScreen(new LevelScifi3Multi(game,server,con));
                        dispose();
                        break;

                }

            }
            else
            {
                waitLabel.setText("Found Opponent...");
                hudstage.clear();
                //Starte Client-Game
                switch((int)level) {
                    case 1: game.setScreen(new Level1Multi(game, client));
                        dispose();
                        break;
                    case 2: game.setScreen(new Level2Multi(game, client));
                        dispose();
                        break;
                    case 3: game.setScreen(new Level3Multi(game, client));
                        dispose();
                        break;
                    case 4: game.setScreen(new LevelIce1Multi(game, client));
                            dispose();
                            break;
                    case 5: game.setScreen(new LevelIce2Multi(game, client));
                        dispose();
                        break;
                    case 6: game.setScreen(new LevelIce3Multi(game, client));
                        dispose();
                        break;
                    case 7: game.setScreen(new LevelScifi1Multi(game, client));
                        dispose();
                        break;
                    case 8: game.setScreen(new LevelScifi2Multi(game, client));
                        dispose();
                        break;
                    case 9: game.setScreen(new LevelScifi3Multi(game, client));
                        dispose();
                        break;
                }

            }
            changeText=-1;
        }
        hudstage.draw();
        hudstage.act(delta);
        switch (changeText)
        {
            case -1: break;
            case 0: waitLabel.setText("Waiting for Opponent");
                    changeText++;
                    break;
            case 20: waitLabel.setText("Waiting for Opponent.");
                    changeText++;
                    break;
            case 40: waitLabel.setText("Waiting for Opponent..");
                    changeText++;
                    break;
            case 60: waitLabel.setText("Waiting for Opponent...");
                    changeText++;
                    break;
            case 80: changeText=0;
                    break;
            default:changeText++;
                    break;
        }
        batch.setProjectionMatrix(gamecam.combined);
        batch.begin();
        batch.draw(logo, 300/MyGdxGame.PPM, 3.5f, logo.getWidth()/MyGdxGame.PPM,logo.getHeight()/MyGdxGame.PPM);
        batch.end();
        world.step(1 / 60f, 1, 1);


        gamecam.update();
        renderer.setView(gamecam);
        renderer.render();

        //DebugRenderer
        b2dr.render(world, gamecam.combined);

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

    }
}



