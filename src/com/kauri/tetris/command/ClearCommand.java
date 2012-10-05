/*
 * This file is part of the tetris package.
 *
 * Copyright (C) 2012, efritz
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

package com.kauri.tetris.command;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.kauri.tetris.GameContext;
import com.kauri.tetris.Shape;

/**
 * @author efritz
 */
public class ClearCommand implements Command
{
	private GameContext context;
	private long lines;
	private long score;
	private SortedMap<Integer, Shape[]> map = new TreeMap<Integer, Shape[]>();

	public ClearCommand(GameContext context)
	{
		this.context = context;
	}

	@Override
	public void execute()
	{
		for (int row = context.getBoard().getHeight() - 1; row >= 0; row--) {
			if (context.getBoard().isRowFull(row)) {
				map.put(row, context.getBoard().getRow(row));
				context.getBoard().removeRow(row);
			}
		}

		lines = context.getLines();
		score = context.getScore();

		context.setLines(lines + map.size());
		context.setScore(score + 40 * (long) Math.pow(3, map.size() - 1));
	}

	@Override
	public void unexecute()
	{
		for (Entry<Integer, Shape[]> e : map.entrySet()) {
			context.getBoard().addRow(e.getKey(), e.getValue());
		}

		context.setLines(lines);
		context.setScore(score);
	}
}
