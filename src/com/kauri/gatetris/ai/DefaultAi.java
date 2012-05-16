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
import com.kauri.gatetris.Tetromino;
import com.kauri.gatetris.Tetromino.Shape;

/**
 * @author Eric Fritz
 */
public class DefaultAi implements Strategy
{
	@Override
	public Move getBestMove(Board board, Tetromino current, Tetromino preview, int x, int y)
	{
		int bestRotationDelta = 0;
		int bestTranslationDelta = 0;

		Board dummy = null;

		double max = Double.NEGATIVE_INFINITY;

		//
		// TODO - also test for preview piece. I'm not sure what is best here to move into their own
		// methods (it's all pretty integrated).
		//

		for (int rotationDelta = 0; rotationDelta < 4; rotationDelta++) {
			int minTranslation = getMinTranslation(board, current, x, y);
			int maxTranslation = getMaxTranslation(board, current, x, y);

			for (int translationDelta = minTranslation; translationDelta <= maxTranslation; translationDelta++) {
				dummy = board.tryClone(dummy);

				if (dummy.tryMove(current, x + translationDelta, dummy.dropHeight(current, x + translationDelta))) {
					double score = score(dummy);

					if (score > max) {
						max = score;
						bestTranslationDelta = translationDelta;
						bestRotationDelta = rotationDelta;
					}
				}
			}

			current = Tetromino.rotateLeft(current);
		}

		return new Move(bestRotationDelta, bestTranslationDelta);
	}

	//
	// TODO - instead of running O(|min|+|max|), combine the methods and make it O(max{|min|,
	// |max|}), if that can keep the readability of the separate methods.
	//

	private int getMinTranslation(Board board, Tetromino piece, int x, int y)
	{
		int translation = 0;
		while (board.canMove(piece, x + translation, y)) {
			translation--;
		}

		return translation;
	}

	private int getMaxTranslation(Board board, Tetromino piece, int x, int y)
	{
		int translation = 0;
		while (board.canMove(piece, x + translation, y)) {
			translation++;
		}

		return translation;
	}

	//
	// TODO - move scoring system out of AI and somehow evolve it between rounds. I'm not sure where
	// this logic should really go. Interface for basic GA algorithm, and then the ai will do what
	// it wants at that point (other strategies could have different weights, etc).
	//

	private final int minWellDepth = 2;

	final double weightSumHeight = -3;
	final double weightMaxHeight = -3;
	final double weightRelHeight = -3;
	final double weightAvgHeight = -3;
	final double weightHoles = -5;
	final double weightWells = 0;
	final double weightBlockades = -10;
	final double weightClear = 40;

	/**
	 * Evaluates the score of a board based on weighted factors.
	 * 
	 * @param board
	 *            The board to evaluate.
	 * @return The board's score.
	 */
	private double score(Board board)
	{
		double score = weightClear * board.clearLines();

		int[] heights = getHeights(board);

		int sumHeight = 0;
		int minHeight = heights[0];
		int maxHeight = heights[0];

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
					score += weightBlockades;
				}
			}

			score += weightHoles * h;

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
					score += weightWells * depth;
				}
			}
		}

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
