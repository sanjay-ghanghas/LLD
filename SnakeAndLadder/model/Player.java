package SnakeAndLadder.model;

public class Player {
    private final String name;
    private int position;

    public Player(String name) {
        this.name = name;
        this.position = 0; // Starting position
    }

    public void moveTo(int newPosition) {
        position = newPosition;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }
}