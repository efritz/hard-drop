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

import com.kauri.gatetris.GameContext.State;
import com.kauri.gatetris.ai.AI;
import com.kauri.gatetris.command.AddJunkCommand;
import com.kauri.gatetris.command.HardDropCommand;
import com.kauri.gatetris.command.MoveLeftCommand;
import com.kauri.gatetris.command.MoveRightCommand;
import com.kauri.gatetris.command.NewTetrominoCommand;
import com.kauri.gatetris.command.RotateClockwiseCommand;
import com.kauri.gatetris.command.RotateCounterClockwiseCommand;
import com.kauri.gatetris.command.SoftDropCommand;
import com.kauri.gatetris.sequence.PieceSequence;
import com.kauri.gatetris.sequence.ShufflePieceSelector;

/**
 * @author Eric Fritz
 */
public class Tetris extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	public GameContext data = new GameContext();
	AI ai = new AI(data);
	UI ui = new UI(data);

	boolean runningAi = false;
	boolean autoRestart = false;

	public void start()
	{
		new Thread(this).start();
	}

	public void startNewGame()
	{
		if (data.getBoard() == null) {
			data.setBoard(new Board(10, 20));
		}

		if (data.getSequence() == null) {
			data.setSequence(new PieceSequence(new ShufflePieceSelector()));
		}

		data.newGame();

		new NewTetrominoCommand(data).execute();
	}

	@Override
	public void run()
	{
		refreshSize();
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

	long lastGravity = System.currentTimeMillis();

	private void update()
	{
		if (data.getState() == State.GAMEOVER) {
			if (autoRestart) {
				startNewGame();
			}
		}

		if (data.getState() == State.PLAYING) {
			if (runningAi) {
				ai.update();
			} else {
				long time = System.currentTimeMillis();
				long wait = (long) (((11 - data.getLevel()) * 0.05) * 1000);

				if (time - wait >= lastGravity) {
					lastGravity = time;
					data.storeAndExecute(new SoftDropCommand(data));
				}
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

	private void refreshSize()
	{
		ui.setSize(getWidth(), getHeight());
	}

	public static void main(String[] args)
	{
		Tetris game = new Tetris();

		JFrame frame = new JFrame();
		frame.setMinimumSize(new Dimension(300, 600));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(game, BorderLayout.CENTER);
		frame.setVisible(true);

		game.start();
	}

	private class InputHandler implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent ke)
		{
			int keyCode = ke.getKeyCode();

			if (data.getState() == State.PLAYING && !runningAi) {
				if (keyCode == KeyEvent.VK_LEFT) {
					data.storeAndExecute(new MoveLeftCommand(data));
				}

				if (keyCode == KeyEvent.VK_RIGHT) {
					data.storeAndExecute(new MoveRightCommand(data));
				}

				if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_Z) {
					data.storeAndExecute(new RotateClockwiseCommand(data));
				}

				if (keyCode == KeyEvent.VK_X) {
					data.storeAndExecute(new RotateCounterClockwiseCommand(data));
				}

				if (keyCode == KeyEvent.VK_DOWN) {
					data.storeAndExecute(new SoftDropCommand(data));
				}

				if (keyCode == KeyEvent.VK_SPACE) {
					data.storeAndExecute(new HardDropCommand(data));
				}
			}

			if (data.getState() != State.PAUSED && !runningAi) {
				if (keyCode == KeyEvent.VK_BACK_SPACE) {
					data.undo();
				}
			}

			if (data.getState() == State.PLAYING) {
				if (keyCode == KeyEvent.VK_J) {
					data.storeAndExecute(new AddJunkCommand(data));
				}
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
				data.setAiDelay(Math.max(1, data.getAiDelay() / 2));
			}

			if (keyCode == KeyEvent.VK_MINUS) {
				data.setAiDelay(Math.min(1000, data.getAiDelay() * 2));
			}

			if (keyCode == KeyEvent.VK_N) {
				data.setShowNextPiece(!data.showNextPiece());
			}

			if (keyCode == KeyEvent.VK_S) {
				data.setShowShadowPiece(!data.showShadowPiece());
			}

			if (keyCode == KeyEvent.VK_P) {
				if (data.getState() != State.GAMEOVER) {
					data.setState((data.getState() == State.PAUSED) ? State.PLAYING : State.PAUSED);
				}
			}

			if (keyCode == KeyEvent.VK_PAGE_UP) {
				int width = Math.min(100, Math.max(4, data.getBoard().getWidth() + 1));
				data.setBoard(new Board(width, width * 2));
				startNewGame();
			}

			if (keyCode == KeyEvent.VK_PAGE_DOWN) {
				int width = Math.min(100, Math.max(4, data.getBoard().getWidth() - 1));
				data.setBoard(new Board(width, width * 2));
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
}
