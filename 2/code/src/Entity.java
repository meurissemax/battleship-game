/**
 * This class is used to represent an element of the grid.
 * An entity is identified by its 'id' (0 for water, > 0 for a ship).
 * The 'hit' attribute is used to know if the entity is hit or not.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.20
 */

public class Entity {
	private int id;
	private boolean hit;

	public Entity(int id) {
		this.id = id;
		hit = false;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public void setHit(boolean status) {
		hit = status;
	}

	public boolean getHit() {
		return hit;
	}
}
