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
import com.kauri.gatetris.command.HardDropCommand;
import com.kauri.gatetris.command.MoveLeftCommand;
import com.kauri.gatetris.command.MoveRightCommand;
import com.kauri.gatetris.command.RotateClockwiseCommand;
import com.kauri.gatetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class AI
{
	private GameData data;

	private int rDelta;
	private int mDelta;
	private boolean animating = false;

	private long lastAi = System.currentTimeMillis();

	public AI(GameData data)
	{
		this.data = data;
	}

	public void update()
	{
		long time = System.currentTimeMillis();

		if (time - data.getAiDelay() >= lastAi) {
			lastAi = time;

			if (animating) {
				animating = animate();

				while (animating && data.getAiDelay() == 1) {
					animating = animate();
				}
			}

			if (!animating) {
				Move move = data.getEvaluator().getNextMove(data.getBoard(), data.getCurrent(), data.getX(), data.getY(), data.getPreview(), data.getBoard().getSpawnX(data.getPreview()), data.getBoard().getSpawnY(data.getPreview()));

				rDelta = move.getRotationDelta();
				mDelta = move.getMovementDelta();

				animating = true;
			}
		}
	}

	private boolean animate()
	{
		if (!data.getBoard().isFalling(data.getCurrent(), data.getX(), data.getY())) {
			data.storeAndExecute(new HardDropCommand(data));
			return false;
		}

		if (rDelta > 0) {
			rDelta--;
			data.storeAndExecute(new RotateClockwiseCommand(data));
			return true;
		}

		if (mDelta < 0) {
			mDelta++;
			data.storeAndExecute(new MoveLeftCommand(data));
			return true;
		}

		if (mDelta > 0) {
			mDelta--;
			data.storeAndExecute(new MoveRightCommand(data));
			return true;
		}

		data.storeAndExecute(new SoftDropCommand(data));
		return true;
	}
}
