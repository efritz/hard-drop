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

package com.kauri.gatetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import com.kauri.gatetris.GameData.State;
import com.kauri.gatetris.Tetromino.Shape;
import com.kauri.gatetris.ai.AI.Move;

/**
 * @author efritz
 */
public class UI
{
	private static Map<Shape, Color> colors = new HashMap<Shape, Color>();

	static {
		colors.put(Shape.I, Color.red);
		colors.put(Shape.J, Color.blue);
		colors.put(Shape.L, Color.orange);
		colors.put(Shape.O, Color.yellow);
		colors.put(Shape.S, Color.magenta);
		colors.put(Shape.T, Color.cyan);
		colors.put(Shape.Z, Color.green);
		colors.put(Shape.Junk, Color.darkGray);
		colors.put(Shape.NoShape, new Color(240, 240, 240));
	}

	boolean showAiPiece = false;
	boolean showNextPiece = false;
	boolean showShadowPiece = false;

	private int width;
	private int height;
	private Game game;

	public UI(Game game)
	{
		this.game = game;
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	private int getWidth()
	{
		return width;
	}

	private int getHeight()
	{
		return height;
	}

	private int getAdjustedBoardWidth()
	{
		return (int) Math.min(getWidth(), (double) getHeight() * game.data.getBoard().getWidth() / game.data.getBoard().getHeight());
	}

	private int getAdjustedBoardHeight()
	{
		return (int) Math.min(getHeight(), (double) getWidth() * game.data.getBoard().getHeight() / game.data.getBoard().getWidth());
	}

	private int getSquareWidth()
	{
		return getAdjustedBoardWidth() / (game.data.getBoard().getWidth() + (showNextPiece ? 2 : 0));
	}

	private int getSquareHeight()
	{
		return getAdjustedBoardHeight() / (game.data.getBoard().getHeight() + (showNextPiece ? 4 : 0));
	}

	private int getLeftMargin()
	{
		return (getWidth() - game.data.getBoard().getWidth() * getSquareWidth()) / 2;
	}

	private int getTopMargin()
	{
		return (getHeight() - game.data.getBoard().getHeight() * getSquareWidth()) / 2;
	}

	public void render(Graphics g)
	{
		g.setColor(colors.get(Shape.NoShape));
		g.fillRect(0, 0, getWidth(), getHeight());

		for (int row = 0; row < game.data.getBoard().getHeight(); row++) {
			for (int col = 0; col < game.data.getBoard().getWidth(); col++) {
				drawSquare(g, translateBoardRow(row), translateBoardCol(col), colors.get(game.data.getBoard().getShapeAt(row, col)));
			}
		}

		if (showShadowPiece) {
			int ghostPosition = game.data.getBoard().dropHeight(game.data.getCurrent(), game.data.getxPos(), game.data.getyPos());

			if (ghostPosition < game.data.getyPos()) {
				drawTetromino(g, game.data.getCurrent(), translateBoardRow(ghostPosition), translateBoardCol(game.data.getxPos()), changeAlpha(colors.get(game.data.getCurrent().getShape()), .3), getTopMargin());
			}
		} else if (showAiPiece) {
			Move move = game.ai.getBestMove(game.data.getBoard(), game.data.getCurrent(), game.data.getPreview(), game.data.getxPos(), game.data.getyPos());

			Tetromino current2 = game.data.getCurrent();

			for (int i = 0; i < move.rotationDelta; i++) {
				current2 = Tetromino.rotateLeft(current2);
			}

			int ghostPosition = game.data.getBoard().dropHeight(current2, game.data.getxPos() + move.translationDelta, game.data.getyPos());

			if (ghostPosition < game.data.getyPos()) {
				drawTetromino(g, current2, translateBoardRow(ghostPosition), translateBoardCol(game.data.getxPos() + move.translationDelta), changeAlpha(colors.get(current2.getShape()), .3), getTopMargin());
			}
		}

		if (game.data.getBoard().canMove(game.data.getCurrent(), game.data.getxPos(), game.data.getyPos())) {
			drawTetromino(g, game.data.getCurrent(), translateBoardRow(game.data.getyPos()), translateBoardCol(game.data.getxPos()), colors.get(game.data.getCurrent().getShape()), getTopMargin());
		}

		if (showNextPiece) {
			int xPos = (game.data.getBoard().getWidth() - game.data.getPreview().getWidth()) / 2 + Math.abs(game.data.getPreview().getMinX());

			int rowOffset = (getTopMargin() - (game.data.getPreview().getHeight() * getSquareHeight())) / 2;

			drawTetromino(g, game.data.getPreview(), rowOffset, translateBoardCol(xPos), colors.get(game.data.getPreview().getShape()), 0);
		}

		if (game.data.getState() == State.PAUSED) {
			drawString(g, "paused");
		}

		if (game.data.getState() == State.GAMEOVER) {
			drawString(g, "game over");
		}

	}

	private Color changeAlpha(Color color, double percent)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(1, (int) (color.getAlpha() * percent))));
	}

	private void drawString(Graphics g, String string)
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setFont(scaleFont(g, new Font("Arial", Font.PLAIN, 20), string, (int) (getWidth() * .85)));
		FontMetrics fm = g.getFontMetrics();

		g.setColor(new Color(0, 0, 0, (int) (255 * .5)));
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(new Color(255, 255, 255));
		g.drawString(string, (getWidth() / 2) - (fm.stringWidth(string) / 2), (getHeight() / 2) + fm.getDescent());
	}

	/**
	 * @see http://stackoverflow.com/questions/876234/need-a-way-to-scale-a-font-to-fit-a-rectangle
	 */
	public Font scaleFont(Graphics g, Font font, String text, int width)
	{
		return g.getFont().deriveFont(font.getSize2D() * width / g.getFontMetrics(font).stringWidth(text));
	}

	private int translateBoardRow(int row)
	{
		return getTopMargin() + (game.data.getBoard().getHeight() - 1 - row) * getSquareHeight();
	}

	private int translateBoardCol(int col)
	{
		return getLeftMargin() + col * getSquareWidth();
	}

	private void drawTetromino(Graphics g, Tetromino piece, int row, int col, Color color, int top)
	{
		if (piece.getShape() != Shape.NoShape) {
			for (int i = 0; i < piece.getSize(); i++) {
				int xPos = col + piece.getX(i) * getSquareWidth();
				int yPos = row + piece.getY(i) * getSquareHeight();

				if (yPos >= top) {
					drawSquare(g, yPos, xPos, color);
				}
			}
		}
	}

	private void drawSquare(Graphics g, int row, int col, Color color)
	{
		g.setColor(color.darker());
		g.fillRect(col, row, getSquareWidth(), getSquareHeight());

		g.setColor(color);
		g.fillRect(col + 1, row + 1, getSquareWidth() - 2, getSquareHeight() - 2);
	}
}
