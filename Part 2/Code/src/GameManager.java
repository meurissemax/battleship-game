import java.util.Date;

/**
 * This class is used to create a game and manage it (attack a position, display the state of the game, ...).
 * Each game is associated to a cookie.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.05.11
 */

public class GameManager {
	private int numberTries;
	private long expiration; /// expiration timestamp of the game, in ms
	private String cookie; /// the 'cookie' instance variable contains "COOKIE_NAME=COOKIE_VALUE".
	private Grid grid;

	public GameManager(String cookie) {
		if(cookie == null) {
			System.out.println("Warning : cookie is null !");

			this.cookie = GameConstants.COOKIE_NAME + "=default";
		} else {
			this.cookie = cookie;
		}

		numberTries = 0;
		expiration = new Date().getTime() + GameConstants.TIMEOUT; /// set to actual timestamp
		grid = new Grid(GameConstants.GRID_SIZE);

		/// We add ships on the grid
		for(int i = 0; i < GameConstants.SHIP_SIZES.length; i++)
			grid.addShip(i + 1, GameConstants.SHIP_SIZES[i]);

		System.out.println("Game initialized for player " + getPlayerID() + ".\n");

		displayStatus();
	}

	public int getNumberTries() {
		return numberTries;
	}

	public int getRemainingTries() {
		return GameConstants.MAX_TRIES - numberTries;
	}

	public long getExpiration() {
		return expiration;
	}

	public synchronized void updateExpiration(long duration) {
		expiration = new Date().getTime() + duration;
	}

	/**
	 * This function is used to return the number of ships still alive.
	 *
	 * @return the number of ships still alive
	 */
	public int getRemainingShips() {
		int alive = 0;

		for(int i = 0; i < GameConstants.SHIP_SIZES.length; i++)
			if(grid.shipAlive(i + 1))
				alive++;

		return alive;
	}

	public String getCookie() {
		return cookie;
	}

	/**
	 * This function is used to get the player ID, i.e. the content of the cookie.
	 *
	 * @return the player ID
	 */
	public String getPlayerID() {
		String[] split = cookie.split("="); /// because player ID is the content of the cookie

		return split[1];
	}

	/**
	 * This function is used to hit a position on the grid.
	 * It returns :
	 * 		- -1 if the position was already hit
	 * 		- 0 if the position corresponds to water
	 * 		- > 0 if the position corresponds to a ship
	 *
	 * @param pos the position to hit
	 * @return an integer indicating the content of the position hit
	 */
	public synchronized int hitPos(int pos) {
		if(pos < 0 || pos >= GameConstants.GRID_SIZE * GameConstants.GRID_SIZE) {
			System.out.println("Warning : player hit a bad position !");
			
			return -1;
		}

		int hit = grid.hitPos(pos);
		String log = "Player " + getPlayerID() + " hit the position " + GameConstants.XAXIS[pos % GameConstants.GRID_SIZE] + GameConstants.YAXIS[(pos - (pos % GameConstants.GRID_SIZE)) / GameConstants.GRID_SIZE];

		switch(hit) {
			case -1:
				log += " (already hit).";

				break;

			case 0:
				log += ".";
				numberTries++;

				break;

			default:
				log += " and hit the " + GameConstants.SHIP_NAMES[hit - 1] + ".";
				numberTries++;

				if(!grid.shipAlive(hit))
					log += "\nPlayer " + getPlayerID() + " sank the boat " + GameConstants.SHIP_NAMES[hit - 1] + " !";
		}

		System.out.println(log);

		return hit;
	}

	/**
	 * This function is used to get the status of a position.
	 * It returns :
	 * 		- 1 if the position is hit and if there is a ship on this position
	 * 		- -1 if the position is hit and if there is no ship on this position
	 * 		- 0 if the position isn't hit
	 *
	 * @param pos a position in the grid
	 * @return an integer indicating the status of the position
	 */
	public int getPosStatus(int pos) {
		if(pos < 0 || pos >= GameConstants.GRID_SIZE * GameConstants.GRID_SIZE)
			return 0;

		boolean hit = false, ship = false;

		if(grid.getHit(pos))
			hit = true;

		if(grid.getID(pos) > 0)
			ship = true;

		if(hit && ship)
			return 1;
		else if(hit && !ship)
			return -1;
		else
			return 0;
	}

	public boolean isWin() {
		if(getRemainingShips() == 0 && numberTries <= GameConstants.MAX_TRIES)
			return true;
		else
			return false;
	}

	public boolean isLose() {
		if(numberTries >= GameConstants.MAX_TRIES) {
			if(!isWin())
				return true;
			else
				return false;
		} else {
			return false;
		}
	}

	private void displayStatus() {
		System.out.println("Legend");
		System.out.println("------");

		for(int i = 0; i < GameConstants.SHIP_SIZES.length; i++)
			System.out.println((i + 1) + " : " + GameConstants.SHIP_NAMES[i] + " (" + GameConstants.SHIP_SIZES[i] + " tiles)");

		System.out.println("\nGrid");
		System.out.println("----");

		grid.display();

		System.out.println();
	}
}
