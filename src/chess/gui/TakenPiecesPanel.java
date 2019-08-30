package chess.gui;

import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import chess.gui.Table.MoveLog;
import com.google.common.primitives.Ints;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class TakenPiecesPanel extends JPanel {

    private final JPanel northPanel;
    private final JPanel southPanel;

    private static String defaultPieceImagesPath = "/pieces/plain/";
    private static final Color PANEL_COLOR = Color.decode("0xFDF5E6");
    private static final Dimension TAKEN_PIECES_PANEL_DIMENSION = new Dimension(40, 80);
    private static final EtchedBorder PANEL_BORDER = new EtchedBorder(EtchedBorder.RAISED);

    public TakenPiecesPanel() {
        super(new BorderLayout());
        setBackground(PANEL_COLOR);
        setBorder(PANEL_BORDER);
        this.northPanel = new JPanel(new GridLayout(8, 2));
        this.southPanel = new JPanel(new GridLayout(8, 2));
        this.northPanel.setBackground(PANEL_COLOR);
        this.southPanel.setBackground(PANEL_COLOR);
        add(this.northPanel, BorderLayout.NORTH);
        add(this.southPanel, BorderLayout.SOUTH);
        setPreferredSize(TAKEN_PIECES_PANEL_DIMENSION);
    }

    public void redo(final MoveLog moveLog) {
        southPanel.removeAll();
        northPanel.removeAll();

        final List<Piece> whiteTakenPieces = new ArrayList<>();
        final List<Piece> blackTakenPieces = new ArrayList<>();

        for(final Move move : moveLog.getMoves()) {
            if(move.isAttack()) {
                final Piece takenPiece = move.getAttackedPiece();
                if(takenPiece.getPieceAlliance().isWhite()) {
                    whiteTakenPieces.add(takenPiece);
                } else if(takenPiece.getPieceAlliance().isBlack()){
                    blackTakenPieces.add(takenPiece);
                } else {
                    throw new RuntimeException("Should not reach here!");
                }
            }
        }

        Collections.sort(whiteTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(final Piece p1, final Piece p2) {
                return Ints.compare(p1.getPieceValue(), p2.getPieceValue());
            }
        });

        Collections.sort(blackTakenPieces, new Comparator<Piece>() {
            @Override
            public int compare(final Piece p1, final Piece p2) {
                return Ints.compare(p1.getPieceValue(), p2.getPieceValue());
            }
        });

        for (final Piece takenPiece : whiteTakenPieces) {
            final Image image = new ImageIcon(getClass().getResource(defaultPieceImagesPath
                    + takenPiece.getPieceAlliance().toString().substring(0, 1) + "" + takenPiece.toString()
                    + ".gif")).getImage();
            final ImageIcon ic = new ImageIcon(image);
            final JLabel imageLabel = new JLabel(new ImageIcon(ic.getImage().getScaledInstance(
                    ic.getIconWidth() - 15, ic.getIconWidth() - 15, Image.SCALE_SMOOTH)));
            this.southPanel.add(imageLabel);
        }

        for (final Piece takenPiece : blackTakenPieces) {
            final Image image = new ImageIcon(getClass().getResource(defaultPieceImagesPath
                    + takenPiece.getPieceAlliance().toString().substring(0, 1) + "" + takenPiece.toString()
                    + ".gif")).getImage();
            final ImageIcon ic = new ImageIcon(image);
            final JLabel imageLabel = new JLabel(new ImageIcon(ic.getImage().getScaledInstance(
                    ic.getIconWidth() - 15, ic.getIconWidth() - 15, Image.SCALE_SMOOTH)));
            this.northPanel.add(imageLabel);
        }

        validate();
    }
}
