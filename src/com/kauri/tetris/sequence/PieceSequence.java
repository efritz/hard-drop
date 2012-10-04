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

import java.util.ArrayList;
import java.util.List;

import com.kauri.tetris.Tetromino;

/**
 * @author Eric Fritz
 */
public class PieceSequence
{
	private int current = -1;
	private int preview = +0;
	private List<Tetromino> pieces = new ArrayList<Tetromino>();

	private PieceSelector selector;

	public PieceSequence(PieceSelector selector)
	{
		this.selector = selector;
	}

	public void clear()
	{
		current = -1;
		preview = +0;

		pieces.clear();
	}

	public void advance()
	{
		current++;
		preview++;

		while (pieces.size() <= preview) {
			pieces.add(selector.getNextPiece());
		}
	}

	public void rewind()
	{
		current--;
		preview--;
	}

	public Tetromino peekCurrent()
	{
		return pieces.get(current);
	}

	public Tetromino peekPreview()
	{
		return pieces.get(preview);
	}
}
