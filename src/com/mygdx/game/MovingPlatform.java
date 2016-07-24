package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class MovingPlatform extends Sprite {

	
	World world;
	TiledMap map;
	Vector2 startPos;
	Vector2 endPos;
	
	public Body body;
	Fixture fixture;
	
	Vector2 velocity;
	boolean reverseDirection = false;
	boolean carrysHero = false;
	
	FixtureDef fdef;
	
	Hero hero;
	
	public MovingPlatform(Hero hero, World world, Vector2 startPos, Vector2 endPos, Vector2 velocity){
		
		this.hero = hero;
		this.world = world;
		this.startPos = startPos;
		this.endPos = endPos;
		
		this.velocity = velocity;
		
		
		BodyDef bdef = new BodyDef();
		fdef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		
		
		bdef.type = BodyDef.BodyType.KinematicBody;
		
		
		if(startPos.x > endPos.x){
			Vector2 temp;
			temp = startPos;
			startPos = endPos;
			endPos = temp;
			reverseDirection = true;
			bdef.position.set(endPos.x  / MyGdxGame.PPM, endPos.y / MyGdxGame.PPM);
		}
		
		else{
			bdef.position.set(startPos.x / MyGdxGame.PPM, startPos.y / MyGdxGame.PPM);
		}
		
		
		body = world.createBody(bdef);
		
		shape.setAsBox(75 / MyGdxGame.PPM, 10 / MyGdxGame.PPM);
		fdef.shape = shape;
		
		
		
		body.setLinearVelocity(velocity.x, velocity.y);
		
		
		fixture = body.createFixture(fdef);
		fixture.setFriction(0.2f);
		fixture.setUserData(this);
		
		
		
		setRegion(new Texture(Gdx.files.internal("tiles/movingplatform.png")));
		setBounds(0, 0, 150 / MyGdxGame.PPM, 20 / MyGdxGame.PPM);
		this.setPosition(0, 0);
		
		
		
	}
	
	
	public void update(float delta){
		
		setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
		
		
		if(carrysHero && hero.b2body.getLinearVelocity().x == this.body.getLinearVelocity().x){
			hero.standingOnPlatform(true);
			hero.walkingOnPlatform(false);
		}
		
		else if(carrysHero){
			hero.standingOnPlatform(false);
			hero.walkingOnPlatform(true);
		}
		

		if(startPos.x < endPos.x){
			if(body.getPosition().x * MyGdxGame.PPM >= endPos.x && !reverseDirection){
				velocity.x = velocity.x * (-1);
				velocity.y = velocity.y * (-1);
				
				if(carrysHero){
					hero.b2body.applyLinearImpulse(new Vector2(velocity.x * 2f, 0f), hero.b2body.getWorldCenter(), true);
				}
				
				
				body.setLinearVelocity(velocity.x, velocity.y);
				
				reverseDirection = true;
			}
			
			else if(body.getPosition().x * MyGdxGame.PPM <= startPos.x && reverseDirection){
				velocity.x = velocity.x * (-1);
				velocity.y = velocity.y * (-1);
				
				if(carrysHero){
					hero.b2body.applyLinearImpulse(new Vector2(velocity.x * 2f, 0f), hero.b2body.getWorldCenter(), true);
				}

				body.setLinearVelocity(velocity.x, velocity.y);

				reverseDirection = false;
			}
		}
		
		else{
			if(body.getPosition().x * MyGdxGame.PPM <= endPos.x && !reverseDirection){
				velocity.x = velocity.x * (-1);
				velocity.y = velocity.y * (-1);
				
				if(carrysHero){
					hero.b2body.applyLinearImpulse(new Vector2(velocity.x * 2f, 0f), hero.b2body.getWorldCenter(), true);
				}
				
				body.setLinearVelocity(velocity.x, velocity.y);
				reverseDirection = true;
			}
			
			else if(body.getPosition().x * MyGdxGame.PPM >= startPos.x && reverseDirection){
				velocity.x = velocity.x * (-1);
				velocity.y = velocity.y * (-1);
				
				if(carrysHero){
					hero.b2body.applyLinearImpulse(new Vector2(velocity.x * 2f, 0f), hero.b2body.getWorldCenter(), true);
				}

				body.setLinearVelocity(velocity.x, velocity.y);

				reverseDirection = false;
			}
		}
	}


	public void onHit() {
		carrysHero = true;
		hero.setMovingPlatform(this);
		Gdx.app.log("Plattform", "Contact");
	}


	public void onEnd() {
		carrysHero = false;
		hero.standingOnPlatform(false);
		hero.walkingOnPlatform(false);
		hero.setMovingPlatform(null);
		Gdx.app.log("Plattform", "End-Contact");
	}


}
