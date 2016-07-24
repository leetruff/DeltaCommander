package com.mygdx.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import javax.swing.JOptionPane;

import network.BigCorrectionMessage;
import network.CorrectionMessage;
import network.JoinHero;
import network.PositionMessage;
import network.ShootingMessage;

/**
 * Created by hermann on 30.06.16.
 */
public class Level1Multi extends Level1 {


    JoinHero joinHero;
    float hovering;
    boolean hoveringUp;
    //Network
    Server server;
    Connection connection;
    Client client;
    long startTime;
    long endTime;
    long correctionStart;
    long correctionEnd;
    boolean correction;
    float corrX;
    float corrY;
    long bigcorrectionStart;
    long bigcorrectionEnd;
    boolean opponentShot;
    float shotX;
    float shotY;
    boolean isServer;
    boolean restart;
    boolean over;


    public Level1Multi(MyGdxGame game, Server server, Connection con) {
        super(game);
        this.server = server;
        connection = con;
        isServer = true;
    }

    public Level1Multi(MyGdxGame game, Client client) {
        super(game);
        this.client = client;
        isServer = false;

    }

    @Override
    public void show() {

        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
        over = false;
        super.show();
        joinHero = new JoinHero(super.game, super.world);
        if (isServer) {
            //Network Listener Server
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    server.addListener(new Listener() {
                        public void received(Connection connection, Object object) {
                            if (object instanceof ShootingMessage) {
                                ShootingMessage response = (ShootingMessage) object;
                                opponentShot = true;
                                shotX = response.getX();
                                shotY = response.getY();
                            }
                            if (object instanceof PositionMessage) {
                                PositionMessage response = (PositionMessage) object;
                                if (response.getPosY() == -2 && response.getPosX() == -2) {
                                    if(Gdx.app.getType()== Application.ApplicationType.Desktop) {
                                        JOptionPane.showMessageDialog(null, "Your Partner quit.");
                                    }
                                    game.setScreen(new MainMenuScreen(game));
                                }
                                else if(response.getPosY()==-3&&response.getPosX()==-3)
                                {
                                    restart=true;
                                }
                            }
                        }
                    });
                }
            });

            startTime = System.currentTimeMillis();
            correctionStart = System.currentTimeMillis();bigcorrectionStart=System.currentTimeMillis();
        } else {
            hovering = 0;
            //Network Listener Client
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    client.addListener(new Listener() {
                        public void received(Connection connection, Object object) {
                            if (object instanceof PositionMessage) {
                                PositionMessage response = (PositionMessage) object;
                                if(response.getPosY()==-2&&response.getPosX()==-2)
                                {
                                    client.close();
                                    game.setScreen(new MainMenuScreen(game));
                                }
                                else if(response.getPosY()==-3&&response.getPosX()==-3)
                                {
                                    restart=true;
                                }
                                hero.b2body.setLinearVelocity(response.getPosX(), response.getPosY());
                                hero.previousState = hero.currentState;
                                hero.currentState = Hero.State.valueOf(response.getState());

                            }
                            if (object instanceof ShootingMessage) {
                                ShootingMessage response = (ShootingMessage) object;
                                opponentShot = true;
                                shotX = response.getX();
                                shotY = response.getY();
                                game.playShoot();
                            }
                            if (object instanceof CorrectionMessage) {
                                CorrectionMessage response = (CorrectionMessage) object;
                                corrX= response.getPosX();
                                corrY=response.getPosY();
                                correction=true;
                            }
                            if (object instanceof BigCorrectionMessage) {
                                BigCorrectionMessage response = (BigCorrectionMessage) object;
                                corrX= response.getPosX();
                                corrY=response.getPosY();
                                correction=true;
                                hero.setHealth(response.getHealth());
                                hero.setLives(response.getLifes());
                                over = (response.getLifes()>0)? false:true;
                            }
                        }
                    });
                }
            });
        }
        restart=false;
    }

    @Override
    public void update(float delta) {
        if (opponentShot) {
            if (isServer) {
                joinHero.forceShoot(shotX, shotY, Gdx.graphics.getDeltaTime());
                opponentShot = false;
            } else {
                super.hero.forceShoot(shotX, shotY, Gdx.graphics.getDeltaTime());
                opponentShot = false;
            }
        }
        if(correction)
        {
            hero.b2body.setTransform(corrX, corrY, 0);
            correction=false;

        }
        super.update(delta);

        if (!paused) {

            for (int i = 0; i < joinHero.getBulletList().size(); i++) {
                joinHero.getBulletList().get(i).update(delta);
            }

        }

    }
    @Override
    public void handleInput(float delta) {
        if(isServer) {
            if (!super.gameOver&&!super.levelCompleted){
                if (Application.ApplicationType.Desktop == Gdx.app.getType()) {

                    if (!super.paused) {

                        //Sprung
                        if ((Gdx.input.isKeyJustPressed(Keys.W) || Gdx.input.isKeyJustPressed(Keys.SPACE)) && super.hero.getState() != Hero.State.JUMPING && super.hero.getState() != Hero.State.FALLING)
                            super.hero.getBody().applyLinearImpulse(new Vector2(0, 5.5f), super.hero.getBody().getWorldCenter(), true);

                        //Nach rechts laufen
                        if (Gdx.input.isKeyPressed(Keys.D) && super.hero.getBody().getLinearVelocity().x <= 3) {
                            super.hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), super.hero.getBody().getWorldCenter(), true);

                            if (super.hero.getBody().getLinearVelocity().x < 0) {
                                //hero.getBody().setLinearVelocity(0, hero.getBody().getLinearVelocity().y);
                                super.hero.getBody().applyLinearImpulse(new Vector2(0.15f, 0), super.hero.getBody().getWorldCenter(), true);
                            }
                        }

                        //Nach links laufen
                        if (Gdx.input.isKeyPressed(Keys.A) && super.hero.getBody().getLinearVelocity().x >= -3) {
                            super.hero.getBody().applyLinearImpulse(new Vector2(-0.15f, 0), hero.getBody().getWorldCenter(), true);

                            if (super.hero.getBody().getLinearVelocity().x > 0) {
                                //hero.getBody().setLinearVelocity(0, hero.getBody().getLinearVelocity().y);
                                super.hero.getBody().applyLinearImpulse(new Vector2(-0.15f, 0), super.hero.getBody().getWorldCenter(), true);
                            }
                        }

                        //Bremst den Spieler aus wenn er sich nicht bewegt
                        if (!Gdx.input.isKeyPressed(Keys.D) && !Gdx.input.isKeyPressed(Keys.A) && super.hero.getState() == Hero.State.RUNNING)
                            super.hero.getBody().applyLinearImpulse(new Vector2(super.hero.getBody().getLinearVelocity().x / 40 * (-1), 0), super.hero.getBody().getWorldCenter(), true);

                        //Schiessen in Richtung Mausposition bei Mausklick
                        if (Gdx.input.isTouched()) {

                            super.hero.multiplayerShoot(gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).x, gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).y, delta,connection);


                        }
                        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                            System.out.println("X: " + super.hero.b2body.getPosition().x + " Y: " + super.hero.b2body.getPosition().y);

                        }
                    }

                    //Pausieren
    				if(Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.MENU) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)){

                        if (super.paused) {
                            Gdx.input.setInputProcessor(hudStage);
                            super.pauseMenu.disableButtons();
                        } else {
                            Gdx.input.setInputProcessor(super.pauseStage);
                            super.pauseMenu.enableButtons();
                        }

                        super.paused = !super.paused;


                    }
                } else {
                    if (!super.paused) {

                        //Android Steuerung
                        if (super.runningStick.touchpad.isTouched()) {
                            if (super.runningStick.touchpad.getKnobPercentX() > 0.3 && super.hero.b2body.getLinearVelocity().x <= 3) {
                                super.hero.b2body.applyLinearImpulse(new Vector2(0.15f, 0), super.hero.b2body.getWorldCenter(), true);
                            } else if (super.runningStick.touchpad.getKnobPercentX() < -0.3 && super.hero.b2body.getLinearVelocity().x >= -3) {
                                super.hero.b2body.applyLinearImpulse(new Vector2(-0.15f, 0), super.hero.b2body.getWorldCenter(), true);
                            }
                        }
                        if (super.aimStick.touchpad.isTouched() && (Math.abs(super.aimStick.touchpad.getKnobPercentX()) > 0.2 || Math.abs(super.aimStick.touchpad.getKnobPercentY()) > 0.2)) {
                            super.hero.multiplayerShoot((super.aimStick.touchpad.getKnobPercentX() > 0) ? (100f - Math.abs(super.aimStick.touchpad.getKnobPercentY() * 100)) : -(100f - Math.abs(super.aimStick.touchpad.getKnobPercentY() * 100)), ((super.aimStick.touchpad.getKnobPercentY() > 0) ? Math.abs(super.aimStick.touchpad.getKnobPercentY()) * 30 : Math.abs(super.aimStick.touchpad.getKnobPercentY()) * -30), delta,connection);
                        }
                    }
                    //Pausieren
                    if (Gdx.input.isKeyJustPressed(Keys.BACK)) {

                        if (super.paused) {
                            Gdx.input.setInputProcessor(super.hudStage);
                            super.pauseMenu.disableButtons();
                        } else {
                            Gdx.input.setInputProcessor(super.pauseStage);
                            super.pauseMenu.enableButtons();
                        }

                        super.paused = !super.paused;


                    }
                }
            }
            else{
                if(super.gameOver) {
                    if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
                        if(isServer)super.gameOverMenu.retry.setY(Gdx.graphics.getHeight() / 2 - 75);
                        super.gameOverMenu.setLabelY(Gdx.graphics.getHeight() / 2 + 75);
                    }
                    Gdx.input.setInputProcessor(super.gameOverStage);
                }

            }
        }
        else
        {
            if(!super.paused){
                //Schiessen in Richtung Mausposition bei Mausklick
                if (Gdx.input.isTouched()) {
                    joinHero.multiplayerShoot(gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).x, gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).y, delta,client);
                }
            }
            //Pausieren
            if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)|| Gdx.input.isKeyJustPressed(Keys.BACK)){

                if(super.paused){
                    Gdx.input.setInputProcessor(super.hudStage);
                    super.pauseMenu.disableButtons();
                }
                else{
                    Gdx.input.setInputProcessor(super.pauseStage);
                    super.pauseMenu.enableButtons();
                }

                super.paused = !super.paused;


            }
        }

    }
    @Override
    public void render(float delta)
    {

        if(!isServer)
        {
            super.gameOver=over;
            if(over)
            {
                super.gameOverMenu.retry.setY(Gdx.graphics.getHeight()+100);
            }
        }

        super.render(delta);

        //Join-Hero Position aktualisieren
        if(hero.getY()<2) {
            joinHero.setPosition(hero.getX(),3+hovering);
            hovering = (hoveringUp)? hovering+0.002f:hovering-0.002f;
            hoveringUp = (Math.abs(hovering)>=0.1)? !hoveringUp:hoveringUp;
        }
        else{
            joinHero.setPosition((hero.getY()<3)? hero.getX()-(hero.getY()-2): hero.getX()-1,(hero.getY()<2.5)? 3+hovering: hero.getY()+0.5f+hovering);
            hovering = (hoveringUp)? hovering+0.002f:hovering-0.002f;
            hoveringUp = (Math.abs(hovering)>=0.1)? !hoveringUp:hoveringUp;
        }
        joinHero.b2body.setTransform(joinHero.getX()+joinHero.getWidth()/2,joinHero.getY()+joinHero.getHeight()/2,0);

        if(restart)
        {
            super.gameOverMenu.disable();
            super.game.setScreen(new Level1Multi(super.game,client));
        }
        super.batch.setProjectionMatrix(super.gamecam.combined);
        super.batch.begin();
        if(!super.paused&&!super.gameOver) {
            joinHero.draw(super.batch);
        }
        for(int i=0;i<joinHero.getBulletList().size();i++)
        {
            if(!joinHero.getBulletList().get(i).isHitObject()) {
                joinHero.getBulletList().get(i).draw(super.batch);
            }
        }
        super.batch.end();



        //Server sendet in diesem Bereich die Nachrichten
        if(isServer)
        {
            endTime=System.currentTimeMillis();
            if(endTime-startTime>0.1) {
                connection.sendTCP(new PositionMessage(hero.b2body.getLinearVelocity().x,hero.b2body.getLinearVelocity().y,hero.currentState));
                startTime=System.currentTimeMillis();
            }
            correctionEnd=System.currentTimeMillis();
            if(correctionEnd-correctionStart>0.5)
            {
                connection.sendTCP(new CorrectionMessage(hero.b2body.getPosition().x,hero.b2body.getPosition().y));
                correctionStart = System.currentTimeMillis();
            }
            bigcorrectionEnd=System.currentTimeMillis();
            if(bigcorrectionEnd-bigcorrectionStart>400)
            {
                connection.sendTCP(new BigCorrectionMessage(hero.b2body.getPosition().x,hero.b2body.getPosition().y,hero.getLives(),hero.getHealth(),-1) );
                bigcorrectionStart=System.currentTimeMillis();

            }
        }




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
        shape.dispose();
        batch.dispose();
        gameOverStage.dispose();

    }
    @Override
    public void stageCompleted(){
        if(levelTime < timeToBeat){
            hero.setScore((int) (hero.getScore() + timeFactor * (timeToBeat - levelTime)));
        }
        XMLInteraction.saveScore("level1", game.getUsername(), hero.getScore());
        hero.updateSavedScore();
        if(isServer) {
            game.setScreen(new Level2Multi(game, hero.getHealth(), hero.getLives(), hero.getAmmo(), hero.getScore(), hero.getLaserAmmo(), hero.getTripleAmmo(),server,connection));
        }
        else
        {
            game.setScreen(new Level2Multi(game, hero.getHealth(), hero.getLives(), hero.getAmmo(), hero.getScore(), hero.getLaserAmmo(), hero.getTripleAmmo(),client));

        }
    }
    @Override
    public void restart()
    {
        if(isServer)
        {
            connection.sendTCP(new PositionMessage(-3,-3, Hero.State.FALLING));
            super.game.setScreen(new Level1Multi(super.game,server,connection));
        }

    }


}
