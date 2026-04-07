package com.nazerke.converging;


public class convergingFunctions {
    public static convergingFunction[] all() {
        return new convergingFunction[]{
                new oneDivSqrtA(),
                new oneDivA()
        };
    }
}