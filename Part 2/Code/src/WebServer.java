import java.io.IOException;

import java.net.ServerSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class is the main class of the project.
 * This class is used to run the server, accept socket, deal with all games and maintains the hall of fame.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.20
 */

public class WebServer {
	private static int poolSize;
	private static ServerSocket sSocket;
	private static ExecutorService threadPool;
	private static ArrayList<GameManager> gameList = new ArrayList<GameManager>();
	private static ArrayList<String> fames = new ArrayList<String>();

	public static void main(String[] args) {
		try {
			poolSize = Integer.parseInt(args[0]);
			sSocket = new ServerSocket(GameConstants.PORT);
			threadPool = Executors.newFixedThreadPool(poolSize);

			while(true)
				threadPool.execute(new ServerWorker(sSocket.accept(), gameList));
		} catch(ArrayIndexOutOfBoundsException aioobe) {
			System.err.println("WebServer : missing max thread pool size.");
		} catch(NumberFormatException nfe) {
			System.err.println("WebServer : unable to parse max thread pool size to int.");
		} catch(IOException ioe) {
			System.err.println("WebServer : error using ServerSocket.");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * This method is used to add in the hall of fame a score.
	 * The hall of fame contains the score of all games, and is always sorted by score ascending.
	 * This method is synchronized because all workers can access it at the same time to add a fame.
	 *
	 * @param playerID the ID of the player that performs a score
	 * @param score the score of the player
	 */
	public synchronized static void addFame(String playerID, int score) {
		int last = GameConstants.MAX_TRIES + 1, i;
		boolean find = false;

		String data, compare;
		String[] split;

		DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm");
		Date date = new Date();

		/// we concatenate data to create the fame (a single String)
		data = playerID + "&" + score + "&" + df.format(date);

		/// if the hall of fame is empty, we juste add the game
		if(fames.isEmpty()) {
			fames.add(data);

			System.out.println("Hall of fame : " + playerID + " with score " + score + " added.");
		}

		/// if the hall of fame is not empty
		else {
			try {
				/// we check if the fame already exits (by searching the player ID)
				for(i = 0; i < fames.size(); i++) {
					compare = fames.get(i);
					split = compare.split("&");

					if(split[0].compareTo(playerID) == 0) {
						find = true;
						last = Integer.parseInt(split[1]);

						break;
					}
				}

				/// if the player ID already exists, we update (or not) its score
				if(find) {
					if(score < last) {
						fames.set(i, data);

						System.out.println("Hall of fame : " + playerID + " with score " + score + " updated.");
					}
				}

				/// if the player ID doesn't exist, we add the fame
				else {
					fames.add(data);

					System.out.println("Hall of fame : " + playerID + " with score " + score + " added.");
				}

				/// we sort the hall of fame
				sortFames();
			} catch(NumberFormatException nfe) {
				System.err.println("WebServer : unable to parse last to int.");
			} catch(Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * This method is used to get 'limit' fames of the hall of fame.
	 * The hall of fames is always sorted by score ascending, so this method returns the 'limit' best scores.
	 *
	 * @param limit the number of fames to get
	 *
	 * @return an array containing the fames, sorted by score ascending.
	 */
	public static String[] getFames(int limit) {
		int size = fames.size();
		String[] best;

		if(size > 0) {
			if(size < limit)
				limit = size;

			best = new String[limit];

			for(int i = 0; i < limit; i++)
				best[i] = fames.get(i);

			return best;
		} else {
			return null;
		}
	}
	
	private static void sortFames() {
		Collections.sort(fames, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				int score1 = 0, score2 = 0;
				String[] split1, split2;

				split1 = s1.split("&");
				split2 = s2.split("&");

				try {
					score1 = Integer.parseInt(split1[1]);
					score2 = Integer.parseInt(split2[1]);
				} catch(NumberFormatException nfe) {
					System.err.println("WebServer : unable to parse score to int.");
				} catch(Exception e) {
					System.err.println(e.getMessage());
				}

				return score1 - score2;
			}
		});
	}
}
