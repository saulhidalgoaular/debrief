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
package org.mwc.debrief.limpet_integration.measured_data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.mwc.cmap.core.CorePlugin;
import org.mwc.cmap.core.operations.CMAPOperation;
import org.mwc.cmap.core.property_support.EditableWrapper;
import org.mwc.cmap.core.property_support.RightClickSupport.RightClickContextItemGenerator;

import Debrief.Wrappers.Extensions.Measurements.DataFolder;
import Debrief.Wrappers.Extensions.Measurements.TimeSeriesCore;
import Debrief.Wrappers.Extensions.Measurements.TimeSeriesDatasetDouble;
import Debrief.Wrappers.Extensions.Measurements.Wrappers.DatasetWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Properties.DebriefColors;
import MWC.GenericData.HiResDate;
import MWC.TacticalData.temporal.ControllableTime;
import MWC.TacticalData.temporal.TimeProvider;
import MWC.Utilities.TextFormatting.GMTDateFormat;
import info.limpet.stackedcharts.model.Chart;
import info.limpet.stackedcharts.model.ChartSet;
import info.limpet.stackedcharts.model.DataItem;
import info.limpet.stackedcharts.model.Dataset;
import info.limpet.stackedcharts.model.Datum;
import info.limpet.stackedcharts.model.DependentAxis;
import info.limpet.stackedcharts.model.IndependentAxis;
import info.limpet.stackedcharts.model.PlainStyling;
import info.limpet.stackedcharts.model.ScatterSet;
import info.limpet.stackedcharts.model.StackedchartsFactory;
import info.limpet.stackedcharts.model.impl.StackedchartsFactoryImpl;
import info.limpet.stackedcharts.ui.view.StackedChartsView;
import info.limpet.stackedcharts.ui.view.StackedChartsView.ControllableDate;
import info.limpet.stackedcharts.ui.view.adapter.IStackedDatasetAdapter;
import info.limpet.stackedcharts.ui.view.adapter.IStackedScatterSetAdapter;
import info.limpet.stackedcharts.ui.view.adapter.IStackedTimeListener;
import info.limpet.stackedcharts.ui.view.adapter.IStackedTimeProvider;

public class MeasuredDataInStackedChartsAdapter implements IStackedDatasetAdapter, IStackedScatterSetAdapter,
		IStackedTimeProvider, RightClickContextItemGenerator {

	/**
	 * helper, to embody some processing
	 *
	 * @author ian
	 *
	 */
	private static interface ProcessHelper {
		/**
		 * handle this instance
		 *
		 * @param index
		 * @param value
		 */
		void processThis(long index, double value);

		/**
		 * set the dataset name
		 *
		 * @param name
		 */
		void setName(String name);

		/**
		 * set the dataset styling
		 *
		 * @param ps
		 */
		void setStyling(PlainStyling ps);
	}

	/**
	 * helper to store information necessary to cancel time listening
	 *
	 * @author ian
	 *
	 */
	private static class TimeDoublet {
		public String eType;
		public TimeProvider provider;
		public PropertyChangeListener event;
	}

	private static class ViewInChartsOperation extends CMAPOperation {

		private static ChartSet produceChartset(final Map<String, List<Dataset>> datasets) {

			// keep track of the current chart
			final StackedchartsFactoryImpl factory = new StackedchartsFactoryImpl();

			// create the chartset
			final ChartSet charts = factory.createChartSet();

			// and the curernt chart
			Chart currentC = factory.createChart();
			charts.getCharts().add(currentC);

			final IndependentAxis timeAxis = factory.createIndependentAxis();
			timeAxis.setAxisType(factory.createDateAxis());
			charts.setSharedAxis(timeAxis);

			// put each group on a new axis
			for (final String units : datasets.keySet()) {
				final List<Dataset> theseSets = datasets.get(units);

				final DependentAxis targetAxis;

				// is the first axis populated?
				if (currentC.getMinAxes().size() == 0) {
					// no, add these datasets to this axis
					targetAxis = factory.createDependentAxis();
					targetAxis.setName(units);
					currentC.getMinAxes().add(targetAxis);
				} else if (currentC.getMaxAxes().size() == 0) {
					// no, add these datasets to this axis
					targetAxis = factory.createDependentAxis();
					targetAxis.setName(units);
					currentC.getMaxAxes().add(targetAxis);
				} else {
					// ok, min and max axes populated, time to insert new chart
					currentC = factory.createChart();
					charts.getCharts().add(currentC);
					// and put the data on the min axis
					targetAxis = factory.createDependentAxis();
					targetAxis.setName(units);
					currentC.getMinAxes().add(targetAxis);
				}

				targetAxis.getDatasets().addAll(theseSets);
			}

			return charts;
		}

		private final List<Editable> _subjects;

		public ViewInChartsOperation(final String title, final List<Editable> datasets) {
			super(title);
			_subjects = datasets;
		}

		@Override
		public boolean canRedo() {
			return false;
		}

		@Override
		public boolean canUndo() {
			return false;
		}

		@Override
		public IStatus execute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {

			final Map<String, List<Dataset>> datasets = new HashMap<String, List<Dataset>>();

			// ok, loop through them, finding groups of the same units
			for (final Editable thisD : _subjects) {
				// get this as a dataset
				final Dataset dataset = _convertToDataset(thisD);

				// did it work?
				if (dataset != null) {
					final String units = dataset.getUnits();

					List<Dataset> matches = datasets.get(units);

					if (matches == null) {
						matches = new ArrayList<Dataset>();
						datasets.put(units, matches);
					}

					matches.add(dataset);
				}
			}

			// get a charts model
			final ChartSet charts = produceChartset(datasets);

			// ok, we have our model. Now create the view, and show the model
			if (charts != null) {

				// ok, get the active editor
				final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window == null) {
					// handle case where application is closing
					return null;
				}
				final IWorkbenchPage page = window.getActivePage();

				// produce a name for the view
				final DateFormat df = new GMTDateFormat("hh_mm_ss");
				final String viewId = "Measured Data " + df.format(new Date());

				// create a new instance of the Tactical Overview
				final String ID = StackedChartsView.ID;
				try {
					page.showView(ID, viewId, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (final PartInitException e) {
					CorePlugin.logError(IStatus.ERROR, "Failed to open Stacked Charts view", e);
					return Status.CANCEL_STATUS;
				}

				// send over the data
				final IViewReference viewRef = page.findViewReference(ID, viewId);
				if (viewRef != null) {
					final IViewPart theView = viewRef.getView(true);

					// double check it's what we're after
					if (theView instanceof StackedChartsView) {
						final StackedChartsView cv = (StackedChartsView) theView;

						// give it the model data
						cv.setModel(charts);

						// see if we have a time provider
						final IEditorPart editor = page.getActiveEditor();
						final TimeProvider timeProv = editor.getAdapter(TimeProvider.class);
						if (timeProv != null) {
							final PropertyChangeListener evt = new PropertyChangeListener() {

								@Override
								public void propertyChange(final PropertyChangeEvent evt) {
									final HiResDate hd = (HiResDate) evt.getNewValue();
									if (hd != null) {
										final Date newDate = new Date(hd.getDate().getTime());
										cv.updateTime(newDate);
									}
								}
							};
							timeProv.addListener(evt, TimeProvider.TIME_CHANGED_PROPERTY_NAME);

							// we also need to listen for it closing, to remove the listner
							cv.addRunOnCloseCallback(new Runnable() {

								@Override
								public void run() {
									// stop listening for time changes
									timeProv.removeListener(evt, TimeProvider.TIME_CHANGED_PROPERTY_NAME);
								}
							});
						}

						// see if we have a time provider
						final ControllableTime timeCont = editor.getAdapter(ControllableTime.class);
						if (timeCont != null && timeProv != null) {

							final ControllableDate dateC = new ControllableDate() {

								@Override
								public Date getDate() {
									return timeProv.getTime().getDate();
								}

								@Override
								public void setDate(final Date time) {
									timeCont.setTime(this, new HiResDate(time), true);
								}
							};

							cv.setDateSupport(dateC);

							// add a utility to cancel the support on close
							cv.addRunOnCloseCallback(new Runnable() {

								@Override
								public void run() {
									// clear date support helper
									cv.setDateSupport(null);
								}
							});
						}
					}
				}
			}

			return Status.CANCEL_STATUS;
		}

		@Override
		public IStatus undo(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
			return Status.CANCEL_STATUS;
		}
	}

	protected static Dataset _convertToDataset(Object data) {
		Dataset res = null;

		DatasetWrapper ds = null;

		if (data instanceof EditableWrapper) {
			// ok, get the editable out, we'll process it in a minute
			final EditableWrapper ew = (EditableWrapper) data;
			data = ew.getEditable();
		}

		if (data instanceof DatasetWrapper) {
			ds = (DatasetWrapper) data;
		}

		if (ds != null) {
			final TimeSeriesCore tsc = ds.getDataset();

			if (tsc instanceof TimeSeriesDatasetDouble) {
				final TimeSeriesDatasetDouble cd = (TimeSeriesDatasetDouble) tsc;

				final StackedchartsFactory factory = new StackedchartsFactoryImpl();
				final Dataset dataset = factory.createDataset();

				dataset.setUnits(cd.getUnits());

				final ProcessHelper ph = new ProcessHelper() {
					@Override
					public void processThis(final long index, final double value) {
						final DataItem item = factory.createDataItem();
						item.setIndependentVal(index);
						item.setDependentVal(value);

						// and store it
						dataset.getMeasurements().add(item);
					}

					@Override
					public void setName(final String name) {
						dataset.setName(name);
					}

					@Override
					public void setStyling(final PlainStyling ps) {
						dataset.setStyling(ps);
					}

				};

				DoDataset(cd, ph, factory);

				// did we find any?
				if (!dataset.getMeasurements().isEmpty()) {
					res = dataset;
				}
			}

		}
		return res;
	}

	private static void DoDataset(final TimeSeriesDatasetDouble cd, final ProcessHelper helper,
			final StackedchartsFactory factory) {
		// sort out a name
		final String name;
		final DataFolder parent = cd.getParent();
		if (parent != null) {
			name = parent.getName() + " : " + cd.getName();
		} else {
			name = cd.getName();
		}
		helper.setName(name);

		final PlainStyling ps = factory.createPlainStyling();
		// get a hash-code, for the color
		final int hash = cd.hashCode();
		ps.setColor(DebriefColors.RandomColorProvider.getRandomColor(hash));
		ps.setLineThickness(2.0d);
		helper.setStyling(ps);

		final Iterator<Long> iIter = cd.getIndices();
		final Iterator<Double> vIter = cd.getValues();

		while (iIter.hasNext()) {
			helper.processThis(iIter.next(), vIter.next());
		}
	}

	private static void DoDatasetCore(final TimeSeriesCore cd, final ProcessHelper helper,
			final StackedchartsFactory factory) {
		// sort out a name
		final String name;
		final DataFolder parent = cd.getParent();
		if (parent != null) {
			name = parent.getName() + " - " + cd.getName();
		} else {
			name = cd.getName();
		}
		helper.setName(name);

		final PlainStyling ps = factory.createPlainStyling();
		// get a hash-code, for the color
		final int hash = cd.hashCode();
		ps.setColor(DebriefColors.RandomColorProvider.getRandomColor(hash));
		ps.setLineThickness(2.0d);
		helper.setStyling(ps);

		final Iterator<Long> iIter = cd.getIndices();

		while (iIter.hasNext()) {
			helper.processThis(iIter.next(), 0d);
		}
	}

	private HashMap<IStackedTimeListener, TimeDoublet> _timeListeners;

	@Override
	public boolean canConvertToDataset(final Object data) {
		boolean res = false;
		if (data instanceof EditableWrapper) {
			final EditableWrapper ew = (EditableWrapper) data;
			final Editable ed = ew.getEditable();
			if (ed instanceof DatasetWrapper) {
				final DatasetWrapper ds = (DatasetWrapper) ed;
				@SuppressWarnings("unused")
				final TimeSeriesCore cd = ds.getDataset();
				res = true;
			}
		}
		return res;
	}

	@Override
	public boolean canConvertToScatterSet(final Object data) {
		boolean res = false;
		if (data instanceof EditableWrapper) {
			final EditableWrapper ew = (EditableWrapper) data;
			final Editable ed = ew.getEditable();
			if (ed instanceof DatasetWrapper) {
				final DatasetWrapper ds = (DatasetWrapper) ed;
				@SuppressWarnings("unused")
				final TimeSeriesCore cd = ds.getDataset();
				res = true;
			}
		}
		return res;
	}

	@Override
	public boolean canProvideControl() {
		boolean canProvideControl = false;

		// ok, get the active editor
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			final IWorkbenchPage page = window.getActivePage();

			if (page != null) {
				final IEditorPart editor = page.getActiveEditor();

				if (editor != null) {
					// see if we have a time provider
					final TimeProvider timeProv = editor.getAdapter(TimeProvider.class);
					if (timeProv != null) {
						canProvideControl = true;
					}
				}
			}
		}

		return canProvideControl;
	}

	@Override
	public void controlThis(final IStackedTimeListener listener) {
		// ok, get ready to control this time view

		// ok, get the active editor
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			// handle case where application is closing
		}
		final IWorkbenchPage page = window.getActivePage();
		final IEditorPart editor = page.getActiveEditor();

		if (editor != null) {
			// see if we have a time provider
			final TimeProvider timeProv = editor.getAdapter(TimeProvider.class);
			if (timeProv != null) {
				final PropertyChangeListener evt = new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						final HiResDate hd = (HiResDate) evt.getNewValue();
						if (hd != null) {
							final Date newDate = new Date(hd.getDate().getTime());
							listener.updateTime(newDate);
						}
					}
				};
				timeProv.addListener(evt, TimeProvider.TIME_CHANGED_PROPERTY_NAME);

				// get ready to store this permutation (so we can cancel it)
				final TimeDoublet match = new TimeDoublet();
				match.provider = timeProv;
				match.event = evt;
				match.eType = TimeProvider.TIME_CHANGED_PROPERTY_NAME;

				// ok, remember it
				if (_timeListeners == null) {
					_timeListeners = new HashMap<IStackedTimeListener, TimeDoublet>();
				}
				_timeListeners.put(listener, match);
			}
		}
	}

	@Override
	public List<Dataset> convertToDataset(final Object data) {
		final Dataset ds = _convertToDataset(data);
		final List<Dataset> res;
		if (ds != null) {
			res = new ArrayList<Dataset>();
			res.add(ds);
		} else {
			res = null;
		}
		return res;
	}

	@Override
	public List<ScatterSet> convertToScatterSet(final Object data) {
		List<ScatterSet> res = null;

		if (data instanceof EditableWrapper) {
			final EditableWrapper ew = (EditableWrapper) data;
			final Editable ed = ew.getEditable();
			if (ed instanceof DatasetWrapper) {
				final DatasetWrapper ds = (DatasetWrapper) ed;
				final StackedchartsFactoryImpl factory = new StackedchartsFactoryImpl();
				final ScatterSet dataset = factory.createScatterSet();

				final ProcessHelper ph = new ProcessHelper() {
					@Override
					public void processThis(final long index, final double value) {
						final Datum item = factory.createDatum();
						item.setVal(index);

						// and store it
						dataset.getDatums().add(item);
					}

					@Override
					public void setName(final String name) {
						dataset.setName(name);
					}

					@Override
					public void setStyling(final PlainStyling ps) {
						// ignore
					}
				};

				final TimeSeriesCore cd = ds.getDataset();
				DoDatasetCore(cd, ph, factory);

				// did we find any?
				if (!dataset.getDatums().isEmpty()) {
					if (res == null) {
						res = new ArrayList<ScatterSet>();
					}
					res.add(dataset);
				}
			}
		}

		return res;
	}

	@Override
	public void generate(final IMenuManager parent, final Layers theLayers, final Layer[] parentLayers,
			final Editable[] subjects) {
		// see if the selection are all datasets
		final List<Editable> res = new ArrayList<Editable>(5);

		boolean foundDodgy = false;

		// ok.
		for (int i = 0; i < subjects.length; i++) {
			final Editable editable = subjects[i];
			if (editable instanceof DatasetWrapper) {
				final DatasetWrapper dw = (DatasetWrapper) editable;
				final TimeSeriesCore ds = dw.getDataset();
				if (ds instanceof TimeSeriesDatasetDouble) {
					// ok, add it
					res.add(editable);
				} else {
					foundDodgy = true;
				}
			} else {
				foundDodgy = true;
			}
		}

		// did it work?
		if (!foundDodgy && res.size() > 0) {
			final String title;
			if (res.size() == 1) {
				title = "View dataset in Stacked Charts";
			} else {
				title = "View datasets in Stacked Charts";
			}

			// ok, generate the action
			final Action doMerge = new Action(title) {
				@Override
				public void run() {
					final IUndoableOperation theAction = new ViewInChartsOperation(title, res);

					CorePlugin.run(theAction);
				}
			};
			parent.add(doMerge);
		}
	}

	@Override
	public void releaseThis(final IStackedTimeListener listener) {
		final TimeDoublet match = _timeListeners.get(listener);
		if (match != null) {
			match.provider.removeListener(match.event, match.eType);
		}
	}

}
