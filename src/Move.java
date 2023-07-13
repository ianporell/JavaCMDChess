public class Move {
    public int startPos;
    public int endPos;
    public int flags;
    public Move(int startPos, int endPos, int flags) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.flags = flags;
    }

    public Move(int startPos, int endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.flags = FLAGS.NONE;
    }
    @Override
    public String toString() { // only works with 8x8 coordinates, but I don't care
        char[] files = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        String moveString = "";
        moveString += files[this.startPos & 7];
        moveString += 8 - (this.startPos >> 3);
        moveString += '-';
        moveString += files[this.endPos & 7];
        moveString += 8 - (this.endPos >> 3);
        moveString += String.format(" FLAGS: %d", this.flags);

        return moveString;
    }
    @Override
    public boolean equals(Object obj) { // needed for .contains() method to work by value and not reference
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Move otherMove = (Move) obj;
        return startPos == otherMove.startPos && endPos == otherMove.endPos && flags == otherMove.flags;
    }
    public static class FLAGS {
        public static final int NONE = 0;
        public static final int BIG_PAWN_MOVE = 1;
        public static final int Q_PROMOTION = 2;
        public static final int R_PROMOTION = 4;
        public static final int B_PROMOTION = 8;
        public static final int N_PROMOTION = 16;
        public static final int K_CASTLE = 32;
        public static final int Q_CASTLE = 64;
        public static final int EP = 128;
    }
}
