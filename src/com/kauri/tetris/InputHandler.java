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

package com.kauri.tetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.kauri.tetris.GameContext.State;
import com.kauri.tetris.command.AddJunkCommand;
import com.kauri.tetris.command.HardDropCommand;
import com.kauri.tetris.command.MoveLeftCommand;
import com.kauri.tetris.command.MoveRightCommand;
import com.kauri.tetris.command.RotateClockwiseCommand;
import com.kauri.tetris.command.RotateCounterClockwiseCommand;
import com.kauri.tetris.command.SoftDropCommand;

class InputHandler implements KeyListener
{
	private final GameContext context;

	private Map<Integer, Boolean> keys = new HashMap<Integer, Boolean>();

	public InputHandler(GameContext context)
	{
		this.context = context;
	}

	@Override
	public void keyPressed(KeyEvent ke)
	{
		toggle(ke.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent ke)
	{
	}

	@Override
	public void keyTyped(KeyEvent ke)
	{
	}

	public void process()
	{
		for (Integer keyCode : getKeys()) {
			handleEvent(keyCode);
		}
	}

	private void handleEvent(int keyCode)
	{
		if (context.getState() == State.PLAYING && !context.isRunningAi()) {
			if (keyCode == KeyEvent.VK_LEFT) {
				context.storeAndExecute(new MoveLeftCommand(context));
			}

			if (keyCode == KeyEvent.VK_RIGHT) {
				context.storeAndExecute(new MoveRightCommand(context));
			}

			if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_Z) {
				context.storeAndExecute(new RotateClockwiseCommand(context));
			}

			if (keyCode == KeyEvent.VK_X) {
				context.storeAndExecute(new RotateCounterClockwiseCommand(context));
			}

			if (keyCode == KeyEvent.VK_DOWN) {
				context.storeAndExecute(new SoftDropCommand(context));
			}

			if (keyCode == KeyEvent.VK_SPACE) {
				context.storeAndExecute(new HardDropCommand(context));
			}
		}

		if (context.getState() != State.PAUSED && !context.isRunningAi()) {
			if (keyCode == KeyEvent.VK_BACK_SPACE) {
				context.undo();
			}
		}

		if (context.getState() == State.PLAYING) {
			if (keyCode == KeyEvent.VK_J) {
				context.storeAndExecute(new AddJunkCommand(context));
			}
		}

		if (keyCode == KeyEvent.VK_ENTER) {
			context.newGame();
		}

		if (keyCode == KeyEvent.VK_A) {
			context.setRunningAi(!context.isRunningAi());
		}

		if (keyCode == KeyEvent.VK_U) {
			context.setAutoRestart(!context.isAutoRestart());
		}

		// KeyEvent.VK_PLUS
		if (keyCode == 61) {
			context.setAiDelay(Math.max(1, context.getAiDelay() / 2));
		}

		if (keyCode == KeyEvent.VK_MINUS) {
			context.setAiDelay(Math.min(1000, context.getAiDelay() * 2));
		}

		if (keyCode == KeyEvent.VK_N) {
			context.setShowPreviewPiece(!context.showPreviewPiece());
		}

		if (keyCode == KeyEvent.VK_S) {
			context.setShowDropPosPiece(!context.showDropPosPiece());
		}

		if (keyCode == KeyEvent.VK_P) {
			if (context.getState() != State.GAMEOVER) {
				context.setState((context.getState() == State.PAUSED) ? State.PLAYING : State.PAUSED);
			}
		}

		if (keyCode == KeyEvent.VK_O) {
			context.setShowScore(!context.getShowScore());
		}

		if (keyCode == KeyEvent.VK_PAGE_UP) {
			int width = Math.min(100, Math.max(4, context.getBoard().getWidth() + 1));
			context.setBoard(new Board(width, width * 2));
			context.newGame();
		}

		if (keyCode == KeyEvent.VK_PAGE_DOWN) {
			int width = Math.min(100, Math.max(4, context.getBoard().getWidth() - 1));
			context.setBoard(new Board(width, width * 2));
			context.newGame();
		}
	}

	private void toggle(int keyCode, boolean down)
	{
		keys.put(keyCode, down);
	}

	private List<Integer> getKeys()
	{
		List<Integer> result = new LinkedList<Integer>();

		for (Entry<Integer, Boolean> entry : keys.entrySet()) {
			if (entry.getValue()) {
				result.add(entry.getKey());
			}

			keys.put(entry.getKey(), false);
		}

		return result;
	}
}
