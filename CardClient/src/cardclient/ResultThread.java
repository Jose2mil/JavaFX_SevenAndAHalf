package cardclient;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Thread that waits for the server's response when the player finishes
 * their game.<br/><br/>
 * This is a test to prevent the main thread from being temporarily blocked,
 * the same should be done when connecting and waiting for the players when
 * you have to repeat or start, but I did not think it necessary.
 * @author Jose Valera
 * @version 1.0
 * @since 20/12/2020
 */
public class ResultThread extends Thread {
    DataInputStream socketIn;
    Label txtResult;
    HBox hBoxRetry;

    /**
     * Thread constructor that references the client socket and the components
     * that will be modified after receiving the result.
     * @param s The client socket.
     * @param txtResult Label in which the result will be displayed.
     * @param hBoxRetry Replay options container.
     */
    public ResultThread(DataInputStream s, Label txtResult, HBox hBoxRetry) {
        socketIn = s;
        this.txtResult = txtResult;
        this.hBoxRetry = hBoxRetry;
    }

    /**
     * Wait for a message from the server containing the result of the game,
     * show it in the label passed in the constructor and enable the option
     * to repeat game.
     */
    @Override
    public void run() {
        String result = "";

        try {
            result = socketIn.readUTF();
        } catch (IOException e) {
            Platform.runLater(() ->
                    MessageUtils.showError(
                            "The server does not respond!",
                            "The game result could not be obtained.")
            );
        }

        if(!result.isEmpty()) {
            String finalResult = result;
            Platform.runLater(() -> {
                txtResult.setText(finalResult);
                System.out.println(finalResult);
                if(finalResult.equals("YOU WIN!"))
                    txtResult.setTextFill(Color.LIGHTGREEN);

                else if(finalResult.equals("TIE!"))
                    txtResult.setTextFill(Color.YELLOWGREEN);

                else
                    txtResult.setTextFill(Color.INDIANRED);

                txtResult.setVisible(true);
            });
        }

        Platform.runLater(() -> hBoxRetry.setVisible(true));
    }
}
