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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.kauri.tetris.GameContext.State;
import com.kauri.tetris.sequence.LinePieceSelector;
import com.kauri.tetris.sequence.PieceSelector;
import com.kauri.tetris.sequence.PieceSequence;
import com.kauri.tetris.sequence.SZPieceSelector;
import com.kauri.tetris.sequence.ShufflePieceSelector;
import com.kauri.tetris.sequence.WorstPieceSelector;

/**
 * @author Eric Fritz
 */
public class Tetris
{
	public static void main(String[] args)
	{
		final GameContext context = new GameContext();
		Game game = new Game(context);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		JFrame frame = new JFrame();
		frame.setMinimumSize(new Dimension(300, 600));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(game, BorderLayout.CENTER);

		final JMenuItem item1 = new JCheckBoxMenuItem("Pause");
		item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

		item1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setState(item1.isSelected() ? State.PAUSED : State.PLAYING);
			}
		});

		final JMenuItem item2 = new JMenuItem("New Game");
		item2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.newGame();
			}
		});

		final JMenuItem item3 = new JCheckBoxMenuItem("Auto-Replay");
		item3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));

		item3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setAutoRestart(item3.isSelected());
			}
		});

		final JMenuItem item4 = new JCheckBoxMenuItem("Show Score");

		item4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setShowScore(item4.isSelected());
			}
		});

		final JMenuItem item5 = new JCheckBoxMenuItem("Show Preview");

		item5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setShowPreviewPiece(item5.isSelected());
			}
		});

		final JMenuItem item6 = new JCheckBoxMenuItem("Show Shadow");

		item6.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setShowDropPosPiece(item6.isSelected());
			}
		});

		final JMenuItem item7 = new JCheckBoxMenuItem("Enabled");
		item7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

		item7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setRunningAi(item7.isSelected());
			}
		});

		JMenu menu2 = new JMenu("Board Size");
		ButtonGroup group1 = new ButtonGroup();

		for (int i = 5; i <= 30; i += 5) {
			final int width = i;
			JMenuItem item = new JRadioButtonMenuItem(width + "x" + (width * 2));

			if (width == 10) {
				item.setSelected(true);
			}

			menu2.add(item);
			group1.add(item);

			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					context.setBoard(new Board(width, width * 2));
					context.newGame();
				}
			});
		}

		JMenu menu3 = new JMenu("Piece Sequence");
		ButtonGroup group2 = new ButtonGroup();

		Map<String, PieceSelector> selectors = new HashMap<String, PieceSelector>();
		selectors.put("Shuffle", new ShufflePieceSelector());
		selectors.put("Line", new LinePieceSelector());
		selectors.put("SZ", new SZPieceSelector());
		selectors.put("Worst", new WorstPieceSelector(context));

		for (Map.Entry<String, PieceSelector> entry : selectors.entrySet()) {
			final PieceSelector selector2 = entry.getValue();
			JMenuItem item = new JRadioButtonMenuItem(entry.getKey());

			if (entry.getKey().equals("Shuffle")) {
				item.setSelected(true);
			}

			menu3.add(item);
			group2.add(item);

			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					context.setSequence(new PieceSequence(selector2));
				}
			});
		}

		JMenu menu6 = new JMenu("Speed");
		ButtonGroup group3 = new ButtonGroup();

		for (int i = 10; i >= 0; i--) {
			final int delay = (int) Math.pow(2, i);
			JMenuItem item = new JRadioButtonMenuItem("Speed " + (10 - i));

			if (delay == 128) {
				item.setSelected(true);
			}

			menu6.add(item);
			group3.add(item);

			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					context.setAiDelay(delay);
				}
			});
		}

		JMenu menu1 = new JMenu("Game");
		menu1.add(item1);
		menu1.add(item2);
		menu1.add(item3);
		menu1.add(menu2);
		menu1.add(menu3);

		JMenuItem menu4 = new JMenu("View");
		menu4.add(item4);
		menu4.add(item5);
		menu4.add(item6);

		JMenuItem menu5 = new JMenu("AI");
		menu5.add(item7);
		menu5.add(menu6);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu1);
		menuBar.add(menu4);
		menuBar.add(menu5);

		frame.setJMenuBar(menuBar);
		frame.setVisible(true);

		game.start();
	}
}
