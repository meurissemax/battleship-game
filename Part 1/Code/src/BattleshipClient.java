import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import java.util.Scanner;

/**
 * This class implements all the features of the client: it sends the requests to the server (based on client data) and interprets the responses of the server.
 *
 * @author Maxime MEURISSE (m.meurisse@student.uliege.be)
 * @version 3/27/2019
 */
public class BattleshipClient {
    private static final int GRID_SIZE = 10;
    private static final int TOTAL_SHIP_SIZE = 17; /// to know when the client hit all the ships
    private static final int MAX_TRY = 70;
    private static final int PORT = 2278;
    private static final String SHIP_NAMES[] = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};

    public static void main(String[] args) {
        String host = "localhost";

        Socket cSocket;
        OutputStream out;
        BufferedInputStream in;

        byte header[];
        byte request[];
        byte response[];

        int length, choice, read, tries, count;
        boolean game;
        boolean grid[] = new boolean[GRID_SIZE * GRID_SIZE];
        Scanner reader = new Scanner(System.in);

        tries = count = 0;
        game = true;

        for(int i = 0; i < grid.length; i++)
            grid[i] = false;

        if(args.length > 0)
            if(!args[0].isEmpty())
                host = args[0];

        try {
            /// Initialization
            cSocket = new Socket(InetAddress.getByName(host), PORT);
			out = cSocket.getOutputStream();
			in = new BufferedInputStream(cSocket.getInputStream());

            System.out.println("Welcome to the game of Battleship !");

            /// Send a request to the server to start a new game
            response = new byte[2];
            response[0] = (byte)1;
            response[1] = (byte)0;

            out.write(response);
			out.flush();

            /// We recover and interpret the response of the server
            header = new byte[2];

            if(in.read(header, 0, 2) <= 0)
                System.out.println("Impossible to connect to the server.");

			if(header[0] != (byte)1 || header[1] != (byte)1)
				System.out.println("Server problem : bad response.");
			else
                System.out.println("A new game starts.");

            /// The game has started
            while(game) {
                /// If the client hit all the ships or has used all his trials
                if(count == TOTAL_SHIP_SIZE) {
                    System.out.println("You hit all the ships ! You win !");
                    break;
                } else if(tries == MAX_TRY) {
                    System.out.println("Maximum number of trials reached, game over.");
                    break;
                }

                try {
                    /// Display the main menu
                    System.out.println("");
                    System.out.println("1) Try a tile (" + (MAX_TRY - tries) + " tries remaining)");
                    System.out.println("2) See game status");
                    System.out.println("3) Quit");
                    System.out.println("");

                    /// Recover the choice of the client
                    System.out.print("Your choice : ");
                    choice = reader.nextInt();

                    System.out.println("");

                    switch(choice) {
                        /// Try a tile
                        case 1:
                            /// We get the guess of the client
                            System.out.println("Enter a position");
                            System.out.println("Format: 'xy' in [0,9] x [0,9], upper left corner corresponds to 00");
                            System.out.print("Your guess : ");
                            read = reader.nextInt();

                            while(read < 0 || read > 99) {
                                System.out.println("");
                                System.out.println("Invalid position.");
                                System.out.println("");

                                System.out.print("Enter your guess (xy in [0,9] x [0,9]) : ");
                                read = reader.nextInt();
                            }

                            System.out.println("");

                            /// We check if this position is already attacked or not
                            if(grid[read]) {
                                System.out.println("Position already attacked.");
                                continue;
                            }

                            grid[read] = true;

                            /// We send to the server the position to attacked
                            response = new byte[3];
                            response[0] = (byte)1;
                            response[1] = (byte)1;
                            response[2] = (byte)read;

                            out.write(response);
                            out.flush();

                            tries++;

                            /// We read the answer of the server and indicating which entity was hit (or an error if append)
                            request = new byte[3];

                            if(in.read(request, 0, 3) <= 0) {
                                System.err.println("Impossible to connect to the server.");
                                break;
                            }

                            if(request[0] != (byte)1 || request[1] != (byte)2) {
                                System.err.println("Server problem : bad response.");
                                continue;
                            } else {
                                if(request[2] == (byte)0) {
                                    System.out.println("Splash (no ship hit)");
                                } else {
                                    count++;

                                    System.out.println("You just hit the " + SHIP_NAMES[request[2] - 1] + " !");
                                }
                            }

                            break;

                        /// See game status
                        case 2:
                            /// We send to the server the request
                            response = new byte[2];
                            response[0] = (byte)1;
                            response[1] = (byte)2;

                            out.write(response);
                            out.flush();

                            /// We read the response of the server
                            request = new byte[3];

                            if(in.read(request, 0, 3) <= 0) {
                                System.err.println("Impossible to connect to the server.");
                                break;
                            }

                            if(request[0] != (byte)1 || request[1] != (byte)3) {
                                System.err.println("Server problem : bad response");
                                continue;
                            }

                            length = request[2] * 2;

                            /// We print the list of positions attacked (or an error if append)
                            if(length == 0) {
                                System.out.println("No position attacked.");
                            } else {
                                byte content[] = new byte[length];

                                if(in.read(content, 0, length) <= 0) {
                                    System.err.println("Impossible to connect to the server.");
                                    break;
                                }

                                String tmp;

                                for(int i = 0; i < length - 1; i += 2) {
                                    tmp = "Position " + content[i] + " -> ";

                                    if(content[i + 1] != (byte)0)
                                        tmp += SHIP_NAMES[content[i + 1] - 1];
                                    else
                                        tmp += "no ship hit";

                                    System.out.println(tmp);
                                }
                            }

                            break;

                        /// Quit game
                        case 3:
                            game = false;
                            break;

                        /// If client makes an invalid choice
                        default:
                            System.err.println("Invalid choice.");
                            continue;
                    }
                } catch(NumberFormatException nfe) {
                    System.err.println(nfe.getMessage());
                    continue;
                } catch(Exception e) {
                    System.err.println(e.getMessage());
				}
            }

            /// If the game is finished, socket is closed
            try {
                cSocket.close();
            } catch(IOException ioe) {
                System.err.println(ioe.getMessage());
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
		}
    }
}
