import java.util.Random;

/**
 * This class implements the game grid. The grid is square of entity, of constant dimension.
 *
 * @author Maxime MEURISSE (m.meurisse@student.uliege.be)
 * @version 3/27/2019
 */
public class Grid {
	private int size;
	private Entity[] grid;

	/**
	 * The constructor initializes the grid with entities having an id equal to 0 (that is to say an empty grid, without any ship).
	 *
	 * @param size The size of the grid (length of one side of the square)
	 */
	public Grid(int size) {
		this.size = size;
		grid = new Entity[size * size];

		for(int i = 0; i < grid.length; i++)
			grid[i] = new Entity(0);
	}

	/**
	 * This function is used to display the grid, indicating the coordinate system used.
	 * An empty slot is represented by "-" and a ship is represented by its id.
	 *
	 * N.B.: This function only display the positions of each ship, not their status.
	 */
	public void display() {
		int id, count = 0, line = 0, limit = size;

		System.out.println("    0 1 2 3 4 5 6 7 8 9");
		System.out.println("    -------------------");

		for(int i = 0; i < grid.length; i++) {
			count++;
			id = grid[i].getID();

			if(i % size == 0)
				System.out.print(line++ + " | ");

			System.out.print(((id == 0) ? "-" : id) + " ");

			if(count == limit) {
				count = 0;

				System.out.println();
			}
		}
	}

	/**
	 * This function allows to place a ship on the grid.
	 * It replaces the id of the entities concerned by the ship's id.
	 *
	 * @param id The id of the ship
	 * @param size The size of the ship (in order to find out how many free consecutive positions have to be found)
	 */
	public void addShip(int id, int size) {
		int dir = new Random().nextInt(2);
		int[] pos;

		if(dir == 0)
			pos = genPos(size, true);
		else
			pos = genPos(size, false);

		for(int i = 0; i < pos.length; i++)
			grid[pos[i]].setID(id);

	}

	/**
	 * This function returns a boolean indicating whether a ship is still in play or is sinking.
	 *
	 * @param id The id of the ship
	 */
	public boolean shipAlive(int id) {
		int count = 0, size = 0;

		for(int i = 0; i < grid.length; i++) {
			if(grid[i].getID() == id) {
				size++;

				if(grid[i].getHit()) {
					count++;
				}
			}
		}

		if(count == size)
			return false;
		else
			return true;
	}

	/**
	 * This function is used to display the status of a ship:
	 * 	- its positions
	 * 	- its positions attacked
	 * 	- its state (alive or sunk)
	 *
	 * @param id The id of the ship
	 */
	public void shipStatus(int id) {
		int count = 0;
		String status, loc, hit;

		status = "Ship " + id + " : ";
		loc = "located at ";
		hit = "hit at ";

		for(int i = 0; i < grid.length; i++) {
			if(grid[i].getID() == id) {
				loc += i + " ";

				if(grid[i].getHit()) {
					count++;
					hit += i + " ";
				}
			}
		}

		status += loc;

		if(!shipAlive(id))
			status += "- down";
		else if(count > 0)
			status += "- " + hit;

		System.out.println(status);
	}

	/**
	 * This function is used to attack a grid position (change the entity's hit attribute to true).
	 * It displays the affected ship, or indicates if no ship has been touched (or if the position has already been attacked).
	 *
	 * @param pos The position (in [0, grid.length]) to hit
	 *
	 * @return The id of the entity attacked (0 for empty slot, > 0 for a ship)
	 */
	public int hitPos(int pos) {
		int id = grid[pos].getID();

		if(grid[pos].getHit()) {
			System.out.println("Position already attacked.");
		} else {
			grid[pos].setHit(true);

			if(id == 0) {
				System.out.println("No ship was hit.");
			} else {
				System.out.println("Ship " + id + " hit.");

				if(!shipAlive(id))
					System.out.println("Ship " + id + " down !");
			}
		}

		return id;
	}

	/**
	 * This function makes it possible to obtain the list of the positions already attacked, with after each position, the id of the entity at this position.
	 * The first item in the list is the number of attacked positions.
	 *
	 * @return The list of position attacked
	 */
	public int[] getList() {
		int j = 0, count = getNumberHit();
		int list[] = new int[1 + count * 2];

		list[j++] = count;

		for(int i = 0; i < grid.length; i++)
			if(grid[i].getHit()) {
				list[j++] = i;
				list[j++] = grid[i].getID();
			}

		return list;
	}

	/**
	 * This function makes it possible to obtain the number of positions on the grid already attacked.
	 *
	 * @return The number of position already attacked
	 */
	private int getNumberHit() {
		int count = 0;

		for(int i = 0; i < grid.length; i++)
			if(grid[i].getHit())
				count++;

		return count;
	}

	/**
	 * This function generates a number of consecutive free positions on the grid equal to the size of the ship.
	 *
	 * @param size The size of the ship, ie the number of positions to find
	 * @param dir A boolean indicating whether the ship should be placed horizontally or not.
	 *
	 * @return An array containing the positions generated
	 */
	private int[] genPos(int size, boolean dir) {
		int pos, limit, i, j;
		int[] find = new int[size];
		boolean valid = false;

		while(!valid) {
			if(dir)
				pos = new Random().nextInt(grid.length - size);
			else
				pos = new Random().nextInt(grid.length - (size * this.size));

			while(grid[pos].getID() != 0)
				if(dir)
					pos = new Random().nextInt(grid.length - size);
				else
					pos = new Random().nextInt(grid.length - (size * this.size));

			valid = true;
			i = pos;
			j = 0;

			if(dir)
				limit = size;
			else
				limit = size * this.size;

			while(i < pos + limit) {
				if(dir)
					if((i - (i % this.size)) - ((i - 1) - ((i - 1) % this.size)) == this.size)
						valid = false;

				if(i > grid.length)
					valid = false;
				else
					if(grid[i].getID() == 0)
						find[j++] = i;
					else
						valid = false;

				if(dir)
					i++;
				else
					i += this.size;
			}
		}

		return find;
	}
}
