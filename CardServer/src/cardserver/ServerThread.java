package cardserver;

import cardmodel.Card;
import cardmodel.Deck;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Thread-handling class that works connections with a
 * card game player.
 * @author Jose Valera
 * @version 1.0
 * @since 20/12/2020
 */
public class ServerThread extends Thread {
    private int numPlayer;

    private Socket service;
    DataInputStream dataSocketIn;
    ObjectOutputStream ObjectSocketOut;
    DataOutputStream DataSocketOut;

    private Deck deck;
    private float totalValue;

    /**
     * Enumerator comprising three states of election: <br/>
     * NOTHING (No election), <br/>
     * YES (Positive election) and <br/>
     * NO (Negative election).
     */
    public enum ELECTION {
        NOTHING,
        YES,
        NO
    }

    private static ELECTION readyToStart = ELECTION.NOTHING;
    private ELECTION ready;
    private boolean gameOver;
    private String gameResult;
    private static ELECTION repeatGame = ELECTION.NOTHING;
    private ELECTION retry;

    private static boolean haveBuggyPlayer = false;
    private boolean connectionErrors = false;

    /**
     * Constructor that initializes the input and output sockets with
     * the player, initializes the necessary variables and assigns the
     * identifier of the player.
     * @param s Socket that connects to the client (Player).
     * @param numPlayer Identification number in the game for the player.
     */
    public ServerThread(Socket s, int numPlayer)
    {
        this.numPlayer = numPlayer;
        setConnection(s);
    }

    /**
     * Returns the current total value of the game of the player who
     * manages the thread.
     * @return current total value.
     */
    public float getTotalValue() {
        return totalValue;
    }

    /**
     * Let all players know whether or not they can start the game.
     * @param ready Boolean that indicates whether or not they can
     *      start the game.
     */
    public static void setReadyToStart(ELECTION ready) {
        readyToStart = ready;
    }

    /**
     * Returns a boolean that responds to whether the player is ready
     * to start the game.
     * @return A boolean that responds to whether the player is ready
     *      to start the game.
     */
    public ELECTION getReady() {
        return ready;
    }

    /**
     * Determines the willingness of the player to start the game.
     * @param ready Willingness of the player to start the game.
     */
    public void setReady(ELECTION ready) {
        this.ready = ready;
    }

    /**
     * Method that checks if the player's game has ended or is still
     * in progress.
     * @return Boolean indicating whether the player's game has ended
     * or is still in progress.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Let all players know whether or not they can repeat the game.
     * @param repeatGame Boolean that indicates whether or not they can
     *      repeat the game.
     */
    public static void setRepeatGame(ELECTION repeatGame) {
        ServerThread.repeatGame = repeatGame;
    }

    /**
     * Returns a boolean that responds to whether the player is ready
     * to retry the game.
     * @return A boolean that responds to whether the player is ready
     *      to retry the game.
     */
    public ELECTION getRetry() {
        return retry;
    }

    /**
     * It allows giving value to the message that the player will receive
     * once his game is over.
     * @param gameResult Message that the player will receive
     * once his game is over.
     */
    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }

    /**
     * Return true if have some player with connection error.
     * @return true if have some player with connection error.
     */
    public static boolean getHaveBuggyPlayer() {
        return haveBuggyPlayer;
    }

    /**
     * Return true if have Connection errors.
     * @return true if have Connection errors.
     */
    public boolean isConnectionErrors() {
        return connectionErrors;
    }

    private void setConnection(Socket s) {
        service = s;

        try {
            dataSocketIn = new DataInputStream(service.getInputStream());
            ObjectSocketOut = new ObjectOutputStream(service.getOutputStream());
            DataSocketOut = new DataOutputStream(service.getOutputStream());
        } catch (IOException e) {
            showConnectionError("Failed to establish connection to client.");
        }
    }

    private void resetData() {
        deck = new Deck();
        totalValue = 0;
        gameOver = false;
        gameResult = "";
        ready = ELECTION.YES;
        retry = ELECTION.NOTHING;
    }

    /**
     * Method that manages the connections with the player in a game
     * or several (if they are repeated).
     */
    @Override
    public void run()
    {
        do {
            resetData();
            waitPlayersToStart();

            play();
            waitToResult();

            askForRetry();
            waitForRetry();

            if(!connectionErrors)
                sendMessage(repeatGame == ELECTION.YES?
                    "RETRY":
                    "FINISH"
                );
        } while (repeatGame == ELECTION.YES);

        closeServer();
        showDebuggerMessage("Left the game.");
    }

    private void waitPlayersToStart() {
        while(readyToStart == ELECTION.NOTHING && !haveBuggyPlayer) {
            sleepSeconds(1);
        }
    }

    private void play() {
        if(!connectionErrors) {
            String message;

            while (!gameOver) {
                sendMessage(getNextCard());
                gameOver = totalValue >= 7.5f;
                showDebuggerMessage("Total value -> " + totalValue);

                if (!gameOver) {
                    message = readMessage();
                    showDebuggerMessage("More cards? -> " + message);

                    gameOver = message == null || message.equals("NO");
                }
            }
        }
    }

    private Card getNextCard() {
        Card card = deck.next();
        totalValue += card.getValue();
        return card;
    }

    private void waitToResult() {
        while(gameResult.isEmpty())
            sleepSeconds(1);

        if(!connectionErrors)
            sendMessage(gameResult);
    }

    private void askForRetry() {
        if(!connectionErrors) {
            String message = readMessage();
            showDebuggerMessage("Retry -> " + message);

            retry = message != null && message.equals("YES") ?
                    ELECTION.YES :
                    ELECTION.NO;
        }
    }

    private void waitForRetry() {
        while(repeatGame == ELECTION.NOTHING)
            sleepSeconds(1);
    }

    private void sendMessage(String message) {
        try {
            DataSocketOut.writeUTF(message);
            DataSocketOut.flush();
        } catch (IOException e) {
            showConnectionError("Error trying to send message to client.");
        }
    }

    private <T extends Serializable> void sendMessage(T message) {
        try {
            ObjectSocketOut.writeObject(message);
            ObjectSocketOut.flush();
        } catch (IOException e) {
            showConnectionError("Error trying to send message to client.");
        }
    }

    private String readMessage() {
        try {
            return dataSocketIn.readUTF();
        } catch (IOException e) {
            showConnectionError("Error trying to read message from client.");
            return null;
        }
    }

    private void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeServer() {
        if (service != null)
            try { service.close(); } catch (IOException ex) {}

        if(dataSocketIn != null)
            try { dataSocketIn.close(); } catch (IOException e) {}

        if(ObjectSocketOut != null)
            try { ObjectSocketOut.close(); } catch (IOException e) {}

        if(DataSocketOut != null)
            try { DataSocketOut.close(); } catch (IOException e) {}
    }

    private void showConnectionError(String message) {
        showDebuggerError(message);
        haveBuggyPlayer = true;
        connectionErrors = true;
    }

    private void showDebuggerError(String message) {
        System.err.println("Player " + numPlayer + ": " + message);
    }

    private void showDebuggerMessage(String message) {
        System.out.println("Player " + numPlayer + ": " + message);
    }
}