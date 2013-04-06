/*
 * This file is part of the tetris package.
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

package com.kauri.tetris;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

import com.kauri.tetris.GameContext.State;
import com.kauri.tetris.ai.AI;
import com.kauri.tetris.command.HardDropCommand;
import com.kauri.tetris.command.MoveLeftCommand;
import com.kauri.tetris.command.MoveRightCommand;
import com.kauri.tetris.command.RotateClockwiseCommand;
import com.kauri.tetris.command.RotateCounterClockwiseCommand;
import com.kauri.tetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class Game extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	private GameContext context;

	private AI ai;
	private UI ui;

	private long lastGravity;

	public Game(final GameContext context)
	{
		this.context = context;

		this.ai = new AI(context);
		this.ui = new UI(context);

		refreshSize();
		this.addKeyListener(new PlayerKeyListener());
		this.addComponentListener(new ResizeListener());
	}

	public void start()
	{
		new Thread(this).start();
	}

	@Override
	public void run()
	{
		context.newGame();

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

	private void update()
	{
		context.execute();

		if (context.getState() == State.GAMEOVER) {
			if (context.isAutoRestart()) {
				context.newGame();
			}
		}

		if (context.getState() == State.PLAYING) {
			if (context.isRunningAi()) {
				ai.update();
			} else if (checkGravityTimeout()) {
				context.store(new SoftDropCommand(context));
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

	private boolean checkGravityTimeout()
	{
		long time = System.currentTimeMillis();
		long wait = (long) (((11 - context.getLevel()) * 0.05) * 1000);

		if (time - wait >= lastGravity) {
			lastGravity = time;
			return true;
		}

		return false;
	}

	private void refreshSize()
	{
		ui.setSize(getWidth(), getHeight());
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

	private class PlayerKeyListener implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent ke)
		{
			if (context.getState() != State.PLAYING || context.isRunningAi()) {
				return;
			}

			switch (ke.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					context.store(new MoveLeftCommand(context));
					break;

				case KeyEvent.VK_RIGHT:
					context.store(new MoveRightCommand(context));
					break;

				case KeyEvent.VK_Z:
				case KeyEvent.VK_UP:
					context.store(new RotateClockwiseCommand(context));
					break;

				case KeyEvent.VK_X:
					context.store(new RotateCounterClockwiseCommand(context));
					break;

				case KeyEvent.VK_DOWN:
					context.store(new SoftDropCommand(context));
					break;

				case KeyEvent.VK_SPACE:
					context.store(new HardDropCommand(context));
					break;
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
}