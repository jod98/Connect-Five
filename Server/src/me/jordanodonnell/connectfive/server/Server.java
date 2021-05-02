package me.jordanodonnell.connectfive.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server Class: Creates connection between server (itself) and two clients (players) and initialises Connect Five game
 */
public class Server {

    public static void main(String[] args) throws Exception {
        int port = 4999;  // port used to bridge connection between client(s) and server

        // server listening on port '4999'... connection closed afterwards as in 'try()'
        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("\nConnect Five Server is Running!");

            while (true) {
                Game game = new Game();  // create a new game instance

                Socket s1 = listener.accept();  // successfully connected to client (player 1) - accepted
                DataInputStream inputName1 = new DataInputStream(s1.getInputStream());  // read in messages from player 1
                String name1 = inputName1.readUTF();  // read in current player 1 message i.e. their name
                String disc1 = "BLUE";  // set player 1 disc colour to red
                Game.Player player1 = game.new Player(s1, name1, disc1);  // create player 1 instance with following arguments

                Socket s2 = listener.accept();  // successfully connected to client (player 2) - accepted
                DataInputStream inputName2 = new DataInputStream(s2.getInputStream());  // read in messages from player 2
                String name2 = inputName2.readUTF();  // read in current player 2 message i.e. their name
                String disc2 = "RED";  // set player 2 disc colour to blue
                Game.Player player2 = game.new Player(s2, name2, disc2);  // create player 2 instance with following arguments

                player1.setOpponent(player2);  // set player 1's opponent as player 2
                player2.setOpponent(player1);  // set player 2's opponent as player 1
                game.currentPlayer = player1;  // set current player as player 1
                player1.start();  // initialise player 1 thread -> run() method
                player2.start();  // initialise player 2 thread -> run() method
            }
        }
    }
}

/**
 * Game Class (Parent class): Empty board initialised and methods to determine...
 1. If there is a winner
 2. If there are any spaces left on board to fill
 3. If move player is trying to make is a possible move
 */
class Game {

    /**
     * Initialising empty board of 54 squares (6x9) grid -> 1D array
     */
    private Player[] board = {
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null};

    /**
     * Current player
     */
    Player currentPlayer;

    /**
     * Returns true if a winner has been found through '4' possible checks
     */
    public boolean foundWinner() {
        /**
         * Winner Check Algorithms Demonstration: https://bit.ly/3f2ipZN
         */

        // Horizontal Check (5 in a row)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 54; j+=9) {
                if (board[i + j]!= null
                        && board[i + j] == board[i + j + 1]
                        && board[i + j] == board[i + j + 2]
                        && board[i + j] ==  board[i + j + 3]
                        && board[i + j] ==  board[i + j + 4]){
                    return true;
                }
            }
        }

        // Vertical Check (5 in a row)
        for (int i = 0; i < 18; i+=9) {
            for (int j = 0; j < 9; j++) {
                if (board[i + j]!= null
                        && board[i + j] == board[i + j + 9]
                        && board[i + j] == board[i + j + 18]
                        && board[i + j] ==  board[i + j + 27]
                        && board[i + j] ==  board[i + j + 36]){
                    return true;
                }
            }
        }


        // Left Diagonal Check (5 in a row)
        for (int i = 0; i< 18; i+=9) {
            for (int j = 0; j <5; j++) {
                if (board[i + j]!= null
                        && board[i + j] == board[i + 9 + j + 1]
                        && board[i + j] == board[i + 18 + j + 2]
                        && board[i + j] ==  board[i + 27 + j + 3]
                        && board[i + j] ==  board[i + 36 + j + 4]){
                    return true;
                }
            }
        }

        // Right Diagonal Check (5 in a row)
        for (int i = 0; i < 18; i+=9) {
            for (int j = 4; j < 9; j++) {
                if (board[i  + j]!= null
                        && board[i +j] == board[i + 9 + j - 1]
                        && board[i +j] == board[i + 18  + j - 2]
                        && board[i +j] ==  board[i + 27 + j - 3]
                        && board[i +j] ==  board[i + 36 + j - 4]){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if no empty spaces left on board (full)
     */
    public boolean boardFull() {
        for (int i = 0; i < 54; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if move player is making is a possible one
     * i.e. if player choose 4th column and lowest row is occupied... place disc in next available row (above) in column
     * 'synchronized' keyword -> allows only one thread to execute inside object at same time
     */
    public synchronized int possibleMove(int location, Player player) {
        int lowestLocationOnBoard = (location % 9)+9*5;  // formula to get lowest possible square (highest value) in given location's column

        // iterate backwards in steps of 9 through board to find lowest possible position we can insert disc (in given location's column)
        for(int i = lowestLocationOnBoard ; i >= location ; i-= 9) {
            // if client is current player and position is not occupied by disc
            if (player == currentPlayer && board[i] == null) {
                System.out.println("Current Player: " + currentPlayer);
                board[i] = currentPlayer;  // set board position of current player to current player's thread id
                currentPlayer = currentPlayer.opponent;  // position occupied not passing over turn to opponent
                currentPlayer.otherPlayerMoved(i);  // set board position of opponent to current player's thread id
                return i;  // return location (index) in which current player inserted disc
            }
        }
        return -1;  // if move not possible i.e. board full, winner declared etc... return -1
    }

    /**
     * Player Class: Initialising attributes of player object and methods to determine:
     1. Each player's opponent
     2. If opponent has moved (i.e. executed their turn)
     3. Current player's commands and process them

     * Threads needed as this is a multi-client server application.
     * Separate thread assigned to clients to differentiate between clients i.e. players
     */

    class Player extends Thread {
        Socket socket;
        String name;
        String discColour;
        Player opponent;
        BufferedReader input;
        PrintWriter output;

        /**
         * Initialising attributes of Player constructor
         * Reading in commands from Player object (thread)
         * Outputting initial messages to Player once connected (thread created)
         */
        public Player(Socket socket, String name, String discColour) {
            this.socket = socket;
            this.name = name;
            this.discColour = discColour;

            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // read messages from client
                output = new PrintWriter(socket.getOutputStream(), true);  // transmit messages to client... flush/delete message once transmitted from memory
                output.println("WELCOME " + name);
                output.println("MESSAGE Waiting for Opponent!");
            }
            catch (IOException e) {
                System.out.println("Player Disconnected! : " + e);  // if fail to retrieve/transmit messages... client then disconnected
            }
        }

        /**
         * Setting opponent for current player
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Determining the status of the game after opponent executes their turn
         */
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);

            if(foundWinner()) {
                output.println("DEFEAT");
            }
            if(boardFull()) {
                output.println("TIE");
            }
        }

        /**
         * Executing run method of this thread
         * Separate threads assigned to each Player object (Client) when both created and connected to Server
         */
        public void run() {
            // this method only executed once all players (clients) have connected to server
            try {
                output.println("MESSAGE All Players Connected!");

                // if current player's disc colour is red... it's their turn
                if (discColour.equals("RED")) {
                    output.println("MESSAGE Your Turn!");
                }

                while (true) {
                    String command = input.readLine();  // reading in commands from client(s)

                    // if player requests to insert disc into board position...
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));  // retrieve board location player requested to insert disc
                        int validLocation = possibleMove(location, this);  // determine if it is possible to insert disc there

                        // if it is a possible move...
                        if (validLocation!= -1) {
                            output.println("VALID_MOVE"+ validLocation);  // output the current board location disc inserted

                            // if winner found...
                            if(foundWinner()) {
                                output.println("VICTORY");
                            }

                            // if board full...
                            if(boardFull()) {
                                output.println("TIE");
                            }

                            // if it is not a possible move...
                        } else {
                            output.println("MESSAGE Not Possible Move!");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player Disconnected!: " + e);  // if fail to retrieve/transmit messages... client then disconnected
            }
            // statement gets executed after try-catch conditions
            finally {
                try {
                    socket.close();  // close current socket if possible
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}