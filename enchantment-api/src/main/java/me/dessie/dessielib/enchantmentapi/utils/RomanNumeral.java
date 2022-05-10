package me.dessie.dessielib.enchantmentapi.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages Roman Numeral numbers by being able to format integers into their respective Roman Numeral counterpart.
 */
public enum RomanNumeral {
    /**
     * Represents 1 in Roman Numerals.
     */
    I(1),

    /**
     * Represents 4 in Roman Numerals.
     */
    IV(4),

    /**
     * Represents 5 in Roman Numerals.
     */
    V(5),

    /**
     * Represents 9 in Roman Numerals.
     */
    IX(9),

    /**
     * Represents 10 in Roman Numerals.
     */
    X(10),

    /**
     * Represents 40 in Roman Numerals.
     */
    XL(40),

    /**
     * Represents 50 in Roman Numerals.
     */
    L(50),

    /**
     * Represents 90 in Roman Numerals.
     */
    XC(90),

    /**
     * Represents 100 in Roman Numerals.
     */
    C(100),

    /**
     * Represents 400 in Roman Numerals.
     */
    CD(400),

    /**
     * Represents 500 in Roman Numerals.
     */
    D(500),

    /**
     * Represents 900 in Roman Numerals.
     */
    CM(900),

    /**
     * Represents 1000 in Roman Numerals.
     */
    M(1000);

    private final int value;
    RomanNumeral(int value) {
        this.value = value;
    }

    /**
     * @return A reversed list of the Roman Numerals.
     */
    private static List<RomanNumeral> reversed() {
        List<RomanNumeral> values = Arrays.asList(RomanNumeral.values());
        Collections.reverse(values);
        return values;
    }

    /**
     * @return The integer value of a Roman Numeral.
     */
    public int getValue() {
        return value;
    }

    /**
     * Builds a String of Roman Numerals to correctly display any integer.
     *
     * @param number The Integer to get the Roman Numeral for.
     * @return The String of Roman Numerals that represents the number provided.
     */
    public static String fromInt(int number) {
        StringBuilder s = new StringBuilder();
        List<RomanNumeral> reversed = reversed();

        while(number >= 1) {
            for(RomanNumeral numeral : reversed) {
                if(number >= numeral.getValue()) {
                    s.append(numeral.name());
                    number -= numeral.getValue();
                    break;
                }
            }
        }

        return s.toString();
    }
}
