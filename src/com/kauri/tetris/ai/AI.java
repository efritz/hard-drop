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

package com.kauri.tetris.ai;

import com.kauri.tetris.GameContext;

/**
 * @author Eric Fritz
 */
public class AI
{
	private GameContext context;

	private Move move;
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

			if (move == null || !move.canPerformUpdate()) {
				move = context.getEvaluator().getNextMove(context.getBoard(), context.getCurrent(), context.getX(), context.getY(), context.getPreview(), context.getBoard().getSpawnX(context.getPreview()), context.getBoard().getSpawnY(context.getPreview()));
			}

			if (move.canPerformUpdate()) {
				move.update(context);

				while (move.canPerformUpdate() && context.getAiDelay() == 1) {
					move.update(context);
				}
			}
		}
	}
}
