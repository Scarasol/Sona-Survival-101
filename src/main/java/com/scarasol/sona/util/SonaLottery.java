package com.scarasol.sona.util;

import java.util.Random;

public class SonaLottery {
    private final int pool;
    private final int prize;
    private final Random random = new Random();

    private int currentPool;
    private int currentPrize;

    public SonaLottery(int pool, int prize) {
        this.pool = pool;
        this.prize = prize;
        currentPool = pool;
        currentPrize = prize;
    }

    public void reset() {
        currentPrize = prize;
        currentPool = pool;
    }

    public boolean draw() {
        boolean lottery = random.nextDouble() < (double) currentPrize / currentPool;
        if (lottery)
            currentPrize--;
        currentPool--;
        if (currentPool == 0)
            reset();
        return lottery;
    }
}
