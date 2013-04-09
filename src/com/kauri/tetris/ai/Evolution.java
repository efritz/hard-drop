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

package com.kauri.tetris.ai;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * @author Eric Fritz
 */
public class Evolution
{
	private final static String filename = "aiscores.txt";

	private final int populationSize = 16;
	private final double elitePercent = 1 / 4.0;
	private final double mutationRate = 1 / 10.0;

	private int current = 0;
	private int generation = 1;

	Long[] scores = new Long[populationSize];
	Weights[] population = new Weights[populationSize];

	private ScoringSystem scoring;

	/**
	 * Creates a new Evolution.
	 */
	public Evolution(ScoringSystem scoring)
	{
		this.scoring = scoring;

		try (Scanner scanner = new Scanner(new BufferedReader(new FileReader(filename)))) {
			for (int i = 0; i < populationSize; i++) {
				double[] weights = new double[8];

				for (int j = 0; j < weights.length; j++) {
					weights[j] = scanner.nextDouble();
				}

				population[i] = new Weights(weights);
				scanner.nextLine();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Population data not found - generating random population.");

			for (int i = 0; i < populationSize; i++) {
				double[] weights = new double[8];

				for (int j = 0; j < weights.length; j++) {
					weights[j] = Math.random() * 10 - 5;
				}

				population[i] = new Weights(weights);
			}
		}
	}

	/**
	 * Apply the next chromosome to the scoring system.
	 */
	public void updateScoring()
	{
		scoring.setWeights(population[current]);
	}

	/**
	 * Records the score of the game played with the current weights.
	 * 
	 * @param score
	 *            The number of lines cleared on the last game with the current weights.
	 */
	public void submit(long score)
	{
		System.out.printf("Generation %-2d - Candidate %-2d: score = %d\n", generation, current + 1, score);

		scores[current++] = score;

		if (current == populationSize) {
			newGeneration();
		}
	}

	/**
	 * Create a new generation based off of the success of the last generation.
	 */
	private void newGeneration()
	{
		Integer[] idx = new Integer[populationSize];

		for (int i = 0; i < populationSize; i++) {
			idx[i] = i;
		}

		Arrays.sort(idx, new Comparator<Integer>() {
			@Override
			public int compare(Integer i, Integer j)
			{
				return Double.compare(scores[j], scores[i]);
			}
		});

		System.out.printf("Generation %-2d - max = %d, med = %d, min = %d\n", generation, scores[idx[0]], scores[idx[populationSize / 2]], scores[idx[populationSize - 1]]);
		System.out.printf("\n");

		Weights[] newPopulation = new Weights[populationSize];

		for (int i = 0; i < populationSize; i++) {
			if (i < populationSize * elitePercent) {
				newPopulation[i] = population[idx[i]];
			} else {
				int w1 = (int) (Math.random() * (populationSize / 2));
				int w2 = (int) (Math.random() * (populationSize / 2));

				double[] child = new double[8];

				for (int j = 0; j < child.length; j++) {
					child[j] = population[idx[Math.random() < .5 ? w1 : w2]].getWeights()[j];

					if (Math.random() < mutationRate) {
						child[j] = Math.random() * 10 - 5;
					}
				}

				newPopulation[i] = new Weights(child);
			}
		}

		population = newPopulation;

		current = 0;
		generation++;

		try (FileWriter writer = new FileWriter(filename)) {
			for (Weights weights : population) {
				writer.write(weights + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Saving File.");
	}
}
