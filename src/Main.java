import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static Chessboard board = new Chessboard();
    public static void main(String[] args) {
        System.out.println("WELCOME TO CHESS!\n\n1. watch a random game of chess (at least in my ide it is quite finicky - don't click on anything or it might stop)\n2. play a game of chess locally\n\nNOTE: promotion moves are denoted by <startpos-endpos=piececode>\nPiececodes:\nn: knight\nb: bishop\nr: rook\nq: queen\nExample promotion to knight: a7-a8=n");
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
        executorService.scheduleAtFixedRate(() -> {
            randomMove();
            printBoard();
            if (board.isGameOver()) {
                executorService.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    static void randomMove() {
        ArrayList<Move> moves = board.getMoves();
        board.makeMove(moves.get(new Random().nextInt(moves.size())));
    }
    static void gameLoop() {
        printBoard();
        if (board.isGameOver()) return;
        System.out.println("Options:\nMake move: -m <startpos-endpos> (Example: -m a2-a4)\nView moves at square: -s <square> (Example: -s a2)\nView all moves: -a\nUndo last move: -u");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine();
        String[] parts = response.split(" ");
        String prefix = parts[0];
        String args = "";
        if (parts.length > 1) {
            args = parts[1];
        }

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
        final String newCoords;
        if (coords.contains("=n")) {
            newCoords = coords.substring(0, 5) + " FLAGS: 16";
        } else if (coords.contains("=b")) {
            newCoords = coords.substring(0, 5) + " FLAGS: 8";
        } else if (coords.contains("=q")) {
            newCoords = coords.substring(0, 5) + " FLAGS: 2";
        } else if (coords.contains("=r")) {
            newCoords = coords.substring(0, 5) + " FLAGS: 4";
        } else {
            newCoords = coords;
        }

        Optional<Move> move = board.getMoves()
                .stream()
                .filter(m -> m.toString().contains(newCoords))
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
        if (board.isGameOver()) {
            if (board.isCheckmate()) {
                System.out.println("CHECKMATE!");
            }
            else if (board.isStalemate()) {
                System.out.println("STALEMATE!");
            }
            else if (board.isInsufficientMaterial()) {
                System.out.println("INSUFFICIENT MATERIAL!");
            }
        }
    }
}