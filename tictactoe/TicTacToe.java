import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

enum Symbol {
    X, O, EMPTY
}

class Cell {
    private final int row;
    private final int col;
    private Symbol symbol;

    Cell(int r, int c, Symbol symbol) {
        row = r;
        col = c;
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }
}

abstract class Player {
    String name;
    Symbol symbol;
    abstract Move getMoveInput(Board board);

    Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }
}

class HumanPlayer extends Player{
    private final Scanner scanner;

    HumanPlayer(String name, Symbol symbol){
        super(name, symbol);
        scanner = new Scanner(System.in);
    }

    @Override
    Move getMoveInput(Board board){
        // getting input from console.
        board.printBoard();
        int row = scanner.nextInt();
        int col = scanner.nextInt();

        return new Move(row, col, this);
    }
}

class Move {
    int row;
    int col;
    Player player;

    Move(int row, int col, Player player) {
        this.row = row;
        this.col = col;
        this.player = player;
    }
}

class Board {
    private final int N;
    Cell[][] grid;
    private final int[] rowCounter;
    private final int[] colCounter;
    private int diagonalCounter;
    private int antiDiagonalCounter;
    int emptyCells;

    Board(int n) {
        N = n;
        grid = new Cell[N][N];
        rowCounter = new int[N];
        colCounter = new int[N];
        diagonalCounter = 0;
        antiDiagonalCounter = 0;
        emptyCells = N*N;

        for (int i = 0; i < N; i++) {
            rowCounter[i] = 0;
            colCounter[i] = 0;
            Cell[] currentRow = new Cell[N];
            for (int j = 0; j < N; j++) {
                currentRow[j] = (new Cell(i, j, Symbol.EMPTY));
            }
            grid[i] = currentRow;
        }
    }

    private boolean isInvalidMove(Move move){
        int row = move.row, col = move.col;
        return row < 0 || col < 0 || row >= N || col >= N;
    }

    public boolean placeMove(Move move) throws IllegalArgumentException {
        if(isInvalidMove(move)){
            throw new IllegalArgumentException("Wrong move, out of board");
        }

        int row = move.row, col = move.col;

        Cell cell = grid[row][col];
        Player player = move.player;

        if (cell.getSymbol() != Symbol.EMPTY) {
            System.out.println("Cell already occupied. Try again.");
            return false;
        }

        cell.setSymbol(player.symbol);
        int counterDelta = player.symbol == Symbol.X ? 1 : -1;
        rowCounter[row] += counterDelta; colCounter[col] += counterDelta;
        if(row == col){
            diagonalCounter += counterDelta;
        }
        if(row + col == N-1){
            antiDiagonalCounter += counterDelta;
        }
        emptyCells--;
        return true;
    }

    public void printBoard(){
        for(int i = 0; i< N; i++){
            for(int j  = 0; j<N; j++){
                System.out.print(grid[i][j].getSymbol() + " ");
            }
            System.out.println();
        }
    }

    public boolean hasWinner(Move move){
        if(isInvalidMove(move)){
            return false;
        }
        int row = move.row, col = move.col;
        return Math.abs(rowCounter[row]) == N || Math.abs(colCounter[col]) == N || Math.abs(diagonalCounter) == N || Math.abs(antiDiagonalCounter) == N;
    }

    public boolean isDraw(Move move){
        return emptyCells == 0 && !hasWinner(move);
    }
}

class Game{
    Board board;
    List<Player> players;
    int turn;
    Player currentPlayer;

    Game(int N){
        board = new Board(N);
        players = new ArrayList<>();
    }

    private void switchTurn(){
        turn = 1 - turn;
        currentPlayer = players.get(turn);
    }

    private void endGame(){
        System.out.println("Game has ended !");
    }

    public void addPlayer(Player player){
        if(players.size() >= 2){
            System.out.println("Maximum two players allowed");
            return;
        }
        players.add(player);
    }

    public void start(){
        if(players.size() != 2){
            System.out.println("Need two players to start the game");
            return;
        }

        turn = 0;
        currentPlayer = players.get(turn);

        while(true){
            Move move = currentPlayer.getMoveInput(board);
            boolean successfulMove = board.placeMove(move);
            if(!successfulMove){
                System.out.println("invalid move, try again");
                continue;
            }
            if(board.hasWinner(move)){
                System.out.printf("Player %s won \n", currentPlayer.name);
                endGame();
                break;
            }
            else if(board.isDraw(move)){
                System.out.println("Draw !!");
                endGame(); break;
            }
            switchTurn();
        }
    }
}

public class TicTacToe{
    public static void main(String[] args){ 
        System.out.println("welcome to tic tac toe game.");
        Player player1 = new HumanPlayer("player1", Symbol.X);
        Player player2 = new HumanPlayer("player2", Symbol.O);

        Game game = new Game(3);
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.start();
    }
}