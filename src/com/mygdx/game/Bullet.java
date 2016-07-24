package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class Bullet extends Sprite{

	Body body;
	int speed;
	Hero hero;
	
	int damage;
	boolean hitObject;
	
	public Bullet(Hero hero, float mouseX, float mouseY, int size){
		
		super(new Texture(Gdx.files.internal("sprites/bullet.png")));
		setBounds(0, 0, 15 / MyGdxGame.PPM, 15 / MyGdxGame.PPM);
		
		
		BodyDef bdef = new BodyDef();
		
		if(hero.runningRight)
		bdef.position.set(hero.getBody().getPosition().x + 14 / MyGdxGame.PPM, hero.getBody().getPosition().y + 6 / MyGdxGame.PPM);
		
		else
		bdef.position.set(hero.getBody().getPosition().x - 14 / MyGdxGame.PPM, hero.getBody().getPosition().y + 6 / MyGdxGame.PPM);
			
		
		
		
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = hero.getWorld().createBody(bdef);
		body.setGravityScale(0);
		body.setUserData(this);

		damage = 15;
		hitObject = false;
		
		FixtureDef fdef = new FixtureDef();
		
		//isSensor damit Projektile nicht an Waenden abprallen
		fdef.isSensor = true;
		CircleShape shape = new CircleShape();
		shape.setRadius(size / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("bullet");
		
		this.hero = hero;
		
		//Trigonometrie Kram, Winkel zum Mauszeiger ausrechnen
		double angle;
		
		if(hero.runningRight)
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x + 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));
		
		else
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x - 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));

		
		speed = 4;

		//System.out.println(mouseX + " " + mouseY  + " " + hero.b2body.getPosition().x + " " + hero.b2body.getPosition().y);
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
	}
	
	public Bullet(Hero hero, float mouseX, float mouseY, int size, double extraAngle){
		
		super(new Texture(Gdx.files.internal("sprites/bullet.png")));
		setBounds(0, 0, 15 / MyGdxGame.PPM, 15 / MyGdxGame.PPM);
		
		
		BodyDef bdef = new BodyDef();
		
		if(hero.runningRight)
		bdef.position.set(hero.getBody().getPosition().x + 14 / MyGdxGame.PPM, hero.getBody().getPosition().y + 6 / MyGdxGame.PPM);
		
		else
		bdef.position.set(hero.getBody().getPosition().x - 14 / MyGdxGame.PPM, hero.getBody().getPosition().y + 6 / MyGdxGame.PPM);
		
		bdef.type = BodyDef.BodyType.DynamicBody;
		body = hero.getWorld().createBody(bdef);
		body.setGravityScale(0);
		body.setUserData(this);

		damage = 15;
		hitObject = false;
		
		FixtureDef fdef = new FixtureDef();
		
		//isSensor damit Projektile nicht an Waenden abprallen
		fdef.isSensor = true;
		CircleShape shape = new CircleShape();
		shape.setRadius(size / MyGdxGame.PPM);
		
		
		fdef.shape = shape;
		body.createFixture(fdef).setUserData("bullet");
		
		this.hero = hero;
		double angle;
		
		//Trigonometrie Kram, Winkel zum Mauszeiger ausrechnen
		if(hero.runningRight)
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x + 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM)) + extraAngle;
		
		else
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x - 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM)) + extraAngle;

		speed = 4;

		//System.out.println(mouseX + " " + mouseY  + " " + hero.b2body.getPosition().x + " " + hero.b2body.getPosition().y);
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
	}
	
	//Setzt die Kugel neu (um nicht staendig neue Kugeln erzeugen zu muessen)
	public void setNew(float mouseX, float mouseY){
		double angle;
		
		if(hero.runningRight)
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x + 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));
		
		else
		angle = Math.atan2(mouseX - (hero.getBody().getPosition().x - 14 / MyGdxGame.PPM), mouseY - (hero.getBody().getPosition().y + 6 / MyGdxGame.PPM));

		
		if(hero.runningRight)
		body.setTransform(hero.b2body.getPosition().x + 14 / MyGdxGame.PPM, hero.b2body.getPosition().y + 6 / MyGdxGame.PPM, (int) (angle + 0.5));
		
		else
		body.setTransform(hero.b2body.getPosition().x - 14 / MyGdxGame.PPM, hero.b2body.getPosition().y + 6 / MyGdxGame.PPM, (int) (angle + 0.5));
			
		body.setLinearVelocity(new Vector2((float) (speed * Math.sin(angle)), (float) (speed * Math.cos(angle))));
		
		hitObject = false;
	}
	
	public void setNew(float mouseX, float mouseY, double extraAngle){
		double angle = Math.atan2(mouseX - hero.b2body.getPosition().x, mouseY - hero.b2body.getPosition().y) + extraAngle;
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
