import java.awt.desktop.SystemSleepEvent;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static Chessboard board = new Chessboard();
    public static void main(String[] args) {
        System.out.println("WELCOME TO CHESS!\n\n1. watch a random game of chess (moves randomly selected)\n2. play a game of chess locally\n\nNOTE: promotion moves are denoted by <startpos-endpos-piececode>\nPiececodes:\nn: knight\nb: bishop\nr: rook\nq: queen\nExample promotion to knight: a7-a8-n");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextLine()) {
            case "1":
                randomGame();
                break;
            case "2":
                gameLoop();
                break;
            default:
                System.out.println("Not an option. Restart the app to pick an option this time");
                break;
        }
    }
    static void randomGame() {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::randomMove, 0, 1, TimeUnit.SECONDS);
    }
    static void randomMove() {
        printBoard();
        if (board.isGameOver()) {
            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.shutdown();
        }
        ArrayList<Move> moves = board.getMoves();
        board.makeMove(moves.get(new Random().nextInt(moves.size())));
    }
    static void gameLoop() {
        printBoard();
        System.out.println("Options:\nMake move: -m <startpos-endpos> (Example: -m a2-a4)\nView moves at square: -s <square> (Example: -s a2)\nView all moves: -a\nUndo last move: -u");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        String prefix = response.split(" ")[0];
        String args = "";
        if (response.length() > 2) args = response.split(" ")[1];

        switch (prefix) {
            case "-m":
                final Move move = getMoveFromCoords(args);
                if (move != null && board.makeMove(move)) {
                    System.out.println("Move made.");
                }
                else {
                    System.out.println("Invalid move");
                }
                break;
            case "-s":
                showMovesAtSquare(args);
                break;
            case "-a":
                showMoves();
                break;
            case "-u":
                board.undoMove();
                break;
            default:
                System.out.println("Unknown command");
                break;
        }
        gameLoop();
    }
    static Move getMoveFromCoords(String coords) {
        Optional<Move> move = board.getMoves()
                .stream()
                .filter(m -> m.toString().contains(coords))
                .findFirst();

        return move.orElse(null);
    }
    static void showMovesAtSquare(String square) {
        System.out.println("Moves at " + square);
        board.getMoves()
                .stream()
                .map(Move::toString)
                .filter(
                        move -> move.split("-")[0].equals(square)
                ).forEach(System.out::println);
    }
    static void showMoves() {
        for (Move move : board.getMoves()) {
            System.out.println(move.toString());
        }
    }
    static void printBoard() {
        System.out.printf("%s's turn\n", board.turn ? "WHITE" : "BLACK");
        final int[] boardData = board.getBoardData();
        final char[] pieceChars = new char[] {' ', 'p', 'n', 'b', 'r', 'q', 'k'};
        for (int row = 0; row < 8; row++) {
            System.out.printf("%d ", 8 - row);
            for (int col = 0; col < 8; col++) {
                int piece = boardData[row * 8 + col];
                char pieceChar = pieceChars[Math.abs(piece)];
                char printedChar = piece > 0 ? Character.toUpperCase(pieceChar) : pieceChar;
                System.out.print(printedChar + " ");
            }
            System.out.println();
        }
        System.out.println("  a b c d e f g h");
    }
}