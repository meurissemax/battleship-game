/**
 * This class allows you to create a game and interact with the game
 * 	- create a new grid
 * 	- attack a position
 * 	- get the game status
 *
 * @author Maxime MEURISSE (m.meurisse@student.uliege.be)
 * @version 3/27/2019
 */
public class GameManager {
	private static final int GRID_SIZE = 10;
	private static final int SHIP_SIZES[] = {5, 4, 3, 3, 2};

	private Grid grid;

	/**
	 * The constructor initializes the grid and add the ship on the grid.
	 */
	public GameManager() {
		grid = new Grid(GRID_SIZE);

		for(int i = 0; i < SHIP_SIZES.length; i++)
			grid.addShip(i + 1, SHIP_SIZES[i]);
	}

	public int hitPos(int pos) {
		if(pos < 0 || pos >= GRID_SIZE * GRID_SIZE) {
			System.out.println("Warning : bad position !");
			
			return 0;
		}

		return grid.hitPos(pos);
	}

	public int[] getList() {
		return grid.getList();
	}

	/**
	 * This function displays the status of the game.
	 *
	 * @param full A boolean indicating if the grid status should be display or not.
	 */
	public void displayStatus(boolean full) {
		if(full) {
			System.out.println("Legend");
			System.out.println("------");
			System.out.println("1 : Carrier (5 tiles)");
			System.out.println("2 : Battleship (4 tiles)");
			System.out.println("3 : Cruiser (3 tiles)");
			System.out.println("4 : Submarine (3 tiles)");
			System.out.println("5 : Destroyer (2 tiles)");
			System.out.println();

			System.out.println("Grid");
			System.out.println("----");

			grid.display();

			System.out.println();
		}

		System.out.println("Ships");
		System.out.println("-----");

		for(int i = 0; i < SHIP_SIZES.length; i++)
			grid.shipStatus(i + 1);

		System.out.println();
	}
}
