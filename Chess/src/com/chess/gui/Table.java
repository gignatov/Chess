package com.chess.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import static javax.swing.SwingUtilities.isRightMouseButton;
import static javax.swing.SwingUtilities.isLeftMouseButton;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;

public class Table {

	private final JFrame gameFrame;
	private final BoardPanel boardPanel;
	private Board chessBoard;
	
	private Tile sourceTile;
	private Tile destinationTile;
	private Piece humanMovedPiece;
	
	private static final Dimension OUTER_FRAME_DIMENSION = new Dimension(600,600);
	private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(400,350);
	private static final Dimension TILE_PANEL_DIMENSION = new Dimension(10,10);
	private static String defaultPieceImagesPath = "art/pieces/default/";
	
	private final Color lightTileColor = Color.decode("#EEEED2");
	private final Color darkTileColor = Color.decode("#769656");
	
	public Table() {
		this.gameFrame = new JFrame("Chess");
		this.gameFrame.setLayout(new BorderLayout());
		final JMenuBar tableMenuBar = createTableMenuBar();
		this.gameFrame.setJMenuBar(tableMenuBar);
		this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
		this.chessBoard = Board.createStandardBoard();
		this.boardPanel = new BoardPanel();
		this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
		this.gameFrame.setVisible(true);
	}

	private JMenuBar createTableMenuBar() {
		final JMenuBar tableMenuBar = new JMenuBar();
		tableMenuBar.add(createFileMenu());
		return tableMenuBar;
	}

	private JMenu createFileMenu() {
		final JMenu fileMenu = new JMenu("File");
		final JMenuItem openPGN = new JMenuItem("Load PGN File");
		openPGN.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				System.out.println("open up PGN file!");
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
	
	private class BoardPanel extends JPanel {
		final List<TilePanel> boardTiles;
		
		BoardPanel() {
			super(new GridLayout(8,8));
			this.boardTiles = new ArrayList<>();
			for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
				final TilePanel tilePanel = new TilePanel(this, i);
				this.boardTiles.add(tilePanel);
				add(tilePanel);
			}
			setPreferredSize(BOARD_PANEL_DIMENSION);
			validate();
		}

		public void drawnBoard(final Board board) {
			removeAll();
			for(final TilePanel tilePanel : boardTiles) {
				tilePanel.drawTile(board);
				add(tilePanel);
			}
			validate();
			repaint();
		}
	}
	
	private class TilePanel extends JPanel {
		
		private final int tileId;
		
		TilePanel(BoardPanel boardPanel, final int tileId) {
			super(new GridBagLayout());
			this.tileId = tileId;
			setPreferredSize(TILE_PANEL_DIMENSION);
			assignTileColor();
			assignTilePieceIcon(chessBoard);
			
			addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(final MouseEvent e) {
					
					if(isRightMouseButton(e)) {
						sourceTile = null;
						destinationTile = null;
						humanMovedPiece = null;
					}	
					else if (isLeftMouseButton(e)) {
						if(sourceTile == null) {
							sourceTile = chessBoard.getTile(tileId);
							humanMovedPiece = sourceTile.getPiece();
							if(humanMovedPiece == null) {
								sourceTile = null;
							}
						}
						else {
							destinationTile = chessBoard.getTile(tileId);
							final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate());
							final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
							if(transition.getMoveStatus().isDone()) {
								chessBoard = transition.getTransitionBoard();
								//TODO add move to log
							}
							sourceTile = null;
							destinationTile = null;
							humanMovedPiece = null;
						}
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								boardPanel.drawnBoard(chessBoard);
							}
							
						});
					}
				}

				@Override
				public void mouseEntered(final MouseEvent e) {
					
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					
				}

				@Override
				public void mousePressed(final MouseEvent e) {
					
				}

				@Override
				public void mouseReleased(final MouseEvent e) {
					
				}
				
			});
			
			validate();
		}
		
		public void drawTile(final Board board) {
			assignTileColor();
			assignTilePieceIcon(board);
			validate();
			repaint();
		}
		
		private void assignTilePieceIcon(final Board board) {
			this.removeAll();
			if(board.getTile(this.tileId).isOccupied()) {
				try {
					final BufferedImage image = ImageIO.read(new File(defaultPieceImagesPath + board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0, 1) + 
															board.getTile(this.tileId).getPiece().toString() + ".png"));
					add(new JLabel(new ImageIcon(image)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void assignTileColor() {
			
			if(BoardUtils.EIGHTH_RANK[this.tileId] ||
			   BoardUtils.SIXTH_RANK[this.tileId] ||
			   BoardUtils.FOURTH_RANK[this.tileId] ||
			   BoardUtils.SECOND_RANK[this.tileId]) {
				setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
			}
			else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
					BoardUtils.FIFTH_RANK[this.tileId] ||
					BoardUtils.THIRD_RANK[this.tileId] ||
					BoardUtils.FIRST_RANK[this.tileId]) {
				setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
			}
		}
	}
}
