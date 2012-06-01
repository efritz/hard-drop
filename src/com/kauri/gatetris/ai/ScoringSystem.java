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

package com.kauri.gatetris.ai;

import com.kauri.gatetris.Board;
import com.kauri.gatetris.Tetromino.Shape;

/**
 * @author Eric Fritz
 */
public class ScoringSystem
{
	private final int minWellDepth = 3;

	private double weightSumHeight;
	private double weightMaxHeight;
	private double weightRelHeight;
	private double weightAvgHeight;
	private double weightHoles;
	private double weightWells;
	private double weightBlockades;
	private double weightClears;

	/**
	 * Updates the scoring weights.
	 * 
	 * @param weightSumHeight
	 *            The weight for the sum of the board's column heights.
	 * @param weightMaxHeight
	 *            The weight for the height of the tallest column.
	 * @param weightRelHeight
	 *            The weight for the relative height distance (maximum-minimum).
	 * @param weightAvgHeight
	 *            The weight for the average column height.
	 * @param weightHoles
	 *            The weight for the number of holes.
	 * @param weightWells
	 *            The weight for the number of wells.
	 * @param weightBlockades
	 *            The weight for the number of blockades.
	 * @param weightClears
	 *            The weight for the number of line clears.
	 */
	public void setWeights(double weightSumHeight, double weightMaxHeight, double weightRelHeight, double weightAvgHeight, double weightHoles, double weightWells, double weightBlockades, double weightClears)
	{
		this.weightSumHeight = weightSumHeight;
		this.weightMaxHeight = weightMaxHeight;
		this.weightRelHeight = weightRelHeight;
		this.weightAvgHeight = weightAvgHeight;
		this.weightHoles = weightHoles;
		this.weightWells = weightWells;
		this.weightBlockades = weightBlockades;
		this.weightClears = weightClears;
	}

	/**
	 * Evaluates the score of a board based on weighted factors.
	 * 
	 * @param board
	 *            The board to evaluate.
	 * @return The board's score.
	 */
	public double score(Board board)
	{
		//
		// TODO - modify line clears so that the board is not modified.
		//

		int clears = 0;
		for (int row = board.getHeight() - 1; row >= 0; row--) {
			if (board.isRowFull(row)) {
				clears++;
				board.removeRow(row);
			}
		}

		int[] heights = getHeights(board);

		int sumHeight = 0;
		int minHeight = heights[0];
		int maxHeight = heights[0];

		int holes = 0;
		int wells = 0;
		int blockades = 0;

		for (int col = 0; col < heights.length; col++) {
			sumHeight = sumHeight + heights[col];
			minHeight = Math.min(heights[col], minHeight);
			maxHeight = Math.max(heights[col], maxHeight);

			// To count the number of holes, count the empty blocks from the bottom to the top
			// of the column. To count the number of blockades, count the non-empty blocks from
			// first hole to the top of the column.

			int h = 0;
			for (int i = 0; i <= heights[col]; i++) {
				if (board.getShapeAt(i, col) == Shape.NoShape) {
					h++;
				} else if (h > 0) {
					blockades++;
				}
			}

			holes += h;

			// A well exists if a column's height is at least minWellDepth less than the columns
			// surrounding it. We compare each column's height with that of both its neighbors. The
			// side columns will only be compared with only one neighbor, as we consider the borders
			// of the board itself as infinite-height columns.

			int l = heights.length - 1;
			int h1 = col == 0 ? Integer.MAX_VALUE : heights[col - 1];
			int h2 = col == l ? Integer.MAX_VALUE : heights[col + 1];

			if (heights[col] < h1 && heights[col] < h2) {
				int depth = Math.min(h1, h2) - heights[col];

				if (depth >= minWellDepth) {
					wells += depth;
				}
			}
		}

		double score = 0;

		score += weightClears * clears;

		score += weightHoles * holes;
		score += weightWells * wells;
		score += weightBlockades * blockades;

		score += weightSumHeight * sumHeight;
		score += weightMaxHeight * maxHeight;
		score += weightRelHeight * maxHeight - minHeight;
		score += weightAvgHeight * sumHeight / heights.length;

		return score;
	}

	/**
	 * Retrieves an array of each column's height.
	 * 
	 * @param board
	 *            The board.
	 * @return An array of heights.
	 */
	private int[] getHeights(Board board)
	{
		int[] heights = new int[board.getWidth()];

		for (int col = 0; col < board.getWidth(); col++) {
			heights[col] = getColumnHeight(board, col);
		}

		return heights;
	}

	/**
	 * Gets the row index of the tallest block in a column.
	 * 
	 * @param board
	 *            The board.
	 * @param col
	 *            The column index.
	 * @return The height of the column.
	 */
	private int getColumnHeight(Board board, int col)
	{
		int height = board.getHeight() - 1;

		while (height > 0 && board.getShapeAt(height - 1, col) == Shape.NoShape) {
			height--;
		}

		return height;
	}
}
