import java.io.InputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.net.Socket;
import java.net.URL;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class implements the server worker.
 * A server worker is created each time a new connection is accepted.
 * So, a server worker is creater for each new HTTP request.
 * The server worker handle the game and all actions of a player.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.05.11
 */

public class ServerWorker implements Runnable {
	private Socket sock;
	private ArrayList<GameManager> gameList;
	private InputStream serverInStream;
	private PrintWriter serverOutStream;
	private HTTPHandler http;
	private URL url;
	private HTMLHandler html;

	public ServerWorker(Socket sock, ArrayList<GameManager> gameList) {
		try {
			this.sock = sock;
			this.gameList = gameList;
			serverInStream = this.sock.getInputStream();
			serverOutStream = new PrintWriter(this.sock.getOutputStream(), true);
			html = new HTMLHandler();
		} catch(IOException ioe) {
			System.err.println("ServerWorker : unable to deal with stream.");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void run() {
		try {
			sock.setSoTimeout(GameConstants.TIMEOUT);
			http = new HTTPHandler(serverInStream);
			url = http.getURL();

			int position;
			String path = url.getPath(), method, cookie;

			cookie = http.searchCookie(GameConstants.COOKIE_NAME);

			/**************/
			/* Page index */
			/**************/

			if(path.equals("/"))
				redirect(GameConstants.PAGE_PLAY);

			/*************/
			/* Page play */
			/*************/

			else if(path.equals(GameConstants.PAGE_PLAY)) {
				method = http.getMethod();

				/* GET method */

				if(method == "GET") {
					position = http.parseQuery(url.getQuery());
					GameManager currentGame = searchGame(cookie);

					/// if there is a position mentionned
					if(position != -1) {
						/// we check if there is a game, of if the game is finished
						if(currentGame == null)
							redirect(GameConstants.PAGE_PLAY);
						else if(currentGame.isWin())
							redirect(GameConstants.PAGE_WIN);
						else if(currentGame.isLose())
							redirect(GameConstants.PAGE_LOSE);

						/// else we send a JSON file with some informations
						else {
							int result = currentGame.hitPos(position);
							boolean win = false, lose = false;
							String json;

							if(currentGame.isWin()) {
								win = true;
								WebServer.addFame(currentGame.getPlayerID(), currentGame.getNumberTries());

								System.out.println("Player " + currentGame.getPlayerID() + " won his game !");
							} else if(currentGame.isLose()) {
								lose = true;

								System.out.println("Player " + currentGame.getPlayerID() + " lost his game.");
							}

							currentGame.updateExpiration(GameConstants.TIMEOUT);

							json = "{\"isWin\":\"" + win + "\", \"isLose\":\"" + lose + "\", \"posStatus\":\"" + result + "\", \"numberTries\":\"" + currentGame.getRemainingTries() + "\", \"remainingShips\":\"" + currentGame.getRemainingShips() + "\"}";

							serverOutStream.print("HTTP/1.1 200 OK\r\n");
							serverOutStream.print("Connection : close\r\n");
							serverOutStream.print("Set-Cookie: " + cookie + "; path=/; " + getExpiration(GameConstants.TIMEOUT) + "\r\n");
							serverOutStream.print("Content-Type: JSON\r\n");
							serverOutStream.print("\r\n");
							serverOutStream.print(json);

							serverOutStream.flush();
						}
					}

					/// if no position is mentionned
					else {
						/// we refresh the game if he exists and he's not finished
						if(currentGame != null && !currentGame.isWin() && !currentGame.isLose()) {
							currentGame.updateExpiration(GameConstants.TIMEOUT);

							serverOutStream.print("HTTP/1.1 200 OK\r\n");
							serverOutStream.print("Connection : close\r\n");
							serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
							serverOutStream.print("Set-Cookie: " + cookie + "; path=/; " + getExpiration(GameConstants.TIMEOUT) + "\r\n");
							serverOutStream.print("Transfer-Encoding: chunked\r\n");

							if(http.gzip())
								serverOutStream.print("Content-Encoding: gzip\r\n");

							serverOutStream.print("\r\n");

							serverOutStream.flush();

							html.generatePlay(sock.getOutputStream(), currentGame, http.gzip());
						}

						/// else we create a new game
						else {
							GameManager newGame;

							synchronized(gameList) {
								/// we check if a game already exist to reuse its cookie
								if(currentGame != null) {
									cookie = currentGame.getCookie();
									gameList.remove(currentGame);
								} else {
									cookie = GameConstants.COOKIE_NAME + "=" + randomString(10);
								}

								newGame = new GameManager(cookie);
								gameList.add(newGame);
							}

							serverOutStream.print("HTTP/1.1 200 OK\r\n");
							serverOutStream.print("Connection : close\r\n");
							serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
							serverOutStream.print("Set-Cookie: " + cookie + "; path=/; " + getExpiration(GameConstants.TIMEOUT) + "\r\n");
							serverOutStream.print("Transfer-Encoding: chunked\r\n");

							if(http.gzip())
								serverOutStream.print("Content-Encoding: gzip\r\n");

							serverOutStream.print("\r\n");

							serverOutStream.flush();

							html.generatePlay(sock.getOutputStream(), newGame, http.gzip());
						}
					}
				}

				/* POST method */

				else if(method == "POST") {
					/// we parse the position
					position = http.parseQuery(http.getContent());

					if(position == -1)
						throw new HTTPException("400");

					/// we search the current game
					GameManager currentGame = searchGame(cookie);

					if(currentGame == null)
						throw new HTTPException("400");

					if(currentGame.isWin() || currentGame.isLose()) {
						gameList.remove(currentGame);

						redirect(GameConstants.PAGE_PLAY);
					}

					/// we hit the position and update the page of the user
					int result = currentGame.hitPos(position);

					if(currentGame.isWin()) {
						WebServer.addFame(currentGame.getPlayerID(), currentGame.getNumberTries());

						System.out.println("Player " + currentGame.getPlayerID() + " won his game !");

						redirect(GameConstants.PAGE_WIN);
					} else if(currentGame.isLose()) {
						System.out.println("Player " + currentGame.getPlayerID() + " lost his game.");

						redirect(GameConstants.PAGE_LOSE);
					} else {
						currentGame.updateExpiration(GameConstants.TIMEOUT);

						serverOutStream.print("HTTP/1.1 200 OK\r\n");
						serverOutStream.print("Connection : close\r\n");
						serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
						serverOutStream.print("Set-Cookie: " + cookie + "; path=/; " + getExpiration(GameConstants.TIMEOUT) + "\r\n");
						serverOutStream.print("Transfer-Encoding: chunked\r\n");

						if(http.gzip())
							serverOutStream.print("Content-Encoding: gzip\r\n");

						serverOutStream.print("\r\n");

						serverOutStream.flush();

						html.generatePlay(sock.getOutputStream(), currentGame, http.gzip());
					}
				}

				/// Unknown method

				else {
					throw new HTTPException("501");
				}
			}

			/*********************/
			/* Page hall of fame */
			/*********************/

			else if(path.equals(GameConstants.PAGE_HALL_OF_FAME)) {
				serverOutStream.print("HTTP/1.1 200 OK\r\n");
				serverOutStream.print("Connection : close\r\n");
				serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
				serverOutStream.print("Transfer-Encoding: chunked\r\n");

				if(http.gzip())
					serverOutStream.print("Content-Encoding: gzip\r\n");

				serverOutStream.print("\r\n");
				serverOutStream.flush();

				html.generateHallOfFame(sock.getOutputStream(), http.gzip());

				System.out.println("Hall of fame visited.");
			}

			/************/
			/* Page win */
			/************/

			else if(path.equals(GameConstants.PAGE_WIN)) {
				GameManager currentGame = searchGame(cookie);

				/// we check if a game exists and if this game is really won
				if(currentGame == null) {
					redirect(GameConstants.PAGE_PLAY);
				} else {
					if(currentGame.isWin()) {
						serverOutStream.print("HTTP/1.1 200 OK\r\n");
						serverOutStream.print("Connection : close\r\n");
						serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
						serverOutStream.print("Transfer-Encoding: chunked\r\n");

						if(http.gzip())
							serverOutStream.print("Content-Encoding: gzip\r\n");

						serverOutStream.print("\r\n");

						serverOutStream.flush();

						html.generateMessage(sock.getOutputStream(), currentGame, "You destroyed all the ships, you won !", http.gzip());
					} else {
						redirect(GameConstants.PAGE_PLAY);
					}
				}
			}

			/*************/
			/* Page lose */
			/*************/

			else if(path.equals(GameConstants.PAGE_LOSE)) {
				GameManager currentGame = searchGame(cookie);

				/// we check if a game exists and if this game is really lost
				if(currentGame == null) {
					redirect(GameConstants.PAGE_PLAY);
				} else {
					if(currentGame.isLose()) {
						serverOutStream.print("HTTP/1.1 200 OK\r\n");
						serverOutStream.print("Connection : close\r\n");
						serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
						serverOutStream.print("Transfer-Encoding: chunked\r\n");

						if(http.gzip())
							serverOutStream.print("Content-Encoding: gzip\r\n");

						serverOutStream.print("\r\n");

						serverOutStream.flush();

						html.generateMessage(sock.getOutputStream(), currentGame, "Game over, you lost.", http.gzip());
					} else {
						redirect(GameConstants.PAGE_PLAY);
					}
				}
			}

			/*****************/
			/* Unknown pages */
			/*****************/

			else {
				throw new HTTPException("404");
			}
		} catch(SocketException se) {
			System.err.println("ServerWorker : error with socket.");
		} catch(HTTPException httpe) {
			try {
				switch(httpe.getMessage()) {
					case "400": httpError(400, "400 Bad Request"); break;
					case "404": httpError(404, "404 Not Found"); break;
					case "405": httpError(405, "405 Method Not Allowed"); break;
					case "411": httpError(411, "411 Length Required"); break;
					case "501": httpError(501, "501 Not Implemented"); break;
					case "505": httpError(505, "505 HTTP Version Not Supported"); break;
					default: System.err.println("ServerWorker : HTTP exception.");
				}
			} catch(IOException ioe) {
				System.err.println("ServerWorker : error during HTTP exception.");
			} catch(Exception e) {
				System.err.println(e.getMessage());
			}
		} catch(IOException ioe) {
			System.err.println("ServerWorker : an IO exception occured.");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				sock.close();
			} catch(IOException ioe) {
				System.err.println("ServerWorker : unable to close socket.");
			}
		}
	}

	/**
	 * This method is used to print in the OutputStream an HTTP request for an error.
	 *
	 * @param code the HTTP error code
	 * @param header the header of the HTTP error request
	 */
	private void httpError(int code, String header) throws IOException {
		serverOutStream.print("HTTP/1.1 " + header + "\r\n");
		serverOutStream.print("Connection : close\r\n");
		serverOutStream.print("Content-Type: text/html; charset=utf-8\r\n");
		serverOutStream.print("Transfer-Encoding: chunked\r\n");

		if(http.gzip())
			serverOutStream.print("Content-Encoding: gzip\r\n");

		serverOutStream.print("\r\n");
		serverOutStream.flush();

		html.generateError(sock.getOutputStream(), code, http.gzip());
	}

	/**
	 * This method is used to redirect to a specific page thanks to HTTP 303 request.
	 *
	 * @param page the page to be redirected
	 */
	private void redirect(String page) {
		if(page == null)
			page = GameConstants.PAGE_PLAY;

		serverOutStream.print("HTTP/1.1 303 See Other\r\n");
		serverOutStream.print("Location: " + page + "\r\n");
		serverOutStream.print("\r\n");

		serverOutStream.flush();
	}

	/**
	 * This method is used to generate an expiration date for a cookie.
	 * The date generated is the actual date + a duration 'duration' in ms.
	 *
	 * @param duration the lifetime of the cookie, in ms
	 *
	 * @return a string that contains an expiration date for a cookie
	 */
	private String getExpiration(int duration) {
		if(duration < 0)
			duration = -duration;

		Date expdate = new Date();
		expdate.setTime(expdate.getTime() + duration);

		DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		return "expires=" + df.format(expdate);
	}
	
	private String randomString(int size) {
		if(size <= 0)
			size = 1;

		final String STRINGS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		int character;
		StringBuilder builder = new StringBuilder();

		while(size-- != 0) {
			character = (int)(Math.random() * STRINGS.length());
			builder.append(STRINGS.charAt(character));
		}

		return builder.toString();
	}

	/**
	 * This method is used to search the game associated to a cookie in the server game list.
	 *
	 * @param cookie the cookie of a game
	 *
	 * @return the game associated to the cookie 'cookie'
	 */
	private GameManager searchGame(String cookie) {
		if(cookie == null || gameList.size() == 0)
			return null;

		for(int i = 0; i < gameList.size(); i++)
			if(gameList.get(i).getCookie().equals(cookie))
				return gameList.get(i);

		return null;
	}
}
