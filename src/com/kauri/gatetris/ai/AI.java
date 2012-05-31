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

import com.kauri.gatetris.GameContext;
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
	private GameContext context;

	private int rDelta;
	private int mDelta;
	private boolean animating = false;

	private long lastAi = System.currentTimeMillis();

	public AI(GameContext context)
	{
		this.context = context;
	}

	public void update()
	{
		long time = System.currentTimeMillis();

		if (time - context.getAiDelay() >= lastAi) {
			lastAi = time;

			if (animating) {
				animating = animate();

				while (animating && context.getAiDelay() == 1) {
					animating = animate();
				}
			}

			if (!animating) {
				Move move = context.getEvaluator().getNextMove(context.getBoard(), context.getCurrent(), context.getX(), context.getY(), context.getPreview(), context.getBoard().getSpawnX(context.getPreview()), context.getBoard().getSpawnY(context.getPreview()));

				rDelta = move.getRotationDelta();
				mDelta = move.getMovementDelta();

				animating = true;
			}
		}
	}

	private boolean animate()
	{
		if (!context.getBoard().isFalling(context.getCurrent(), context.getX(), context.getY())) {
			context.storeAndExecute(new HardDropCommand(context));
			return false;
		}

		if (rDelta > 0) {
			rDelta--;
			context.storeAndExecute(new RotateClockwiseCommand(context));
			return true;
		}

		if (mDelta < 0) {
			mDelta++;
			context.storeAndExecute(new MoveLeftCommand(context));
			return true;
		}

		if (mDelta > 0) {
			mDelta--;
			context.storeAndExecute(new MoveRightCommand(context));
			return true;
		}

		context.storeAndExecute(new SoftDropCommand(context));
		return true;
	}
}
