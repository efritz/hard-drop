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
import java.awt.image.BufferStrategy;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import com.kauri.gatetris.Tetromino.Shape;
import com.kauri.gatetris.ai.DefaultAi;
import com.kauri.gatetris.ai.DefaultAi.Move;
import com.kauri.gatetris.command.Command;
import com.kauri.gatetris.command.HardDropCommand;
import com.kauri.gatetris.command.MoveLeftCommand;
import com.kauri.gatetris.command.MoveRightCommand;
import com.kauri.gatetris.command.RotateLeftCommand;
import com.kauri.gatetris.command.RotateRightCommand;
import com.kauri.gatetris.command.SoftDropCommand;
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

	private List<Command> history = new LinkedList<Command>();

	State state = State.PLAYING;
	public Board board = new Board(10, 22);
	PieceSequence sequence = new PieceSequence(new ShufflePieceSelector());
	DefaultAi ai = new DefaultAi();
	UI ui = new UI(this);

	boolean autoRestart = false;
	boolean runningAi = false;
	boolean showAiPiece = false;
	boolean showNextPiece = false;
	boolean showShadowPiece = false;

	long aidelay = 128;

	public long pieceValue;

	long score = 0;
	long level = 1;
	long lines = 0;
	long drops = 0;

	public int xPos;
	public int yPos;
	public Tetromino current = sequence.peekCurrent();
	public Tetromino preview = sequence.peekPreview();

	public void storeAndExecute(Command command)
	{
		this.history.add(command);
		command.execute();
	}

	public void start()
	{
		new Thread(this).start();
	}

	public void startNewGame()
	{
		state = State.PLAYING;

		score = 0;
		level = 1;
		lines = 0;
		drops = 0;

		board.clear();
		history.clear();

		chooseTetromino();
	}

	@Override
	public void run()
	{
		sequence.advance();
		ui.setSize(getWidth(), getHeight());

		this.addKeyListener(new InputHandler(this));
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
						this.storeAndExecute(new RotateRightCommand(this));
					} else {
						this.storeAndExecute(new RotateLeftCommand(this));
					}
				} else if (move.translationDelta < 0) {
					this.storeAndExecute(new MoveLeftCommand(this));
				} else if (move.translationDelta > 0) {
					this.storeAndExecute(new MoveRightCommand(this));
				} else {
					if (hardDrops) {
						this.storeAndExecute(new HardDropCommand(this));
					} else {
						this.storeAndExecute(new SoftDropCommand(this));
					}
				}
			}
		} else {
			long gravityDelay = Math.max(100, 600 - (level - 1) * 20);

			if (now - gravityDelay >= lastGravity) {
				lastGravity = now;
				this.storeAndExecute(new SoftDropCommand(this));
			}
		}
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

	public boolean tryMove(Tetromino piece, int xPos, int yPos)
	{
		return tryMove(piece, xPos, yPos, false);
	}

	public boolean tryMove(Tetromino piece, int xPos, int yPos, boolean drop)
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

		// TODO - make a command for this so it's reversible

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

	public boolean isFalling()
	{
		return board.canMove(current, xPos, yPos - 1);
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
