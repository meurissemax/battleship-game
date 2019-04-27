import java.util.Random;

/**
 * This class is used to implement the game grid.
 * A grid is an array of 'Entity'.
 * Positions on the grid are represented by a value between 0 and ('size' * 'size' - 1).
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.27
 */

public class Grid {
	private Entity[] grid;

	public Grid(int size) {
		grid = new Entity[size * size];

		for(int i = 0; i < grid.length; i++)
			grid[i] = new Entity(0); /// by default, all the grid is water
	}

	/**
	 * This function is used to add a ship randomly on the grid.
	 *
	 * @param id the id of the ship
	 * @param size the size of the ship
	 */
	public void addShip(int id, int size) {
		int dir = new Random().nextInt(2);
		int[] pos;

		if(dir == 0)
			pos = genPos(size, true); /// horizontal
		else
			pos = genPos(size, false); /// vertical

		for(int i = 0; i < pos.length; i++)
			grid[pos[i]].setID(id);

	}

	/**
	 * This function is used to print the grid in the console log.
	 * The grid will always be well displayed, regardless of its size and its axis names.
	 */
	public void display() {
		int id, maxY = -1, maxX = -1, count = 0, line = 0, limit = GameConstants.GRID_SIZE;
		String spaces = "", xaxis = "", separator = "";

		for(int i = 0; i < GameConstants.GRID_SIZE; i++) {
			if(GameConstants.YAXIS[i].length() > maxY)
				maxY = GameConstants.YAXIS[i].length();

			if(GameConstants.XAXIS[i].length() > maxX)
				maxX = GameConstants.XAXIS[i].length();
		}

		for(int i = 0; i < GameConstants.GRID_SIZE; i++) {
			xaxis += String.format("%" + maxX + "s", GameConstants.XAXIS[i]) + " ";
			separator += String.format("%" + maxX + "s", "•") + " ";
		}

		for(int i = 0; i < maxY + 3; i++)
			spaces += " ";

		System.out.println(spaces + xaxis);
		System.out.println(spaces + separator);

		for(int i = 0; i < grid.length; i++) {
			count++;
			id = grid[i].getID();

			if(i % GameConstants.GRID_SIZE == 0)
				System.out.print(String.format("%" + maxY + "s", GameConstants.YAXIS[line++]) + " • ");

			System.out.print(((id == 0) ? String.format("%" + maxX + "s", "-") : String.format("%" + maxX + "s", id)) + " ");

			if(count == limit) {
				count = 0;

				System.out.println();
			}
		}
	}

	/**
	 * This function is used to know if a ship is still alive on the grid.
	 *
	 * @param id the id of the ship
	 *
	 * @return a boolean value indicating if the ship is still alive or not
	 */
	public boolean shipAlive(int id) {
		int size = 0, hit = 0;

		for(int i = 0; i < grid.length; i++) {
			if(grid[i].getID() == id) {
				size++;

				if(grid[i].getHit())
					hit++;
			}
		}

		if(hit == size)
			return false;
		else
			return true;
	}

	/**
	 * This function is used to hit a position on the grid.
	 * It returns -1 if the position was already hit, else it returns the id of the position (0 for water, > 0 for a ship).
	 *
	 * @param pos the position to hit
	 *
	 * @return an integer indicating the status of the hit
	 */
	public int hitPos(int pos) {
		if(grid[pos].getHit()) {
			return -1;
		} else {
			grid[pos].setHit(true);

			return grid[pos].getID();
		}
	}

	public int getID(int pos) {
		return grid[pos].getID();
	}

	public boolean getHit(int pos) {
		return grid[pos].getHit();
	}

	/**
	 * This function is used to generate an array of valid positions in the grid of size 'size'.
	 * Positions are horizontaly or verticaly directed depending of the variable 'dir'.
	 *
	 * @param size the size of the ships, i.e. the number of consecutive valid positions in the grid to generate
	 * @param dir a boolean value indicating if positions are horizontaly directed or not
	 *
	 * @return an array of consecutive valid positions in the grid
	 */
	private int[] genPos(int size, boolean dir) {
		int pos, limit, i, j, tmp;
		int[] find = new int[size];
		boolean valid = false;

		while(!valid) {
			/// we find a free position 'pos' in the grid
			do {
				tmp = (dir) ? (grid.length - size) : (grid.length - (size * GameConstants.GRID_SIZE));
				pos = new Random().nextInt(tmp);
			} while(grid[pos].getID() != 0);

			valid = true;
			i = pos;
			j = 0;

			limit = (dir) ? size : size * GameConstants.GRID_SIZE;

			/// we check if the ('size' - 1) position around 'pos' are valid
			while(i < pos + limit) {
				if(dir)
					if((i - (i % GameConstants.GRID_SIZE)) - ((i - 1) - ((i - 1) % GameConstants.GRID_SIZE)) == GameConstants.GRID_SIZE)
						valid = false;

				if(i > grid.length)
					valid = false;
				else
					if(grid[i].getID() == 0)
						find[j++] = i;
					else
						valid = false;

				i = (dir) ? i + 1 : i + GameConstants.GRID_SIZE;
			}
		}

		return find;
	}
}
