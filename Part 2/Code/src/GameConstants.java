import java.util.stream.IntStream;

/**
 * This class contains all the constants of the game (and the program in general).
 * The rules of the game can be completely modified here without causing errors in the rest of the code.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.20
 */

public final class GameConstants {
	public static final int PORT = 8005;
	public static final int TIMEOUT = 600000; /// socket timeout; 10 minutes

	public static final String COOKIE_NAME = "BATTLESHIP_PLAYER_ID";

	public static final String PAGE_PLAY = "/play.html";
	public static final String PAGE_HALL_OF_FAME = "/halloffame.html";
	public static final String PAGE_WIN = "/win.html";
	public static final String PAGE_LOSE = "/lose.html";

	public static final int GRID_SIZE = 10;
	public static final String[] XAXIS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
	public static final String[] YAXIS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

	public static final int[] SHIP_SIZES = {2, 3, 3, 4, 5};
	public static final String[] SHIP_NAMES = {"Destroyer", "Submarine", "Cruiser", "Battleship", "Carrier"};

	public static final int MAX_TRIES = 70;

	public static final int MAX_FAMES = 10;

	/// Verification of the validity of the constants.
	static {
		if(PORT < 8000 || PORT > 8099)
			printError("invalid port.");

		if(TIMEOUT < 1000)
			printError("timeout too short.");

		if(COOKIE_NAME == null || PAGE_PLAY == null || PAGE_HALL_OF_FAME == null || PAGE_WIN == null || PAGE_LOSE == null)
			printError("data can't be empty.");

		if(GRID_SIZE < 2 || GRID_SIZE > 10)
			printError("the size of the grid must be between 2 and 10.");

		if(XAXIS.length < GRID_SIZE || YAXIS.length < GRID_SIZE)
			printError("an axis is too small.");

		if(SHIP_SIZES.length < 1 || SHIP_SIZES.length != SHIP_NAMES.length)
			printError("error in the data about ships.");

		if(IntStream.of(SHIP_SIZES).sum() > GRID_SIZE * GRID_SIZE)
			printError("too many ships.");

		if(MAX_TRIES < IntStream.of(SHIP_SIZES).sum())
			printError("number of trials too small.");

		if(MAX_FAMES < 1)
			printError("number of fames printed must be greater than 0.");
	}

	/// The constructor is private to prevent instantiation of the class.
	private GameConstants() { }

	private static void printError(String message) {
		System.err.println("GameConstants : " + message);
		System.exit(1);
	}
}
