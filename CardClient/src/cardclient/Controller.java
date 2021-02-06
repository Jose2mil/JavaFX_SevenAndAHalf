package cardclient;

import cardmodel.Card;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Class that controls the player window.
 * @author Jose Valera
 * @version 1.0
 * @since 20/12/2020
 */
public class Controller implements Initializable {
    @FXML
    private ListView listCard;
    @FXML
    private HBox hBoxUpperBar;
    @FXML
    private HBox hBoxMoreCards;
    @FXML
    private ImageView imgCurrentCard;
    @FXML
    private HBox hBoxRetry;
    @FXML
    private Label txtResult;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtPort;
    @FXML
    private Label txtCurrentCard;
    @FXML
    private Label txtScore;

    private int defaultPort;

    private Socket mySocket;
    private ObjectInputStream objectSocketIn;
    private DataOutputStream dataSocketOut;
    private DataInputStream dataSocketIn;

    final private Image cardSheet;
    private float totalValue;

    static private Color[] suitsColors =  {
            Color.GOLD, //GOLDS
            Color.CORAL, //CUPS
            Color.DEEPSKYBLUE, //SWORDS
            Color.GREEN //CLUBS
    };

    /**
     * Constructor that initializes the internal variables of the window.
     */
    public Controller() {
        defaultPort = 7000;
        totalValue = 0;
        File file = new File("cards.png");
        cardSheet = new Image(file.toURI().toString());
    }

    /**
     * Method responsible for initializing all the components of the window
     * when it is opened.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        disableMoreCardsOption(true);

        imgCurrentCard.setImage(cardSheet);
        configureListWithImages();
        resetGame();
    }

    private void configureListWithImages(){
        listCard.setCellFactory(param -> new ListCell<Card>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(Card card, boolean empty) {
                super.updateItem(card, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                }

                else {
                    setText(card.toString());
                    setTextFill(suitsColors[card.getSuitIndex()]);

                    imageView.setFitWidth(27.5);
                    imageView.setFitHeight(40);
                    imageView.setImage(cardSheet);

                    showCard(card, imageView);

                    setGraphic(imageView);
                }
            }
        });
    }

    @FXML
    private void connectToServer() {
        disableUpperBar(true);

        try {
            mySocket = new Socket(
                    txtAddress.getText(),
                    extractPortNumber()
            );

            objectSocketIn =
                    new ObjectInputStream(mySocket.getInputStream());
            dataSocketOut =
                    new DataOutputStream(mySocket.getOutputStream());
            dataSocketIn =
                    new DataInputStream(mySocket.getInputStream());

            getNextCard();
        } catch (IOException e) {
            MessageUtils.showError(
                    "Connection error!",
                    "Could not connect to server and port.");
            closeResources();
        }
    }

    private int extractPortNumber() {
        int port;

        try {
            port = Integer.parseInt(txtPort.getText());
        } catch (NumberFormatException e) {
            txtPort.setText(String.valueOf(defaultPort));
            return defaultPort;
        }

        return port;
    }

    private void closeResources() {
        if(mySocket != null) {
            try { mySocket.close(); } catch (IOException e) {}
        }

        if(objectSocketIn != null) {
            try { objectSocketIn.close(); } catch (IOException e) {}
        }

        if(dataSocketOut != null) {
            try { dataSocketOut.close(); } catch (IOException e) {}
        }

        if(dataSocketIn != null) {
            try { dataSocketIn.close(); } catch (IOException e) {}
        }

        disableUpperBar(false);
    }

    @FXML
    private void getNextCard() {
        disableMoreCardsOption(false);

        try {
            Card currentCard = (Card) objectSocketIn.readObject();
            listCard.getItems().add(currentCard);
            totalValue += currentCard.getValue();

            showCard(currentCard, imgCurrentCard);
            setCardName(currentCard);
            setScore();
        } catch (IOException e) {
            MessageUtils.showError(
                    "The server does not respond!",
                    "Could not receive a card from the server.");
            resetByConnectionError();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            resetByConnectionError();
        }

        if(totalValue >= 7.5f) {
            getResult();
            disableMoreCardsOption(true);
        }
    }

    private void setScore() {
        txtScore.setText(String.valueOf(totalValue));
        txtScore.setTextFill(
                totalValue <= 7.5f?
                        Color.BLUE:
                        Color.RED
        );
    }

    private void setCardName(Card card) {
        txtCurrentCard.setText(card.toString());
        txtCurrentCard.setTextFill(suitsColors[card.getSuitIndex()]);
    }

    private void disableUpperBar(boolean disable) {
        hBoxUpperBar.setDisable(disable);
    }

    private void disableMoreCardsOption(boolean disable) {
        hBoxMoreCards.setVisible(!disable);
    }

    @FXML
    private void askForNewCard() {
        if(sendMessageToServer("YES"))
            getNextCard();
    }

    @FXML
    private void refuseNewCard() {
        if(sendMessageToServer("NO"))
            getResult();

        disableMoreCardsOption(true);
    }

    @FXML
    private void retryGame() {
        if(sendMessageToServer("YES")) {
            boolean canRetry = canRetryGame();

            resetGame();

            if (canRetry)
                getNextCard();

            else {
                MessageUtils.showMessage(
                        "Game Over!",
                        "Some player has not been able or wanted to repeat the game.");
                disableUpperBar(false);
            }
        }
    }

    @FXML
    private void NoRetryGame() {
        if(sendMessageToServer("NO")) {
            canRetryGame();
            resetGame();
            disableUpperBar(false);
        }
    }

    private boolean sendMessageToServer(String message) {
        try {
            dataSocketOut.writeUTF(message);
            dataSocketOut.flush();
        } catch (IOException e) {
            MessageUtils.showError(
                    "The server does not respond!",
                    "The server could not receive the message.");
            resetByConnectionError();

            return false;
        }

        return true;
    }

    private void getResult() {
        new ResultThread(dataSocketIn, txtResult, hBoxRetry).start();
    }

    private void showCard(Card cardToShow, ImageView imageView) {
        final int CARD_WIDTH = 55, CARD_HEIGHT = 80;
        int minX, minY;

        if(cardToShow == null) {
            minX = 10 * CARD_WIDTH;
            minY = 0;
        }

        else {
            minX = cardToShow.getSymbolIndex() * CARD_WIDTH;
            minY = cardToShow.getSuitIndex() * CARD_HEIGHT;
        }

        Rectangle2D cardTile =
                new Rectangle2D(
                        minX,
                        minY,
                        CARD_WIDTH,
                        CARD_HEIGHT
                );
        imageView.setViewport(cardTile);
    }

    private void resetGame() {
        listCard.getItems().clear();
        showCard(null, imgCurrentCard);
        totalValue = 0;
        txtScore.setText("...");
        txtResult.setVisible(false);
        txtCurrentCard.setText("...");
        hBoxRetry.setVisible(false);
        txtScore.setTextFill(Color.BLACK);
        txtCurrentCard.setTextFill(Color.BLACK);
    }

    private void resetByConnectionError() {
        disableMoreCardsOption(true);
        resetGame();
        closeResources();
    }

    private boolean canRetryGame() {
        String result = "";

        try {
            result = dataSocketIn.readUTF();
        } catch (IOException e) {
            MessageUtils.showError(
                    "The server does not respond!",
                    "A response from the server was not detected.");
        }

        if(result.equals("RETRY"))
            return true;

        return false;
    }
}
