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

import javax.swing.JFrame;

import com.kauri.gatetris.GameData.State;
import com.kauri.gatetris.ai.AI;
import com.kauri.gatetris.command.NewTetrominoCommand;
import com.kauri.gatetris.command.SoftDropCommand;
import com.kauri.gatetris.sequence.PieceSequence;
import com.kauri.gatetris.sequence.ShufflePieceSelector;

/**
 * @author Eric Fritz
 */
public class Game extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	public GameData data = new GameData();
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

	private void update()
	{
		if (data.getState() == State.GAMEOVER) {
			if (autoRestart) {
				startNewGame();
			}
		}

		if (data.getState() != State.PLAYING) {
			return;
		}

		long now = System.currentTimeMillis();

		if (now - 1000 >= lastCounter) {
			lastCounter = now;

			// System.out.printf("Score: %-10d", data.getScore());
			// System.out.printf("Level: %-10d", data.getLevel());
			// System.out.printf("Lines: %-10d", data.getLines());
			// System.out.printf("Drops: %-10d", data.getDrops());
			// System.out.println();
		}

		if (runningAi) {
			if (now - data.getAiDelay() >= lastAi) {
				lastAi = now;

				ai.update();
			}
		} else {
			long gravityDelay = (long) (((11 - data.getLevel()) * 0.05) * 1000);

			if (now - gravityDelay >= lastGravity) {
				lastGravity = now;
				data.storeAndExecute(new SoftDropCommand(data));
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
