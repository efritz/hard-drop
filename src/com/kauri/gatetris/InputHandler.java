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

package com.kauri.gatetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.kauri.gatetris.GameData.State;
import com.kauri.gatetris.command.HardDropCommand;
import com.kauri.gatetris.command.MoveLeftCommand;
import com.kauri.gatetris.command.MoveRightCommand;
import com.kauri.gatetris.command.RotateRightCommand;
import com.kauri.gatetris.command.SoftDropCommand;

class InputHandler implements KeyListener
{
	private Game game;

	public InputHandler(Game game)
	{
		this.game = game;
	}

	@Override
	public void keyPressed(KeyEvent ke)
	{
		int keyCode = ke.getKeyCode();

		if (game.data.getState() == State.PLAYING && !game.runningAi) {
			if (keyCode == KeyEvent.VK_LEFT) {
				game.storeAndExecute(new MoveLeftCommand(game));
			}

			if (keyCode == KeyEvent.VK_RIGHT) {
				game.storeAndExecute(new MoveRightCommand(game));
			}

			if (keyCode == KeyEvent.VK_UP) {
				game.storeAndExecute(new RotateRightCommand(game));
			}

			if (keyCode == KeyEvent.VK_DOWN) {
				game.storeAndExecute(new SoftDropCommand(game));
			}

			if (keyCode == KeyEvent.VK_SPACE) {
				game.storeAndExecute(new HardDropCommand(game));
			}
		}

		if (keyCode == KeyEvent.VK_ENTER) {
			game.startNewGame();
		}

		if (keyCode == KeyEvent.VK_A) {
			game.runningAi = !game.runningAi;
		}

		if (keyCode == KeyEvent.VK_U) {
			game.autoRestart = !game.autoRestart;
		}

		if (keyCode == 61) {
			game.aidelay = Math.max(1, game.aidelay / 2);
		}

		if (keyCode == KeyEvent.VK_MINUS) {
			game.aidelay = Math.min(1000, game.aidelay * 2);
		}

		if (keyCode == KeyEvent.VK_N) {
			game.ui.showNextPiece = !game.ui.showNextPiece;
		}

		if (keyCode == KeyEvent.VK_S) {
			game.ui.showShadowPiece = !game.ui.showShadowPiece;
		}

		if (keyCode == KeyEvent.VK_Q) {
			game.ui.showAiPiece = !game.ui.showAiPiece;
		}

		// TESTING

		if (keyCode == KeyEvent.VK_SEMICOLON) {
			game.undo();
		}

		if (keyCode == KeyEvent.VK_P) {
			if (game.data.getState() != State.GAMEOVER) {
				game.data.setState((game.data.getState() == State.PAUSED) ? State.PLAYING : State.PAUSED);
			}
		}

		if (keyCode == KeyEvent.VK_PAGE_UP) {
			int width = Math.min(200, Math.max(4, game.data.getBoard().getWidth() + 1));
			game.data.setBoard(new Board(width, width * 2));
			game.startNewGame();
		}

		if (keyCode == KeyEvent.VK_PAGE_DOWN) {
			int width = Math.min(200, Math.max(4, game.data.getBoard().getWidth() - 1));
			game.data.setBoard(new Board(width, width * 2));
			game.startNewGame();
		}
	}

	@Override
	public void keyReleased(KeyEvent ke)
	{
	}

	@Override
	public void keyTyped(KeyEvent ke)
	{
	}
}
