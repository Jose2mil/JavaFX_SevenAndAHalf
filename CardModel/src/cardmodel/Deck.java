package cardmodel;

import java.util.*;

/**
 * Class representing a deck of cards.
 * @author Jose Valera
 * @version 1.0
 * @since 20/10/2020
 */
public class Deck {
    private Stack<Card> cards;

    /**
     * Initialize a deck with the 40 possible combinations in
     * pseudo-random order.
     */
    public Deck() {
        cards = new Stack<>();

        Arrays.stream(CardSuit.values()).forEach(suit -> {
            Arrays.stream(CardSymbol.values()).forEach( symbol -> {
                cards.push(new Card(symbol, suit));
            });
        });

        Collections.shuffle(cards);
    }

    /**
     * Return and remove the next card from the deck.
     * @return The next card of the deck.
     */
    public Card next() {
        if(cards.size() > 0)
            return cards.pop();

        else
            return null;
    }
}
