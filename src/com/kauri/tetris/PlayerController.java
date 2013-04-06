/*
 * This file is part of the tetris package.
 *
 * Copyright (C) 2013, Eric Fritz
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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;

import com.kauri.tetris.GameContext.State;
import com.kauri.tetris.command.Command;
import com.kauri.tetris.command.HardDropCommand;
import com.kauri.tetris.command.MoveLeftCommand;
import com.kauri.tetris.command.MoveRightCommand;
import com.kauri.tetris.command.RotateClockwiseCommand;
import com.kauri.tetris.command.RotateCounterClockwiseCommand;
import com.kauri.tetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class PlayerController implements KeyListener
{
	private GameContext context;
	private long lastGravity = System.currentTimeMillis();
	private Queue<Command> commands = new LinkedList<Command>();

	public PlayerController(GameContext context)
	{
		this.context = context;
	}

	public void update()
	{
		if (checkGravityTimeout()) {
			context.store(new SoftDropCommand(context));
		}

		//
		// [BUG] Commands are stashed while AI is running and will be executed when AI is turned
		// off. Disable commands from caching while the player is not enabled (key presses should be
		// a no-op in this situation).

		while (commands.size() > 0) {
			context.store(commands.remove());
		}
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

	@Override
	public void keyPressed(KeyEvent ke)
	{
		if (context.getState() != State.PLAYING) {
			return;
		}

		switch (ke.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				commands.add(new MoveLeftCommand(context));
				break;

			case KeyEvent.VK_RIGHT:
				commands.add(new MoveRightCommand(context));
				break;

			case KeyEvent.VK_Z:
			case KeyEvent.VK_UP:
				commands.add(new RotateClockwiseCommand(context));
				break;

			case KeyEvent.VK_X:
				commands.add(new RotateCounterClockwiseCommand(context));
				break;

			case KeyEvent.VK_DOWN:
				commands.add(new SoftDropCommand(context));
				break;

			case KeyEvent.VK_SPACE:
				commands.add(new HardDropCommand(context));
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
