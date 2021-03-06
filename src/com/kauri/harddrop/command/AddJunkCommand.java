/*
 * This file is part of the tetris package.
 *
 * Copyright (c) 2014 Eric Fritz
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

package com.kauri.harddrop.command;

import com.kauri.harddrop.GameContext;
import com.kauri.harddrop.Shape;

/**
 * @author efritz
 */
public class AddJunkCommand implements Command
{
	private GameContext context;
	private Shape[] overflow;
	private Command subcommand;

	public AddJunkCommand(GameContext context) {
		this.context = context;
	}

	@Override
	public void execute() {
		overflow = context.getBoard().getRow(context.getBoard().getHeight() - 1);

		Shape[] line = new Shape[context.getBoard().getWidth()];

		for (int i = 0; i < context.getBoard().getWidth(); i++) {
			line[i] = Shape.Junk;
		}

		int holes = (int) (Math.random() * (context.getBoard().getWidth() - 1) + 1);

		while (holes > 0) {
			int index = (int) (Math.random() * context.getBoard().getWidth());

			if (line[index] != Shape.NoShape) {
				line[index] = Shape.NoShape;
				holes--;
			}
		}

		if (!context.getBoard().canMove(context.getCurrent(), context.getX(), context.getY() - 1)) {
			subcommand = new HardDropCommand(context);
			subcommand.execute();
		}

		context.getBoard().addRow(0, line);
	}

	@Override
	public void unexecute() {
		context.getBoard().removeRow(0);

		if (subcommand != null) {
			subcommand.unexecute();
		}

		context.getBoard().addRow(context.getBoard().getHeight() - 1, overflow);
	}
}
