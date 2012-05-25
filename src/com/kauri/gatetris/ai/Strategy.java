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

import com.kauri.gatetris.Board;
import com.kauri.gatetris.Tetromino;

/**
 * @author Eric Fritz
 */
public class Strategy
{
	public static class Move
	{
		public int rotationDelta;
		public int translationDelta;

		public Move(int rotationDelta, int translationDelta)
		{
			this.rotationDelta = rotationDelta;
			this.translationDelta = translationDelta;
		}
	}

	private static ScoringSystem scoring = new ScoringSystem();

	static {
		//
		// TODO - Somehow evolve the scoring system between rounds. I'm not sure where this logic
		// should
		// really go. Interface for basic GA algorithm, and then the ai will do what it wants at
		// that
		// point (other strategies could have different weights, etc).
		//

		scoring.setWeights(2, -3, -3, -3, -3, -5, 0, -10);
	}

	private Board dummy = null;

	public Move getBestMove(Board board, Tetromino current, int x, int y)
	{
		int bestRotationDelta = 0;
		int bestTranslationDelta = 0;

		double bestScore = Double.NEGATIVE_INFINITY;

		//
		// TODO - also test for preview piece. I'm not sure what is best here to move into their own
		// methods (it's all pretty integrated).
		//

		for (int rotationDelta = 0; rotationDelta < 4; rotationDelta++) {
			int minTranslationDelta = getMaxTranslationDelta(board, current, x, y, -1);
			int maxTranslationDelta = getMaxTranslationDelta(board, current, x, y, +1);

			if (!board.canMove(current, x, y)) {
				break;
			}

			for (int translationDelta = minTranslationDelta; translationDelta <= maxTranslationDelta; translationDelta++) {
				dummy = board.tryClone(dummy);

				if (dummy.canMove(current, x + translationDelta, dummy.dropHeight(current, x + translationDelta))) {
					dummy.addPiece(current, x + translationDelta, dummy.dropHeight(current, x + translationDelta));

					double score = scoring.score(dummy);

					if (score > bestScore) {
						bestScore = score;
						bestTranslationDelta = translationDelta;
						bestRotationDelta = rotationDelta;
					}
				}
			}

			current = Tetromino.rotateCounterClockwise(current);
		}

		return new Move(bestRotationDelta, bestTranslationDelta);
	}

	private int getMaxTranslationDelta(Board board, Tetromino piece, int x, int y, int step)
	{
		int translation = 0;
		while (board.canMove(piece, x + translation + step, y)) {
			translation += step;
		}

		return translation;
	}
}
