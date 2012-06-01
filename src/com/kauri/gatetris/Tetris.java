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
import com.kauri.gatetris.command.RotateClockwiseCommand;
import com.kauri.gatetris.command.RotateCounterClockwiseCommand;
import com.kauri.gatetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class Tetris extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	public GameContext context = new GameContext();
	AI ai = new AI(context);
	UI ui = new UI(context);

	boolean runningAi = false;
	boolean autoRestart = false;

	public void start()
	{
		new Thread(this).start();
	}

	public void startNewGame()
	{
		context.newGame();
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
		if (!context.getBoard().canMove(context.getCurrent(), context.getX(), context.getY())) {
			context.setState(State.GAMEOVER);
		}

		if (context.getState() == State.GAMEOVER) {
			if (autoRestart) {
				startNewGame();
			}
		}

		if (context.getState() == State.PLAYING) {
			if (runningAi) {
				ai.update();
			} else {
				long time = System.currentTimeMillis();
				long wait = (long) (((11 - context.getLevel()) * 0.05) * 1000);

				if (time - wait >= lastGravity) {
					lastGravity = time;
					context.storeAndExecute(new SoftDropCommand(context));
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

			if (context.getState() == State.PLAYING && !runningAi) {
				if (keyCode == KeyEvent.VK_LEFT) {
					context.storeAndExecute(new MoveLeftCommand(context));
				}

				if (keyCode == KeyEvent.VK_RIGHT) {
					context.storeAndExecute(new MoveRightCommand(context));
				}

				if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_Z) {
					context.storeAndExecute(new RotateClockwiseCommand(context));
				}

				if (keyCode == KeyEvent.VK_X) {
					context.storeAndExecute(new RotateCounterClockwiseCommand(context));
				}

				if (keyCode == KeyEvent.VK_DOWN) {
					context.storeAndExecute(new SoftDropCommand(context));
				}

				if (keyCode == KeyEvent.VK_SPACE) {
					context.storeAndExecute(new HardDropCommand(context));
				}
			}

			if (context.getState() != State.PAUSED && !runningAi) {
				if (keyCode == KeyEvent.VK_BACK_SPACE) {
					context.undo();
				}
			}

			if (context.getState() == State.PLAYING) {
				if (keyCode == KeyEvent.VK_J) {
					context.storeAndExecute(new AddJunkCommand(context));
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
				context.setAiDelay(Math.max(1, context.getAiDelay() / 2));
			}

			if (keyCode == KeyEvent.VK_MINUS) {
				context.setAiDelay(Math.min(1000, context.getAiDelay() * 2));
			}

			if (keyCode == KeyEvent.VK_N) {
				context.setShowNextPiece(!context.showNextPiece());
			}

			if (keyCode == KeyEvent.VK_S) {
				context.setShowShadowPiece(!context.showShadowPiece());
			}

			if (keyCode == KeyEvent.VK_P) {
				if (context.getState() != State.GAMEOVER) {
					context.setState((context.getState() == State.PAUSED) ? State.PLAYING : State.PAUSED);
				}
			}

			if (keyCode == KeyEvent.VK_PAGE_UP) {
				int width = Math.min(100, Math.max(4, context.getBoard().getWidth() + 1));
				context.setBoard(new Board(width, width * 2));
				startNewGame();
			}

			if (keyCode == KeyEvent.VK_PAGE_DOWN) {
				int width = Math.min(100, Math.max(4, context.getBoard().getWidth() - 1));
				context.setBoard(new Board(width, width * 2));
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
