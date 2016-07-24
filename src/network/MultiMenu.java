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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.B2WorldCreator;
import com.mygdx.game.GameScreen;
import com.mygdx.game.Hero;
import com.mygdx.game.LevelSelect;
import com.mygdx.game.MainMenuScreen;
import com.mygdx.game.MyGdxGame;

import hudElements.JumpButton;

/**
 * Created by hermann on 09.06.16.
 */
public class MultiMenu implements Screen {

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

    //Player
    Hero hero;
    float previousX;
    float previousY;

    //Pads
    hudElements.Joystick runningStick;
    hudElements.Joystick aimStick;
    JumpButton jumpButton;

    //Bodies fuer Menuepunkte
    Body hostButtonBody;
    Body joinButtonBody;
    Body backButtonBody;

    Texture button;
    Texture logo;
    BitmapFont font;


    public MultiMenu(MyGdxGame game,float x,float y){
        this.game = game;
        previousX=x;
        previousY=y;
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
        hero.b2body.setTransform(previousX,previousY,0);
        hero.setY(previousY);

        button =  new Texture("buttons/mmButton.png");
        logo = new Texture("fonts/dcLogo.png");
        Texture fontTexture = new Texture(Gdx.files.internal("fonts/futura.png"));
        fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font= new BitmapFont(Gdx.files.internal("fonts/futura.fnt"),new TextureRegion(fontTexture), false);
        font.getData().setScale((Gdx.graphics.getWidth())/((MyGdxGame.V_WIDTH)/1f));
        font.setColor(Color.WHITE);

        b2creator = new B2WorldCreator(world, map);

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

        //Menuepunkte als Box2D Bodies
        BodyDef buttonBodyDef = new BodyDef();
        buttonBodyDef.type = BodyDef.BodyType.StaticBody;

        //Highscores Button
        //40: Rest von der Wand, 36 Abstand zw. Buttons, 100: halbe Breite
        buttonBodyDef.position.set(new Vector2( (40+36+100)/MyGdxGame.PPM, 390/MyGdxGame.PPM));


        PolygonShape buttonShape = new PolygonShape();
        buttonShape.setAsBox(100/MyGdxGame.PPM, 30/MyGdxGame.PPM);

        //Host Button
        buttonBodyDef.position.set(new Vector2((40+(2*36)+(2*100))/MyGdxGame.PPM, 460/MyGdxGame.PPM));
        hostButtonBody = world.createBody(buttonBodyDef);
        Fixture singlePlayerButtonFixture = hostButtonBody.createFixture(buttonShape, 0.0f);
        singlePlayerButtonFixture.setUserData("host");


        //Join Button
        buttonBodyDef.position.set(new Vector2((40+(4*36)+(8*100))/MyGdxGame.PPM, 460/MyGdxGame.PPM));
        joinButtonBody = world.createBody(buttonBodyDef);
        Fixture multiPlayerButtonFixture = joinButtonBody.createFixture(buttonShape, 0.0f);
        multiPlayerButtonFixture.setUserData("join");
        
        //Back Button
        buttonBodyDef.position.set(new Vector2((40+(3*36)+(5*100))/MyGdxGame.PPM, 460/MyGdxGame.PPM));
        backButtonBody = world.createBody(buttonBodyDef);
        Fixture backButtonFixture = backButtonBody.createFixture(buttonShape, 0.0f);
        backButtonFixture.setUserData("back");

        //Labels
        Label.LabelStyle buttonFont = new Label.LabelStyle(font,Color.LIGHT_GRAY);



        Label hostLabel = new Label("Host Game",buttonFont);
        hostLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(hostButtonBody.getPosition().x*MyGdxGame.PPM-76)),
                Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(hostButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-76,480-15


        Label joinLabel = new Label("Join Game",buttonFont);
        joinLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(joinButtonBody.getPosition().x*MyGdxGame.PPM-72)),
                Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(joinButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-72,480-15

        Label backLabel = new Label("Main Menu",buttonFont);
        backLabel.setPosition(Gdx.graphics.getWidth()/(MyGdxGame.V_WIDTH/(backButtonBody.getPosition().x*MyGdxGame.PPM-76)),
                Gdx.graphics.getHeight()/(MyGdxGame.V_HEIGHT/(backButtonBody.getPosition().y*MyGdxGame.PPM-15)));//x-72,480-15


        hudStage.addActor(hostLabel);
        hudStage.addActor(joinLabel);
        hudStage.addActor(backLabel);
        //Cursor wird zum Crosshair
        //Quelle: http://addcomponent.com/lesson-2-create-first-person-weapon/
        Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_red.png"));
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
        pm.dispose();

    }


    public void update(float delta){
        handleInput(delta);


        world.step(1/60f, 1, 1);
        hero.update(delta);


        gamecam.update();
        renderer.setView(gamecam);
    }

    public void handleInput(float delta) {
        if(Application.ApplicationType.Desktop==Gdx.app.getType()) {

            //Sprung
            if ((Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && hero.getState() != Hero.State.JUMPING && hero.getState() != Hero.State.FALLING)
                hero.getBody().applyLinearImpulse(new Vector2(0, 5.5f), hero.getBody().getWorldCenter(), true);

            //Nach rechts laufen
            if (Gdx.input.isKeyPressed(Input.Keys.D) && hero.getBody().getLinearVelocity().x <= 3)
                hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), hero.getBody().getWorldCenter(), true);

            //Nach links laufen
            if (Gdx.input.isKeyPressed(Input.Keys.A) && hero.getBody().getLinearVelocity().x >= -3)
                hero.getBody().applyLinearImpulse(new Vector2(-0.15f, 0), hero.getBody().getWorldCenter(), true);

            //Bremst den Spieler aus wenn er sich nicht bewegt
            if (!Gdx.input.isKeyPressed(Input.Keys.D) && !Gdx.input.isKeyPressed(Input.Keys.A) && hero.getState() == Hero.State.RUNNING)
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

        batch.draw(logo, 300/MyGdxGame.PPM, 3.5f, logo.getWidth()/MyGdxGame.PPM,logo.getHeight()/MyGdxGame.PPM);
        batch.draw(button, hostButtonBody.getPosition().x-(100/MyGdxGame.PPM), hostButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
        batch.draw(button, joinButtonBody.getPosition().x-(100/MyGdxGame.PPM), joinButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);
        batch.draw(button, backButtonBody.getPosition().x-(100/MyGdxGame.PPM), joinButtonBody.getPosition().y-(30/MyGdxGame.PPM), 200/MyGdxGame.PPM, 60/MyGdxGame.PPM);

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

    //KollisionsController
    public class WorldContactListener implements ContactListener {

        // Contact listener for all fixtures in the world
        @Override
        public void beginContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            System.out.println("Contact between " + fixtureA.getUserData() + " and "
                    + fixtureB.getUserData());

            //Wenn Ball beteiligt -> Sicherstellen BulletFixture immer B
            if(fixtureA.getUserData()=="bullet")
            {
                Fixture temp = fixtureA;
                fixtureA = fixtureB;
                fixtureB = temp;
            }

            //Kollision von fixtureA mit B
            if(fixtureB.getUserData()=="bullet")
            {
                if(fixtureA.getUserData() == "host")
                {
                	game.playButtonClicked();
                    game.setScreen(new LevelSelect(game,true));
                }
                else if(fixtureA.getUserData() == "join")
                {
                	game.playButtonClicked();
                    game.setScreen(new JoinGameMenu(game));
                }
                else if(fixtureA.getUserData() == "back")
                {
                	game.playButtonClicked();
                    game.setScreen(new MainMenuScreen(game));
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
