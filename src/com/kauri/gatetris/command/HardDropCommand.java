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
public class HardDropCommand extends MovementCommand
{
	private Game game;
	private int y;
	private long score;
	private boolean success = false;
	private Command subcommand1;
	private Command subcommand2;

	public HardDropCommand(Game game)
	{
		super(game);
		this.game = game;
	}

	@Override
	public void execute()
	{
		y = game.data.getY();

		success = tryMove(game.data.getCurrent(), game.data.getX(), game.data.getBoard().dropHeight(game.data.getCurrent(), game.data.getX(), y));

		if (success) {
			int pieceReward = ((game.data.getBoard().getHeight() + (3 * game.data.getLevel())) - (game.data.getBoard().getHeight() - y));

			score = game.data.getScore();
			game.data.setScore(score + pieceReward);
			game.data.setDrops(game.data.getDrops() + 1);

			game.data.getBoard().addPiece(game.data.getCurrent(), game.data.getX(), game.data.getY());

			subcommand1 = new ClearCommand(game);
			subcommand1.execute();

			subcommand2 = new NewTetrominoCommand(game);
			subcommand2.execute();
		}
	}

	@Override
	public void unexecute()
	{
		if (success) {
			subcommand2.unexecute();
			subcommand1.unexecute();

			game.data.getBoard().removePiece(game.data.getCurrent(), game.data.getX(), game.data.getY());

			tryMove(game.data.getCurrent(), game.data.getX(), y);

			game.data.setScore(score);
			game.data.setDrops(game.data.getDrops() - 1);
		}
	}
}
