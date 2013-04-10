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

import com.kauri.tetris.Board;
import com.kauri.tetris.Tetromino;

/**
 * @author Eric Fritz
 */
public class MoveEvaluator
{
	private ScoringSystem scoring;

	public MoveEvaluator(ScoringSystem scoring)
	{
		this.scoring = scoring;
	}

	public Move getNextMove(Board board, Tetromino current, int x1, int y1)
	{
		return getNextMove(board, current, x1, y1, null, 0, 0);
	}

	public Move getNextMove(Board board, Tetromino current, int x1, int y1, Tetromino preview, int x2, int y2)
	{
		double best = Double.NEGATIVE_INFINITY;
		Move move = new Move(best, 0, 0);

		for (int i = 0; i < 4; i++) {
			Move m = getBestMoveForRotatedPiece(board, i, current, x1, y1, preview, x2, y2);

			if (m.getScore() > best) {
				best = m.getScore();
				move = m;
			}

			current = Tetromino.rotateClockwise(current);
		}

		return move;
	}

	public Move getBestMoveForRotatedPiece(Board board, int rot, Tetromino current, int x1, int y1, Tetromino preview, int x2, int y2)
	{
		double best = Double.NEGATIVE_INFINITY;
		Move move = new Move(best, 0, 0);

		int min = getMinTranslationDelta(board, current, x1, y1);
		int max = getMaxTranslationDelta(board, current, x1, y1);

		for (int translation = min; translation <= max; translation++) {
			int target = board.dropHeight(current, x1 + translation, y1);
			board.addPiece(current, x1 + translation, target);

			double score = preview == null ? scoring.score(board) : getNextMove(board, preview, x2, y2).getScore();

			if (score > best) {
				best = score;
				move = new Move(score, rot, translation);
			}

			board.removePiece(current, x1 + translation, target);
		}

		return move;
	}

	private int getMinTranslationDelta(Board board, Tetromino current, int xPos, int yPos)
	{
		return getMaxTranslationDeltaMagnitude(board, current, xPos, yPos, -1);
	}

	private int getMaxTranslationDelta(Board board, Tetromino current, int xPos, int yPos)
	{
		return getMaxTranslationDeltaMagnitude(board, current, xPos, yPos, +1);
	}

	private int getMaxTranslationDeltaMagnitude(Board board, Tetromino current, int xPos, int yPos, int step)
	{
		int delta = 0;
		while (board.canMove(current, xPos + delta + step, yPos)) {
			delta += step;
		}

		return delta;
	}
}
