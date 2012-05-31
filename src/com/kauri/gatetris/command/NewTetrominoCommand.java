/*
 * This file is part of the ga-tetris package.
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

package com.kauri.gatetris.command;

import com.kauri.gatetris.GameContext;
import com.kauri.gatetris.GameContext.State;
import com.kauri.gatetris.Tetromino;

/**
 * @author efritz
 */
public class NewTetrominoCommand implements Command
{
	private GameContext data;
	private Tetromino current;
	private Tetromino preview;
	private int x;
	private int y;

	public NewTetrominoCommand(GameContext data)
	{
		this.data = data;
	}

	@Override
	public void execute()
	{
		current = data.getCurrent();
		preview = data.getPreview();

		x = data.getX();
		y = data.getY();

		data.getSequence().advance();
		data.setCurrent(data.getSequence().peekCurrent());
		data.setPreview(data.getSequence().peekPreview());

		data.setX(data.getBoard().getSpawnX(data.getCurrent()));
		data.setY(data.getBoard().getSpawnY(data.getCurrent()));

		// TODO - move this somewhere else

		if (!data.getBoard().canMove(data.getCurrent(), data.getX(), data.getY())) {
			data.setState(State.GAMEOVER);
		}
	}

	@Override
	public void unexecute()
	{
		data.setState(State.PLAYING);

		data.setCurrent(current);
		data.setPreview(preview);

		data.setX(x);
		data.setY(y);

		data.getSequence().rewind();
	}
}
