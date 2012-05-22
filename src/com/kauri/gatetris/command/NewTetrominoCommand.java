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

import com.kauri.gatetris.Game;
import com.kauri.gatetris.GameData.State;
import com.kauri.gatetris.Tetromino;

/**
 * @author efritz
 */
public class NewTetrominoCommand implements Command
{
	private Game game;
	private Tetromino current;
	private Tetromino preview;
	private int x;
	private int y;

	public NewTetrominoCommand(Game game)
	{
		this.game = game;
	}

	@Override
	public void execute()
	{
		current = game.data.getCurrent();
		preview = game.data.getPreview();

		x = game.data.getX();
		y = game.data.getY();

		game.data.getSequence().advance();
		game.data.setCurrent(game.data.getSequence().peekCurrent());
		game.data.setPreview(game.data.getSequence().peekPreview());

		game.data.setX(game.data.getBoard().getSpawnX(game.data.getCurrent()));
		game.data.setY(game.data.getBoard().getSpawnY(game.data.getCurrent()));

		// TODO - move this somewhere else

		if (!game.data.getBoard().canMove(game.data.getCurrent(), game.data.getX(), game.data.getY())) {
			game.data.setState(State.GAMEOVER);
		}
	}

	@Override
	public void unexecute()
	{
		game.data.setCurrent(current);
		game.data.setPreview(preview);

		game.data.setX(x);
		game.data.setY(y);

		game.data.getSequence().rewind();
	}
}
