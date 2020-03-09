/*******************************************************************************
 * Debrief - the Open Source Maritime Analysis Application
 * http://debrief.info
 *
 * (C) 2000-2020, Deep Blue C Technology Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *******************************************************************************/

package com.planetmayo.debrief.satc.model.generator.impl.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.mwc.debrief.track_shift.zig_detector.Precision;
import org.uncommons.watchmaker.framework.CandidateFactory;

import com.planetmayo.debrief.satc.model.legs.LegType;
import com.planetmayo.debrief.satc.model.legs.StraightLeg;
import com.planetmayo.debrief.satc.model.legs.StraightRoute;
import com.planetmayo.debrief.satc.util.MathUtils;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class RoutesCandidateFactory implements CandidateFactory<List<StraightRoute>> {
	private class BearingLinePointsGenerator implements PointsGenerator {
		private final List<Point> segments;
		private final AtomicInteger currentSegment;

		public BearingLinePointsGenerator(final LineString bearingLine) {
			segments = new ArrayList<Point>();
			final Point startBearing = bearingLine.getStartPoint();
			final Point endBearing = bearingLine.getEndPoint();
			for (double t = 0; t < 1; t += 0.05) {
				segments.add(MathUtils.calculateBezier(t, startBearing, endBearing, null));
			}
			segments.add(endBearing);
			currentSegment = new AtomicInteger(-1);
		}

		@Override
		public Point next(final Random rng) {
			currentSegment.compareAndSet(2, -1);
			final int segment = rng.nextInt(segments.size() - 2);
			final Point segmentStart = segments.get(segment);
			final Point segmentEnd = segments.get(segment + 1);
			return MathUtils.calculateBezier(rng.nextDouble(), segmentStart, segmentEnd, null);
		}
	}

	private class GriddedPointsGenerator implements PointsGenerator {
		private final List<Point> points;

		public GriddedPointsGenerator(final List<Point> points) {
			this.points = points;
		}

		@Override
		public Point next(final Random rng) {
			return points.get(rng.nextInt(points.size()));
		}
	}

	private interface PointsGenerator {

		Point next(Random rng);
	}

	private final List<StraightLeg> straightLegs;

	private final List<PointsGenerator> startPoints;

	private final List<PointsGenerator> endPoints;

	public RoutesCandidateFactory(final List<StraightLeg> legs) {
		this.startPoints = new ArrayList<RoutesCandidateFactory.PointsGenerator>();
		this.endPoints = new ArrayList<RoutesCandidateFactory.PointsGenerator>();
		this.straightLegs = new ArrayList<StraightLeg>();
		for (final StraightLeg leg : legs) {
			if (leg.getType() == LegType.STRAIGHT) {
				final LineString startBearing = leg.getFirst().getBearingLine();
				final LineString endBearing = leg.getLast().getBearingLine();
				this.straightLegs.add(leg);
				if (startBearing == null || endBearing == null) {
					leg.generatePoints(Precision.MEDIUM.getNumPoints());
				}
				if (startBearing != null) {
					startPoints.add(new BearingLinePointsGenerator(startBearing));
				} else {
					startPoints.add(new GriddedPointsGenerator(leg.getStartPoints()));
				}
				if (endBearing != null) {
					endPoints.add(new BearingLinePointsGenerator(endBearing));
				} else {
					endPoints.add(new GriddedPointsGenerator(leg.getEndPoints()));
				}
			}
		}
	}

	@Override
	public List<List<StraightRoute>> generateInitialPopulation(final int populationSize,
			final Collection<List<StraightRoute>> seedCandidates, final Random rng) {
		final ArrayList<List<StraightRoute>> population = new ArrayList<List<StraightRoute>>(seedCandidates);
		if (population.size() < populationSize) {
			population.addAll(generateInitialPopulation(populationSize - population.size(), rng));
		}
		return Collections.unmodifiableList(population);
	}

	@Override
	public List<List<StraightRoute>> generateInitialPopulation(final int populationSize, final Random rng) {
		final List<List<StraightRoute>> population = new ArrayList<List<StraightRoute>>(populationSize);
		for (int i = 0; i < populationSize; i++) {
			population.add(generateRandomCandidate(rng));
		}
		return Collections.unmodifiableList(population);
	}

	@Override
	public List<StraightRoute> generateRandomCandidate(final Random rng) {
		final List<StraightRoute> solution = new ArrayList<StraightRoute>(straightLegs.size());
		for (int j = 0; j < straightLegs.size(); j++) {
			final StraightLeg leg = straightLegs.get(j);
			final Point start = startPoints.get(j).next(rng);
			final Point end = endPoints.get(j).next(rng);
			solution.add((StraightRoute) leg.createRoute(start, end, null));
		}
		return solution;
	}
}
