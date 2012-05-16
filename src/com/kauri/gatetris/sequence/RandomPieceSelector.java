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

import java.util.Random;

import com.kauri.gatetris.Tetromino;
import com.kauri.gatetris.Tetromino.Shape;

/**
 * @author Eric Fritz
 */
public class RandomPieceSelector implements PieceSelector
{
	private static Shape[] shapes = new Shape[] { Shape.I, Shape.J, Shape.L, Shape.O, Shape.S, Shape.T, Shape.Z };

	private Random random;

	public RandomPieceSelector()
	{
		this(System.nanoTime());
	}

	public RandomPieceSelector(long seed)
	{
		random = new Random(seed);
	}

	@Override
	public Tetromino getNextPiece()
	{
		return Tetromino.tetrominoes.get(shapes[random.nextInt(shapes.length)]);
	}
}
