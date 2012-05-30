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

package com.kauri.gatetris.sequence;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.kauri.gatetris.GameData;
import com.kauri.gatetris.Tetromino;
import com.kauri.gatetris.Tetromino.Shape;
import com.kauri.gatetris.ai.Move;
import com.kauri.gatetris.ai.MoveEvaluator;
import com.kauri.gatetris.ai.ScoringSystem;

/**
 * @author Eric Fritz
 */
public class WorstPieceSelector implements PieceSelector
{
	private static Shape[] shapes = new Shape[] { Shape.I, Shape.J, Shape.L, Shape.O, Shape.S, Shape.T, Shape.Z };

	private GameData data;
	private MoveEvaluator strategy;

	public WorstPieceSelector(GameData data)
	{
		this.data = data;

		ScoringSystem scoring = new ScoringSystem();

		//
		// TODO - Somehow evolve the scoring system between rounds. I'm not sure where this logic
		// should really go. Interface for basic GA algorithm, and then the AI will do what it wants
		// at that point (other strategies could have different weights, etc).
		//

		scoring.setWeights(Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15, Math.random() * 10 - 15);

		strategy = new MoveEvaluator(scoring);
	}

	@Override
	public Tetromino getNextPiece()
	{
		Map<Move, Tetromino> moves = new HashMap<Move, Tetromino>();

		for (Shape s : shapes) {
			Tetromino tetromino = Tetromino.tetrominoes.get(s);
			Move move = strategy.getNextMove(data.getBoard(), tetromino, data.getBoard().getSpawnX(tetromino), data.getBoard().getSpawnY(tetromino));

			moves.put(move, tetromino);
		}

		return moves.get(Collections.min(moves.keySet(), new Comparator<Move>() {
			@Override
			public int compare(Move m1, Move m2)
			{
				return Double.compare(m1.getScore(), m2.getScore());
			}
		}));
	}
}
