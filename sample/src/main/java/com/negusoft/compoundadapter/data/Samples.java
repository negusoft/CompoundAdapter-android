package com.negusoft.compoundadapter.data;

import java.util.Random;

/**
 * Some sample dummy data.
 */
public class Samples {

    public static final String[] VALUES = new String[] {
            "ONE",
            "TWO",
            "THREE",
            "FOUR",
            "FIVE",
            "SIX",
            "SEVEN",
            "EIGHT",
            "NINE"
    };

    public static String getRandomSample() {
        return VALUES[new Random().nextInt(VALUES.length)];
    }

}
