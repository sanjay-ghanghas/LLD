package SnakeAndLadder.Board;

public class SnakeLadderTransition implements Transition {
    private final int destination;

    public SnakeLadderTransition(int destination) {
        this.destination = destination;
    }

    @Override
    public int apply() {
        return destination;
    }
}