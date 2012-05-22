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

import java.util.Comparator;

import com.kauri.gatetris.Board;
import com.kauri.gatetris.Game;
import com.kauri.gatetris.Tetromino;
import com.kauri.gatetris.command.HardDropCommand;
import com.kauri.gatetris.command.MoveLeftCommand;
import com.kauri.gatetris.command.MoveRightCommand;
import com.kauri.gatetris.command.RotateLeftCommand;
import com.kauri.gatetris.command.RotateRightCommand;

/**
 * @author Eric Fritz
 */
public class AI
{
	private Game game;
	private Move move;

	public AI(Game game)
	{
		this.game = game;
	}

	public void update()
	{
		if (move == null) {
			move = getBestMove(game.data.getBoard(), game.data.getCurrent(), game.data.getX(), game.data.getY());
		}

		if (move.rotationDelta < 0) {
			move.rotationDelta++;
			game.storeAndExecute(new RotateRightCommand(game));
		} else if (move.rotationDelta > 0) {
			move.rotationDelta--;
			game.storeAndExecute(new RotateLeftCommand(game));
		} else if (move.translationDelta < 0) {
			move.translationDelta++;
			game.storeAndExecute(new MoveLeftCommand(game));
		} else if (move.translationDelta > 0) {
			move.translationDelta--;
			game.storeAndExecute(new MoveRightCommand(game));
		} else {
			// TODO - implement soft drop

			game.storeAndExecute(new HardDropCommand(game));
			move = null;
		}
	}

	private static class Move
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

	private Move getBestMove(Board board, Tetromino current, int x, int y)
	{
		return getBestMove(board, current, x, y, new Comparator<Double>() {
			@Override
			public int compare(Double d1, Double d2)
			{
				return (d1 == d2) ? 0 : ((d1 > d2) ? 1 : -1);
			}
		});
	}

	private Board dummy = null;

	private Move getBestMove(Board board, Tetromino current, int x, int y, Comparator<Double> comp)
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

			dummy = board.tryClone(dummy);

			if (!dummy.canMove(current, x, y)) {
				break;
			}

			for (int translationDelta = minTranslationDelta; translationDelta <= maxTranslationDelta; translationDelta++) {
				dummy = board.tryClone(dummy);

				if (dummy.canMove(current, x + translationDelta, dummy.dropHeight(current, x + translationDelta))) {
					dummy.addPiece(current, x + translationDelta, dummy.dropHeight(current, x + translationDelta));

					double score = scoring.score(dummy);

					if (comp.compare(score, bestScore) > 0) {
						bestScore = score;
						bestTranslationDelta = translationDelta;
						bestRotationDelta = rotationDelta;
					}
				}
			}

			current = Tetromino.rotateLeft(current);
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
