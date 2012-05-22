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

package com.kauri.gatetris.command;

import com.kauri.gatetris.Game;

/**
 * @author Eric Fritz
 */
public class SoftDropCommand extends MovementCommand
{
	private Game game;
	private Command subcommand;
	private boolean success = false;
	private long pieceValue;

	public SoftDropCommand(Game game)
	{
		super(game);
		this.game = game;
	}

	@Override
	public void execute()
	{
		if (!isFalling()) {
			subcommand = new HardDropCommand(game);
			subcommand.execute();
		} else {
			success = tryMove(game.data.getCurrent(), game.data.getX(), game.data.getY() - 1);

			pieceValue = game.data.pieceValue;
			game.data.pieceValue = Math.max(0, game.data.pieceValue - 1);
		}
	}

	private boolean isFalling()
	{
		return game.data.getBoard().canMove(game.data.getCurrent(), game.data.getX(), game.data.getY() - 1);
	}

	@Override
	public void unexecute()
	{
		if (success) {
			game.data.pieceValue = pieceValue;
			tryMove(game.data.getCurrent(), game.data.getX(), game.data.getY() + 1);
		}

		if (subcommand != null) {
			subcommand.unexecute();
		}
	}
}
