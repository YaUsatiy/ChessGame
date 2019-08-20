package chess.engine.board;

import chess.engine.pieces.Piece;

public abstract class Move {

    final Board board;
    final Piece movedPiece;
    final int destinationCoordinte;

    private Move(Board board, Piece movedPiece, int destinationCoordinte) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinte = destinationCoordinte;
    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinte) {
            super(board, movedPiece, destinationCoordinte);
        }
    }

    public static final class AttackMove extends Move{

        final Piece attackedPiece;

        public AttackMove(final Board board, final Piece movedPiece, final int destinationCoordinte, Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinte);
            this.attackedPiece = attackedPiece;
        }
    }
}
