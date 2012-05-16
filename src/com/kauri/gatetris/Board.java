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

import java.util.Arrays;

import com.kauri.gatetris.Tetromino.Shape;

/**
 * A board provides access to the state of the current pile.
 * 
 * @author Eric Fritz
 */
public class Board implements Cloneable
{
	private int width;
	private int height;
	private Shape[] board;

	/**
	 * Creates a new Board.
	 * 
	 * @param width
	 *            The board width.
	 * @param height
	 *            The board height.
	 */
	public Board(int width, int height)
	{
		this.width = width;
		this.height = height;

		board = new Shape[width * height];
		Arrays.fill(board, Shape.NoShape);
	}

	public void clear()
	{
		Arrays.fill(board, Shape.NoShape);
	}

	/**
	 * Clone the state of the board. If `fill` is non-null and has the same dimensions, the state
	 * will be copied into that board instance. Otherwise, a new instance will be created.
	 * 
	 * @param fill
	 *            The board to modify, if possible.
	 * @return The filled board.
	 */
	public Board tryClone(Board fill)
	{
		if (fill == null || fill.width != width || fill.height != height) {
			fill = new Board(width, height);
		}

		for (int i = 0; i < board.length; i++) {
			fill.board[i] = board[i];
		}

		return fill;
	}

	/**
	 * @return The width of the board.
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * @return The height of the board.
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Retrieves the block type at the given position.
	 * 
	 * @param row
	 *            The row index.
	 * @param col
	 *            The column index.
	 * 
	 * @return The tetromino type.
	 */
	public Shape getShapeAt(int row, int col)
	{
		return board[(row * width) + col];
	}

	/**
	 * Updates the block type at the given position.
	 * 
	 * @param row
	 *            The row index.
	 * @param col
	 *            The column index.
	 * @param type
	 *            The tetromino type.
	 */
	private void setShapeAt(int row, int col, Shape type)
	{
		board[(row * width) + col] = type;
	}

	/**
	 * Add a piece to the board, if the piece can fit in the board legally.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
	 * @return Whether the piece has been added to the board.
	 */
	public boolean tryMove(Tetromino piece, int xPos, int yPos)
	{
		if (canMove(piece, xPos, yPos)) {
			addPiece(piece, xPos, yPos);
			return true;
		}

		return false;
	}

	/**
	 * Determines if a tetromino can be placed at the given x and y-coordinates without collision.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
	 * @return <tt>true</tt> if there is no collision, <tt>false</tt> otherwise.
	 */
	public boolean canMove(Tetromino piece, int xPos, int yPos)
	{
		for (int i = 0; i < piece.getSize(); i++) {
			int x = xPos + piece.getX(i);
			int y = yPos - piece.getY(i);

			if (x < 0 || x >= width || y < 0) {
				return false;
			}

			if (y < height && getShapeAt(y, x) != Shape.NoShape) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Adds the blocks of a tetromino onto this board.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
	 */
	public void addPiece(Tetromino piece, int xPos, int yPos)
	{
		for (int i = 0; i < piece.getSize(); i++) {
			int x = xPos + piece.getX(i);
			int y = yPos - piece.getY(i);

			if (x >= 0 && x < width && y >= 0 && y < height) {
				setShapeAt(y, x, piece.getShape());
			}
		}
	}

	/**
	 * Inserts a given line into the board. All rows that lie above this index will be pushed up by
	 * one. The blocks in the highest row will be pushed off of the board.
	 * 
	 * @param row
	 *            The row index.
	 * @param line
	 *            The row to add.
	 */
	public void addLine(int row, Shape[] line)
	{
		if (line.length != width) {
			throw new IllegalArgumentException("Cannot add line to board with non-matching dimensions.");
		}

		for (int i = height - 1; i > row; i--) {
			for (int col = 0; col < width; col++) {
				setShapeAt(i, col, getShapeAt(i - 1, col));
			}
		}

		for (int col = 0; col < width; col++) {
			setShapeAt(row, col, line[col]);
		}
	}

	/**
	 * Removes full lines and compacts the board downwards.
	 * 
	 * @return The number of lines that were removed.
	 */
	public int clearLines()
	{
		int lines = 0;
		for (int row = height - 1; row >= 0; row--) {
			if (isRowFull(row)) {
				lines++;
				collapseLine(row);
			}
		}

		return lines;
	}

	/**
	 * Determines if a row is filled horizontally.
	 * 
	 * @param row
	 *            The row index.
	 * @return Whether the row is full.
	 */
	private boolean isRowFull(int row)
	{
		for (int col = 0; col < width; col++) {
			if (getShapeAt(row, col) == Shape.NoShape) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Remove a line by collapsing the lines above it down by one.
	 * 
	 * @param row
	 *            The row index.
	 */
	private void collapseLine(int row)
	{
		for (int i = row; i < height - 1; i++) {
			for (int col = 0; col < width; col++) {
				setShapeAt(i, col, getShapeAt(i + 1, col));
			}
		}
	}

	/**
	 * Determines the column index where the given piece would come to rest if dropped straight
	 * down. This method uses the top of the board as the starting y-position.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The piece's current x-position.
	 * @return The resting y-position.
	 */
	public int dropHeight(Tetromino piece, int xPos)
	{
		return dropHeight(piece, xPos, height);
	}

	/**
	 * Determines the column index where the given piece would come to rest if dropped straight
	 * down.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The piece's current x-position.
	 * @param yPos
	 *            The piece's current y-position.
	 * @return The resting y-position.
	 */
	public int dropHeight(Tetromino piece, int xPos, int yPos)
	{
		int diff = 0;
		while (canMove(piece, xPos, yPos - diff)) {
			diff++;
		}

		return yPos - diff + 1;
	}
}
