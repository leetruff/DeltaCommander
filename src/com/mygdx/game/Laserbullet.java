package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;


public class Laserbullet extends Sprite{

	Body body;
	int speed;
	Hero hero;
	
	int damage;
	boolean hitObject;
	
	public Laserbullet(Hero hero, float mouseX, float mouseY){
		
		super(new Texture(Gdx.files.internal("sprites/lasershot.png")));
		setBounds(0, 0, 23 / MyGdxGame.PPM, 15 / MyGdxGame.PPM);
		
		
		BodyDef bdef = new BodyDef();

		
		if(hero.runningRight)
		bdef.position.set(hero.getBody().getPosition().x + 14 / MyGdxGame.PPM, hero.getBody().getPosition().y + 6 / MyGdxGame.PPM);
			
		else
		bdef.position.set(hero.getBody().getPosition().x - 14 / MyGdxGame.PPM, hero.getBody().getPosition().y + 6 / MyGdxGame.PPM);
		
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = hero.getWorld().createBody(bdef);
		body.setGravityScale(0);
		body.setUserData(this);

		damage = 25;
		hitObject = false;
		
		FixtureDef fdef = new FixtureDef();
		
		//isSensor damit Projektile nicht an Waenden abprallen
		fdef.isSensor = true;
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(15 / MyGdxGame.PPM / 2, 23 / MyGdxGame.PPM / 2);
		
		
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("laserbullet");
		
		this.hero = hero;
		
		//Trigonometrie Kram, Winkel zum Mauszeiger ausrechnen
		double angle;
		
		if(hero.runningRight)
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x + 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));
		
		else
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x - 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));

		
		speed = 8;

		
		body.setTransform(body.getPosition().x, body.getPosition().y, (float) -angle); //Body drehen
		this.setOrigin(this.getWidth() / 2, this.getHeight() / 2);
		this.setRotation((float) (body.getAngle() * (180/Math.PI) - 90)); //Sprite drehen
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
	}
	
	//Setzt die Kugel neu (um nicht staendig neue Kugeln erzeugen zu muessen)
	public void setNew(float mouseX, float mouseY){
		double angle;
		
		if(hero.runningRight)
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x + 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));
		
		else
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x - 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));

		
		body.setTransform(hero.b2body.getPosition().x, hero.b2body.getPosition().y, (int) (angle + 0.5));
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
		
		hitObject = false;
	}

	public void update(float delta) {
		setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
	}
	
	public Body getBody()
	{
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public int getDamage() {
		return damage;
	}

	public boolean isHitObject() {
		return hitObject;
	}

	public void setHitObject(boolean hitObject) {
		this.hitObject = hitObject;
	}
	
	
	
}
