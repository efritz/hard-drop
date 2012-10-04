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

package com.kauri.tetris.sequence;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.kauri.tetris.GameContext;
import com.kauri.tetris.Tetromino;
import com.kauri.tetris.ai.Move;

/**
 * @author Eric Fritz
 */
public class WorstPieceSelector implements PieceSelector
{
	private GameContext context;

	public WorstPieceSelector(GameContext context)
	{
		this.context = context;
	}

	@Override
	public Tetromino getNextPiece()
	{
		Map<Move, Tetromino> moves = new HashMap<Move, Tetromino>();

		for (Tetromino tetromino : Tetromino.tetrominoes.values()) {
			moves.put(context.getEvaluator().getNextMove(context.getBoard(), tetromino, context.getBoard().getSpawnX(tetromino), context.getBoard().getSpawnY(tetromino)), tetromino);
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
