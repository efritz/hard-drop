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

import com.kauri.gatetris.GameData;

/**
 * @author Eric Fritz
 */
public class HardDropCommand extends MovementCommand
{
	private GameData data;
	private int y;
	private long score;
	private boolean success = false;
	private Command subcommand1;
	private Command subcommand2;

	public HardDropCommand(GameData data)
	{
		super(data);
		this.data = data;
	}

	@Override
	public void execute()
	{
		y = data.getY();

		success = tryMove(data.getCurrent(), data.getX(), data.getBoard().dropHeight(data.getCurrent(), data.getX(), y));

		if (success) {
			int pieceReward = ((data.getBoard().getHeight() + (3 * data.getLevel())) - (data.getBoard().getHeight() - y));

			score = data.getScore();
			data.setScore(score + pieceReward);
			data.setDrops(data.getDrops() + 1);

			data.getBoard().addPiece(data.getCurrent(), data.getX(), data.getY());

			subcommand1 = new ClearCommand(data);
			subcommand1.execute();

			subcommand2 = new NewTetrominoCommand(data);
			subcommand2.execute();
		}
	}

	@Override
	public void unexecute()
	{
		if (success) {
			subcommand2.unexecute();
			subcommand1.unexecute();

			data.getBoard().removePiece(data.getCurrent(), data.getX(), data.getY());

			tryMove(data.getCurrent(), data.getX(), y);

			data.setScore(score);
			data.setDrops(data.getDrops() - 1);
		}
	}
}
