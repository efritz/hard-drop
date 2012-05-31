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
import com.kauri.gatetris.Tetromino.Shape;

/**
 * @author efritz
 */
public class AddJunkCommand implements Command
{
	private GameContext data;
	private Shape[] overflow;
	private Command subcommand;

	public AddJunkCommand(GameContext data)
	{
		this.data = data;
	}

	@Override
	public void execute()
	{
		//
		// TODO - also game over if overflow contains blocks?
		//

		overflow = data.getBoard().getRow(data.getBoard().getHeight() - 1);

		Shape[] line = new Shape[data.getBoard().getWidth()];

		for (int i = 0; i < data.getBoard().getWidth(); i++) {
			line[i] = Shape.Junk;
		}

		int holes = (int) (Math.random() * (data.getBoard().getWidth() - 1) + 1);

		while (holes > 0) {
			int index = (int) (Math.random() * data.getBoard().getWidth());

			if (line[index] != Shape.NoShape) {
				line[index] = Shape.NoShape;
				holes--;
			}
		}

		if (!data.getBoard().canMove(data.getCurrent(), data.getX(), data.getY() - 1)) {
			subcommand = new HardDropCommand(data);
			subcommand.execute();
		}

		data.getBoard().addRow(0, line);
	}

	@Override
	public void unexecute()
	{
		data.getBoard().removeRow(0);

		if (subcommand != null) {
			subcommand.unexecute();
		}

		data.getBoard().addRow(data.getBoard().getHeight() - 1, overflow);
	}
}
