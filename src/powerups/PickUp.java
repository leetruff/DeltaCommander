package powerups;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Interface fuer die verschiedenen PowerUps <br>
 * Erstellt um die PowerUps im GameScreen in einer Liste verwalten zu koennen
 * 
 * @author Tim
 */
public interface PickUp{

	 public World getWorld();
	 public Body getBody();
	 
	 /**
	  * Position fuer das Zeichnen des Sprites an die Position des Bodys anpassen
	  */
	 public void update();
	 
	 /**
	  * Erstellt den Box2D Body und die zugehoerige Fixture fuer das PowerUp
	  * @param xPos Die x-Koordinate des Bodys
	  * @param yPos Die y-Koordinate des Bodys
	  */
	 public void defineBody(float xPos, float yPos);
	 public boolean isCollected();
}
