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

import com.kauri.gatetris.sequence.PieceSequence;

/**
 * @author Eric Fritz
 */
public class GameData
{
	public enum State {
		PLAYING, PAUSED, GAMEOVER;
	}

	private State state = State.PLAYING;
	private Board board;
	private PieceSequence sequence;

	private long score = 0;
	private long lines = 0;
	private long drops = 0;

	private int xPos;
	private int yPos;
	private Tetromino current;
	private Tetromino preview;

	public GameData(Board board, PieceSequence sequence)
	{
		this.board = board;
		this.sequence = sequence;

		sequence.advance();
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
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

	public long getScore()
	{
		return score;
	}

	public void setScore(long score)
	{
		this.score = score;
	}

	public long getLevel()
	{
		return Math.min(10, (drops / 10) + 1);
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
}
