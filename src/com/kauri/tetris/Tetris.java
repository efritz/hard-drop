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

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.kauri.tetris.GameContext.State;

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

		item1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setState(item1.isSelected() ? State.PAUSED : State.PLAYING);
			}
		});

		final JMenuItem item2 = new JMenuItem("New Game");

		item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.newGame();
			}
		});

		final JMenuItem item3 = new JCheckBoxMenuItem("Auto-Replay");

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
			JMenuItem item8 = new JRadioButtonMenuItem(width + "x" + (width * 2));

			menu2.add(item8);
			group1.add(item8);

			item8.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e)
				{
					context.setBoard(new Board(width, width * 2));
					context.newGame();
				}
			});
		}

		JMenu menu5 = new JMenu("Speed");
		ButtonGroup group2 = new ButtonGroup();

		for (int i = 10; i >= 0; i--) {
			final int delay = (int) Math.pow(2, i);
			JMenuItem item9 = new JRadioButtonMenuItem("Speed " + (10 - i));

			menu5.add(item9);
			group2.add(item9);

			item9.addActionListener(new ActionListener() {
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

		JMenuItem menu3 = new JMenu("View");
		menu3.add(item4);
		menu3.add(item5);
		menu3.add(item6);

		JMenuItem menu4 = new JMenu("AI");
		menu4.add(item7);
		menu4.add(menu5);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu1);
		menuBar.add(menu3);
		menuBar.add(menu4);

		frame.setJMenuBar(menuBar);
		frame.setVisible(true);

		game.start();
	}
}
