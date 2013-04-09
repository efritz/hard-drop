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
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.kauri.tetris.GameContext.State;
import com.kauri.tetris.ai.AI;
import com.kauri.tetris.ai.Evolution;
import com.kauri.tetris.ai.MoveEvaluator;
import com.kauri.tetris.ai.ScoringSystem;
import com.kauri.tetris.sequence.LinePieceSelector;
import com.kauri.tetris.sequence.PieceSelector;
import com.kauri.tetris.sequence.PieceSequence;
import com.kauri.tetris.sequence.SZPieceSelector;
import com.kauri.tetris.sequence.ShufflePieceSelector;
import com.kauri.tetris.sequence.WorstPieceSelector;

/**
 * @author Eric Fritz
 */
public class Tetris extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	private GameContext context = new GameContext();

	private ScoringSystem scoring = new ScoringSystem();
	private MoveEvaluator evaluator = new MoveEvaluator(scoring);
	private Evolution evo = new Evolution(scoring);

	private UI ui = new UI(context);
	private AI ai = new AI(context, evaluator);
	private PlayerController player = new PlayerController(context);

	public Tetris()
	{
		this.addKeyListener(player);
		this.addComponentListener(ui);
	}

	public void start()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		JFrame frame = new JFrame();
		frame.setTitle("Tetris");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(300, 600));
		frame.setLocationRelativeTo(null);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		buildMenu(frame);

		frame.setVisible(true);

		new Thread(this).start();
	}

	@Override
	public void run()
	{
		context.newGame();

		while (true) {
			update();
			render();

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Thread.yield();
		}
	}

	private void update()
	{
		context.execute();

		if (context.getState() == State.GAMEOVER) {
			if (context.isAutoRestart()) {
				context.newGame();
			}
		}

		if (context.getState() == State.PLAYING) {
			if (ai.isEnabled()) {
				ai.update();
			} else {
				player.update();
			}
		}
	}

	private void render()
	{
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			requestFocus();
			createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		ui.render(g);

		g.dispose();
		bs.show();
	}

	private void buildMenu(final JFrame frame)
	{
		final JMenuItem item1 = new JCheckBoxMenuItem();

		item1.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setState(((JMenuItem) e.getSource()).isSelected() ? State.PAUSED : State.PLAYING);
			}
		});

		item1.setText("Pause");
		item1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

		context.registerNewGameListener(new NewGameListener() {
			@Override
			public void onNewGame()
			{
				item1.setEnabled(true);
				item1.setSelected(false);
			}
		});

		context.registerEndGameListener(new EndGameListener() {
			@Override
			public void onEndGame()
			{
				item1.setEnabled(false);
			}
		});

		JMenuItem item2 = new JMenuItem();

		item2.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.newGame();
			}
		});

		item2.setText("New Game");
		item2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		JMenuItem item3 = new JCheckBoxMenuItem();

		item3.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setAutoRestart(((JMenuItem) e.getSource()).isSelected());
			}
		});

		item3.setText("Auto-Replay");
		item3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));

		JMenuItem item4 = new JCheckBoxMenuItem();

		item4.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ui.setShowScore(((JMenuItem) e.getSource()).isSelected());
			}
		});

		item4.setText("Show Score");

		JMenuItem item5 = new JCheckBoxMenuItem();

		item5.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ui.setShowPreviewPiece(((JMenuItem) e.getSource()).isSelected());
			}
		});

		item5.setText("Show Preview");

		JMenuItem item6 = new JCheckBoxMenuItem();

		item6.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ui.setShowDropPosPiece(((JMenuItem) e.getSource()).isSelected());
			}
		});

		item6.setText("Show Shadow");

		JMenuItem item7 = new JCheckBoxMenuItem();

		item7.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ai.setEnabled(((JMenuItem) e.getSource()).isSelected());
			}
		});

		item7.setText("Enabled");
		item7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

		JMenuItem item8 = new JCheckBoxMenuItem();

		item8.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ai.setTraining(((JMenuItem) e.getSource()).isSelected());
			}
		});

		item8.setText("Train/Evolve");

		context.registerNewGameListener(new NewGameListener() {
			@Override
			public void onNewGame()
			{
				evo.updateScoring();
			}
		});

		context.registerEndGameListener(new EndGameListener() {
			@Override
			public void onEndGame()
			{
				if (ai.isTraining()) {
					evo.submit(context.getLines());
				}
			}
		});

		JMenuItem item9 = new JMenuItem();

		item9.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!item1.isSelected()) {
					item1.doClick();
				}

				JDialog dialog = new JDialog(frame);
				dialog.setTitle("About Tetris");

				dialog.setSize(200, 100);
				dialog.setLocationRelativeTo(frame);
				dialog.setVisible(true);
			}
		});

		item9.setText("About Tetris");

		JMenu menu2 = new JMenu("Board Size");
		ButtonGroup group1 = new ButtonGroup();

		for (int i = 1; i <= 6; i++) {
			createBoardSizeItem(menu2, group1, i * 5);
		}

		JMenu menu3 = new JMenu("Piece Sequence");
		ButtonGroup group2 = new ButtonGroup();

		Map<String, PieceSelector> selectors = new HashMap<String, PieceSelector>();
		selectors.put("Shuffle", new ShufflePieceSelector());
		selectors.put("Line", new LinePieceSelector());
		selectors.put("SZ", new SZPieceSelector());
		selectors.put("Worst", new WorstPieceSelector(context, evaluator));

		for (Map.Entry<String, PieceSelector> entry : selectors.entrySet()) {
			createSelectorItem(menu3, group2, entry.getValue(), entry.getKey());
		}

		JMenu menu6 = new JMenu("Speed");
		ButtonGroup group3 = new ButtonGroup();

		for (int i = 10; i >= 0; i--) {
			createSpeedItem(menu6, group3, (int) Math.pow(2, i));
		}

		JMenu menu1 = new JMenu("Game");
		menu1.add(item1);
		menu1.add(item2);
		menu1.add(item3);
		menu1.addSeparator();
		menu1.add(menu2);
		menu1.add(menu3);

		JMenuItem menu4 = new JMenu("View");
		menu4.add(item4);
		menu4.add(item5);
		menu4.add(item6);

		JMenuItem menu5 = new JMenu("AI");
		menu5.add(item7);
		menu5.add(item8);
		menu5.add(menu6);

		JMenuItem menu7 = new JMenu("Help");
		menu7.add(item9);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu1);
		menuBar.add(menu4);
		menuBar.add(menu5);
		menuBar.add(menu7);

		frame.setJMenuBar(menuBar);
	}

	private void createBoardSizeItem(JMenu menu, ButtonGroup group, final int width)
	{
		JMenuItem item = new JRadioButtonMenuItem();

		item.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setBoard(new Board(width, width * 2));
				context.newGame();
			}
		});

		if (width == 10) {
			item.setSelected(true);
		}

		menu.add(item);
		group.add(item);
		item.setText(width + "x" + (width * 2));
	}

	private void createSelectorItem(JMenu menu, ButtonGroup group, final PieceSelector selector, final String label)
	{
		JMenuItem item = new JRadioButtonMenuItem();

		item.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				context.setSequence(new PieceSequence(selector));
			}
		});

		if (label.equals("Shuffle")) {
			item.setSelected(true);
		}

		menu.add(item);
		group.add(item);
		item.setText(label);
	}

	private void createSpeedItem(JMenu menu, ButtonGroup group, final int delay)
	{
		JMenuItem item = new JRadioButtonMenuItem();

		item.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ai.setDelay(delay);
			}
		});

		if (delay == 128) {
			item.setSelected(true);
		}

		menu.add(item);
		group.add(item);
		item.setText("Speed " + delay);
	}

	public static void main(String[] args)
	{
		new Tetris().start();
	}
}
