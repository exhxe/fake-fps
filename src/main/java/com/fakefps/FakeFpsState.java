package com.fakefps;

import java.util.Random;

/** 範囲内でそれっぽく揺れる偽FPSを生成する */
public class FakeFpsState {
    private static final Random RANDOM = new Random();
    private static int current = -1;
    private static long lastUpdate = 0;

    public static int get() {
        FakeFpsConfig c = FakeFpsConfig.INSTANCE;
        long now = System.currentTimeMillis();
        boolean outOfRange = current < c.minFps || current > c.maxFps;
        if (current < 0 || outOfRange || now - lastUpdate >= c.intervalMs) {
            update(c);
            lastUpdate = now;
        }
        return current;
    }

    private static void update(FakeFpsConfig c) {
        int range = c.maxFps - c.minFps;
        if (range <= 0) {
            current = c.minFps;
            return;
        }
        if (current < c.minFps || current > c.maxFps || RANDOM.nextInt(10) == 0) {
            // たまに範囲内で大きく飛ぶ
            current = c.minFps + RANDOM.nextInt(range + 1);
            return;
        }
        int step = Math.max(1, range / 4);
        current += RANDOM.nextInt(step * 2 + 1) - step;
        current = Math.max(c.minFps, Math.min(c.maxFps, current));
    }
}
