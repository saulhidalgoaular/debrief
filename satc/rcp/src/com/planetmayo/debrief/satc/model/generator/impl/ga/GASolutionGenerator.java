package com.planetmayo.debrief.satc.model.generator.impl.ga;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionUtils;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.TerminationCondition;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.ListCrossover;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.ElapsedTime;

import com.planetmayo.debrief.satc.log.LogFactory;
import com.planetmayo.debrief.satc.model.Precision;
import com.planetmayo.debrief.satc.model.generator.IContributions;
import com.planetmayo.debrief.satc.model.generator.IGenerateSolutionsListener;
import com.planetmayo.debrief.satc.model.generator.IJobsManager;
import com.planetmayo.debrief.satc.model.generator.impl.AbstractSolutionGenerator;
import com.planetmayo.debrief.satc.model.generator.jobs.Job;
import com.planetmayo.debrief.satc.model.legs.AlteringRoute;
import com.planetmayo.debrief.satc.model.legs.CompositeRoute;
import com.planetmayo.debrief.satc.model.legs.CoreLeg;
import com.planetmayo.debrief.satc.model.legs.CoreRoute;
import com.planetmayo.debrief.satc.model.legs.LegType;
import com.planetmayo.debrief.satc.model.legs.StraightRoute;
import com.planetmayo.debrief.satc.model.states.SafeProblemSpace;
import com.vividsolutions.jts.geom.Point;

public class GASolutionGenerator extends AbstractSolutionGenerator
{
	private static final String GA_GENERATOR_GROUP = "gaGeneratorGroup"; 
	
	private volatile List<LegOperations> legs;

	private volatile Job<Void, Void> mainJob;
	
	private final GAParameters parameters;
	
	public GASolutionGenerator(IContributions contributions, IJobsManager jobsManager, SafeProblemSpace problemSpace) 
	{
		super(contributions, jobsManager, problemSpace);
		parameters = new GAParameters();
		parameters.setElitizm(70);
		parameters.setMutationProbability(0.25);
		parameters.setPopulationSize(500);
		parameters.setStagnationSteps(100);
		parameters.setTopRoutes(10);
		parameters.setTimeoutBetweenIterations(0);
		parameters.setTimeout(30000);
		parameters.setRecalculatePointsProbs(10);
		parameters.setCheckReachability(25);
		parameters.setExtendBestPoints(33);
	}
	
	public GAParameters getParameters()
	{
		return parameters;
	}

	@Override
	public void clear()
	{
		Job<?, ?> job = mainJob;
		if (job != null)
		{
			jobsManager.cancelGroup(GA_GENERATOR_GROUP);
			try
			{
				jobsManager.waitFor(job);
			}
			catch (InterruptedException ex)
			{
				LogFactory.getLog().error("Thread was interrupted", ex);
			}
		}
		if (legs != null)
		{
			if (legs != null)
			{
				legs.clear();
				legs = null;
			}
		}		
	}

	@Override
	public synchronized void generateSolutions(boolean fullRerun)
	{		
		if (mainJob != null)
		{
			return;
		}
		fireStartingGeneration();
		Job<Void, Void> previous = null;
		if (fullRerun || legs == null) 
		{
			final Random random = new MersenneTwisterRNG();
			previous = jobsManager.schedule(new Job<Void, Void>("Generate Legs", GA_GENERATOR_GROUP)
				{

				@Override
				protected <E> Void run(IProgressMonitor monitor, Job<Void, E> previous)
						throws InterruptedException
					{
					List<CoreLeg> rawLegs = getTheLegs(problemSpaceView.states(), monitor);
					legs = new ArrayList<LegOperations>();
					for (CoreLeg leg : rawLegs)
					{
						leg.generatePoints(_myPrecision.toMeters());
						legs.add(new LegOperations(leg, random));
					}					
					return null;
				}
			});
		}
		mainJob = jobsManager.scheduleAfter(new Job<Void, Void>("Calculate GA", GA_GENERATOR_GROUP)
		{

			@Override
			protected <E> Void run(IProgressMonitor monitor, Job<Void, E> previous)
					throws InterruptedException
			{
				runGA(monitor);
				return null;
			}

			@Override
			protected void onComplete()
			{
				synchronized (GASolutionGenerator.this) 
				{
					mainJob = null;
				}
				fireFinishedGeneration(getException());
			}
						
		}, previous);
		if (mainJob != null && mainJob.isComplete()) 
		{
			mainJob = null;
		}
	}
	
	private void runGA(final IProgressMonitor progressMonitor) throws InterruptedException
	{		
		List<EvolutionaryOperator<List<Point>>> operators = new ArrayList<EvolutionaryOperator<List<Point>>>();
		operators.add(new ListCrossover<Point>(new NumberGenerator<Integer>()
		{
			Random rng = new MersenneTwisterRNG();

			@Override
			public Integer nextValue()
			{
				return rng.nextInt(2) + 1;
			}			
		}));
		operators.add(new PointsMutation(legs, new Probability(parameters.getMutationProbability())));
		
		final GAEngine engine = new GAEngine(
				new RoutesCandidateFactory(legs), 
				new EvolutionPipeline<List<Point>>(operators),
				new RoutesFitnessEvaluator(legs, contributions),
				new RouletteWheelSelection(), 				
				new MersenneTwisterRNG()
		);
		TerminationCondition progressMonitorCondition = new TerminationCondition()
		{			
			@Override
			public boolean shouldTerminate(PopulationData<?> populationData)
			{
				return progressMonitor.isCanceled();
			}
		};
		List<Point> solution = engine.evolve(
				parameters.getPopulationSize(), 
				parameters.getElitizm(), 
				progressMonitorCondition,
				new ElapsedTime(parameters.getTimeout()),
				progressMonitorCondition,
				new Stagnation(parameters.getStagnationSteps())
				{

					@Override
					public boolean shouldTerminate(PopulationData<?> populationData)
					{						
						if (engine.getTopRoutesScore() == Double.MAX_VALUE) 
						{
							return false;
						}
						return super.shouldTerminate(populationData);
					}
				} 
		);
		if (progressMonitor.isCanceled())
		{
			throw new InterruptedException();
		}
		fireSolutionsReady(new CompositeRoute[] {solutionToRoute(solution, true)});
	}
	
	protected CompositeRoute solutionToRoute(List<Point> solution, boolean createAltering) 
	{
		List<CoreRoute> routes = new ArrayList<CoreRoute>();
		int i = 0;
		for (LegOperations leg : legs)
		{
			if (leg.getLeg().getType() == LegType.STRAIGHT) 
			{
				routes.add(leg.getLeg().createRoute("", solution.get(i), solution.get(i + 1)));
			}
			i += 2;
		}
		if (createAltering)
		{
			List<CoreRoute> result = new ArrayList<CoreRoute>();
			StraightRoute before, after = null;
			for (i = 0; i < routes.size() - 1; i++)
			{
				before = (StraightRoute) routes.get(i);
				after = (StraightRoute) routes.get(i + 1);
				AlteringRoute altering = new AlteringRoute("", 
						before.getEndPoint(), before.getEndTime(), 
						after.getStartPoint(), after.getStartTime());
				altering.constructRoute(before, after);
				result.add(before);
				result.add(altering);
			}
			if (after != null)
			{
				result.add(after);
			}
			routes = result;
		}
		return new CompositeRoute(routes);
		
	}
	
	@Override
	public void cancel()
	{
		Job<?, ?> job = mainJob;
		if (job != null)
		{
			jobsManager.cancelGroup(GA_GENERATOR_GROUP);
		}		
	}
	
	@Override
	public void setPrecision(Precision precision)
	{
		super.setPrecision(precision);
		if (legs != null) 
		{
			generateSolutions(true);
		}
	}	
	
	protected void fireIterationComputed(List<CompositeRoute> topRoutes) 
	{		
		for (IGenerateSolutionsListener listener : _readyListeners)
		{
			if (listener instanceof IGASolutionsListener) 
			{
				((IGASolutionsListener) listener).iterationComputed(new ArrayList<CompositeRoute>(topRoutes));
			}
		}		
	}
	
	private class GAEngine extends GenerationalEvolutionEngine<List<Point>> 
	{
		private long iterations = 0;
		private CandidateFactory<List<Point>> candidateFactory;
		private double topRoutesScore;
		
		
		public GAEngine(CandidateFactory<List<Point>> candidateFactory,
				EvolutionaryOperator<List<Point>> evolutionScheme,
				FitnessEvaluator<? super List<Point>> fitnessEvaluator,
				SelectionStrategy<? super List<Point>> selectionStrategy, Random rng)
		{
			super(candidateFactory, evolutionScheme, fitnessEvaluator, selectionStrategy,
					rng);
			this.candidateFactory = candidateFactory;
		}
		
		public double getTopRoutesScore()
		{
			return topRoutesScore;
		}

		@Override
		protected List<EvaluatedCandidate<List<Point>>> nextEvolutionStep(
				List<EvaluatedCandidate<List<Point>>> evaluatedPopulation, int eliteCount,
				Random rng)
		{			
			List<EvaluatedCandidate<List<Point>>> result = super.nextEvolutionStep(evaluatedPopulation, eliteCount, rng);
			EvolutionUtils.sortEvaluatedPopulation(result, false);
			List<CompositeRoute> routes = new ArrayList<CompositeRoute>(parameters.getTopRoutes());
			for (int i = 0; i < parameters.getTopRoutes(); i++) 
			{
				if (i >= result.size()) 
				{
					break;
				}
				routes.add(solutionToRoute(result.get(i).getCandidate(), false));
			}
			fireIterationComputed(routes);
			if (parameters.getTimeoutBetweenIterations() > 0) 
			{
				try 
				{
					Thread.sleep(parameters.getTimeoutBetweenIterations());
				}
				catch (InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
			int topCounts = Math.min(15, result.size());
			topRoutesScore = 0;
			for (int i = 0; i < topCounts; i++) 
			{
				if (result.get(i).getFitness() == Double.MAX_VALUE) 
				{
					topRoutesScore = Double.MAX_VALUE;
					break;
				}
				topRoutesScore += result.get(i).getFitness();
			}
			if (topRoutesScore != Double.MAX_VALUE) 
			{
				topRoutesScore = topRoutesScore / topCounts;
			}			
			iterations++;
			return applyIterationChanges(result, rng);
		}	
		
		private List<EvaluatedCandidate<List<Point>>> applyIterationChanges(final List<EvaluatedCandidate<List<Point>>> population, Random rng) 
		{			
			List<EvaluatedCandidate<List<Point>>> result = population;
			double currentBestScore = result.get(0).getFitness();
			if (iterations % parameters.getRecalculatePointsProbs() == 0) 
			{				
				for (LegOperations leg : legs)
				{
					leg.recalculateProbabilities(10);
				}
			}
			if (iterations % parameters.getCheckReachability() == 0) 
			{
				boolean regeneratePopulation = false;
				for (ListIterator<LegOperations> it = legs.listIterator(); it.hasNext(); ) 
				{
					LegOperations leg = it.next();
					if (leg.hasNoAchievablePoints()) 
					{
						regeneratePopulation = true;
						CoreLeg rawLeg = leg.getLeg();
						rawLeg.generatePoints(rawLeg.getCurrentGridPrecision() / 2);
						it.set(new LegOperations(rawLeg, rng));
					}
				}
				if (regeneratePopulation) 
				{
					List<List<Point>> newPopulation = candidateFactory.generateInitialPopulation(parameters.getPopulationSize(), rng);
					result = evaluatePopulation(newPopulation);
				}
			}
			if (currentBestScore != Double.MAX_VALUE &&  iterations % parameters.getExtendBestPoints() == 0) 
			{
					int i = 0;					
					for (LegOperations leg : legs) 
					{
						if (leg.getLeg().getType() == LegType.STRAIGHT) 
						{
							for (int j = 0; j < 1; j++)
							{
								leg.extendStartPoint(result.get(j).getCandidate().get(i));
								leg.extendEndPoint(result.get(j).getCandidate().get(i + 1));
							}
						}
						i += 2;
					}
					int third = result.size() / 3;				
					List<EvaluatedCandidate<List<Point>>> newPopulation = new ArrayList<EvaluatedCandidate<List<Point>>>();
					newPopulation.addAll(result.subList(0, 2 * third));
					List<List<Point>> randoms = new ArrayList<List<Point>>(third);
					for (i = 0; i < third; i++)
					{
						randoms.add(candidateFactory.generateRandomCandidate(rng));
					}				
					newPopulation.addAll(evaluatePopulation(randoms));
					result = newPopulation;
					for (LegOperations leg : legs) 
					{
						leg.useAllPoints();
					}
			}
			/*if (iterations % parameters.getExtendBestPoints() == 0 && population == result)
			{				
				for (LegOperations leg : legs)
				{
					if (leg.getLeg().getType() == LegType.STRAIGHT) 
					{
						leg.extendBestPoints(3, true);
					}
				}
				int third = result.size() / 3;				
				List<EvaluatedCandidate<List<Point>>> newPopulation = new ArrayList<EvaluatedCandidate<List<Point>>>();
				newPopulation.addAll(result.subList(0, 2 * third));
				List<List<Point>> randoms = new ArrayList<List<Point>>(third);
				for (int i = 0; i < third; i++)
				{
					randoms.add(candidateFactory.generateRandomCandidate(rng));
				}				
				newPopulation.addAll(evaluatePopulation(randoms));
				result = newPopulation;
				for (LegOperations leg : legs) 
				{
					leg.useAllPoints();
				}
			}*/
			
			return result;
		}
	}
}
