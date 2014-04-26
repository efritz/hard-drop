/*
 * This file is part of the tetris package.
 *
 * Copyright (c) 2014 Eric Fritz
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

package com.kauri.harddrop.ai;

import com.kauri.harddrop.GameContext;
import com.kauri.harddrop.command.Command;
import com.kauri.harddrop.command.HardDropCommand;
import com.kauri.harddrop.command.MoveLeftCommand;
import com.kauri.harddrop.command.MoveRightCommand;
import com.kauri.harddrop.command.RotateClockwiseCommand;
import com.kauri.harddrop.command.SoftDropCommand;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Eric Fritz
 */
public class AI
{
	private GameContext context;

	private long lastUpdate = System.currentTimeMillis();
	private Queue<Command> commands = new LinkedList<>();

	private int delay = 128;
	private boolean enabled = false;
	private boolean training = false;
	private MoveEvaluator evaluator;

	public AI(GameContext context, MoveEvaluator evaluator)
	{
		this.context = context;
		this.evaluator = evaluator;
	}

	public void update()
	{
		long time = System.currentTimeMillis();

		if (time - delay >= lastUpdate) {
			lastUpdate = time;

			if (commands.size() == 0) {
				int x1 = context.getX();
				int y1 = context.getY();
				int x2 = context.getBoard().getSpawnX(context.getPreview());
				int y2 = context.getBoard().getSpawnY(context.getPreview());

				Move move = evaluator.getNextMove(context.getBoard(), context.getCurrent(), x1, y1, context.getPreview(), x2, y2);

				int rDelta = move.getRotationDelta();
				int mDelta = move.getMovementDelta();

				int currX = context.getBoard().getSpawnX(context.getCurrent()) + mDelta;
				int currY = context.getBoard().getSpawnY(context.getCurrent());

				while (rDelta != 0 || mDelta != 0) {
					if (rDelta > 0) {
						rDelta--;
						commands.add(new RotateClockwiseCommand(context));
					} else if (mDelta < 0) {
						mDelta++;
						commands.add(new MoveLeftCommand(context));
					} else if (mDelta > 0) {
						mDelta--;
						commands.add(new MoveRightCommand(context));
					}
				}

				if (delay > 1) {
					while (context.getBoard().isFalling(context.getCurrent(), currX, currY--)) {
						commands.add(new SoftDropCommand(context));
					}
				}

				commands.add(new HardDropCommand(context));
			}

			animate();
		}
	}

	private void animate()
	{
		if (commands.size() > 0) {
			do {
				context.store(commands.remove());
			} while (commands.size() > 0 && delay == 1);
		}
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isTraining()
	{
		return training;
	}

	public void setTraining(boolean training)
	{
		this.training = training;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}
}
