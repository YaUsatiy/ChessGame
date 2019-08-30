package chess.engine.player;

import chess.engine.board.Board;
import chess.engine.board.Move;

public class MoveTransition {

    private final Board transitionBoard;
    private final Move move;
    private final MoveStatus moveStatus;

    private final Board fromBoard;
    private final Board toBoard;
    private final Move transitionMove;

    public MoveTransition(final Board transitionBoard, final Move move, final MoveStatus moveStatus) {
        this.transitionBoard = transitionBoard;
        this.move = move;
        this.moveStatus = moveStatus;
        fromBoard = null;
        toBoard = null;
        transitionMove = null;
    }

    public MoveTransition(final Board fromBoard,
                          final Board toBoard,
                          final Move transitionMove,
                          final MoveStatus moveStatus) {
        this.fromBoard = fromBoard;
        this.toBoard = toBoard;
        this.transitionMove = transitionMove;
        this.moveStatus = moveStatus;
        transitionBoard = null;
        move = null;
    }

    public Board getToBoard() {
        return this.toBoard;
    }

    public MoveStatus getMoveStatus() {
        return this.moveStatus;
    }

    public Board getTransitionBoard() {
        return this.transitionBoard;
    }
}
