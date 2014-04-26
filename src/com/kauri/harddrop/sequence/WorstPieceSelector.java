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

package com.kauri.harddrop.sequence;

import com.kauri.harddrop.GameContext;
import com.kauri.harddrop.Tetromino;
import com.kauri.harddrop.ai.Move;
import com.kauri.harddrop.ai.MoveEvaluator;

/**
 * @author Eric Fritz
 */
public class WorstPieceSelector implements PieceSelector
{
	private GameContext context;
	private MoveEvaluator evaluator;

	public WorstPieceSelector(GameContext context, MoveEvaluator evaluator) {
		this.context = context;
		this.evaluator = evaluator;
	}

	@Override
	public Tetromino getNextPiece() {
		double worst = Double.POSITIVE_INFINITY;
		Tetromino piece = null;

		for (Tetromino tetromino : Tetromino.tetrominoes.values()) {
			int x = context.getBoard().getSpawnX(tetromino);
			int y = context.getBoard().getSpawnY(tetromino);

			Move m = evaluator.getNextMove(context.getBoard(), tetromino, x, y);

			if (m.getScore() < worst) {
				worst = m.getScore();
				piece = tetromino;
			}
		}

		return piece;
	}
}
