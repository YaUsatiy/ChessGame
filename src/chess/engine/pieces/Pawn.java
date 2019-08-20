package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.Move.MajorMove;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static chess.engine.board.BoardUtils.*;

public class Pawn extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATES = { 7, 8, 9, 16 };

    public Pawn(int piecePosition, Alliance pieceAlliance) {
        super(piecePosition, pieceAlliance);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {

        final List<Move> legalMoves = new ArrayList<>();

        for (final int currentCandidateOffset : CANDIDATE_MOVE_COORDINATES) {
            final int candidateDestinationCoordinate = this.piecePosition + (this.getPieceAlliance().getDirection() * currentCandidateOffset);

            if (!isValidTileCoordinate(candidateDestinationCoordinate)){
                continue;
            }

            if (currentCandidateOffset == 8 && !board.getTile(candidateDestinationCoordinate).isTileOccupied()){
                //TODO more work to do here
                legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
            } else if ( currentCandidateOffset == 16 && this.isFirstMove() &&
                    (SECOND_ROW[this.piecePosition] && this.getPieceAlliance().isBlack()) ||
                    (SEVENTH_ROW[this.piecePosition] && this.getPieceAlliance().isWhite()) ){
                        final int behindCandidateDestinationCoordinate = this.piecePosition + (this.getPieceAlliance().getDirection() * 8);// я думаю *16
                        if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                            legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
                        }
                    }
            /*if (isFirstColumnExclusion(candidateDestinationCoordinate, currentCandidateOffset, this.getPieceAlliance()) ||
                    isEighthColumnExclusion(candidateDestinationCoordinate, currentCandidateOffset, this.getPieceAlliance())) {
                break; //мб continue
            }*/
            else if (currentCandidateOffset == 7 &&
                !( (EIGHTH_COLUMN[this.piecePosition] && this.getPieceAlliance().isWhite()) ||
                   (FIRST_COLUMN[this.piecePosition] && this.getPieceAlliance().isBlack()))) {
                            if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                                final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                                if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                                    legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));//здесь AttackMove, на мой взгляд
                                }
                            }
                   }
            else if (currentCandidateOffset == 9 &&
                    !( (FIRST_COLUMN[this.piecePosition] && this.getPieceAlliance().isWhite()) ||
                            (EIGHTH_COLUMN[this.piecePosition] && this.getPieceAlliance().isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));//здесь AttackMove, на мой взгляд
                    }
                }
            }
        }

        return ImmutableList.copyOf(legalMoves);
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset, final Alliance alliance) {
        return (candidateOffset == 7 && (FIRST_COLUMN[currentPosition] && alliance.isBlack())) ||
                (candidateOffset == 9 && FIRST_COLUMN[currentPosition] && alliance.isWhite());
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset, final Alliance alliance){
        return (candidateOffset == 7 && (EIGHTH_COLUMN[currentPosition] && alliance.isWhite())) ||
                (candidateOffset == 9 && (EIGHTH_COLUMN[currentPosition] && alliance.isBlack()));

    }
}
