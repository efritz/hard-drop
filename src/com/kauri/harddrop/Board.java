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

package com.kauri.harddrop;

import java.util.Arrays;

/**
 * A board provides access to the state of the current pile.
 * 
 * @author Eric Fritz
 */
public class Board 
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
		clear();
	}

	/**
	 * Remove all blocks from the board.
	 */
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

		System.arraycopy(board, 0, fill.board, 0, board.length);

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
		fillTetromino(piece, xPos, yPos, piece.getShape());
	}

	/**
	 * Removes the blocks of a tetromino from this board.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
	 */
	public void removePiece(Tetromino piece, int xPos, int yPos)
	{
		fillTetromino(piece, xPos, yPos, Shape.NoShape);
	}

	/**
	 * Determines if a row is filled horizontally.
	 * 
	 * @param row
	 *            The row index.
	 * @return Whether the row is full.
	 */
	public boolean isRowFull(int row)
	{
		for (int col = 0; col < width; col++) {
			if (getShapeAt(row, col) == Shape.NoShape) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Retrieve the shapes that compose a given row.
	 * 
	 * @param row
	 *            The row index.
	 * @return An array of shapes.
	 */
	public Shape[] getRow(int row)
	{
		Shape[] shapes = new Shape[width];

		for (int col = 0; col < width; col++) {
			shapes[col] = getShapeAt(row, col);
		}

		return shapes;
	}

	/**
	 * Inserts a given row into the board. All rows that lie above this index will be pushed up by
	 * one. The blocks in the highest row will be pushed off of the board.
	 * 
	 * @param row
	 *            The row index.
	 * @param shapes
	 *            The row to add.
	 */
	public void addRow(int row, Shape[] shapes)
	{
		if (shapes.length != width) {
			throw new IllegalArgumentException("Cannot add row to board with non-matching dimensions.");
		}

		for (int i = height - 1; i > row; i--) {
			for (int col = 0; col < width; col++) {
				setShapeAt(i, col, getShapeAt(i - 1, col));
			}
		}

		for (int col = 0; col < width; col++) {
			setShapeAt(row, col, shapes[col]);
		}
	}

	/**
	 * Remove a row by collapsing the rows above it down by one.
	 * 
	 * @param row
	 *            The row index.
	 */
	public void removeRow(int row)
	{
		for (int i = row; i < height - 1; i++) {
			for (int col = 0; col < width; col++) {
				setShapeAt(i, col, getShapeAt(i + 1, col));
			}
		}
	}

	/**
	 * Determines the x-position of a piece as if it were spawning at the top of the board. This
	 * attempts to center the piece horizontally on the board.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @return The x-position.
	 */
	public int getSpawnX(Tetromino piece)
	{
		return (width - piece.getWidth()) / 2 + Math.abs(piece.getMinX());
	}

	/**
	 * Determines the y-position of a piece as if it were spawning at the top of the board. This
	 * attempts to place the piece so that all of its blocks are visible at the time of spawn.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @return The y-position.
	 */
	public int getSpawnY(Tetromino piece)
	{
		return height - 1 - piece.getMinY();
	}

	/**
	 * Determines the column index where the given piece would come to rest if dropped straight
	 * down. This method uses the top of the board as the starting y-position.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
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
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
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

	/**
	 * Determines if the piece can move straight down vertically.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
	 * @return <tt>true</tt> if the piece can move straight down vertically, <tt>false</tt> otherwise.
	 */
	public boolean isFalling(Tetromino piece, int xPos, int yPos)
	{
		return canMove(piece, xPos, yPos - 1);
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
	 * Fill the shape of a tetromino with the given shape at the given position.
	 * 
	 * @param piece
	 *            The tetromino.
	 * @param xPos
	 *            The x-position.
	 * @param yPos
	 *            The y-position.
	 * @param shape
	 *            The shape that composes the tetromino.
	 */
	private void fillTetromino(Tetromino piece, int xPos, int yPos, Shape shape)
	{
		for (int i = 0; i < piece.getSize(); i++) {
			int col = xPos + piece.getX(i);
			int row = yPos - piece.getY(i);

			if (col >= 0 && col < width && row >= 0 && row < height) {
				setShapeAt(row, col, shape);
			}
		}
	}
}
