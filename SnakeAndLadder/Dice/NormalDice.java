package SnakeAndLadder.Dice;

import java.util.concurrent.ThreadLocalRandom;

public class NormalDice implements Dice {
    private static final int FACES = 6;

    @Override
    public int roll() {
        return ThreadLocalRandom.current().nextInt(1, FACES + 1);
    }
}