import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.ByteArrayOutputStream;

import java.util.zip.GZIPOutputStream;

/**
 * This class is used to handle the generation and compression of all HTML pages.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.05.11
 */

public class HTMLHandler {
	private final int MAX_CHUNCK_SIZE = 128;
	private final String CSS_PATH = "src/resources/css/battleship.css";
	private final String JS_PATH = "src/resources/js/battleship.js";

	private ImageHandler images;

	public HTMLHandler() {
		images = new ImageHandler();
	}

	/**
	 * This function is used to compress and write in the OutputStream the page of the game.
	 *
	 * @param outStream the output stream of the socket
	 * @param currentGame the current game executed on the server worker
	 * @param gzip a boolean value indicating if gzip compression is supported or not
	 *
	 * @throws IOException and exception if the encoding fails
	 */
	public void generatePlay(OutputStream outStream, GameManager currentGame, boolean gzip) throws IOException {
		byte bArray[];
		String html = getHeader() + getGrid(currentGame) + getSidebar(currentGame) + getFooter();
		PrintWriter serverWriter = new PrintWriter(outStream);

		if(gzip) {
			bArray = GZIPcompress(html);
			chunckEncoding(bArray, outStream);

			outStream.flush();
		} else {
			chunckEncoding(html, serverWriter);

			serverWriter.flush();
		}
	}

	/**
	 * This function is used to compress and write in the OutputStream the hall of fame page.
	 *
	 * @param outStream the output stream of the socket
	 * @param gzip a boolean value indicating if gzip compression is supported or not
	 *
	 * @throws IOException and exception if the encoding fails
	 */
	public void generateHallOfFame(OutputStream outStream, boolean gzip) throws IOException {
		byte bArray[];
		String html;
		String[] fames = WebServer.getFames(GameConstants.MAX_FAMES), split;
		PrintWriter serverWriter = new PrintWriter(outStream);

		html = getHeader();

		if(fames != null) {
			html += "<div class='alert alert-info'>The score is the number of shots the player used to sink all boats.</div>";
			html += "<table class='halloffame text-center'><thead><tr><th scope='col'>Rank</th><th scope='col' class='text-left'>Player ID</th><th scope='col'>Score</th><th scope='col'>Date</th></thead><tbody>";

			for(int i = 0; i < fames.length; i++) {
				split = fames[i].split("&");

				switch(i) {
					case 0: html += "<tr class='gold-fame'><td>" + (i + 1) + "</td><td class='text-left'>" + split[0] + "</td><td>" + split[1] + "</td><td>" + split[2] + "</td></tr>"; break;
					case 1: html += "<tr class='silver-fame'><td>" + (i + 1) + "</td><td class='text-left'>" + split[0] + "</td><td>" + split[1] + "</td><td>" + split[2] + "</td></tr>"; break;
					case 2: html += "<tr class='bronze-fame'><td>" + (i + 1) + "</td><td class='text-left'>" + split[0] + "</td><td>" + split[1] + "</td><td>" + split[2] + "</td></tr>"; break;
					default: html += "<tr><td>" + (i + 1) + "</td><td class='text-left'>" + split[0] + "</td><td>" + split[1] + "</td><td>" + split[2] + "</td></tr>";
				}
			}

			html += "</tbody></table>";
		} else {
			html += "<p>Hall of fame is empty.</p>";
		}

		html += "</div></body></html>";

		if(gzip) {
			bArray = GZIPcompress(html);
			chunckEncoding(bArray, outStream);

			outStream.flush();
		} else {
			chunckEncoding(html, serverWriter);

			serverWriter.flush();
		}
	}

	/**
	 * This function is used to compress and write in the OutputStream the message page.
	 *
	 * @param outStream the output stream of the socket
	 * @param currentGame the current game executed on the server worker
	 * @param message the message to display on the page
	 * @param gzip a boolean value indicating if gzip compression is supported or not
	 *
	 * @throws IOException and exception if the encoding fails
	 */
	public void generateMessage(OutputStream outStream, GameManager currentGame, String message, boolean gzip) throws IOException {
		byte bArray[];
		String html;
		PrintWriter serverWriter = new PrintWriter(outStream);

		html = getHeader();

		if(message != null) {
			html += "<div class='message'>";
			html += "<p>" + message + "</p>";

			if(currentGame.isWin()) {
				html += "<p class='description'>Your player ID is <b>" + currentGame.getPlayerID() + "</b> and your score is <b>" + currentGame.getNumberTries() + "</b>.</p>";
				html += "<p class='description'>Check the <a href='" + GameConstants.PAGE_HALL_OF_FAME + "' target='_blank'>hall of fame</a> !</p>";
			}

			html += "<p><a class='a-button' href='" + GameConstants.PAGE_PLAY + "'>Replay</a></p>";
			html += "</div>";
		}

		html += "</div></body></html>";

		if(gzip) {
			bArray = GZIPcompress(html);
			chunckEncoding(bArray, outStream);

			outStream.flush();
		} else {
			chunckEncoding(html, serverWriter);

			serverWriter.flush();
		}
	}

	/**
	 * This function is used to compress and write in the OutputStream the error page.
	 *
	 * @param outStream the output stream of the socket
	 * @param errorCode the code of the HTTP error
	 * @param gzip a boolean value indicating if gzip compression is supported or not
	 *
	 * @throws IOException and exception if the encoding fails
	 */
	public void generateError(OutputStream outStream, int errorCode, boolean gzip) throws IOException {
		byte bArray[];
		String html;
		PrintWriter serverWriter = new PrintWriter(outStream);

		html = getHeader() + "<div class='alert alert-info'>";

		switch(errorCode) {
			case 400: html += "<b>Error 400 (bad request)</b> : the server cannot or will not process the request due to an apparent client error."; break;
			case 404: html += "<b>Error 404 (not found)</b> : the requested url was not found on this server."; break;
			case 405: html += "<b>Error 405 (method not allowed)</b> : a request method is not supported for the requested resource."; break;
			case 411: html += "<b>Error 411 (length required)</b> : the request did not specify the length of its content."; break;
			case 501: html += "<b>Error 501 (not implemented)</b> : the server does not recognize the request method."; break;
			case 505: html += "<b>Error 505 (HTTP version not supported)</b> : the server does not support the HTTP protocol version used in the request."; break;

			default: html += "Error.";
		}

		html += "</div></div></body></html>";

		if(gzip) {
			bArray = GZIPcompress(html);
			chunckEncoding(bArray, outStream);

			outStream.flush();
		} else {
			chunckEncoding(html, serverWriter);

			serverWriter.flush();
		}
	}

	/**
	 * This function is used to get the HTML code of the header of a page.
	 *
	 * @return the HTML code of the header of a page
	 */
	private String getHeader() {
		return "<!doctype html><html lang='fr'><head><meta charset='utf-8'><meta name='author' content='Maxime Meurisse, Valentin Vermeylen'><link rel='icon' type='image/png' href='" + images.getImage("favicon") + "'><link rel='shortcut icon' type='image/png' href='" + images.getImage("favicon") + "'><link rel='apple-touch-icon' href='" + images.getImage("apple-touch-icon") + "'><style>" + loadFile(CSS_PATH) + "</style><title>Battleship</title></head><body><div class='header text-center'><p class='main-title'>The battleship game</p><p class='description'>A game developed by <b>Maxime Meurisse</b> and <b>Valentin Vermeylen</b> as part of a project of the <a href='https://uliege.be/' target='_blank'>University of Liège</a>.</p><p class='sources'>Most of the images used come from <a href='https://www.flaticon.com' target='_blank'>flaticon</a>.</p></div><div class='container centered'>";
	}

	/**
	 * This function is used to get the HTML code of the game grid.
	 *
	 * @param currentGame the current game executed on the server worker
	 *
	 * @return the HTML code of the game grid.
	 */
	private String getGrid(GameManager currentGame) {
		int pos;
		String html = "<div style='float: left;'><table class='game-grid centered'><thead><tr><th scope='col'></th>";

		for(int i = 0; i < GameConstants.GRID_SIZE; i++)
			html += "<th scope='col'>" + GameConstants.XAXIS[i] + "</th>";

		html += "</tr></thead><tbody>";

		for(int i = 0; i < GameConstants.GRID_SIZE; i++) {
			html += "<tr><th scope='row'>" + GameConstants.YAXIS[i] + "</th>";

			for(int j = 0; j < GameConstants.GRID_SIZE; j++) {
				pos = i * GameConstants.GRID_SIZE + j;

				switch(currentGame.getPosStatus(pos)) {
					case 1: html += "<td id='" + pos + "' onclick = 'hitPos(" + pos + ");'><img src='" + images.getImage("hit") + "' width='50' height='50' alt='Hit'></td>"; break;
					case -1: html += "<td id='" + pos + "' onclick = 'hitPos(" + pos + ");'><img src='" + images.getImage("miss") + "' width='50' height='50' alt='Miss'></td>"; break;
					default: html += "<td id='" + pos + "' onclick = 'hitPos(" + pos + ");'><img src='" + images.getImage("water") + "' width='50' height='50' alt='Water'></td>";
				}
			}

			html += "</tr>";
		}

		html += "</tbody></table></div>";

		return html;
	}

	/**
	 * This function is used to get the HTML code of the sidebar.
	 *
	 * @param currentGame the current game executed on the server worker
	 *
	 * @return the HTML code of the sidebar.
	 */
	private String getSidebar(GameManager currentGame) {
		String html = "<div style='float: right; max-width: 32%;'><div class='player' id='player'>Player ID : <b>" + currentGame.getPlayerID() + "</b></div><table class='stats'><tbody><tr><td>Number of remaining trials</td><td class='text-center' id='numberTries'><b>" + currentGame.getRemainingTries() + "</b></td></tr><tr><td>Number of remaining ships</td><td class='text-center' id='remainingShips'><b>" + currentGame.getRemainingShips() + "</b></td></tr></tbody></table><div class='alert alert-info text-justify'><p><b>How to play ?</b></p><div id='jshow' style='display: none;'><p>Just click on a tile for your next guess !</p></div><noscript><p>Javascript is disabled on the browser. No worries ! Choose a position in the drop-down list and submit.</p><form method='post'><div class='select-pos'><select name='pos'>";

		for(int i = 0; i < GameConstants.GRID_SIZE; i++)
			for(int j = 0; j < GameConstants.GRID_SIZE; j++)
				html += "<option value='" + (j * GameConstants.GRID_SIZE + i) + "'>Position " + GameConstants.XAXIS[i] + GameConstants.YAXIS[j] + "</option>";

		html += "</select></div><button type='submit'>Submit</button></form></noscript></div><div class='alert alert-info text-justify'><p><b>Want to be the best ?</b></p><p>Check the <a href='/halloffame.html' target='_blank'>hall of fame</a> !</p></div></div>";

		return html;
	}

	/**
	 * This function is used to get the HTML code of the footer of a page.
	 *
	 * @return the HTML code of the footer of a page
	 */
	private String getFooter() {
		return "<br style='clear: both;'></div><script type='text/javascript'>var imgMiss = '" + images.getImage("miss") + "', imgHit = '" + images.getImage("hit") + "', pagePlay = '" + GameConstants.PAGE_PLAY + "', queryName = '" + GameConstants.QUERY_NAME + "'; " + loadFile(JS_PATH) + "</script></body></html>";
	}

	/**
	 * This function is used to read the content of a file.
	 *
	 * @param filepath the path to the file to read
	 *
	 * @return the content of the file
	 */
	private String loadFile(String filepath) {
		String content = "", line;

		try {
			BufferedReader bufReader = new BufferedReader(new FileReader(new File(filepath)));

			while((line = bufReader.readLine()) != null)
				content += line;

			bufReader.close();
		} catch(IOException ioe) {
			System.err.println("HTMLHandler : unable to read in buffer.");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}

		return content;
	}
	
	private void chunckEncoding(String html, PrintWriter serverWriter) {
		while(html.length() > MAX_CHUNCK_SIZE) {
			serverWriter.print(Integer.toHexString(MAX_CHUNCK_SIZE) + "\r\n");
			serverWriter.print(html.substring(0, MAX_CHUNCK_SIZE));

			html = html.substring(MAX_CHUNCK_SIZE);
		}

		serverWriter.print(Integer.toHexString(html.length()) + "\r\n");
		serverWriter.print(html);
		serverWriter.print(Integer.toHexString(0) + "\r\n\r\n");
	}

	private void chunckEncoding(byte[] bArray, OutputStream outStream) throws IOException {
		int i = 0;
		byte[] tmpArray = new byte[MAX_CHUNCK_SIZE];
		String tmpString;

		while(bArray.length - i * MAX_CHUNCK_SIZE > MAX_CHUNCK_SIZE) {
			tmpString = new String(Integer.toHexString(MAX_CHUNCK_SIZE) + "\r\n");
			outStream.write(tmpString.getBytes());

			System.arraycopy(bArray, i * MAX_CHUNCK_SIZE, tmpArray, 0, MAX_CHUNCK_SIZE);
			outStream.write(tmpArray);

			tmpString = new String("\r\n");
			outStream.write(tmpString.getBytes());

			i++;
		}

		tmpString = new String(Integer.toHexString(bArray.length - i * MAX_CHUNCK_SIZE) + "\r\n");
		outStream.write(tmpString.getBytes());

		System.arraycopy(bArray, i * MAX_CHUNCK_SIZE, tmpArray, 0, bArray.length - i * MAX_CHUNCK_SIZE);
		outStream.write(tmpArray, 0, bArray.length - i * MAX_CHUNCK_SIZE);

		tmpString = new String("\r\n");
		outStream.write(tmpString.getBytes());

		tmpString = new String(Integer.toHexString(0) + "\r\n\r\n");
		outStream.write(tmpString.getBytes());
	}

	private byte[] GZIPcompress(String data) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(baos);

		gzip.write(data.getBytes("UTF-8"));
		gzip.close();

		byte[] compressed = baos.toByteArray();
		baos.close();

		return compressed;
	}
}
