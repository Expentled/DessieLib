package me.dessie.dessielib.enchantmentapi.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RomanNumeral {
    I(1),
    IV(4),
    V(5),
    IX(9),
    X(10),
    XL(40),
    L(50),
    XC(90),
    C(100),
    CD(400),
    D(500),
    CM(900),
    M(1000);

    int value;

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
