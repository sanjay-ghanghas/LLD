package SnakeAndLadder.Board;

import java.util.Map;

public class Board {
    private final int size;
    private final Map<Integer, Transition> transitions;

    public Board(int size, Map<Integer, Transition> transitions) {
        this.size = size;
        this.transitions = transitions;
    }

    public int getSize() {
        return size;
    }

    public int resolvePosition(int position) {
        Transition transition = transitions.get(position);

        if (transition == null) {
            return position;
        }

        return transition.apply();
    }
}