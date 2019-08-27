package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.Move.PawnAttackMove;
import chess.engine.board.Move.PawnEnPassantAttackMove;
import chess.engine.board.Move.PawnMove;
import chess.engine.board.Move.PawnPromotion;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static chess.engine.board.BoardUtils.*;

public class Pawn extends Piece {

    private static final int[] CANDIDATE_MOVE_COORDINATES = { 7, 8, 9, 16 };

    public Pawn(final int piecePosition, final Alliance pieceAlliance) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, true);
    }

    public Pawn(final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
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
                if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                    legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate)));
                } else {
                    legalMoves.add(new PawnMove(board, this, candidateDestinationCoordinate));
                }
            } else if ( currentCandidateOffset == 16 && this.isFirstMove() &&
                    ((SEVENTH_RANK[this.piecePosition] && this.getPieceAlliance().isBlack()) ||
                    (SECOND_RANK[this.piecePosition] && this.getPieceAlliance().isWhite())) ){
                        final int behindCandidateDestinationCoordinate = this.piecePosition + (this.getPieceAlliance().getDirection() * 8);// я думаю *16
                        if (!board.getTile(behindCandidateDestinationCoordinate).isTileOccupied() &&
                        !board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                            legalMoves.add(new Move.PawnJump(board, this, candidateDestinationCoordinate));
                        }
                    }
            else if (currentCandidateOffset == 7 && //TODO: +/- decide with EnPassant
                !( (EIGHTH_COLUMN[this.piecePosition] && this.getPieceAlliance().isWhite()) ||
                   (FIRST_COLUMN[this.piecePosition] && this.getPieceAlliance().isBlack()))) {
                            if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                                final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                                if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                                    if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                                        legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate)));
                                    } else {
                                        legalMoves.add(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));//здесь AttackMove, на мой взгляд
                                    }
                                }
                            } else if (board.getEnPassantPawn() != null) {
                                if (board.getEnPassantPawn().piecePosition == (this.piecePosition + (this.pieceAlliance.getOppositeDirection()))) {
                                    final Piece pieceOnCandidate = board.getEnPassantPawn();
                                    if (this.pieceAlliance != pieceOnCandidate.pieceAlliance) {
                                        legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                                    }
                                }
                            }
                   }
            else if (currentCandidateOffset == 9 &&
                    !( (FIRST_COLUMN[this.piecePosition] && this.getPieceAlliance().isWhite()) ||
                            (EIGHTH_COLUMN[this.piecePosition] && this.getPieceAlliance().isBlack()))) {
                if (board.getTile(candidateDestinationCoordinate).isTileOccupied()) {
                    final Piece pieceOnCandidate = board.getTile(candidateDestinationCoordinate).getPiece();
                    if (this.pieceAlliance != pieceOnCandidate.getPieceAlliance()) {
                        if (this.pieceAlliance.isPawnPromotionSquare(candidateDestinationCoordinate)) {
                            legalMoves.add(new PawnPromotion(new PawnMove(board, this, candidateDestinationCoordinate)));
                        } else {
                            legalMoves.add(new PawnAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));//здесь AttackMove, на мой взгляд
                        }
                    }
                } else if (board.getEnPassantPawn() != null) {
                    if (board.getEnPassantPawn().piecePosition == (this.piecePosition - (this.pieceAlliance.getOppositeDirection()))) {
                        final Piece pieceOnCandidate = board.getEnPassantPawn();
                        if (this.pieceAlliance != pieceOnCandidate.pieceAlliance) {
                            legalMoves.add(new PawnEnPassantAttackMove(board, this, candidateDestinationCoordinate, pieceOnCandidate));
                        }
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }

    @Override
    public Pawn movePiece(final Move move) {
        return new Pawn(move.getDestinationCoordinate(), move.getMovedPiece().getPieceAlliance());
    }

    private static boolean isFirstColumnExclusion(final int currentPosition, final int candidateOffset, final Alliance alliance) {
        return (candidateOffset == 7 && (FIRST_COLUMN[currentPosition] && alliance.isBlack())) ||
                (candidateOffset == 9 && FIRST_COLUMN[currentPosition] && alliance.isWhite());
    }

    private static boolean isEighthColumnExclusion(final int currentPosition, final int candidateOffset, final Alliance alliance){
        return (candidateOffset == 7 && (EIGHTH_COLUMN[currentPosition] && alliance.isWhite())) ||
                (candidateOffset == 9 && (EIGHTH_COLUMN[currentPosition] && alliance.isBlack()));

    }

    public Piece getPromotionPiece() {
        return new Queen(this.piecePosition, this.pieceAlliance, false);
    }
}
