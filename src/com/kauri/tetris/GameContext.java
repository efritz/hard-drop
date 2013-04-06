/*
 * This file is part of the tetris package.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import com.kauri.tetris.command.Command;
import com.kauri.tetris.command.NewTetrominoCommand;
import com.kauri.tetris.sequence.PieceSequence;
import com.kauri.tetris.sequence.ShufflePieceSelector;

/**
 * @author Eric Fritz
 */
public class GameContext
{
	public enum State {
		PLAYING, PAUSED, GAMEOVER;
	}

	private final int MAX_HISTORY = 5000;

	private State state = State.PLAYING;
	private Board board = new Board(10, 20);
	private PieceSequence sequence = new PieceSequence(new ShufflePieceSelector());

	private long score = 0;
	private long lines = 0;
	private long drops = 0;

	private int xPos;
	private int yPos;
	private Tetromino current;
	private Tetromino preview;

	private boolean autoRestart = false;

	private Queue<Command> queue = new LinkedList<Command>();

	private Stack<Command> history = new Stack<Command>() {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean add(Command element)
		{
			if (this.size() > MAX_HISTORY - 1) {
				this.remove(0);
			}

			return super.add(element);
		}
	};

	private List<NewGameListener> newGameListeners = new ArrayList<NewGameListener>();
	private List<EndGameListener> endGameListeners = new ArrayList<EndGameListener>();

	//
	// General Game Settings

	boolean isAutoRestart()
	{
		return autoRestart;
	}

	void setAutoRestart(boolean autoRestart)
	{
		this.autoRestart = autoRestart;
	}

	public Board getBoard()
	{
		return board;
	}

	public void setBoard(Board board)
	{
		this.board = board;
	}

	public PieceSequence getSequence()
	{
		return sequence;
	}

	public void setSequence(PieceSequence sequence)
	{
		this.sequence = sequence;
	}

	//
	// Current Piece State

	public int getX()
	{
		return xPos;
	}

	public void setX(int xPos)
	{
		this.xPos = xPos;
	}

	public int getY()
	{
		return yPos;
	}

	public void setY(int yPos)
	{
		this.yPos = yPos;
	}

	public Tetromino getCurrent()
	{
		return current;
	}

	public void setCurrent(Tetromino current)
	{
		this.current = current;
	}

	public Tetromino getPreview()
	{
		return preview;
	}

	public void setPreview(Tetromino preview)
	{
		this.preview = preview;
	}

	//
	// Score State

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	public long getScore()
	{
		return score;
	}

	public void setScore(long score)
	{
		this.score = score;
	}

	public int getLevel()
	{
		return (int) Math.min(10, ((lines - 1) / 10) + 1);
	}

	public long getLines()
	{
		return lines;
	}

	public void setLines(long lines)
	{
		this.lines = lines;
	}

	public long getDrops()
	{
		return drops;
	}

	public void setDrops(long drops)
	{
		this.drops = drops;
	}

	//
	// Command Execution

	public void newGame()
	{
		this.score = 0;
		this.lines = 0;
		this.drops = 0;

		state = State.PLAYING;

		board.clear();
		history.clear();
		sequence.clear();

		this.store(new NewTetrominoCommand(this));
		this.execute();

		for (NewGameListener listener : newGameListeners) {
			listener.onNewGame();
		}
	}

	public void registerNewGameListener(NewGameListener listener)
	{
		newGameListeners.add(listener);
	}

	public void registerEndGameListener(EndGameListener listener)
	{
		endGameListeners.add(listener);
	}

	public void store(Command command)
	{
		queue.add(command);
	}

	public void execute()
	{
		for (Command command : queue) {
			if (state == State.GAMEOVER) {
				break;
			}

			command.execute();
			history.add(command);

			if (!getBoard().canMove(getCurrent(), getX(), getY())) {
				state = State.GAMEOVER;

				for (EndGameListener listener : endGameListeners) {
					listener.onEndGame();
				}
			}
		}

		queue.clear();
	}

	public void undo()
	{
		undo(1);
	}

	public void undo(int turns)
	{
		while (turns-- > 0 && history.size() > 0) {
			history.pop().unexecute();
		}
	}
}
