package me.jordanodonnell.connectfive.server.client;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Client Class: Creates connection between client and server and creates GUI to portray current game state
 */
public class Client {

    private JFrame frame = new JFrame("Connect Five Game");  // creating a GUI (JFrame = container that inherits java)
    private JLabel messageLabel = new JLabel("");  // creating message label to print messages to GUI
    private ImageIcon icon;  // stores player 1's disc icon (image)
    private ImageIcon opponentIcon;  // stores player 2's disc icon (image)

    private Square[] board = new Square[54];  // creating board of 54 'Square' objects (to store coloured discs)
    private Square currentSquare;  // creating Square object (used to check and insert disc)

    private static int PORT = 4999;  // current port used for connection between server and client(s)
    private Socket socket;  // creating socket instance to bridge connection between current client object and server
    private BufferedReader in;  // used to read in messages from server
    private PrintWriter out;  // uses to transmit messages to server

    /**
     * Initialising Client object's attributes
     */
    public Client(String serverAddress) throws Exception {
        socket = new Socket("localhost", PORT);  // creating socket connection to server on port 4999
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // used to read in messages from server
        out = new PrintWriter(socket.getOutputStream(), true);  // uses to transmit messages to server... flushes(deletes) previously transmitted messages
        System.out.println("\nConnected to Server!");

        Scanner scan = new Scanner(System.in);  // used to retrieve user input
        String name = null;  // creating local variable 'name' to store current player name
        System.out.print("\nPlayer Name: ");  // requesting player to enter their name
        name = scan.nextLine();  // storing user input in variable

        // if user input is a valid string i.e. name then...
        if(name != null && !name.isEmpty()) {
            DataOutputStream outputName = new DataOutputStream(socket.getOutputStream());  // using different library 'DataStreamObject' as need to transmit name before player object created
            outputName.writeUTF(name);  // transmitting player's name to server
            outputName.flush();  // flushing/ deleting transmitted message to allow for new outgoing messages
        }

        messageLabel.setBackground(Color.lightGray);  // setting message box to light grey colour
        frame.getContentPane().add(messageLabel, "South");  // adding message box to bottom 'South' of GUI

        JPanel boardPanel = new JPanel();  // creating component instance within JFrame window
        boardPanel.setBackground(Color.black);  // setting background colour of panel (component) to black
        boardPanel.setLayout(new GridLayout(6, 9, 2, 2));  // creating a 6x9 grid with 2x2 square sizes

        // iterating through each square/element on the board/array
        for (int i = 0; i < board.length; i++) {
            final int j = i;  // 'j' denotes location where player requests their disc to be inserted - has to be final to be accessed by inner mousePressed class
            board[i] = new Square();  // creating a 'Square' object for every square in the board i.e. initialising background colour

            /* using 'MouseAdapter' class to receive mouse events i.e. when player clicks a certain square we insert disc into corresponding column
               using 'MouseListener' method we check if current square has been 'left-clicked' by player's mouse
            */
            board[i].addMouseListener(new MouseAdapter() {
                /*
                    if mouse was clicked on current square 'ith' then we simply insert (i.e. move') to specified location
                    we retrieve the column (from user's square click) and insert disc into lowest possible position via 'possibleMove()' in Server class
                */
                public void mousePressed(MouseEvent e) {
                    //currentSquare = board[j];  // setting current square as clicked
                    out.println("MOVE " + j);  // using 'MOVE' keyword to aid Server to process location player wishes to place disc
                    //System.out.println(j);
                }
            });
            boardPanel.add(board[i]);  // add current square's contents if any to board panel to be displayed
        }
        frame.getContentPane().add(boardPanel, "Center");  // display panel in centre of GUI
    }

    /**
     * Initialises playing state of Connect Five game
     * -> using keywords like 'WELCOME', 'VALID_MOVE'... to distinguish between different game states
     */
    public void play() throws Exception {
        String response;  // used to store messages read in from server

        // using try/catch statements to avoid our program crashing if it fails to retrieve certain elements
        try {
            response = in.readLine();  // reading in current message from server

            // if we receive 'WELCOME' message from server... we then welcome current player to game
            if (response.startsWith("WELCOME")) {
                String input = response.substring(8);  // reading in all characters after 'WELCOME' i.e. the player's name
                System.out.println("\nWelcome To Connect Five " + input + "!");

                icon = new ImageIcon(getClass().getResource("/resources/disc_blue.png"));  // setting current client to 'RED' disc
                opponentIcon = new ImageIcon(getClass().getResource("/resources/disc_red.png"));  // setting opponent client to 'BLUE' disc

                frame.setTitle("Connect Five (Genesys) - Player = " + input);  // setting GUI frame title
            }

            while (true) {
                response = in.readLine();  // reading in current message from server

                // if receive 'VALID_MOVE' from server... insert current player's disc into board and create message
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Opponent's Turn!");  // setting message of current player's GUI to 'Opponent's Turn!'
                    currentSquare=board[Integer.parseInt(response.substring(10))];  // retrieving current square on board where current player inserted disc
                    currentSquare.placeIcon(icon);  // inserting current player's disc colour into board
                    currentSquare.repaint();  // use 'repaint()' method to alter current square colour

                    // if receive 'OPPONENT_MOVED' from server... insert opponent player's disc into board and create message
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));  // retrieving index on board where opponent player inserted disc
                    board[loc].placeIcon(opponentIcon);  // inserting opponent player's disc colour into board
                    board[loc].repaint();  // use 'repaint()' method to alter opponent player's square colour
                    messageLabel.setText("Your Turn!");  // setting message of opponent player's GUI to 'Your Turn!'

                    // if receive 'VICTORY' from server...
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You Win!");  // setting message of current player's GUI to 'Your Win!'
                    break;  // game over i.e. break

                    // if receive 'DEFEAT' from server...
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You Lose!");  // setting message of current player's GUI to 'You Lose!'
                    break;  // game over i.e. break

                    // if receive 'TIE' from server...
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("Draw!");  // setting message of current player's GUI to 'You Lose!'
                    break;  // game over i.e. break

                    // if receive message 'MESSAGE' from server...
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));  // setting message of current player's GUI to current inputted message
                }
            }
        }
        // statement gets executed after try-catch conditions
        finally {
            socket.close();  // close current socket
        }
    }

    /**
     * Square Class: Used to set square background colour to white in grid and used to place current disc (.png) in position requested
     */
    static class Square extends JPanel {
        JLabel label = new JLabel((Icon)null);  // JLabel used to display short string or image icon... set to null initially

        /**
         * Initialising all squares to white initially
         */
        public Square() {
            setBackground(Color.white);
            add(label);
        }

        /**
         * Used to set current square to current player's icon (disc)
         */
        public void placeIcon(Icon icon) {
            label.setIcon(icon);
        }
    }

    /**
     * Main method which creates a new Client instance, creates GUI and initialises current Player to begin playing
     */
    public static void main(String[] args) throws Exception {
        while (true) {
            Client client = new Client("localhost");  // creating new Client object on localhost (this machine's IP address)
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // exit/stop application once GUI closed
            client.frame.setSize(480, 320);  // setting width and height of frame
            client.frame.setVisible(true);  // showing GUI to screen
            client.frame.setResizable(false);  // GUI not resizable (square sizes need to be fixed)
            client.play();  // initialise 'play()' method to begin
            break; // when game ends (winner/draw/user left...) we exit/stop application (Client instance)
        }
    }
}