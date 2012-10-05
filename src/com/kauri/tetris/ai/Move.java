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

package com.kauri.tetris.ai;

import com.kauri.tetris.GameContext;
import com.kauri.tetris.command.HardDropCommand;
import com.kauri.tetris.command.MoveLeftCommand;
import com.kauri.tetris.command.MoveRightCommand;
import com.kauri.tetris.command.RotateClockwiseCommand;
import com.kauri.tetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class Move
{
	private double score;
	private int rDelta = 0;
	private int mDelta = 0;
	private boolean animating = true;

	public Move(double score, int rotationDelta, int movementDelta)
	{
		this.score = score;
		this.rDelta = rotationDelta;
		this.mDelta = movementDelta;
	}

	public double getScore()
	{
		return score;
	}

	public int getRotationDelta()
	{
		return rDelta;
	}

	public int getMovementDelta()
	{
		return mDelta;
	}

	public boolean canPerformUpdate()
	{
		return animating;
	}

	public void update(GameContext context)
	{
		if (rDelta > 0) {
			rDelta--;
			context.storeAndExecute(new RotateClockwiseCommand(context));
		} else if (mDelta < 0) {
			mDelta++;
			context.storeAndExecute(new MoveLeftCommand(context));
		} else if (mDelta > 0) {
			mDelta--;
			context.storeAndExecute(new MoveRightCommand(context));
		} else if (context.getBoard().isFalling(context.getCurrent(), context.getX(), context.getY())) {
			context.storeAndExecute(new SoftDropCommand(context));
		} else {
			context.storeAndExecute(new HardDropCommand(context));
			animating = false;
		}
	}
}
