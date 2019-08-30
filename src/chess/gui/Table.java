package chess.gui;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import chess.engine.player.MoveTransition;
import chess.engine.player.ai.MiniMax;
import chess.engine.player.ai.MoveStrategy;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static javax.swing.JFrame.setDefaultLookAndFeelDecorated;
import static javax.swing.SwingUtilities.*;

public class Table extends Observable {

    private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private static String defaultPieceImagesPath = "/pieces/plain/";
    private final JFrame gameFrame;
    private final GameHistoryPanel gameHistoryPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final BoardPanel boardPanel;
    private final MoveLog moveLog;
    private final GameSetup gameSetup;
    private Board chessBoard;
    private Piece sourceTile;
    private Piece humanMovedPiece;
    private BoardDirection boardDirection;
    private Move computerMove;

    private boolean highlightLegalMoves;

    private final Color lightTileColor = Color.decode("#FFFACD");
    private final Color darkTileColor = Color.decode("#593E1A");

    public static final Table INSTANCE = new Table();

    public static Table get() {
        return INSTANCE;
    }

    private GameSetup getGameSetup() {
        return this.gameSetup;
    }

    private Table() {
        this.gameFrame = new JFrame("ChessGame");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessBoard = Board.createStandardBoard();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.highlightLegalMoves = true;
        this.boardPanel = new BoardPanel();
        this.moveLog = new MoveLog();
        this.addObserver(new TableGameAIWatcher());
        this.gameFrame.setIconImage(new ImageIcon(getClass().getResource("/icon/icon.png")).getImage());
        this.gameSetup = new GameSetup(gameFrame, true);
        this.boardDirection = BoardDirection.NORMAL;
        this.gameFrame.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        setDefaultLookAndFeelDecorated(true);
        this.gameFrame.setResizable(false);
        this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        center(this.gameFrame);
        this.gameFrame.setVisible(true);
    }

    private static void center(final JFrame frame) {
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = frame.getSize().width;
        final int h = frame.getSize().height;
        final int x = (dim.width - w) / 2;
        final int y = (dim.height - h) / 2;
        frame.setLocation(x, y);
    }

    private JMenu createOptionsMenu() {
        final JMenu optionsMenu = new JMenu("Options");

        final JMenuItem resetMenuItem = new JMenuItem("New Game", KeyEvent.VK_P);
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                undoAllMoves();
            }

        });
        optionsMenu.add(resetMenuItem);

        final JMenuItem undoMoveMenuItem = new JMenuItem("Undo last move");
        undoMoveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if(Table.get().getMoveLog().size() > 0) {
                    undoLastMove();
                }
            }
        });
        optionsMenu.add(undoMoveMenuItem);

        final JMenuItem setupGameMenuItem = new JMenuItem("Setup game");
        setupGameMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Table.get().getGameSetup().promptUser();
                Table.get().setUpdate(Table.get().getGameSetup());
            }
        });
        optionsMenu.add(setupGameMenuItem);

        return optionsMenu;
    }

    private void undoLastMove() {
        final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
        this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getToBoard();
        this.computerMove = null;
        Table.get().getMoveLog().removeMove(lastMove);
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
    }

    private void undoAllMoves() {
        for(int i = Table.get().getMoveLog().size() - 1; i >= 0; i--) {
            final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
            this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getToBoard();
        }
        this.computerMove = null;
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
    }

    private void setUpdate(final GameSetup gameSetup) {
        setChanged();
        notifyObservers(gameSetup);
    }

    public void show() {
        invokeLater(new Runnable() {
            public void run() {
                Table.get().getMoveLog().clear();
                Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
            }
        });
    }

    private static class TableGameAIWatcher implements Observer {
        @Override
        public void update(final Observable o, final Object arg) {
            if (Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) &&
            !Table.get().getGameBoard().currentPlayer().isInCheckMate() &&
            !Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
                //create AI thread and execute AI work
                final AIThinkTank thinkTank = new AIThinkTank();
                thinkTank.execute();
            }
            endGameInfo();
        }
    }

    public void updateGameBoard(final Board board) {
        this.chessBoard = board;
    }

    private void moveMadeUpdate(PlayerType playerType) {
        setChanged();
        notifyObservers(playerType);
    }

    private TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }

    private GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }

    private MoveLog getMoveLog() {
        return this.moveLog;
    }

    private void updateComputerMove(final Move move) {
        this.computerMove = move;
    }

    private BoardPanel getBoardPanel() {
        return this.boardPanel;
    }

    private Board getGameBoard() {
        return this.chessBoard;
    }

    private static class AIThinkTank extends SwingWorker<Move, String> {


        private AIThinkTank() {
        }

        @Override
        protected Move doInBackground() throws Exception {
            final MoveStrategy miniMax = new MiniMax(Table.get().gameSetup.getSearchDepth());
            final Move bestMove = miniMax.execute(Table.get().getGameBoard());
            return bestMove;
        }
        @Override
        public void done() {

            try {
                final Move bestMove = get();
                Table.get().updateComputerMove(bestMove);
                Table.get().updateGameBoard(Table.get().getGameBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                Table.get().getMoveLog().addMove(bestMove);
                Table.get().getGameHistoryPanel().redo(Table.get().getGameBoard(), Table.get().getMoveLog());
                Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
                Table.get().getBoardPanel().drawBoard(Table.get().getGameBoard());
                Table.get().moveMadeUpdate(PlayerType.COMPUTER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

    }

    public enum BoardDirection {

        NORMAL {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            BoardDirection opposite() {
                return FLIPED;
            }
        },
        FLIPED {
            @Override
            List<TilePanel> traverse(List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            BoardDirection opposite() {
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
        abstract BoardDirection opposite();
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        tableMenuBar.add(createPreferencesMenu());
        tableMenuBar.add(createOptionsMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("open up that PGN file!");
            }
        });
        fileMenu.add(openPGN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        return fileMenu;
    }

    private JMenu createPreferencesMenu() {
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
        flipBoardMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardDirection = boardDirection.opposite();
                boardPanel.drawBoard(chessBoard);
            }
        });
        preferencesMenu.add(flipBoardMenuItem);

        preferencesMenu.addSeparator();

        final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight Legal Moves", true);

        preferencesMenu.add(legalMoveHighlighterCheckbox);

        legalMoveHighlighterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
            }
        });

        return preferencesMenu;
    }

    private class BoardPanel extends JPanel {
        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }

        public void drawBoard(final Board board) {
            removeAll();
            for (final TilePanel tilePanel: boardDirection.traverse(boardTiles)) {
                tilePanel.drawTile(board);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    public static class MoveLog {

        private final List<Move> moves;

        MoveLog() {
            this.moves = new ArrayList<>();
        }

        public List<Move> getMoves() {
            return this.moves;
        }

        void addMove(final Move move) {
            this.moves.add(move);
        }

        public int size() {
            return this.moves.size();
        }

        void clear() {
            this.moves.clear();
        }

        Move removeMove(final int index) {
            return this.moves.remove(index);
        }

        boolean removeMove(final Move move) {
            return this.moves.remove(move);
        }

        public Move getLastMove() {
            return this.moves.get(this.moves.size() - 1);
        }

    }

    public enum PlayerType {
        HUMAN,
        COMPUTER
    }

    public static void endGameInfo() {
        if (Table.get().getGameBoard().currentPlayer().isInCheckMate()) {
            JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                    "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in checkmate!", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        if (Table.get().getGameBoard().currentPlayer().isInStaleMate()) {
            JOptionPane.showMessageDialog(Table.get().getBoardPanel(),
                    "Game Over: Player " + Table.get().getGameBoard().currentPlayer() + " is in stalemate!", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class TilePanel extends JPanel {

        private final int tileId;

        TilePanel(final BoardPanel boardPanel, final int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);
            highlightTileBorder(chessBoard);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(Table.get().getGameSetup().isAIPlayer(Table.get().getGameBoard().currentPlayer()) ||
                            BoardUtils.isEndGame(Table.get().getGameBoard())) {
                        return;
                    }

                    if (isRightMouseButton(e)) {
                        sourceTile = null;
                        humanMovedPiece = null;
                    } else if (isLeftMouseButton(e)) {
                        if (sourceTile == null) {
                            //first click
                            sourceTile = chessBoard.getPiece(tileId);
                            humanMovedPiece = sourceTile;
                            if (humanMovedPiece == null) {
                                sourceTile = null;
                            }
                        } else {
                            //second click
                            final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getPiecePosition(), tileId);
                            final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                            if (transition.getMoveStatus().isDone()) {
                                chessBoard = transition.getTransitionBoard();
                                moveLog.addMove(move);

                            }
                            sourceTile = null;
                            humanMovedPiece = null;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                gameHistoryPanel.redo(chessBoard, moveLog);
                                takenPiecesPanel.redo(moveLog);
                                if (gameSetup.isAIPlayer(chessBoard.currentPlayer())) {
                                    Table.get().moveMadeUpdate(PlayerType.HUMAN);
                                }
                                boardPanel.drawBoard(chessBoard);
                                endGameInfo();
                            }
                        });
                    }
                }
            });
            validate();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileId).isTileOccupied()) {
                final Image image = new ImageIcon(getClass().getResource(defaultPieceImagesPath +
                        board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0, 1) +
                        board.getTile(this.tileId).getPiece().toString() + ".gif")).getImage();
                add(new JLabel(new ImageIcon(image)));
        }
        }

        public void drawTile(final Board board) {
            assignTileColor();
            assignTilePieceIcon(board);
            highlightTileBorder(board);
            highlightLegals(board);
            highlightLastMove();
            validate();
            repaint();
        }

        private void highlightLegals(final Board board) {
            if (highlightLegalMoves) {
                for (final Move move : pieceLegalMoves(board)) {
                    MoveTransition transition = board.currentPlayer().makeMove(move);
                    if(!transition.getMoveStatus().isDone()){
                        continue;
                    }
                    if (move.getDestinationCoordinate() == this.tileId) {
                        add(new JLabel(new ImageIcon(getClass().getResource("/misc/green_dot.png"))));
                    }
                }
            }
        }

        private void highlightLastMove() {
            if (Table.get().getMoveLog().size() != 0) {
                final Move lastMove = Table.get().getMoveLog().getLastMove();
                if (lastMove != null) {
                    if (this.tileId == lastMove.getCurrentCoordinate()) {
                        setBackground(Color.pink);
                    } else if (this.tileId == lastMove.getDestinationCoordinate()) {
                        setBackground(Color.red);
                    }
                }
            }
        }

        private void highlightTileBorder(final Board board) {
            if(humanMovedPiece != null &&
                    humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance() &&
                    humanMovedPiece.getPiecePosition() == this.tileId) {
                setBorder(BorderFactory.createLineBorder(Color.cyan));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        }

        private Collection<Move> pieceLegalMoves(final Board board) {
            if (humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()) {
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            if (BoardUtils.EIGHTH_RANK[this.tileId] ||
                    BoardUtils.SIXTH_RANK[this.tileId] ||
                    BoardUtils.FOURTH_RANK[this.tileId] ||
                    BoardUtils.SECOND_RANK[this.tileId]) {
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            } else if (BoardUtils.SEVENTH_RANK[this.tileId] ||
                    BoardUtils.FIFTH_RANK[this.tileId] ||
                    BoardUtils.THIRD_RANK[this.tileId] ||
                    BoardUtils.FIRST_RANK[this.tileId]) {
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
            }
        }
    }

}
