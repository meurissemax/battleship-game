import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.Socket;

/**
 * This class is used to manage the game of one (and only one) client.
 * It responds to the client's requests.
 *
 * @author Maxime MEURISSE (m.meurisse@student.uliege.be)
 * @version 3/27/2019
 */
public class ServerWorker extends Thread {
    private final int TIMEOUT = 300000; // 5 minutes 

    private Socket wSocket;
    private OutputStream out;
    private BufferedInputStream in;

    private byte header[];
    private byte request[];
    private byte response[];

    private int id;
    public boolean play;
    private GameManager game;

    public ServerWorker(Socket s, int id) {
        wSocket = s;
        this.id = id;
        play = true;

        try {
            out = wSocket.getOutputStream();
            in = new BufferedInputStream(wSocket.getInputStream());
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        /// We first try to set the timeout
        try {
            wSocket.setSoTimeout(TIMEOUT);
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

        /// We know the size of the header of each request (fixed size to 2)
        header = new byte[2];

        while(play) {
            try {
                /// We check if the player is still here
                if(in.read(header, 0, 2) <= 0) {
                    System.out.println("Player #" + id + " leaves the server.");
                    System.out.println();
                    break;
                }

                /// We check the first byte of the request (that must always be 1)
                if(header[0] != (byte)1) {
                    badRequest();
                    continue;
                }

                switch(header[1]) {
                    /// Start a new game
                    case 0:
                        System.out.println("Player #" + id + " requests a new game.");

                        /// We create a new game
                        game = new GameManager();

                        System.out.println("Game initialized.");
                        System.out.println();

                        game.displayStatus(true);

                        /// We send the response to the client
                        response = new byte[2];
                        response[0] = (byte)1;
                        response[1] = (byte)1;

                        out.write(response);
                        out.flush();

                        break;

                    /// Hit a position
                    case 1:
                        /// We get the position send by the client
                        request = new byte[1];

                        if(in.read(request, 0, 1) <= 0) {
                            System.out.println("Position of the ship missing.");
                            continue;
                        }

                        System.out.println("Player #" + id + " requests to hit the position " + request[0] + ".");

                        /// We send the id of the entity attacked to the client
                        response = new byte[3];
                        response[0] = (byte)1;
                        response[1] = (byte)2;
                        response[2] = (byte)game.hitPos(request[0]); // we hit the position

                        System.out.println();

                        game.displayStatus(false);

                        out.write(response);
                        out.flush();

                        break;

                    /// Get the game status
                    case 2:
                        System.out.println("Player #" + id + " requests the game status.");
                        System.out.println();

                        /// We get the list of positions attacked
                        int[] list = game.getList();
                        int length = list[0];


                        /// We send the response to the client
                        response = new byte[3 + length * 2];
                        response[0] = (byte)1;
                        response[1] = (byte)3;
                        response[2] = (byte)length;

                        /// We check if there is at least one position attacked
                        if(length > 0)
                            for(int i = 0; i < length * 2; i++)
                            	response[i + 3] = (byte)list[i + 1];

                        out.write(response);
                        out.flush();

                        break;

                    /// If the server receives a bad request
                    default:
                        badRequest();
                }
            } catch(IOException ioe) {
                System.err.println(ioe.getMessage());
                break;
            } catch(Exception e) {
                System.out.println(e.getMessage());
                badRequest();
            }
        }

        /// If the game is finished, we try to close the socket
        try {
            wSocket.close();
        } catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    private void badRequest() {
        try {
            System.out.println("Bad request.");

            response = new byte[2];
            response[0] = (byte)1;
            response[1] = (byte)4;

            out.write(response);
            out.flush();
		} catch(IOException e) {
			play = false;
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
    }
}
