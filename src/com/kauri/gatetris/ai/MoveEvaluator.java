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
	private ScoringSystem scoring = new ScoringSystem();

	public Move getNextMove(Board board, Tetromino current, int xPos, int yPos)
	{
		//
		// TODO - Somehow evolve the scoring system between rounds. I'm not sure where this logic
		// should really go. Interface for basic GA algorithm, and then the AI will do what it wants
		// at that point (other strategies could have different weights, etc).
		//

		scoring.setWeights(2, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15);

		Tetromino t1 = current;
		Tetromino t2 = Tetromino.rotateClockwise(t1);
		Tetromino t3 = Tetromino.rotateClockwise(t2);
		Tetromino t4 = Tetromino.rotateClockwise(t3);

		List<Move> moves = new LinkedList<Move>();
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 0, 0), board, t1, xPos, yPos));
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 1, 0), board, t2, xPos, yPos));
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 2, 0), board, t3, xPos, yPos));
		moves.add(getBestMoveForRotatedPiece(new Move(Double.NEGATIVE_INFINITY, 3, 0), board, t4, xPos, yPos));

		return Collections.max(moves, new Comparator<Move>() {
			@Override
			public int compare(Move m1, Move m2)
			{
				return Double.compare(m1.getScore(), m2.getScore());
			}
		});
	}

	private Move getBestMoveForRotatedPiece(Move move, Board board, Tetromino current, int xPos, int yPos)
	{
		boolean posIsValid = true;
		boolean negIsValid = true;

		int delta = 0;
		while (negIsValid || posIsValid) {
			posIsValid = posIsValid && board.canMove(current, xPos + delta, yPos);
			negIsValid = negIsValid && board.canMove(current, xPos - delta, yPos);

			if (posIsValid) {
				move = getBestMoveForTranslation(move, board, current, xPos, yPos, delta);
			}

			if (negIsValid) {
				move = getBestMoveForTranslation(move, board, current, xPos, yPos, delta * -1);
			}

			delta++;
		}

		return move;
	}

	private Move getBestMoveForTranslation(Move move, Board board, Tetromino current, int xPos, int yPos, int translationDelta)
	{
		double score = scoring.score(board, current, xPos + translationDelta, yPos);

		if (score > move.getScore()) {
			return new Move(score, move.getRotationDelta(), translationDelta);
		}

		return move;
	}
}
