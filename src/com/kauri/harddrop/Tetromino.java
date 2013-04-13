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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A tetromino is a geometric shape composed of four squares, connected orthogonally.
 * 
 * A tetromino is immutable and privately created. To reference a new tetromino, use the map of
 * pre-constructed one-sided {@link Tetromino#tetrominoes tetrominoes}.
 * 
 * @author Eric Fritz
 */
public class Tetromino
{
	/**
	 * A map of all seven pre-constructed one-sided tetrominoes.
	 */
	public static Map<Shape, Tetromino> tetrominoes = new HashMap<Shape, Tetromino>();

	static {
		tetrominoes.put(Shape.I, new Tetromino(Shape.I, new Point(-2, +0), new Point(-1, +0), new Point(+0, +0), new Point(+1, +0)));
		tetrominoes.put(Shape.J, new Tetromino(Shape.J, new Point(+1, +1), new Point(+1, +0), new Point(+0, +0), new Point(-1, +0)));
		tetrominoes.put(Shape.L, new Tetromino(Shape.L, new Point(-1, +1), new Point(-1, +0), new Point(+0, +0), new Point(+1, +0)));
		tetrominoes.put(Shape.O, new Tetromino(Shape.O, new Point(+0, +0), new Point(+1, +0), new Point(+0, +1), new Point(+1, +1)));
		tetrominoes.put(Shape.S, new Tetromino(Shape.S, new Point(+1, +0), new Point(+0, +0), new Point(+0, +1), new Point(-1, +1)));
		tetrominoes.put(Shape.T, new Tetromino(Shape.T, new Point(-1, +0), new Point(+0, +0), new Point(+1, +0), new Point(+0, +1)));
		tetrominoes.put(Shape.Z, new Tetromino(Shape.Z, new Point(+0, +0), new Point(-1, +0), new Point(+1, +1), new Point(+0, +1)));
	}

	/**
	 * A lazily-filled cache of the clockwise-rotation of tetrominoes.
	 */
	private static Map<Tetromino, Tetromino> rotationCache = new HashMap<Tetromino, Tetromino>();

	/**
	 * Point comparator for x-values.
	 */
	private static Comparator<Point> xComparator = new Comparator<Point>() {
		@Override
		public int compare(Point p1, Point p2)
		{
			return p1.x == p2.x ? 0 : (p1.x < p2.x ? -1 : 1);
		}
	};
	/**
	 * Point comparator for y-values.
	 */
	private static Comparator<Point> yComparator = new Comparator<Point>() {
		@Override
		public int compare(Point p1, Point p2)
		{
			return p1.y == p2.y ? 0 : (p1.y < p2.y ? -1 : 1);
		}
	};

	private Shape shape;
	private List<Point> points;

	/**
	 * Creates a new Tetromino.
	 * 
	 * @param shape
	 *            The tetromino shape.
	 * @param points
	 *            The points composing the tetromino.
	 */
	private Tetromino(Shape shape, Point... points)
	{
		this.shape = shape;
		this.points = Arrays.asList(points);
	}

	/**
	 * @return The tetromino shape.
	 */
	public Shape getShape()
	{
		return shape;
	}

	/**
	 * @return The number of points stored in the tetromino.
	 */
	public int getSize()
	{
		return points.size();
	}

	/**
	 * Retrieves the x-position of the <tt>i</tt>th point.
	 * 
	 * @param i
	 *            The index of the point to retrieve.
	 * @return The x-position.
	 */
	public int getX(int i)
	{
		return points.get(i).x;
	}

	/**
	 * Retrieves the y-position of the <tt>i</tt>th point.
	 * 
	 * @param i
	 *            The index of the point to retrieve.
	 * @return The y-position.
	 */
	public int getY(int i)
	{
		return points.get(i).y;
	}

	/**
	 * @return The x-component value of the point with the smallest x-component.
	 */
	public int getMinX()
	{
		return Collections.min(points, xComparator).x;
	}

	/**
	 * @return The x-component value of the point with the largest x-component.
	 */
	public int getMaxX()
	{
		return Collections.max(points, xComparator).x;
	}

	/**
	 * @return The y-component value of the point with the smallest y-component.
	 */
	public int getMinY()
	{
		return Collections.min(points, yComparator).y;
	}

	/**
	 * @return The y-component value of the point with the largest y-component.
	 */
	public int getMaxY()
	{
		return Collections.max(points, yComparator).y;
	}

	/**
	 * @return The number of horizontal blocks the tetromino occupies.
	 */
	public int getWidth()
	{
		return Math.abs(getMinX()) + Math.abs(getMaxX()) + 1;
	}

	/**
	 * @return The number of vertical blocks the tetromino occupies.
	 */
	public int getHeight()
	{
		return Math.abs(getMinY()) + Math.abs(getMaxY()) + 1;
	}

	/**
	 * Creates a tetromino which is a clockwise transformation of <tt>original</tt>.
	 * 
	 * @param original
	 *            The tetromino to transform.
	 * @return A new tetromino.
	 */
	public static Tetromino rotateClockwise(Tetromino original)
	{
		if (original.shape == Shape.O) {
			return original;
		}

		if (!rotationCache.containsKey(original)) {
			Point[] points = new Point[original.getSize()];

			int i = 0;
			for (Point p : original.points) {
				points[i++] = new Point(-p.y, p.x);
			}

			rotationCache.put(original, new Tetromino(original.shape, points));
		}

		return rotationCache.get(original);
	}

	/**
	 * Creates a tetromino which is a counter-clockwise transformation of <tt>original</tt>.
	 * 
	 * @param original
	 *            The tetromino to transform.
	 * @return A new tetromino.
	 */
	public static Tetromino rotateCounterClockwise(Tetromino original)
	{
		return rotateClockwise(rotateClockwise(rotateClockwise(original)));
	}

	@Override
	public int hashCode()
	{
		int result = shape.hashCode();

		for (int i = 0; i < this.getSize(); i++) {
			result = 31 * result + this.getX(i);
			result = 31 * result + this.getY(i);
		}

		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		Tetromino other = (Tetromino) obj;

		if (this.shape != other.shape || this.getSize() != other.getSize()) {
			return false;
		}

		for (int i = 0; i < getSize(); i++) {
			if (this.getX(i) != other.getX(i) || this.getY(i) != other.getY(i)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * A Point represents a single (x, y) position.
	 * 
	 * @author Eric Fritz
	 */
	private static class Point
	{
		public int x;
		public int y;

		public Point(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}
}
