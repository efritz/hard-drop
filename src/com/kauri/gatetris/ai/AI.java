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

package com.kauri.gatetris.ai;

import com.kauri.gatetris.GameData;
import com.kauri.gatetris.ai.Strategy.Move;
import com.kauri.gatetris.command.HardDropCommand;
import com.kauri.gatetris.command.MoveLeftCommand;
import com.kauri.gatetris.command.MoveRightCommand;
import com.kauri.gatetris.command.RotateClockwiseCommand;
import com.kauri.gatetris.command.RotateCounterClockwiseCommand;
import com.kauri.gatetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class AI
{
	private GameData data;
	private boolean useHardDrops = false;

	private Strategy strategy = new Strategy();

	private int rotationDelta;
	private int translationDelta;

	public AI(GameData data)
	{
		this.data = data;
	}

	public void update()
	{
		if (animate()) {
			return;
		}

		Move m = strategy.getBestMove(data.getBoard(), data.getCurrent(), data.getX(), data.getY());

		rotationDelta = m.rotationDelta;
		translationDelta = m.translationDelta;

		animate();
	}

	private boolean animate()
	{
		if (rotationDelta < 0) {
			rotationDelta++;
			data.storeAndExecute(new RotateClockwiseCommand(data));
		} else if (rotationDelta > 0) {
			rotationDelta--;
			data.storeAndExecute(new RotateCounterClockwiseCommand(data));
		} else if (translationDelta < 0) {
			translationDelta++;
			data.storeAndExecute(new MoveLeftCommand(data));
		} else if (translationDelta > 0) {
			translationDelta--;
			data.storeAndExecute(new MoveRightCommand(data));
		} else {
			if (useHardDrops || !data.getBoard().isFalling(data.getCurrent(), data.getX(), data.getY())) {
				data.storeAndExecute(new HardDropCommand(data));
				return false;
			} else {
				data.storeAndExecute(new SoftDropCommand(data));
			}
		}

		return true;
	}
}
