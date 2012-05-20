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
import java.util.Stack;

import javax.swing.JFrame;

import com.kauri.gatetris.GameData.State;
import com.kauri.gatetris.Tetromino.Shape;
import com.kauri.gatetris.ai.AI;
import com.kauri.gatetris.ai.AI.Move;
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

	private Stack<Command> history = new Stack<Command>();

	public GameData data;

	AI ai = new AI();
	UI ui = new UI(this);

	long aidelay = 128;
	public long pieceValue;
	boolean runningAi = false;
	boolean autoRestart = false;

	public void storeAndExecute(Command command)
	{
		command.execute();
		this.history.add(command);
	}

	public void undo()
	{
		Command command = this.history.pop();

		if (command != null) {
			command.unexecute();
		}
	}

	public void start()
	{
		new Thread(this).start();
	}

	public void startNewGame()
	{
		history.clear();
		data = new GameData(State.PLAYING, new Board(10, 22), new PieceSequence(new ShufflePieceSelector()), 0, 1, 0, 0);

		chooseTetromino();
	}

	@Override
	public void run()
	{
		refreshSize();
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

	private void refreshSize()
	{
		ui.setSize(getWidth(), getHeight());
	}

	long lastAi = System.currentTimeMillis();
	long lastCounter = System.currentTimeMillis();
	long lastGravity = System.currentTimeMillis();

	private boolean hardDrops = false;

	private void update()
	{
		if (data.getState() == State.GAMEOVER) {
			if (autoRestart) {
				startNewGame();
			}

			return;
		}

		long now = System.currentTimeMillis();

		if (now - 1000 >= lastCounter) {
			lastCounter = now;
			System.out.printf("Score: %-10d Level: %-10d Lines: %-10d Drops: %-10d\n", data.getScore(), data.getLevel(), data.getLines(), data.getDrops());
		}

		if (runningAi) {
			if (now - aidelay >= lastAi) {
				lastAi = now;

				// TODO - cache this
				// TODO - need AI to be able to make the move it's given (sometimes the rotation
				// delta given is impossible - translation delta usually seems to be okay).

				Move move = ai.getBestMove(data.getBoard(), data.getCurrent(), data.getPreview(), data.getX(), data.getY());

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
			long gravityDelay = Math.max(100, 600 - (data.getLevel() - 1) * 20);

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

	//
	// TODO - this needs to be a command as well
	//

	/**
	 * Inserts a randomly generated junk line at the very bottom of the board. The line will consist
	 * of `Shape.Junk` blocks and will contain a random number of holes (normalized to be between 1
	 * and width - 1).
	 */
	public void addJunkLine()
	{
		if (data.getState() == State.GAMEOVER) {
			return;
		}

		Shape[] line = new Shape[data.getBoard().getWidth()];

		for (int i = 0; i < data.getBoard().getWidth(); i++) {
			line[i] = Shape.Junk;
		}

		int holes = (int) (Math.random() * (data.getBoard().getWidth() - 1) + 1);

		while (holes > 0) {
			int index = (int) (Math.random() * data.getBoard().getWidth());

			if (line[index] != Shape.NoShape) {
				line[index] = Shape.NoShape;
				holes--;
			}
		}

		if (!data.getBoard().canMove(data.getCurrent(), data.getX(), data.getY() - 1)) {
			dropPiece();
		}

		data.getBoard().addLine(0, line);
	}

	//
	// TODO - add to abstract command class, or game data?
	//

	public boolean tryMove(Tetromino piece, int xPos, int yPos)
	{
		return tryMove(piece, xPos, yPos, false);
	}

	public boolean tryMove(Tetromino piece, int xPos, int yPos, boolean drop)
	{
		if (data.getState() != State.PLAYING) {
			return false;
		}

		if (data.getBoard().canMove(piece, xPos, yPos)) {
			this.data.setX(xPos);
			this.data.setY(yPos);
			this.data.setCurrent(piece);

			if (drop) {
				dropPiece();
			}

			return true;
		}

		return false;
	}

	private void dropPiece()
	{
		data.getBoard().tryMove(data.getCurrent(), data.getX(), data.getY());

		// TODO - make a command for this so it's reversible.

		int numLines = data.getBoard().clearLines();

		data.setDrops(data.getDrops() + 1);
		data.setLines(data.getLines() + numLines);
		data.setLevel(Math.min(10, data.getDrops() / 10 + 1));

		if (numLines >= 0) {
			data.setScore(data.getScore() + (int) (40 * Math.pow(3, numLines - 1)));
		}

		data.setScore(pieceValue);

		chooseTetromino();
	}

	public boolean isFalling()
	{
		return data.getBoard().canMove(data.getCurrent(), data.getX(), data.getY() - 1);
	}

	//
	// TODO - move this to appropriate place.
	//

	private void chooseTetromino()
	{
		data.getSequence().advance();
		data.setCurrent(data.getSequence().peekCurrent());
		data.setPreview(data.getSequence().peekPreview());

		data.setX((data.getBoard().getWidth() - data.getCurrent().getWidth()) / 2 + Math.abs(data.getCurrent().getMinX()));
		data.setY(data.getBoard().getHeight() - 1 - data.getCurrent().getMinY());

		if (!data.getBoard().canMove(data.getCurrent(), data.getX(), data.getY())) {
			data.setState(State.GAMEOVER);
		}

		pieceValue = 24 + 3 * (data.getLevel() - 1);
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
			refreshSize();
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
