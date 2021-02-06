package cardmodel;

import java.io.Serializable;

/**
 * Class representing a card.
 * @author Jose Valera
 * @version 1.0
 * @since 20/10/2020
 */
public class Card implements Serializable {
    private CardSymbol symbol;
    private CardSuit suit;

    /**
     * Constructor that initializes a card with a symbol and suit
     * passed by parameter.
     * @param symbol Card's symbol.
     * @param suit Card's suit.
     */
    public Card(CardSymbol symbol, CardSuit suit) {
        this.symbol = symbol;
        this.suit = suit;
    }

    /**
     * Returns the value of the card in the game of seven and a half.
     * @return Value of the card in the game of seven and a half.
     */
    public float getValue() {
        return symbol.getValue();
    }

    /**
     * Returns the index in the enumerator of its symbol.
     * @return The index in the enumerator of its symbol.
     */
    public int getSymbolIndex() {
        return symbol.ordinal();
    }

    /**
     * Returns the index in the enumerator of its suit.
     * @return The index in the enumerator of its suit.
     */
    public int getSuitIndex() {
        return suit.ordinal();
    }

    /**
     * Returns a text string with the card information.
     * @return Card's symbol + " " + Card's suit.
     */
    @Override
    public String toString() {
        return symbol + " " + suit;
    }
}
