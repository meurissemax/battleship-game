/**
 * This class implements an entity, that is, an element that can represent a ship or an empty slot.
 * An empty slot is represented by an id of 0. A ship is represented by an id > 0.
 * The hit attribute allows you to know if the position has already been attacked or not.
 *
 * @author Maxime MEURISSE (m.meurisse@student.uliege.be)
 * @version 3/27/2019
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
