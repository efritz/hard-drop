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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.kauri.tetris.GameContext.State;
import com.kauri.tetris.command.HardDropCommand;
import com.kauri.tetris.command.MoveLeftCommand;
import com.kauri.tetris.command.MoveRightCommand;
import com.kauri.tetris.command.RotateClockwiseCommand;
import com.kauri.tetris.command.RotateCounterClockwiseCommand;
import com.kauri.tetris.command.SoftDropCommand;

class InputHandler implements KeyListener
{
	private final GameContext context;

	public InputHandler(GameContext context)
	{
		this.context = context;
	}

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
