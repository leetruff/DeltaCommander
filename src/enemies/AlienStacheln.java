package enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.MyGdxGame;

public class AlienStacheln extends Sprite {

	BodyDef bdef = new BodyDef();
	PolygonShape shape = new PolygonShape();
	FixtureDef fdef = new FixtureDef();
	Body body;
	
	static Texture defaultTexture = new Texture(Gdx.files.internal("tiles/SciFiTiles/Spike.png"));
	//bla
	public AlienStacheln(float xPos, float yPos, World world){
		
		super(defaultTexture);	
		
		bdef.type = BodyDef.BodyType.StaticBody;
		bdef.position.set(xPos, yPos);
		
		body = world.createBody(bdef);
		
		shape.setAsBox(32 / MyGdxGame.PPM, 22 / MyGdxGame.PPM);
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("alienstacheln");
		
		setBounds(0, 0, 64 / MyGdxGame.PPM, 64 / MyGdxGame.PPM);
		this.setPosition(0, 0);
	}
	
	public void update(){	
		setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 + 0.07f);
	}
}
