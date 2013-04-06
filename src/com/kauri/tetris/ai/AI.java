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

import java.util.LinkedList;
import java.util.Queue;

import com.kauri.tetris.GameContext;
import com.kauri.tetris.command.Command;
import com.kauri.tetris.command.HardDropCommand;
import com.kauri.tetris.command.MoveLeftCommand;
import com.kauri.tetris.command.MoveRightCommand;
import com.kauri.tetris.command.RotateClockwiseCommand;
import com.kauri.tetris.command.SoftDropCommand;

/**
 * @author Eric Fritz
 */
public class AI
{
	private GameContext context;

	private Move move;
	private long lastAi = System.currentTimeMillis();
	private Queue<Command> commands = new LinkedList<Command>();

	private int delay = 128;
	private boolean enabled = false;
	private ScoringSystem scoring = new DefaultScoringSystem();
	private MoveEvaluator evaluator = new MoveEvaluator(scoring);

	// TODO - update this when a game ends
	// private Evolution evo = new Evolution(scoring);

	public AI(GameContext context)
	{
		this.context = context;
	}

	public void update()
	{
		long time = System.currentTimeMillis();

		if (time - delay >= lastAi) {
			lastAi = time;

			if (commands.size() == 0) {
				move = evaluator.getNextMove(context.getBoard(), context.getCurrent(), context.getX(), context.getY(), context.getPreview(), context.getBoard().getSpawnX(context.getPreview()), context.getBoard().getSpawnY(context.getPreview()));

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

	public void setDelay(int delay)
	{
		this.delay = delay;
	}
}
