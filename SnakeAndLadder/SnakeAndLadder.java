import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SnakeAndLadder.Board.SnakeLadderTransition;
import SnakeAndLadder.Board.Transition;
import SnakeAndLadder.Dice.Dice;
import SnakeAndLadder.Dice.NormalDice;
import SnakeAndLadder.Game.Game;
import SnakeAndLadder.model.Player;

public class SnakeAndLadder {
    public static void main(String[] args) {
        List<Player> players = new ArrayList<>();
        players.add(new Player("Alice"));
        players.add(new Player("Bob"));

        Map<Integer, Transition> transitions = new HashMap<>();
        transitions.put(3, new SnakeLadderTransition(11));
        transitions.put(10, new SnakeLadderTransition(2));
        transitions.put(15, new SnakeLadderTransition(5));
        transitions.put(20, new SnakeLadderTransition(17));
        transitions.put(25, new SnakeLadderTransition(9));

        Dice dice = new NormalDice();
        Game game = new Game(30, transitions, players, dice);
        game.play();
    }
}