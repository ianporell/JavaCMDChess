import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Chessboard {
    private record PastMove(
            int startPos,
            int endPos,
            int flags,
            int enPassantSquare,
            int pieceCaptured,
            int[] castlingRights,
            int pieceType
    ) {}
    public static class CASTLEFLAGS {
        private static final int NONE = 0;
        private static final int KINGSIDE = 1;
        private static final int QUEENSIDE = 2;
        private static final int BOTH = 3;
    }
    public static final int EMPTY = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;
    private static final int X = 7; // out of bounds
    public static final boolean WHITE = true;
    public static final boolean BLACK = false;
    private static final int[] ATTACKS = { // pre-calculated array of possible attacks based on distance
            20, 0, 0, 0, 0, 0, 0, 24,  0, 0, 0, 0, 0, 0,20, 0,
            0,20, 0, 0, 0, 0, 0, 24,  0, 0, 0, 0, 0,20, 0, 0,
            0, 0,20, 0, 0, 0, 0, 24,  0, 0, 0, 0,20, 0, 0, 0,
            0, 0, 0,20, 0, 0, 0, 24,  0, 0, 0,20, 0, 0, 0, 0,
            0, 0, 0, 0,20, 0, 0, 24,  0, 0,20, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,20, 2, 24,  2,20, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 2,53, 56, 53, 2, 0, 0, 0, 0, 0, 0,
            24,24,24,24,24,24,56,  0, 56,24,24,24,24,24,24, 0,
            0, 0, 0, 0, 0, 2,53, 56, 53, 2, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,20, 2, 24,  2,20, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0,20, 0, 0, 24,  0, 0,20, 0, 0, 0, 0, 0,
            0, 0, 0,20, 0, 0, 0, 24,  0, 0, 0,20, 0, 0, 0, 0,
            0, 0,20, 0, 0, 0, 0, 24,  0, 0, 0, 0,20, 0, 0, 0,
            0,20, 0, 0, 0, 0, 0, 24,  0, 0, 0, 0, 0,20, 0, 0,
            20, 0, 0, 0, 0, 0, 0, 24,  0, 0, 0, 0, 0, 0,20
    };

    private static final int[] RAYS = {
            17,  0,  0,  0,  0,  0,  0, 16,  0,  0,  0,  0,  0,  0, 15, 0,
            0, 17,  0,  0,  0,  0,  0, 16,  0,  0,  0,  0,  0, 15,  0, 0,
            0,  0, 17,  0,  0,  0,  0, 16,  0,  0,  0,  0, 15,  0,  0, 0,
            0,  0,  0, 17,  0,  0,  0, 16,  0,  0,  0, 15,  0,  0,  0, 0,
            0,  0,  0,  0, 17,  0,  0, 16,  0,  0, 15,  0,  0,  0,  0, 0,
            0,  0,  0,  0,  0, 17,  0, 16,  0, 15,  0,  0,  0,  0,  0, 0,
            0,  0,  0,  0,  0,  0, 17, 16, 15,  0,  0,  0,  0,  0,  0, 0,
            1,  1,  1,  1,  1,  1,  1,  0, -1, -1,  -1,-1, -1, -1, -1, 0,
            0,  0,  0,  0,  0,  0,-15,-16,-17,  0,  0,  0,  0,  0,  0, 0,
            0,  +0,  0,  0,  0,-15,  0,-16,  0,-17,  0,  0,  0,  0,  0, 0,
            0,  0,  0,  0,-15,  0,  0,-16,  0,  0,-17,  0,  0,  0,  0, 0,
            0,  0,  0,-15,  0,  0,  0,-16,  0,  0,  0,-17,  0,  0,  0, 0,
            0,  0,-15,  0,  0,  0,  0,-16,  0,  0,  0,  0,-17,  0,  0, 0,
            0, -15,  0,  0,  0,  0,  0,-16,  0,  0,  0,  0,  0,-17,  0, 0,
            -15,  0,  0,  0,  0,  0,  0,-16,  0,  0,  0,  0,  0,  0,-17
    };
    private static final int[] PIECE_MASKS = { 0x1, 0x2, 0x4, 0x8, 0x10, 0x20 };
    private static final int[][] PIECE_DIRECTIONS = new int[][] {
            new int[] {-18, -33, -14, -31, 18, 33, 14, 31}, // KNIGHT
            new int[] {-17, -15, 17, 15}, // BISHOP
            new int[] {-16, -1, 16, 1}, // ROOK
            new int[] {-17, -15, 17, 15, -16, -1, 16, 1}, // QUEEN
            new int[] {-17, -16, -15, -1, 1, 17, 16, 15} // KING
    };

    private static final int[] DEFAULT_BOARD = new int[] {
        -ROOK, -KNIGHT, -BISHOP, -QUEEN, -KING, -BISHOP, -KNIGHT, -ROOK,
        -PAWN, -PAWN, -PAWN, -PAWN, -PAWN, -PAWN, -PAWN, -PAWN,
        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
        EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
        PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN, PAWN,
        ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK
    };

    public int[] getBoardData() {
        return arrayFrom0x88(_boardData);
    }
    public ArrayList<Move> getMoves() {
        ArrayList<Move> moves = new ArrayList<Move>();
        for (final Move move : _moves) {
            moves.add(moveFrom0x88(move));
        }
        return moves;
    }
    public int enPassantSquare = -1;
    public int[] castlingRights = new int[] {
            CASTLEFLAGS.BOTH, // white's castling rights
            CASTLEFLAGS.BOTH  // black's castling rights
    };
    public boolean turn = WHITE;
    private int[] _boardData = new int[120]; // 0x88 board representation
    private ArrayList<Move> _moves = new ArrayList<Move>(); // moves at current position
    private ArrayList<PastMove> _history = new ArrayList<PastMove>();
    private int[] _kings = new int[] {-1, -1}; // idx 0 = white, 1 = black
    public Chessboard() {
        populateBoard(DEFAULT_BOARD);
    }
    private static int indexTo0x88(int index) {
        return (index >> 3) // get rank
                * 16 // multiply by rank length of 0x88 board
                + (index & 7); // add file
    }
    private static int indexFrom0x88(int index) {
        return (index >> 4) // get rank
                * 8 // multiply by rank length of 8x8 board
                + (index & 15); // add file
    }
    private static int[] arrayTo0x88(int[] array) {
        int[] arr = new int[120];
        for (int i = 0; i < 120; i++) {
            int index = indexFrom0x88(i);
            arr[i] = (index >= 0 && index < 64) ? array[index] : X;
        }
        return arr;
    }
    private static int[] arrayFrom0x88(int[] array) {
        int[] arr = new int[64];
        for (int i = 0; i < 64; i++) {
            arr[i] = array[indexTo0x88(i)];
        }
        return arr;
    }
    private static Move moveTo0x88(Move move) {
        return new Move(
                indexTo0x88(move.startPos),
                indexTo0x88(move.endPos),
                move.flags
                );
    }
    private static Move moveFrom0x88(Move move) {
        return new Move(
                indexFrom0x88(move.startPos),
                indexFrom0x88(move.endPos),
                move.flags
        );
    }
    private static int getRank(int index) {
        return index >> 4;
    }
    private static int getFile(int index) {
        return index & 15;
    }
    private static boolean isValidIndex(int index) {
        return (index & 0x88) == 0;
        /* Explanation:
           a 0x88 chess board has a length of 120, with the last 8 indexes of every 16 indexes being empty.
           it is used to make some optimizations with the move generation and check/pin system

           example usage of this method:
           index = 6 (0110)
           0110 & 10001000 = 0
           6 is a valid index.

           index = 12 (1100)
           1100 & 10001000 = 1000 (not 0)
           12 is NOT a valid index
        */
    }
    /*
    The removeIllegalMoves method checks for pins and such by calling _makeMove and undoMove repeatedly for every move
    and checking if the king can be captured. As such, _makeMove should not validate moves or check for game over.
    */
    public boolean makeMove(Move move) {
        final Move convertedMove = moveTo0x88(move);
        if (!_moves.contains(convertedMove)) return false; // check if move is valid
        _makeMove(convertedMove);
        _moves = genMoves();
        return true;
    }
    private void _makeMove(Move move) {
        final int pieceCaptured = _boardData[move.endPos];
        final int pieceMoved = _boardData[move.startPos];
        _boardData[move.endPos] = _boardData[move.startPos];
        _boardData[move.startPos] = EMPTY;
        _history.add(new PastMove(
                move.startPos,
                move.endPos,
                move.flags,
                enPassantSquare,
                pieceCaptured,
                castlingRights.clone(),
                pieceMoved
        ));

        if (move.flags == Move.FLAGS.BIG_PAWN_MOVE) {
            final int difference = turn ? 16 : -16;
            enPassantSquare = move.endPos + difference;
        }
        else {
            this.enPassantSquare = -1;
        }

        if (Math.abs(pieceMoved) == PAWN) {
            if (move.flags == Move.FLAGS.EP) {
                final int difference = turn ? 16 : -16;
                _boardData[move.endPos + difference] = 0;
            }
            else if (move.flags == Move.FLAGS.Q_PROMOTION) {
                this._boardData[move.endPos] = turn ? 5 : -5;
            }
            else if (move.flags == Move.FLAGS.R_PROMOTION) {
                this._boardData[move.endPos] = turn ? 4 : -4;
            }
            else if (move.flags == Move.FLAGS.B_PROMOTION) {
                this._boardData[move.endPos] = turn ? 3 : -3;
            }
            else if (move.flags == Move.FLAGS.N_PROMOTION) {
                this._boardData[move.endPos] = turn ? 2 : -2;
            }
        }
        else if (Math.abs(pieceMoved) == ROOK) {
            if (move.startPos == (turn ? 112 : 0)) {
                castlingRights[turn ? 0 : 1] &= ~CASTLEFLAGS.QUEENSIDE; // remove queenside castling rights
            }
            else if (move.startPos == (turn ? 119 : 7)) {
                castlingRights[turn ? 0 : 1] &= ~CASTLEFLAGS.KINGSIDE; // remove kingside castling rights
            }
        }
        else if (Math.abs(pieceMoved) == KING) {
            this.castlingRights[turn ? 0 : 1] = 0;
            updateKingPositions();

            if (move.flags == Move.FLAGS.K_CASTLE) { // move rooks if castled
                _boardData[turn ? 117 : 5] = turn ? 4 : -4;
                _boardData[turn ? 119 : 7] = 0;
            }
            else if (move.flags == Move.FLAGS.Q_CASTLE) {
                _boardData[turn ? 115 : 3] = turn ? 4 : -4;
                _boardData[turn ? 112 : 0] = 0;
            }
        }
        turn = !turn;
    }
    public void undoMove() {
        final PastMove move = _history.remove(_history.size() - 1);
        _boardData[move.startPos] = move.pieceType;
        _boardData[move.endPos] = move.pieceCaptured;

        if (move.flags == Move.FLAGS.EP) {
            _boardData[move.endPos + (turn ? -16 : 16)] = turn ? 1 : -1;
        }
        if (Math.abs(move.pieceType) == KING) {
            updateKingPositions();

            if (move.flags == Move.FLAGS.K_CASTLE) { // move rooks back to original position if castled
                _boardData[turn ? 7 : 119] = turn ? -4 : 4;
                _boardData[turn ? 5 : 117] = 0;
            }
            else if (move.flags == Move.FLAGS.K_CASTLE) {
                _boardData[turn ? 0 : 112] = turn ? -4 : 4;
                _boardData[turn ? 3 : 115] = 0;
            }
        }
        enPassantSquare = move.enPassantSquare;
        castlingRights = move.castlingRights.clone();
        turn = !turn;
    }
    private ArrayList<Move> genMoves() {
        ArrayList<Move> moves = new ArrayList<Move>();
        for (int i = 0; i < 120; i++) {
            if (!isValidIndex(i)) {
                i += 7;
                continue;
            }
            final int piece = _boardData[i];
            if (piece == 0 || piece > 0 != turn) continue;

            moves.addAll(genMovesAtIndex(i));
        }
        moves = removeIllegalMoves(moves);
        return moves;
    }
    private ArrayList<Move> genMovesAtIndex(int index) {
        final int piece = this._boardData[index];
        final int type = Math.abs(piece);
        ArrayList<Move> moves = new ArrayList<Move>();
        switch (type) {
            case EMPTY:
                return moves;
            case PAWN:
                final int multiplier = this.turn ? -1 : 1;
                int tempIndex;

                // small non-capture pawn move
                tempIndex = index + 16 * multiplier;
                if (_boardData[tempIndex] == EMPTY) {
                    moves.add(new Move(index, tempIndex));

                    // big pawn move
                    tempIndex = index + 32 * multiplier;
                    if (getRank(index) == (turn ? 6 : 1) && _boardData[tempIndex] == EMPTY) {
                        moves.add(new Move(index, tempIndex, Move.FLAGS.BIG_PAWN_MOVE));
                    }
                }
                tempIndex = index + 15 * multiplier; // pawn captures
                if (isValidIndex(tempIndex)) {
                    if (_boardData[tempIndex] != EMPTY && _boardData[tempIndex] > 0 != turn) {
                        moves.add(new Move(index, tempIndex));
                    }
                    else if (tempIndex == enPassantSquare) {
                        moves.add(new Move(index, tempIndex, Move.FLAGS.EP));
                    }
                }
                tempIndex = index + 17 * multiplier;
                if (isValidIndex(tempIndex)) {
                if (_boardData[tempIndex] != EMPTY && _boardData[tempIndex] > 0 != turn) {
                        moves.add(new Move(index, tempIndex));
                    }
                    else if (tempIndex == enPassantSquare) {
                        moves.add(new Move(index, tempIndex, Move.FLAGS.EP));
                    }
                }
                List<Move> movesToRemove = new ArrayList<Move>();
                List<Move> movesToAdd = new ArrayList<Move>();

                for (Move move : moves) {
                    if (getRank(move.endPos) == (turn ? 0 : 7)) {
                        Move[] promotionMoves = {
                                new Move(move.startPos, move.endPos, Move.FLAGS.Q_PROMOTION),
                                new Move(move.startPos, move.endPos, Move.FLAGS.N_PROMOTION),
                                new Move(move.startPos, move.endPos, Move.FLAGS.B_PROMOTION),
                                new Move(move.startPos, move.endPos, Move.FLAGS.R_PROMOTION)
                        };
                        movesToRemove.add(move);
                        movesToAdd.addAll(Arrays.asList(promotionMoves));
                    }
                }

                moves.removeAll(movesToRemove);
                moves.addAll(movesToAdd);
                break;
            case KING: // no break statement, falls through to case KNIGHT
                int currentCastlingRights = castlingRights[turn ? 0 : 1];
                if ((currentCastlingRights & CASTLEFLAGS.KINGSIDE) != CASTLEFLAGS.NONE
                        && _boardData[index + 1] == EMPTY
                        && !attacked(index + 1, !turn)
                        && _boardData[index + 2] == EMPTY
                ) {
                    moves.add(new Move(index, index + 2, Move.FLAGS.K_CASTLE));
                }
                if ((currentCastlingRights & CASTLEFLAGS.QUEENSIDE) != CASTLEFLAGS.NONE
                        && _boardData[index - 1] == EMPTY
                        && !attacked(index - 1, !turn)
                        && _boardData[index - 2] == EMPTY
                        && _boardData[index - 3] == EMPTY
                ) {
                    moves.add(new Move(index, index - 2, Move.FLAGS.Q_CASTLE));
                }
            case KNIGHT:
                for (final int dir : PIECE_DIRECTIONS[type - 2]) {
                    int currentSquare = index + dir;
                    if (isValidIndex(currentSquare)
                            && (_boardData[currentSquare] == EMPTY || _boardData[currentSquare] > 0 != turn))
                        moves.add(new Move(index, currentSquare));
                }
                break;
            case BISHOP: // sliding pieces
            case ROOK:
            case QUEEN:
                for (final int dir : PIECE_DIRECTIONS[type - 2]) {
                    int currentSquare = index + dir;
                    while (isValidIndex(currentSquare)) {
                        if (_boardData[currentSquare] == EMPTY) {
                            moves.add(new Move(index, currentSquare));
                        } else if (_boardData[currentSquare] > 0 != turn) {
                            moves.add(new Move(index, currentSquare));
                            break;
                        } else {
                            break;
                        }

                        currentSquare += dir;
                    }
                }
                break;
        }
        return moves;
    }
    private ArrayList<Move> removeIllegalMoves(ArrayList<Move> moves) {
        return moves
                .stream()
                .filter(move -> {
                    _makeMove(move);
                    if (isKingAttacked(!turn)) {
                        undoMove();
                        return false;
                    }
                    else {
                        undoMove();
                        return true;
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public boolean isGameOver() {
        return isCheckmate() || isStalemate() || isInsufficientMaterial();
    }
    public boolean isCheckmate() {
        return this._moves.size() == 0 && isKingAttacked(turn);
    }
    public boolean isStalemate() { return this._moves.size() == 0 && !isKingAttacked(turn); }
    public boolean isInsufficientMaterial() {
        int[] material = new int[2];
        for (int i = 0; i < 120; i++) {
            if (!isValidIndex(i)) {
                i += 7;
                continue;
            }
            final int piece = _boardData[i];
            final boolean team = piece > 0;
            final int type = Math.abs(piece);

            if (type == QUEEN || type == PAWN || type == ROOK) {
                return false;
            }
            else if (type == BISHOP || type == ROOK) {
                material[team ? 0 : 1] += 1;
                if (material[team ? 0 : 1] > 1) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isKingAttacked(boolean team) {
        return attacked(_kings[team ? 0 : 1], !team);
    }
    private boolean attacked(int pieceIndex, boolean attackingTeam) {
        for (int i = 0; i < 120; i++) {
            if (!isValidIndex(i)) {
                i += 7;
                continue;
            }

            int piece = _boardData[i];
            if (piece == EMPTY || (piece > 0) != attackingTeam) continue;

            int difference = i - pieceIndex;
            if (difference == 0) continue;

            int index = difference + 119;

            if ((ATTACKS[index] & PIECE_MASKS[Math.abs(piece) - 1]) != 0) {
                if (Math.abs(piece) == 1) {
                    if (difference > 0) {
                        if (piece > 0) return true;
                    } else {
                        if (piece < 0) return true;
                    }
                } else if (Math.abs(piece) == KNIGHT || Math.abs(piece) == KING) {
                    return true;
                }

                int offset = RAYS[index];
                boolean blocked = false;
                for (int j = i + offset; j != pieceIndex; j += offset) {
                    if (_boardData[j] != EMPTY) {
                        blocked = true;
                    }
                }
                if (!blocked) return true;
            }
        }
        return false;
    }
    private void populateBoard(int[] board) {
        _boardData = arrayTo0x88(board);
        updateKingPositions();
        _moves = genMoves();
    }
    private void updateKingPositions() {
        for (int i = 0; i < 120; i++) {
            if (!isValidIndex(i)) {
                i += 7;
                continue;
            }
            if (_boardData[i] == KING) {
                this._kings[0] = i;
            }
            else if (_boardData[i] == -KING) {
                this._kings[1] = i;
            }
        }
    }
}
