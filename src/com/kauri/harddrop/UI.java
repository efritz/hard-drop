/*
 * This file is part of the tetris package.
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

package com.kauri.harddrop;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

import com.kauri.harddrop.GameContext.State;

/**
 * @author efritz
 */
public class UI implements ComponentListener
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
		return getAdjustedBoardWidth() / (context.getBoard().getWidth() + (showPreviewPiece() ? maximumTetrominoHeight : 0));
	}

	private int getSquareHeight()
	{
		return getAdjustedBoardHeight() / (context.getBoard().getHeight() + (showPreviewPiece() ? maximumTetrominoHeight * 2 : 0));
	}

	private int getLeftMargin()
	{
		return (getWidth() - context.getBoard().getWidth() * (getSquareWidth() - 1)) / 2;
	}

	private int getTopMargin()
	{
		return (getHeight() - context.getBoard().getHeight() * (getSquareHeight() - 1)) / 2;
	}

	private int translateBoardRow(int row)
	{
		return getTopMargin() + (context.getBoard().getHeight() - 1 - row) * (getSquareHeight() - 1);
	}

	private int translateBoardCol(int col)
	{
		return getLeftMargin() + col * getSquareWidth() - col;
	}

	public void render(Graphics g)
	{
		clear(g, colors.get(Shape.NoShape));

		for (int row = 0; row < context.getBoard().getHeight(); row++) {
			for (int col = 0; col < context.getBoard().getWidth(); col++) {
				drawBoardTranslatedSquare(g, row, col, colors.get(context.getBoard().getShapeAt(row, col)));
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

		int x = context.getX();
		int y = context.getY();

		drawTetromino(g, current, y, x, colors.get(current.getShape()));
	}

	private void renderDropPosTetromino(Graphics g, Tetromino current)
	{
		if (context.getState() == State.GAMEOVER || !showDropPosPiece()) {
			return;
		}

		int x = context.getX();
		int y = context.getBoard().dropHeight(current, context.getX(), context.getY());

		drawTetromino(g, current, y, x, changeAlpha(colors.get(current.getShape()), 30));
	}

	private void renderPreviewTetromino(Graphics g, Tetromino preview)
	{
		if (!showPreviewPiece()) {
			return;
		}

		int x = context.getBoard().getSpawnX(preview);
		int y = context.getBoard().getSpawnY(preview) + preview.getHeight();

		drawTetromino(g, preview, y, x, colors.get(preview.getShape()), true);
	}

	private void renderGui(Graphics g)
	{
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (context.getState() == GameContext.State.GAMEOVER) {
			clear(g, textBackgroundColor);
			g.setColor(textForegroundColor);
			drawWindowWideString(g, String.format("Game Over: %d (%d)", context.getScore(), context.getLines()));
		} else if (context.getState() == GameContext.State.PAUSED) {
			clear(g, textBackgroundColor);
			g.setColor(textForegroundColor);
			drawWindowWideString(g, "Paused");
		} else if (getShowScore()) {
			clear(g, textBackgroundColor);
			g.setColor(textForegroundColor);
			drawWindowWideString(g, String.format("%d (%d)", context.getScore(), context.getLines()));
		}
	}

	private void clear(Graphics g, Color c)
	{
		g.setColor(c);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawTetromino(Graphics g, Tetromino piece, int row, int col, Color color)
	{
		drawTetromino(g, piece, row, col, color, false);
	}

	private void drawTetromino(Graphics g, Tetromino piece, int row, int col, Color color, boolean displayOffBoard)
	{
		if (piece.getShape() != Shape.NoShape) {
			for (int i = 0; i < piece.getSize(); i++) {
				int x = col + piece.getX(i);
				int y = row - piece.getY(i);

				if (displayOffBoard || y < context.getBoard().getHeight()) {
					drawBoardTranslatedSquare(g, y, x, color);
				}
			}
		}
	}

	private void drawBoardTranslatedSquare(Graphics g, int row, int col, Color color)
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

	private void drawWindowWideString(Graphics g, String string)
	{
		drawCenteredString(g, scaleFont(g, baseFont, string, (int) (getWidth() * .85)), string, getWidth() / 2, getHeight() / 2);
	}

	private void drawCenteredString(Graphics g, Font font, String string, int x, int y)
	{
		g.setFont(font);

		x -= g.getFontMetrics().stringWidth(string) / 2;
		y += g.getFontMetrics().getDescent();

		g.drawString(string, x, y);
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

	//
	// UI Settings

	private boolean showScore = false;
	private boolean showPreviewPiece = false;
	private boolean showDropPosPiece = false;

	public boolean getShowScore()
	{
		return showScore;
	}

	public void setShowScore(boolean b)
	{
		this.showScore = b;
	}

	public boolean showPreviewPiece()
	{
		return showPreviewPiece;
	}

	public void setShowPreviewPiece(boolean showPreviewPiece)
	{
		this.showPreviewPiece = showPreviewPiece;
	}

	public boolean showDropPosPiece()
	{
		return showDropPosPiece;
	}

	public void setShowDropPosPiece(boolean showDropPosPiece)
	{
		this.showDropPosPiece = showDropPosPiece;
	}

	@Override
	public void componentShown(ComponentEvent ce)
	{
	}

	@Override
	public void componentHidden(ComponentEvent ce)
	{
	}

	@Override
	public void componentMoved(ComponentEvent ce)
	{
	}

	@Override
	public void componentResized(ComponentEvent ce)
	{
		setSize(ce.getComponent().getWidth(), ce.getComponent().getHeight());
	}
}
