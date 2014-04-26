/*
 * This file is part of the tetris package.
 *
 * Copyright (c) 2014 Eric Fritz
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

import com.kauri.harddrop.GameContext.State;
import com.kauri.harddrop.ai.AI;
import com.kauri.harddrop.ai.Evolution;
import com.kauri.harddrop.ai.MoveEvaluator;
import com.kauri.harddrop.ai.ScoringSystem;
import com.kauri.harddrop.sequence.LinePieceSelector;
import com.kauri.harddrop.sequence.PieceSelector;
import com.kauri.harddrop.sequence.PieceSequence;
import com.kauri.harddrop.sequence.SZPieceSelector;
import com.kauri.harddrop.sequence.ShufflePieceSelector;
import com.kauri.harddrop.sequence.WorstPieceSelector;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.WindowConstants;

/**
 * @author Eric Fritz
 */
public class Tetris extends Canvas implements Runnable
{
	private static final long serialVersionUID = 1L;

	private GameContext context = new GameContext();

	private JFrame frame;
	private ScoringSystem scoring = new ScoringSystem();
	private MoveEvaluator evaluator = new MoveEvaluator(scoring);
	private Evolution evo = new Evolution(scoring);

	private UI ui = new UI(context);
	private AI ai = new AI(context, evaluator);
	private PlayerController player = new PlayerController(context);

	public Tetris() {
		this.addKeyListener(player);
		this.addComponentListener(ui);
	}

	public void start() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		frame = new JFrame();
		frame.setTitle("Tetris");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.setMinimumSize(new Dimension(300, 600));
		frame.setLocationRelativeTo(null);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		buildMenu(frame);

		frame.setVisible(true);

		new Thread(this).start();
	}

	@Override
	public void run() {
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

	private void update() {
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

	private void render() {
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

	private void buildMenu(final JFrame frame) {
		context.registerNewGameListener(evo::updateScoring);

		context.registerEndGameListener(() -> {
			if (ai.isTraining()) {
				evo.submit(context.getLines());
			}
		});

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(buildGameMenu());
		menuBar.add(buildViewMenu());
		menuBar.add(buildAiMenu());
		menuBar.add(buildHelpMenu());

		frame.setJMenuBar(menuBar);
	}

	private JMenu buildGameMenu() {
		JMenuItem pauseItem;
		JMenuItem newGameItem;
		JMenuItem autoReplayItem;

		pauseItem = new JCheckBoxMenuItem();
		pauseItem.setText("Pause");
		pauseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		pauseItem.addActionListener((e) -> context.pause(((JMenuItem) e.getSource()).isSelected()));

		newGameItem = new JMenuItem();
		newGameItem.setText("New Game");
		newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		newGameItem.addActionListener((e) -> context.newGame());

		autoReplayItem = new JCheckBoxMenuItem();
		autoReplayItem.setText("Auto-Replay");
		autoReplayItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		autoReplayItem.addActionListener((e) -> context.setAutoRestart(((JMenuItem) e.getSource()).isSelected()));

		context.registerNewGameListener(() -> {
			pauseItem.setEnabled(true);
			pauseItem.setSelected(false);
		});

		context.registerEndGameListener(() -> pauseItem.setEnabled(false));

		JMenu gameMenu = new JMenu("Game");
		gameMenu.add(pauseItem);
		gameMenu.add(newGameItem);
		gameMenu.add(autoReplayItem);
		gameMenu.addSeparator();
		gameMenu.add(buildBoardSizeMenu());
		gameMenu.add(buildSequenceMenu());

		return gameMenu;
	}

	private JMenu buildViewMenu() {
		JMenuItem showScoreItem;
		JMenuItem showPreviewItem;
		JMenuItem showShadowItem;

		showScoreItem = new JCheckBoxMenuItem();
		showScoreItem.setText("Show Score");
		showScoreItem.addActionListener((e) -> ui.setShowScore(((JMenuItem) e.getSource()).isSelected()));

		showPreviewItem = new JCheckBoxMenuItem();
		showPreviewItem.setText("Show Preview");
		showPreviewItem.addActionListener((e) -> ui.setShowPreviewPiece(((JMenuItem) e.getSource()).isSelected()));

		showShadowItem = new JCheckBoxMenuItem();
		showShadowItem.setText("Show Shadow");
		showShadowItem.addActionListener((e) -> ui.setShowDropPosPiece(((JMenuItem) e.getSource()).isSelected()));

		JMenu menu = new JMenu("View");
		menu.add(showScoreItem);
		menu.add(showPreviewItem);
		menu.add(showShadowItem);

		return menu;
	}

	private JMenu buildAiMenu() {
		JMenuItem aiEnabledItem;
		JMenuItem evolveItem;

		aiEnabledItem = new JCheckBoxMenuItem();
		aiEnabledItem.setText("Enabled");
		aiEnabledItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		aiEnabledItem.addActionListener((e) -> ai.setEnabled(((JMenuItem) e.getSource()).isSelected()));

		evolveItem = new JCheckBoxMenuItem();
		evolveItem.setText("Train/Evolve");
		evolveItem.addActionListener((e) -> ai.setTraining(((JMenuItem) e.getSource()).isSelected()));

		JMenu menu = new JMenu("AI");
		menu.add(aiEnabledItem);
		menu.add(evolveItem);
		menu.add(buildSpeedMenu());

		return menu;
	}

	private JMenu buildHelpMenu() {
		JMenuItem aboutItem;

		aboutItem = new JMenuItem();
		aboutItem.setText("About Tetris");
		aboutItem.addActionListener((e) -> {
			JDialog dialog = new JDialog(frame);
			dialog.setTitle("About Tetris");

			dialog.setSize(200, 100);
			dialog.setLocationRelativeTo(frame);
			dialog.setVisible(true);
		});

		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(aboutItem);

		return helpMenu;
	}

	private JMenu buildBoardSizeMenu() {
		JMenu menu = new JMenu("Board Size");
		ButtonGroup group = new ButtonGroup();

		for (int i = 1; i <= 6; i++) {
			createBoardSizeItem(menu, group, i * 5);
		}

		return menu;
	}

	private JMenu buildSequenceMenu() {
		JMenu menu = new JMenu("Piece Sequence");
		ButtonGroup group = new ButtonGroup();

		Map<String, PieceSelector> selectors = new HashMap<>();
		selectors.put("Shuffle", new ShufflePieceSelector());
		selectors.put("Line", new LinePieceSelector());
		selectors.put("SZ", new SZPieceSelector());
		selectors.put("Worst", new WorstPieceSelector(context, evaluator));

		for (Map.Entry<String, PieceSelector> entry : selectors.entrySet()) {
			createSelectorItem(menu, group, entry.getValue(), entry.getKey());
		}

		return menu;
	}

	private JMenu buildSpeedMenu() {
		JMenu speedMenu = new JMenu("Speed");
		ButtonGroup group3 = new ButtonGroup();

		for (int i = 10; i >= 0; i--) {
			createSpeedItem(speedMenu, group3, (int) Math.pow(2, i));
		}

		return speedMenu;
	}

	private void createBoardSizeItem(JMenu menu, ButtonGroup group, final int width) {
		JMenuItem item = new JRadioButtonMenuItem();
		item.addActionListener((e) -> {
			context.setBoard(new Board(width, width * 2));
			context.newGame();
		});

		if (width == 10) {
			item.setSelected(true);
		}

		menu.add(item);
		group.add(item);
		item.setText(width + "x" + (width * 2));
	}

	private void createSelectorItem(JMenu menu, ButtonGroup group, final PieceSelector selector, final String label) {
		JMenuItem item = new JRadioButtonMenuItem();
		item.addActionListener((e) -> context.setSequence(new PieceSequence(selector)));

		if (label.equals("Shuffle")) {
			item.setSelected(true);
		}

		menu.add(item);
		group.add(item);
		item.setText(label);
	}

	private void createSpeedItem(JMenu menu, ButtonGroup group, final int delay) {
		JMenuItem item = new JRadioButtonMenuItem();
		item.addActionListener((e) -> ai.setDelay(delay));

		if (delay == 128) {
			item.setSelected(true);
		}

		menu.add(item);
		group.add(item);
		item.setText("Speed " + delay);
	}

	public static void main(String[] args) {
		new Tetris().start();
	}
}
