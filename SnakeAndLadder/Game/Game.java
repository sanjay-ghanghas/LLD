package SnakeAndLadder.Game;

import java.util.List;
import java.util.Map;

import SnakeAndLadder.Board.Board;
import SnakeAndLadder.Board.Transition;
import SnakeAndLadder.Dice.Dice;
import SnakeAndLadder.model.Player;

public class Game {
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;

    public Game(int boardSize, Map<Integer, Transition> transitions, List<Player> players, Dice dice) {
        this.board = new Board(boardSize, transitions);
        this.players = players;
        this.dice = dice;
        this.currentPlayerIndex = 0;
    }

    private void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        int roll = dice.roll();
        System.out.println(currentPlayer.getName() + " rolled " + roll);

        int nextPosition = currentPlayer.getPosition() + roll;
        if (nextPosition <= board.getSize()) {
            nextPosition = board.resolvePosition(nextPosition);
            currentPlayer.moveTo(nextPosition);
        }

        System.out.println(currentPlayer.getName() + " moved to " + currentPlayer.getPosition());
    }

    private void changeTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private boolean hasWon() {
        Player currentPlayer = players.get(currentPlayerIndex);
        return currentPlayer.getPosition() == board.getSize();
    }

    public void play() {
        while (true) {
            playTurn();
            if (hasWon()) {
                Player currentPlayer = players.get(currentPlayerIndex);
                System.out.println(currentPlayer.getName() + " has won the game!");
                break;
            }
            changeTurn();
        }
    }
}
