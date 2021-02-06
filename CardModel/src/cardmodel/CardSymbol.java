package cardmodel;

/**
 * Enumerator with the symbols of a deck of playing cards.
 * @author Jose Valera
 * @version 1.0
 * @since 20/10/2020
 */
public enum CardSymbol {
    SA(1),
    S2(2),
    S3(3),
    S4(4),
    S5(5),
    S6(6),
    S7(7),
    SJ(0.5f),
    SQ(0.5f),
    SK(0.5f);

    private float value;

    CardSymbol(float value) {
        this.value = value;
    }

    /**
     * Returns the value of the symbol in the game of seven and a half.
     * @return Value of the symbol in the game of seven and a half.
     */
    public float getValue() {
        return value;
    }

    /**
     * Returns the symbol in text string format.
     * @return The symbol in text string format.
     */
    @Override
    public String toString() {
        return super.toString().substring(1);
    }
}
