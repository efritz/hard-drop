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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Eric Fritz
 */
public class Evolution
{
	private final int populationSize = 16;
	private final double elitePercent = .25;
	private final double mutationRate = .10;

	private int generation = 1;

	private static class Entity
	{
		public long score;
		public double[] chromosomes;

		public Entity(double[] chromosomes)
		{
			this.chromosomes = chromosomes;
		}
	}

	List<Entity> population = new ArrayList<Entity>();

	int current = 0;

	public Evolution()
	{
		for (int i = 0; i < populationSize; i++) {
			double[] chromosomes = new double[8];

			for (int j = 0; j < 8; j++) {
				chromosomes[j] = Math.random() * 10 - 5;
			}

			population.add(new Entity(chromosomes));
		}
	}

	public void updateScoring(ScoringSystem scoring)
	{
		scoring.setWeights(population.get(current).chromosomes[0], population.get(current).chromosomes[1], population.get(current).chromosomes[2], population.get(current).chromosomes[3], population.get(current).chromosomes[4], population.get(current).chromosomes[5], population.get(current).chromosomes[6], population.get(current).chromosomes[7]);
	}

	private void newGeneration()
	{
		Collections.sort(population, new Comparator<Entity>() {
			@Override
			public int compare(Entity e1, Entity e2)
			{
				return Long.compare(e2.score, e1.score);
			}
		});

		System.out.printf("Generation %-2d - max = %d, med = %d, min = %d\n", generation, population.get(0).score, population.get(populationSize / 2).score, population.get(populationSize - 1).score);
		System.out.printf("\n");

		List<Entity> new_population = new ArrayList<Entity>();

		for (int i = 0; i < populationSize * elitePercent; i++) {
			new_population.add(new Entity(population.get(i).chromosomes));
		}

		while (new_population.size() < populationSize) {
			int w1 = (int) (Math.random() * (populationSize / 2));
			int w2 = (int) (Math.random() * (populationSize / 2));

			double[] child = new double[8];

			for (int j = 0; j < 8; j++) {
				child[j] = population.get(Math.random() < .5 ? w1 : w2).chromosomes[j];

				if (Math.random() < mutationRate) {
					child[j] = Math.random() * 10 - 5;
				}
			}

			new_population.add(new Entity(child));
		}

		population = new_population;

		current = 0;
		generation++;
	}

	public void submit(long score)
	{
		String chromosomes = "";
		for (int i = 0; i < population.get(current).chromosomes.length; i++) {
			chromosomes += String.format("%+2.2f%s", population.get(current).chromosomes[i], i != population.get(current).chromosomes.length - 1 ? ", " : "");
		}

		System.out.printf("Generation %-2d - Candidate %-2d: [%s] score = %d\n", generation, current + 1, chromosomes, score);

		population.get(current).score = score;
		current++;

		if (current == populationSize) {
			newGeneration();
		}
	}
}