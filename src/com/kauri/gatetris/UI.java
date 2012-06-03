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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import com.kauri.gatetris.GameContext.State;
import com.kauri.gatetris.Tetromino.Shape;

/**
 * @author efritz
 */
public class UI
{
	private static final int maximumTetrominoHeight = 2;

	private static final Color gameBackgroundColor = Color.white;
	private static final Color textForegroundColor = Color.white;
	private static final Color textBackgroundColor = changeAlpha(Color.black, 50);
	private static final Font baseFont = new Font("Arial", Font.PLAIN, 20);

	private static final Map<Shape, Color> colors = new HashMap<Shape, Color>();

	static {
		colors.put(Shape.I, Color.red);
		colors.put(Shape.J, Color.blue);
		colors.put(Shape.L, Color.orange);
		colors.put(Shape.O, Color.yellow);
		colors.put(Shape.S, Color.magenta);
		colors.put(Shape.T, Color.cyan);
		colors.put(Shape.Z, Color.green);
		colors.put(Shape.Junk, Color.darkGray);
		colors.put(Shape.NoShape, gameBackgroundColor);
	}

	private int width;
	private int height;
	private GameContext context;

	public UI(GameContext context)
	{
		this.context = context;
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
		return (int) Math.min(getWidth(), (double) getHeight() * context.getBoard().getWidth() / context.getBoard().getHeight());
	}

	private int getAdjustedBoardHeight()
	{
		return (int) Math.min(getHeight(), (double) getWidth() * context.getBoard().getHeight() / context.getBoard().getWidth());
	}

	private int getSquareWidth()
	{
		return getAdjustedBoardWidth() / (context.getBoard().getWidth() + (context.showPreviewPiece() ? maximumTetrominoHeight : 0));
	}

	private int getSquareHeight()
	{
		return getAdjustedBoardHeight() / (context.getBoard().getHeight() + (context.showPreviewPiece() ? maximumTetrominoHeight * 2 : 0));
	}

	private int getLeftMargin()
	{
		return (getWidth() - context.getBoard().getWidth() * (getSquareWidth() - 1)) / 2;
	}

	private int getTopMargin()
	{
		return (getHeight() - context.getBoard().getHeight() * (getSquareHeight() - 1)) / 2;
	}

	public void render(Graphics g)
	{
		clear(g, colors.get(Shape.NoShape));

		for (int row = 0; row < context.getBoard().getHeight(); row++) {
			for (int col = 0; col < context.getBoard().getWidth(); col++) {
				drawBoardSquare(g, row, col, colors.get(context.getBoard().getShapeAt(row, col)));
			}
		}

		renderCurrentTetromino(g, context.getCurrent());
		renderDropPosTetromino(g, context.getCurrent());
		renderPreviewTetromino(g, context.getPreview());

		renderGui(g);
	}

	private void renderCurrentTetromino(Graphics g, Tetromino current)
	{
		if (context.getState() == State.GAMEOVER) {
			return;
		}

		int xPos = context.getX();
		int yPos = context.getY();

		drawTetromino(g, current, yPos, xPos, colors.get(current.getShape()));
	}

	private void renderDropPosTetromino(Graphics g, Tetromino current)
	{
		if (context.getState() == State.GAMEOVER || !context.showDropPosPiece()) {
			return;
		}

		int xPos = context.getX();
		int yPos = context.getBoard().dropHeight(current, context.getX(), context.getY());

		drawTetromino(g, current, yPos, xPos, changeAlpha(colors.get(current.getShape()), 30));
	}

	private void renderPreviewTetromino(Graphics g, Tetromino preview)
	{
		if (!context.showPreviewPiece()) {
			return;
		}

		int xPos = context.getBoard().getSpawnX(preview);
		int yPos = context.getBoard().getHeight() - 1 + preview.getHeight();

		drawTetromino(g, preview, yPos, xPos, colors.get(preview.getShape()));
	}

	private void renderGui(Graphics g)
	{
		if (context.getShowScore()) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			clear(g, textBackgroundColor);
			g.setColor(textForegroundColor);
			drawWindowWideString(g, String.format("%d (%d)", context.getScore(), context.getLines()));
		}
	}

	private void drawWindowWideString(Graphics g, String string)
	{
		g.setFont(scaleFont(g, baseFont, string, (int) (getWidth() * .85)));

		drawCenteredString(g, string, getWidth() / 2, getHeight() / 2);
	}

	private void drawCenteredString(Graphics g, String string, int x, int y)
	{
		g.drawString(string, x - (g.getFontMetrics().stringWidth(string) / 2), y + g.getFontMetrics().getDescent());
	}

	private void clear(Graphics g, Color c)
	{
		g.setColor(c);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private int translateBoardRow(int row)
	{
		return getTopMargin() + (context.getBoard().getHeight() - 1 - row) * getSquareHeight() - (context.getBoard().getHeight() - 1 - row);
	}

	private int translateBoardCol(int col)
	{
		return getLeftMargin() + col * getSquareWidth() - col;
	}

	private void drawTetromino(Graphics g, Tetromino piece, int row, int col, Color color)
	{
		if (piece.getShape() != Shape.NoShape) {
			for (int i = 0; i < piece.getSize(); i++) {
				int xPos = col + piece.getX(i);
				int yPos = row - piece.getY(i);

				drawBoardSquare(g, yPos, xPos, color);
			}
		}
	}

	private void drawBoardSquare(Graphics g, int row, int col, Color color)
	{
		drawSquare(g, translateBoardRow(row), translateBoardCol(col), color);
	}

	private void drawSquare(Graphics g, int row, int col, Color color)
	{
		g.setColor(color.darker());
		g.fillRect(col, row, getSquareWidth(), getSquareHeight());

		g.setColor(color);
		g.fillRect(col + 1, row + 1, getSquareWidth() - 2, getSquareHeight() - 2);
	}

	private static Color changeAlpha(Color color, double percent)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(1, (int) (color.getAlpha() * (percent / 100.0)))));
	}

	/**
	 * @see http://stackoverflow.com/questions/876234/need-a-way-to-scale-a-font-to-fit-a-rectangle
	 */
	private static Font scaleFont(Graphics g, Font font, String text, int width)
	{
		return g.getFont().deriveFont(font.getSize2D() * width / g.getFontMetrics(font).stringWidth(text));
	}
}
