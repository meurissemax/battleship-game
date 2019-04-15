import java.io.InterruptedIOException;
import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;

/**
 * This class accepts new socket from client and starts a new game for each socket accepted (on different Thread).
 *
 * @author Maxime MEURISSE (m.meurisse@student.uliege.be)
 * @version 3/27/2019
 */
public class BattleshipServer {
	private static final int PORT = 2278;

	private static int gameID = 0;

	public static void main(String[] args) {
		System.out.println("Server starts.");
		
		try {
			ServerSocket sSocket = new ServerSocket(PORT);

			try {
				while(true) {
					Socket gameSocket = sSocket.accept();
					ServerWorker game = new ServerWorker(gameSocket, gameID++);
					game.start();
				}
			} catch(InterruptedIOException iioe) {
				System.err.println(iioe.getMessage());
			} catch(IOException ioe) {
				System.err.println(ioe.getMessage());
			} finally {
				sSocket.close();
			}
		} catch(IOException ioe) {
			System.err.println(ioe.getMessage());
		}
	}
}
