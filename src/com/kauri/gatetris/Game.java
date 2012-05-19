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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

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

	public enum State {
		PLAYING, PAUSED, GAMEOVER;
	}

	State state = State.PLAYING;
	Board board = new Board(10, 22);
	PieceSequence sequence = new PieceSequence(new ShufflePieceSelector());
	Strategy ai = new DefaultAi();
	UI ui = new UI(this);

	boolean autoRestart = false;
	boolean runningAi = false;
	boolean showAiPiece = false;
	boolean showNextPiece = false;
	boolean showShadowPiece = false;

	long aidelay = 128;

	long pieceValue;

	long score = 0;
	long level = 1;
	long lines = 0;
	long drops = 0;

	int xPos;
	int yPos;
	Tetromino current = sequence.peekCurrent();
	Tetromino preview = sequence.peekPreview();

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

	private class ResizeListener implements ComponentListener
	{
		@Override
		public void componentShown(ComponentEvent ce)
		{
		}

		@Override
		public void componentHidden(ComponentEvent ce)
		{
		}

		@Override
		public void componentMoved(ComponentEvent ce)
		{
		}

		@Override
		public void componentResized(ComponentEvent ce)
		{
			ui.setSize(getWidth(), getHeight());
		}
	}

	@Override
	public void run()
	{
		sequence.advance();
		ui.setSize(getWidth(), getHeight());

		this.addKeyListener(new InputHandler());
		this.addComponentListener(new ResizeListener());

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

	private boolean hardDrops = false;

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

				// TODO - cache this
				// TODO - need AI to be able to make the move it's given (sometimes the rotation
				// delta given is impossible - translation delta usually seems to be okay).

				Move move = ai.getBestMove(board, current, preview, xPos, yPos);

				if (move.rotationDelta > 0) {
					if (move.rotationDelta == 3) {
						if (!this.rotateRight()) {
							this.hardDrop();
						}
					} else {
						if (!this.rotateLeft()) {
							this.hardDrop();
						}
					}
				} else if (move.translationDelta < 0) {
					if (!this.moveLeft()) {
						this.hardDrop();
					}
				} else if (move.translationDelta > 0) {
					if (!this.moveRight()) {
						this.hardDrop();
					}
				} else {
					if (hardDrops) {
						this.hardDrop();
					} else {
						this.dropDownOneLine();
					}
				}
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

		if (tryMove(current, xPos, yPos - 1)) {
			pieceValue = Math.max(0, pieceValue - 1);
			return true;
		}

		return false;
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

		if (numLines >= 0) {
			score += 40 * Math.pow(3, numLines - 1);
		}

		score += pieceValue;

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

		ui.render(g);

		g.dispose();
		bs.show();
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

		pieceValue = 24 + 3 * (level - 1);
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
