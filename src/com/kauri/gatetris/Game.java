/*
 * This file is part of the ga-tetris package.
 *
 * Copyright (C) 2012, Eric Fritz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */

package com.kauri.gatetris;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import com.kauri.gatetris.Tetromino.Shape;
import com.kauri.gatetris.ai.DefaultAi;
import com.kauri.gatetris.ai.Strategy;
import com.kauri.gatetris.ai.Strategy.Move;
import com.kauri.gatetris.sequence.PieceSequence;
import com.kauri.gatetris.sequence.ShufflePieceSelector;

/**
 * @author Eric Fritz
 */
public class Game extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	Strategy ai = new DefaultAi();

	private static Map<Shape, Color> colors = new HashMap<Shape, Color>();

	static {
		colors.put(Shape.I, Color.red);
		colors.put(Shape.J, Color.blue);
		colors.put(Shape.L, Color.orange);
		colors.put(Shape.O, Color.yellow);
		colors.put(Shape.S, Color.magenta);
		colors.put(Shape.T, Color.cyan);
		colors.put(Shape.Z, Color.green);
		colors.put(Shape.Junk, Color.darkGray);
		colors.put(Shape.NoShape, new Color(240, 240, 240));
	}

	public enum State {
		PLAYING, PAUSED, GAMEOVER;
	}

	private State state = State.PLAYING;
	private Board board = new Board(10, 22);
	private PieceSequence sequence = new PieceSequence(new ShufflePieceSelector());

	private boolean autoRestart = false;
	private boolean runningAi = false;
	private boolean showAiPiece = false;
	private boolean showNextPiece = false;
	private boolean showShadowPiece = false;

	private long aidelay = 128;

	private long score = 0;
	private long level = 1;
	private long lines = 0;
	private long drops = 0;

	public int xPos;
	public int yPos;
	public Tetromino current = sequence.peekCurrent();
	public Tetromino preview = sequence.peekPreview();

	public void start()
	{
		new Thread(this).start();
	}

	private class InputHandler implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent ke)
		{
			int keyCode = ke.getKeyCode();

			if (!runningAi) {
				if (keyCode == KeyEvent.VK_LEFT) {
					moveLeft();
				}

				if (keyCode == KeyEvent.VK_RIGHT) {
					moveRight();
				}

				if (keyCode == KeyEvent.VK_UP) {
					rotateRight();
				}

				if (keyCode == KeyEvent.VK_DOWN) {
					dropDownOneLine();
				}

				if (keyCode == KeyEvent.VK_SPACE) {
					hardDrop();
				}
			}

			if (keyCode == KeyEvent.VK_J) {
				addJunkLine();
			}

			if (keyCode == KeyEvent.VK_ENTER) {
				startNewGame();
			}

			if (keyCode == KeyEvent.VK_A) {
				runningAi = !runningAi;
			}

			if (keyCode == KeyEvent.VK_U) {
				autoRestart = !autoRestart;
			}

			if (keyCode == 61) {
				aidelay = Math.max(1, aidelay / 2);
			}

			if (keyCode == KeyEvent.VK_MINUS) {
				aidelay = Math.min(1000, aidelay * 2);
			}

			if (keyCode == KeyEvent.VK_N) {
				showNextPiece = !showNextPiece;
			}

			if (keyCode == KeyEvent.VK_S) {
				showShadowPiece = !showShadowPiece;
			}

			if (keyCode == KeyEvent.VK_P) {
				if (state != State.GAMEOVER) {
					state = state == State.PAUSED ? State.PLAYING : State.PAUSED;
				}
			}

			if (keyCode == KeyEvent.VK_PAGE_UP) {
				int width = Math.min(200, Math.max(4, board.getWidth() + 1));
				board = new Board(width, width * 2);
				startNewGame();
			}

			if (keyCode == KeyEvent.VK_PAGE_DOWN) {
				int width = Math.min(200, Math.max(4, board.getWidth() - 1));
				board = new Board(width, width * 2);
				startNewGame();
			}
		}

		@Override
		public void keyReleased(KeyEvent ke)
		{
		}

		@Override
		public void keyTyped(KeyEvent ke)
		{
		}
	}

	public void startNewGame()
	{
		state = State.PLAYING;

		score = 0;
		level = 1;
		lines = 0;
		drops = 0;

		board.clear();

		chooseTetromino();
	}

	@Override
	public void run()
	{
		sequence.advance();

		this.addKeyListener(new InputHandler());

		startNewGame();

		while (true) {
			update();
			render();

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Thread.yield();
		}
	}

	long lastAi = System.currentTimeMillis();
	long lastCounter = System.currentTimeMillis();
	long lastGravity = System.currentTimeMillis();

	private void update()
	{
		if (state == State.GAMEOVER) {
			if (autoRestart) {
				startNewGame();
			}

			return;
		}

		long now = System.currentTimeMillis();

		if (now - 1000 >= lastCounter) {
			lastCounter = now;
			System.out.printf("Score: %-10d Level: %-10d Lines: %-10d Drops: %-10d\n", score, level, lines, drops);
		}

		if (runningAi) {
			if (now - aidelay >= lastAi) {
				lastAi = now;

				Move move = ai.getBestMove(board, current, preview, xPos, yPos);

				for (int i = 0; i < move.rotationDelta; i++) {
					if (!this.rotateLeft()) {
						break;
					}
				}

				while (move.translationDelta < 0) {
					if (!this.moveLeft()) {
						break;
					}

					move.translationDelta++;
				}

				while (move.translationDelta > 0) {
					if (!this.moveRight()) {
						break;
					}

					move.translationDelta--;
				}

				this.hardDrop();
			}
		} else {
			long gravityDelay = Math.max(100, 600 - (level - 1) * 20);

			if (now - gravityDelay >= lastGravity) {
				lastGravity = now;
				dropDownOneLine();
			}
		}
	}

	public boolean moveLeft()
	{
		return tryMove(current, xPos - 1, yPos);
	}

	public boolean moveRight()
	{
		return tryMove(current, xPos + 1, yPos);
	}

	public boolean rotateLeft()
	{
		return tryMove(Tetromino.rotateLeft(current), xPos, yPos);
	}

	public boolean rotateRight()
	{
		return tryMove(Tetromino.rotateRight(current), xPos, yPos);
	}

	public boolean dropDownOneLine()
	{
		if (!isFalling()) {
			return hardDrop();
		}

		return tryMove(current, xPos, yPos - 1);
	}

	public boolean hardDrop()
	{
		return tryMove(current, xPos, board.dropHeight(current, xPos, yPos), true);
	}

	/**
	 * Inserts a randomly generated junk line at the very bottom of the board. The line will consist
	 * of `Shape.Junk` blocks and will contain a random number of holes (normalized to be between 1
	 * and width - 1).
	 */
	public void addJunkLine()
	{
		if (state == State.GAMEOVER) {
			return;
		}

		Shape[] line = new Shape[board.getWidth()];

		for (int i = 0; i < board.getWidth(); i++) {
			line[i] = Shape.Junk;
		}

		int holes = (int) (Math.random() * (board.getWidth() - 1) + 1);

		while (holes > 0) {
			int index = (int) (Math.random() * board.getWidth());

			if (line[index] != Shape.NoShape) {
				line[index] = Shape.NoShape;
				holes--;
			}
		}

		if (!board.canMove(current, xPos, yPos - 1)) {
			dropPiece();
		}

		board.addLine(0, line);
	}

	private boolean tryMove(Tetromino piece, int xPos, int yPos)
	{
		return tryMove(piece, xPos, yPos, false);
	}

	private boolean tryMove(Tetromino piece, int xPos, int yPos, boolean drop)
	{
		if (state != State.PLAYING) {
			return false;
		}

		if (board.canMove(piece, xPos, yPos)) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.current = piece;

			if (drop) {
				dropPiece();
			}

			return true;
		}

		return false;
	}

	private void dropPiece()
	{
		board.tryMove(current, xPos, yPos);

		int numLines = board.clearLines();

		drops = drops + 1;
		lines = lines + numLines;
		level = Math.min(10, drops / 10 + 1);

		score += 24 + 3 * (level - 1);

		if (numLines >= 0) {
			score += 40 * Math.pow(3, numLines - 1);
		}

		chooseTetromino();
	}

	private boolean isFalling()
	{
		return board.canMove(current, xPos, yPos - 1);
	}

	private void render()
	{
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			requestFocus();
			createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		g.setColor(colors.get(Shape.NoShape));
		g.fillRect(0, 0, getWidth(), getHeight());

		int padding = 0;

		int boardWidthCells = board.getWidth();
		int boardHeightCells = board.getHeight();

		int fullDrawingWidth = getWidth() - padding * 2;
		int fullDrawingHeight = getHeight() - padding * 2;

		int squareWidth;
		int squareHeight;

		double targetAspectRatio = boardWidthCells / (double) boardHeightCells;

		int targetDrawingWidth, targetDrawingHeight;

		if (fullDrawingWidth > (targetAspectRatio * fullDrawingHeight)) {
			// fit to height
			targetDrawingWidth = (int) (fullDrawingHeight * targetAspectRatio);
			targetDrawingHeight = fullDrawingHeight;
		} else {
			// fit to width
			targetDrawingWidth = fullDrawingWidth;
			targetDrawingHeight = (int) (fullDrawingWidth / targetAspectRatio);
		}

		if (showNextPiece) {
			squareWidth = targetDrawingWidth / (boardWidthCells + 3);
			squareHeight = targetDrawingHeight / (boardHeightCells + 4);
		} else {
			squareWidth = targetDrawingWidth / boardWidthCells;
			squareHeight = targetDrawingHeight / boardHeightCells;
		}

		int leftMargin = (getWidth() - boardWidthCells * squareWidth) / 2;
		int topMargin = (getHeight() - boardHeightCells * squareHeight) / 2;

		for (int row = 0; row < board.getHeight(); row++) {
			for (int col = 0; col < board.getWidth(); col++) {
				int rowOffset = topMargin + (board.getHeight() - 1 - row) * squareHeight;
				int colOffset = leftMargin + col * squareWidth;

				drawSquare(g, rowOffset, colOffset, squareWidth, squareHeight, colors.get(board.getShapeAt(row, col)));
			}
		}

		if (showShadowPiece) {
			int ghostPosition = board.dropHeight(current, xPos, yPos);

			if (ghostPosition < yPos) {
				int rowOffset = topMargin + (board.getHeight() - 1 - ghostPosition) * squareHeight;
				int colOffset = leftMargin + xPos * squareWidth;

				Color color = colors.get(current.getShape());
				drawTetromino(g, current, rowOffset, colOffset, squareWidth, squareHeight, changeAlpha(color, .3), topMargin);
			}
		} else if (showAiPiece) {
			Move move = ai.getBestMove(board, current, preview, xPos, yPos);

			Tetromino current2 = current;

			for (int i = 0; i < move.rotationDelta; i++) {
				current2 = Tetromino.rotateLeft(current2);
			}

			int ghostPosition = board.dropHeight(current2, xPos + move.translationDelta, yPos);

			if (ghostPosition < yPos) {
				int rowOffset = topMargin + (board.getHeight() - 1 - ghostPosition) * squareHeight;
				int colOffset = leftMargin + (xPos + move.translationDelta) * squareWidth;

				Color color = colors.get(current2.getShape());
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255 / 3);
				drawTetromino(g, current2, rowOffset, colOffset, squareWidth, squareHeight, color, topMargin);
			}
		}

		if (board.canMove(current, xPos, yPos)) {
			int rowOffset = topMargin + (board.getHeight() - 1 - yPos) * squareHeight;
			int colOffset = leftMargin + xPos * squareWidth;

			Color color = colors.get(current.getShape());

			drawTetromino(g, current, rowOffset, colOffset, squareWidth, squareHeight, color, topMargin);
		}

		if (showNextPiece) {
			int xPos = (boardWidthCells - preview.getWidth()) / 2 + Math.abs(preview.getMinX());

			int rowOffset = (topMargin - (preview.getHeight() * squareHeight)) / 2;
			int colOffset = leftMargin + xPos * squareWidth;

			Color color = colors.get(preview.getShape());

			drawTetromino(g, preview, rowOffset, colOffset, squareWidth, squareHeight, color, 0);
		}

		if (state == State.PAUSED) {
			drawString(g, "paused");
		}

		if (state == State.GAMEOVER) {
			drawString(g, "game over");
		}

		g.dispose();
		bs.show();
	}

	private final Font font = new Font("Arial", Font.PLAIN, 40);

	private Color changeAlpha(Color color, double percent)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(1, (int) (color.getAlpha() * percent))));
	}

	private void drawString(Graphics g, String string)
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(font);

		FontMetrics fm = g.getFontMetrics();

		int w = fm.stringWidth(string);
		int h = fm.getAscent();

		int borderPadding = h / 3;

		g.setColor(new Color(0, 0, 0, (int) (255 * .75)));
		g.fillRect(0, (getHeight() / 2) - h - borderPadding, getWidth(), h + borderPadding * 2);

		g.setColor(new Color(255, 255, 255));
		g.drawString(string, (getWidth() / 2) - (w / 2), (getHeight() / 2) - fm.getDescent());
	}

	private void drawTetromino(Graphics g, Tetromino piece, int row, int col, int width, int height, Color color, int top)
	{
		if (piece.getShape() != Shape.NoShape) {
			for (int i = 0; i < piece.getSize(); i++) {
				int xPos = col + piece.getX(i) * width;
				int yPos = row + piece.getY(i) * height;

				if (yPos >= top) {
					drawSquare(g, yPos, xPos, width, height, color);
				}
			}
		}
	}

	private void chooseTetromino()
	{
		sequence.advance();
		current = sequence.peekCurrent();
		preview = sequence.peekPreview();

		xPos = (board.getWidth() - current.getWidth()) / 2 + Math.abs(current.getMinX());
		yPos = board.getHeight() - 1 - current.getMinY();

		if (!board.canMove(current, xPos, yPos)) {
			state = State.GAMEOVER;
		}
	}

	private void drawSquare(Graphics g, int row, int col, int width, int height, Color color)
	{
		g.setColor(color.darker());
		g.fillRect(col, row, width, height);

		g.setColor(color);
		g.fillRect(col + 1, row + 1, width - 2, height - 2);
	}

	public static void main(String[] args)
	{
		Game game = new Game();

		JFrame frame = new JFrame();
		frame.setMinimumSize(new Dimension(300, 600));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(game, BorderLayout.CENTER);
		frame.setVisible(true);

		game.start();
	}
}
