package cardclient;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Class in charge of feedback with the user by launching pop-up windows.
 * @author Jose Valera
 * @version 1.0
 * @since 25/10/2020
 */
public class MessageUtils {
    /**
     * Displays on the screen a pop-up window with a header and content passed
     * by parameter, which is showed and waits for the answer to it
     * ("Accept" or "Cancel") and returns a ButtonType with the answer.
     * @param header Window header.
     * @param message Window message.
     * @return Response returned by dialog.
     */
    public static ButtonType showConfirmation(String header, String message){
        return createAlert("Error Dialog",
                header,
                message,
                Alert.AlertType.CONFIRMATION)
                .showAndWait().get();
    }

    /**
     * Displays a popup window in error format with a header and content passed
     * by parameter on the screen.
     * @param header Window header.
     * @param message Window message.
     */
    public static void showError(String header, String message){
        createAlert("Error Dialog",
                header,
                message,
                Alert.AlertType.ERROR)
                .show();
    }

    /**
     * Displays a popup window in information format with a header and content
     * passed by parameter on the screen.
     * @param header Window header.
     * @param message Window message.
     */
    public static void showMessage(String header, String message){
        createAlert("Message Dialog",
                header,
                message,
                Alert.AlertType.INFORMATION)
                .show();
    }

    private static Alert createAlert(String title, String header, String message, Alert.AlertType alertType){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert;
    }
}
