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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.kauri.gatetris.Board;
import com.kauri.gatetris.Tetromino;

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

	public Move getNextMove(Board board, Tetromino current, int xPos, int yPos)
	{
		return getNextMove(board, current, xPos, yPos, null, 0, 0);
	}

	public Move getNextMove(Board board, Tetromino current, int x1, int y1, Tetromino preview, int x2, int y2)
	{
		Tetromino t1 = current;
		Tetromino t2 = Tetromino.rotateClockwise(t1);
		Tetromino t3 = Tetromino.rotateClockwise(t2);
		Tetromino t4 = Tetromino.rotateClockwise(t3);

		List<Move> moves = new LinkedList<Move>();
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 0, 0), board, t1, x1, y1, preview, x2, y2));
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 1, 0), board, t2, x1, y1, preview, x2, y2));
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 2, 0), board, t3, x1, y1, preview, x2, y2));
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 3, 0), board, t4, x1, y1, preview, x2, y2));

		return Collections.max(moves, new Comparator<Move>() {
			@Override
			public int compare(Move m1, Move m2)
			{
				return Double.compare(m1.getScore(), m2.getScore());
			}
		});
	}

	private Move getBestMoveForRotatedPiece(Move move, Board board, Tetromino current, int x1, int y1, Tetromino preview, int x2, int y2)
	{
		boolean posIsValid = true;
		boolean negIsValid = true;

		int delta = 0;
		while (negIsValid || posIsValid) {
			posIsValid = posIsValid && board.canMove(current, x1 + delta, y1);
			negIsValid = negIsValid && board.canMove(current, x1 - delta, y1);

			if (posIsValid) {
				move = getBetterMove(move, new Move(getScoreForDrop(move, board, current, x1 + delta, y1, preview, x2, y2), move.getRotationDelta(), delta));
			}

			if (negIsValid) {
				move = getBetterMove(move, new Move(getScoreForDrop(move, board, current, x1 - delta, y1, preview, x2, y2), move.getRotationDelta(), delta * -1));
			}

			delta++;
		}

		return move;
	}

	private Move getBetterMove(Move m1, Move m2)
	{
		return m1.getScore() > m2.getScore() ? m1 : m2;
	}

	private double getScoreForDrop(Move move, Board board, Tetromino current, int x1, int y1, Tetromino preview, int x2, int y2)
	{
		//
		// TODO - use commands and undo (place execution inside of move instead of inside
		// AI.animate() and update that method to use the move update method).
		//

		Board dummy = null;

		dummy = board.tryClone(dummy);
		dummy.addPiece(current, x1, dummy.dropHeight(current, x1, y1));

		double score = scoring.score(dummy);

		if (preview != null) {
			score += getNextMove(dummy, preview, x2, y2).getScore();
		}

		return score;
	}
}
