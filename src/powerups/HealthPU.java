package powerups;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.GameScreen;
import com.mygdx.game.MyGdxGame;

/**
 * PowerUp, das in der World platziert werden kann. <br>
 * Der Spieler erhaelt beim aufsammeln 25HP zurueck.
 * 
 * @author Tim
 */
public class HealthPU extends Sprite implements PickUp{

	
	World world;
	public Body b2body;
	
	FixtureDef fdef;
	
	static Texture defaultTexture = new Texture(Gdx.files.internal("sprites/healthPU.png"));
    
    boolean collected;
    
    /**
     * Konstruktor fuer die Erstellung eines Health-PowerUp Objekts
     * 
     * @param world Die Box2D Welt in der der Body des PowerUps platziert wird
     * @param xPos Die x-Koordinate des PowerUps
     * @param yPos Die y-Koordinate des PowerUps
     */
	public HealthPU(World world, float xPos, float yPos){
		
		super(defaultTexture);
		
		this.world = world;
		defineBody(xPos, yPos);
		
		setBounds(0, 0, 28 / MyGdxGame.PPM, 28 / MyGdxGame.PPM);
		this.setPosition(0, 0);
		
		collected = false;
	}
	

	/**
	 * Position fuer das Zeichnen des Sprites an die Position des Bodys anpassen
	 */
	public void update(){	
		
		setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);

	}
	
	
	/**
	 * Erstellt den Box2D Body und die zugehoerige Fixture fuer das PowerUp
	 * @param xPos Die x-Koordinate des Bodys
     * @param yPos Die y-Koordinate des Bodys
	 */
	public void defineBody(float xPos, float yPos) {
		BodyDef bdef = new BodyDef();
		bdef.position.set(xPos / MyGdxGame.PPM, yPos / MyGdxGame.PPM);
		bdef.type = BodyDef.BodyType.StaticBody;
		b2body = world.createBody(bdef);
		
		fdef = new FixtureDef();
		fdef.isSensor = true;
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(14 / MyGdxGame.PPM, 14 / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		b2body.setUserData(this);
		b2body.createFixture(fdef).setUserData("health");
	}

	
	
	public boolean isCollected() {
		return collected;
	}


	public void setCollected(boolean collected) {
		this.collected = collected;
	}


	public World getWorld(){
		return world;
	}
	

	public Body getBody(){
		return this.b2body;
	}


}


