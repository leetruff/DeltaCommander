package network;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import com.mygdx.game.B2WorldCreator;

import com.mygdx.game.MyGdxGame;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * Created by hermann on 08.06.16.
 */
public class JoinGameMenu implements Screen {
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
    Label availableServer;
    ScrollPane scrollPane;
    List<String> list;
    Skin skin;
    ShapeRenderer shapeRenderer;
    //Kryonet
    Client client;
    String[] ips;

    //Refresh Button
    TextureAtlas atlas;
    TextButton.TextButtonStyle buttonstyle;
    TextButton refreshButton;
    //Connect Button
    TextButton connectButton;
    TextButton.TextButtonStyle connectbuttonstyle;
    //BackButton
    TextButton backButton;
    TextButton.TextButtonStyle backbuttonstyle;
    //ConnectByIp-Button
    TextButton connectByIpButton;
    //Textfeld für IP Eingabe
    TextField ipField;
    Skin textSkin;
    Label connectByIp;
    boolean isIP;
    boolean check;
    Texture wrongIp;
    Texture rightIP;

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");




    public JoinGameMenu(MyGdxGame game) {
        this.game = game;
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
        Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font = new BitmapFont(Gdx.files.internal("fonts/futura.fnt"), new TextureRegion(fontTexture), false);
        if(Gdx.graphics.getWidth()<(MyGdxGame.V_WIDTH)){
        	font.getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/0.9f));
        }
        else{
        	font.getData().setScale(0.9f);
        }
        font.setColor(Color.WHITE);

        b2creator = new B2WorldCreator(world, map);

        hudStage = new Stage(hudport);
        shapeRenderer = new ShapeRenderer();

        world.setContactListener(new WorldContactListener());
        //Client initialisieren
        client = new Client();
        check= false;
        Kryo kryo = client.getKryo();
        kryo.register(PositionMessage.class);
        client.start();

        //Menuepunkte als Box2D Bodies
        BodyDef buttonBodyDef = new BodyDef();
        buttonBodyDef.type = BodyDef.BodyType.StaticBody;



        //Cursor wird zum Crosshair
        //Quelle: http://addcomponent.com/lesson-2-create-first-person-weapon/
        Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_red.png"));
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth() / 2, pm.getHeight() / 2));
        pm.dispose();

        //Liste
        TextureAtlas selection =new TextureAtlas("ui/ListAtlas");
        skin = new Skin(Gdx.files.internal("ui/table.json"), selection);
        skin.getFont("white").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        skin.getFont("black").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        skin.getFont("blue").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        list = new List<String>(skin);
        ips = new String[1];
        ips[0]="--No Server available--";

        list.setItems(ips);
        scrollPane = new ScrollPane(list);
        scrollPane.setBounds(0, 0, Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(300f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(300f)));
        scrollPane.setSmoothScrolling(false);
        scrollPane.setPosition(Gdx.graphics.getWidth()/6,Gdx.graphics.getHeight()/4);
        scrollPane.setTransform(true);
        //scrollPane.setScale(Gdx.graphics.getHeight()/720);
        
        Label.LabelStyle labelFont = new Label.LabelStyle(font,Color.BLUE);
        availableServer = new Label("Available Server:", labelFont);
        availableServer.setPosition(scrollPane.getX()-Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(150f)),scrollPane.getY()+scrollPane.getHeight());
        availableServer.setFontScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1.2f));
        
        connectByIp = new Label("Connect by IP:",labelFont);
        connectByIp.setPosition(availableServer.getX()+Gdx.graphics.getWidth()/2,availableServer.getY());
        connectByIp.setFontScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1.2f));

        //Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(900f)), Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(250f)));
        //Buttons
        atlas = new TextureAtlas("buttons/button.pack");
        skin = new Skin(atlas);
       
        //RefreshButton
        buttonstyle = new TextButton.TextButtonStyle();
        buttonstyle.font = font;
        buttonstyle.up = skin.getDrawable("button.up");
        buttonstyle.down= skin.getDrawable("button.down");
        buttonstyle.fontColor= Color.BLACK;
                
        refreshButton = new TextButton("Refresh",buttonstyle);
        refreshButton.pad(20);
        refreshButton.setBounds(scrollPane.getX(),scrollPane.getY()-Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(75f)),Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100f)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50f)));
        refreshButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	game.playButtonClicked();
            	java.util.List<InetAddress> address = client.discoverHosts(53012,1000);
                if(address.size()>0) {
                    ips = new String[address.size()];
                    for (int i = 0; i < address.size(); i++) {
                        ips[i] = address.get(i).toString();
                    }
                    ips= removeDuplicates(ips);
                }
                else
                {
                    ips = new String[1];
                    ips[0]="--No Server available--";
                }
                System.out.println("Gecheckt");
                list.setItems(ips);
                }});
        //ConnectButton
        connectbuttonstyle = new TextButton.TextButtonStyle();
        connectbuttonstyle.font = font;
        connectbuttonstyle.up = skin.getDrawable("button.up");
        connectbuttonstyle.down= skin.getDrawable("button.down");
        connectbuttonstyle.fontColor= Color.RED;
        connectButton = new TextButton("Connect",connectbuttonstyle);
        connectButton.pad(20);
        connectButton.setBounds(scrollPane.getX()+refreshButton.getWidth()+Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(25f)),scrollPane.getY()-Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(75f)),Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100f)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50f)));
        connectButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	game.playButtonClicked();
            	if(connectButton.getStyle().fontColor==Color.BLACK)
                {
            		hudStage.clear();
                    game.setScreen(new BuiltConnection(game,client,list.getSelected()));
                }
            }});
        
        //ConnectButton
        backbuttonstyle = new TextButton.TextButtonStyle();
        backbuttonstyle.font = font;
        backbuttonstyle.up = skin.getDrawable("button.up");
        backbuttonstyle.down= skin.getDrawable("button.down");
        backbuttonstyle.fontColor= Color.LIGHT_GRAY;
        backButton = new TextButton("Back",backbuttonstyle);
        backButton.pad(20);
        backButton.setBounds(scrollPane.getX()+refreshButton.getWidth()+connectButton.getWidth()+Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(50f)),scrollPane.getY()-Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(75f)),Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100f)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50f)));
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	game.playButtonClicked();
            	hudStage.clear();
            	game.setScreen(new MultiMenu(game,640 / MyGdxGame.PPM, 100 / MyGdxGame.PPM));
            	dispose();
            }});
        wrongIp = new Texture("ui/wrongIP.png");
        rightIP = new Texture("ui/rightIP.png");
        //ConnectByIPButton
        connectByIpButton = new TextButton("Connect", buttonstyle);
        connectByIpButton.pad(20);
        connectByIpButton.setBounds(scrollPane.getX()+Gdx.graphics.getWidth()/2,connectByIp.getY()/2,Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(100f)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50f)));
        connectByIpButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	game.playButtonClicked();
            	if(isIP)
                {
            		hudStage.clear();
                    game.setScreen(new BuiltConnection(game,client,ipField.getText()));
                }
            }});
        textSkin = new Skin(Gdx.files.internal("ui/textfield.json"), selection);
        textSkin.getFont("white").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/0.8f));
        textSkin.getFont("black").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/0.8f));
        textSkin.getFont("blue").getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/0.8f));
        ipField = new TextField("",textSkin);
        ipField.setBounds(scrollPane.getX()+Gdx.graphics.getWidth()/2,connectByIpButton.getY()+Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(50f))+ipField.getHeight(),Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(200f)),Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(30f)));
        ipField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                check=true;
                if(validate(ipField.getText()))
                {
                    isIP=true;
                }
                else
                {
                    isIP=false;
                }
            }});
        isIP=false;

        hudStage.addActor(ipField);
        hudStage.addActor(connectButton);
        hudStage.addActor(refreshButton);
        hudStage.addActor(backButton);
        hudStage.addActor(connectByIpButton);
        hudStage.addActor(availableServer);
        hudStage.addActor(connectByIp);
        hudStage.addActor(scrollPane);
        Gdx.input.setInputProcessor(hudStage);






    }


    public void update(float delta) {
        handleInput(delta);


        world.step(1 / 60f, 1, 1);


        gamecam.update();
        renderer.setView(gamecam);
    }

    public void handleInput(float delta) {

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
        shapeRenderer.rect(ipField.getX(),ipField.getY(),ipField.getWidth(),ipField.getHeight());
        shapeRenderer.end();




        batch.begin();
        batch.draw(logo, 300 / MyGdxGame.PPM, 3.5f, logo.getWidth() / MyGdxGame.PPM, logo.getHeight() / MyGdxGame.PPM);
        if(check) {
            if (!isIP) {
                batch.draw(wrongIp, ((ipField.getX() + ipField.getWidth() + 20))/ MyGdxGame.PPM*1280f/(float)Gdx.graphics.getWidth(), (ipField.getY() ) / MyGdxGame.PPM*(720f/Gdx.graphics.getHeight()), 25 / MyGdxGame.PPM, 25 / MyGdxGame.PPM);
            } else {
                batch.draw(rightIP, ((ipField.getX() + ipField.getWidth() + 20))/ MyGdxGame.PPM*1280f/(float)Gdx.graphics.getWidth(), (ipField.getY() ) / MyGdxGame.PPM*(720f/Gdx.graphics.getHeight()), 35 / MyGdxGame.PPM, 35 / MyGdxGame.PPM);

            }
        }
        batch.end();
        if(!list.getSelected().startsWith("--"))
        {
            connectButton.getStyle().fontColor = Color.BLACK;
        }
        else
        {
            connectButton.getStyle().fontColor = Color.RED;
        }

        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();
        if(list.getSelected().startsWith("--")) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1);
            shapeRenderer.rect((scrollPane.getX() + scrollPane.getWidth() - 50), scrollPane.getY(), 100, scrollPane.getHeight());
            shapeRenderer.end();
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
        button.dispose();
        ;
        logo.dispose();
        ;
        font.dispose();
        ;
        wrongIp.dispose();
        rightIP.dispose();

    }



    //KollisionsController
    public class WorldContactListener implements ContactListener {

        // Contact listener for all fixtures in the world
        @Override
        public void beginContact(Contact contact) {

        }

        @Override
        public void endContact(Contact contact) {
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
        }
    }

    /**
     * Überprüft ob eingegebene IP-Adresse gültig ist
     * @param ip
     * @return
     */
    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    public String[] removeDuplicates(String[] ips)
    {
        ArrayList<String> tmp = new ArrayList<String>();
        for(int i =0;i<ips.length;i++)
        {
            for(int j =0;j<tmp.size()+1;j++)
            {
                if(j==tmp.size())
                {
                    tmp.add(ips[i]);
                    break;
                }
                else
                {
                    if(tmp.get(j).equals(ips[i]))
                    {
                        break;
                    }
                }
            }
        }
        String[] newIps = new String[tmp.size()];
        for(int i=0;i<tmp.size();i++)
        {
            newIps[i]=tmp.get(i);
        }
        return newIps;

    }
}