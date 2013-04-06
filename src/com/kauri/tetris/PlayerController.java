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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private Map<Integer, Boolean> keys = new HashMap<Integer, Boolean>();

	public PlayerController(GameContext context)
	{
		this.context = context;
	}

	public void update()
	{
		if (checkGravityTimeout()) {
			context.store(new SoftDropCommand(context));
		}

		for (int keyCode : getKeys()) {
			switch (keyCode) {
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
		toggle(ke.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent ke)
	{
		toggle(ke.getKeyCode(), false);
	}

	@Override
	public void keyTyped(KeyEvent ke)
	{
	}

	private void toggle(int keyCode, boolean down)
	{
		keys.put(keyCode, down);
	}

	private List<Integer> getKeys()
	{
		List<Integer> result = new LinkedList<Integer>();

		for (Map.Entry<Integer, Boolean> entry : keys.entrySet()) {
			if (entry.getValue()) {
				result.add(entry.getKey());
			}

			keys.put(entry.getKey(), false);
		}

		return result;
	}
}
