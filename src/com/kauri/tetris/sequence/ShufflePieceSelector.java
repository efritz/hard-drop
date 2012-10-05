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

package com.kauri.tetris.sequence;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.kauri.tetris.Tetromino;

/**
 * @author Eric Fritz
 */
public class ShufflePieceSelector implements PieceSelector
{
	private Random random;
	private List<Tetromino> bag = new LinkedList<Tetromino>();

	public ShufflePieceSelector()
	{
		this(System.nanoTime());
	}

	public ShufflePieceSelector(long seed)
	{
		random = new Random(seed);
	}

	@Override
	public Tetromino getNextPiece()
	{
		if (bag.size() == 0) {
			bag.addAll(Tetromino.tetrominoes.values());
			Collections.shuffle(bag, random);
		}

		return bag.remove(0);
	}
}
