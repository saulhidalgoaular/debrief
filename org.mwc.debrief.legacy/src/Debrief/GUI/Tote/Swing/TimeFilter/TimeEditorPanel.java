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

// $RCSfile: TimeEditorPanel.java,v $
// @author $Author: Ian.Mayo $
// @version $Revision: 1.10 $
// $Log: TimeEditorPanel.java,v

package Debrief.GUI.Tote.Swing.TimeFilter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import Debrief.Tools.FilterOperations.CopyTimeDataToClipboard;
import Debrief.Tools.FilterOperations.FilterOperation;
import Debrief.Tools.FilterOperations.HideRevealObjects;
import Debrief.Tools.FilterOperations.ReformatFixes;
import Debrief.Tools.FilterOperations.SetTimeZero;
import Debrief.Tools.FilterOperations.ShowTimeVariablePlot3;
import Debrief.Wrappers.SensorWrapper;
import Debrief.Wrappers.TMAWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Plottable;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WatchableList;

/**
 * Implementation of TimeEditorPanel
 */
final public class TimeEditorPanel extends JPanel {
	///////////////////////////////////
	// nested classes
	//////////////////////////////////
	/**
	 * one of the operations we provide (filtering). When all tracks are selected
	 * for this operation, we reduce the range of coverage of the sliders and the
	 * time stepper (if assigned)
	 */
	final protected class DoFilter implements FilterOperation {
		/**
		 * the tracks which have been selected for this operation
		 */
		private Vector<WatchableList> _theTracks;
		/**
		 * line-break character
		 */
		private final String _theSeparator = System.getProperties().getProperty("line.separator");

		/**
		 * null operation, since we currently do the work in the getData
		 *
		 * @param p1 the results of the action
		 */
		@Override
		public void actionPerformed(final java.awt.event.ActionEvent p1) {
		}

		/**
		 * close operation
		 */
		@Override
		public void close() {
		}

		/**
		 * execute operation
		 */
		@Override
		public void execute() {
		}

		/**
		 * perform/prepare the operation @@@ switch to storing the data in the Action
		 *
		 * @return an Action representing the data for this operation
		 */
		@Override
		public MWC.GUI.Tools.Action getData() {
			final MWC.GUI.Tools.Action res = null;

			// check that we have tracks selected
			if (_theTracks == null) {
				MWC.GUI.Dialogs.DialogFactory.showMessage("Filter to time period", "Please select one or more tracks");
				return null;
			}

			// for our watchables, get them to shrink to this new period
			// setting all of their objects to visible/not visible as
			// necessary
			final HiResDate start = _starter.get_DTG();
			final HiResDate end = _finisher.get_DTG();

			// check they are the right way around
			if (start.greaterThan(end)) {
				MWC.GUI.Dialogs.DialogFactory.showMessage("Filter times",
						"Please ensure finish time is later than start time");

			} else {
				// do the dirty to the watchables
				final Enumeration<WatchableList> iter = _theTracks.elements();
				while (iter.hasMoreElements()) {
					final WatchableList wl = iter.nextElement();
					wl.filterListTo(start, end);
				}

				// set the limits for the bars (if we have hidden all of the tracks)
				if (_theTracks.size() == getWatchables(_theData).size()) {
					// reset the sliders - so that they only show the filtered time period
					resetMe(start, end);

					// also set the limits on the time stepper. For TrackWrappers the new time
					// periods
					// do get propagated to the time stepper (via property changes), but this is
					// just to
					// double-check and cater for future, unknown WatchableLists.
					if (_theStepper != null) {
						_theStepper.setStartTime(start);
						_theStepper.setEndTime(end);
					}
				}

				// inform the layers that they have been reformatted
				_theData.fireReformatted(null);

				// and repaint
				_theChart.update();
			}

			return res;
		}

		/**
		 * provide guidance
		 *
		 * @return instructions on use of this operation
		 */
		@Override
		public String getDescription() {
			String res = "2. Select tracks to be filtered from list (below)";
			res += _theSeparator + "3. Select time period of data to remain visible using sliders";
			res += _theSeparator + "4. Press 'Apply' button";
			res += _theSeparator + "====================";
			res += _theSeparator + "This operation hides all points outside selected time period.";
			res += _theSeparator
					+ "Note, if ALL tracks are selected for this operation, the period covered by the time sliders is reduced to only display the filtered time period,";
			res += _theSeparator + "and the time period covered by the stepper control is reduced";
			res += " Press RESET to expand slider limits to track defaults";
			return res;
		}

		/**
		 * the image for this operation
		 *
		 * @return image for this operation
		 */
		@Override
		public String getImage() {
			return null;
		}

		/**
		 * label for this operation
		 *
		 * @return a label for this operation
		 */
		@Override
		public String getLabel() {
			return "Filter to time period";
		}

		/**
		 * the user has pressed RESET whilst this button is pressed
		 *
		 * @param startTime the new start time
		 * @param endTime   the new end time
		 */
		@Override
		public void resetMe(final HiResDate startTime, final HiResDate endTime) {
			doResetMe(startTime, endTime);
		}

		/**
		 * set the period specified by the user
		 *
		 * @param startDTG  start DTG
		 * @param finishDTG finish DTG
		 */
		@Override
		public void setPeriod(final HiResDate startDTG, final HiResDate finishDTG) {
		}

		/**
		 * store the tracks for this operation
		 *
		 * @param selectedTracks the selected tracks
		 */
		@Override
		public void setTracks(final Vector<WatchableList> selectedTracks) {
			_theTracks = selectedTracks;
		}
	}

	//////////////////////////////////////////////////
	// class to store a WatchableList in a labelled
	// object which indicates if that object is visible, or not
	//////////////////////////////////////////////////
	public static final class LabelledWatchableHolder {
		/**
		 * the list itself
		 */
		private final WatchableList _myWatch;

		/**
		 * the constructor
		 *
		 * @param watch the list we wrap
		 */
		public LabelledWatchableHolder(final WatchableList watch) {
			_myWatch = watch;
		}

		public final WatchableList getList() {
			return _myWatch;
		}

		/**
		 * get the labelled description
		 *
		 * @return
		 */
		@Override
		public final String toString() {
			String res = _myWatch.toString();
			if (_myWatch.getVisible()) {
				// don't bother
			} else {
				res += " (hidden)";
			}
			return res;
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final long T_30_MINS = 30l * 60 * 1000 * 1000;

	private static final long T_30_DAYS = 30l * 24 * 60 * 60 * 60 * 1000 * 1000;

	/**
	 * utility method to wrap the watchable in a labelled object whihc indicates if
	 * that object is hidden or not
	 */
	private static void addElement(final WatchableList watchable, final Vector<LabelledWatchableHolder> theList) {
		theList.add(new LabelledWatchableHolder(watchable));
	}

	///////////////////////////////////
	// member variables
	//////////////////////////////////
	/**
	 * the data we sort out our time period limits from
	 */
	final Layers _theData;

	/**
	 * the slider representing the start of the time period
	 */
	SwingCompositeTimeEditor _starter;

	/**
	 * the slider representing the end of the time period
	 */
	SwingCompositeTimeEditor _finisher;

	/**
	 * the panel we need to inform when we are closing
	 */
	private final MWC.GUI.Properties.PropertiesPanel _theParent;

	/**
	 * the "outer" limits of time for the current data
	 */
	private HiResDate _startTime = null;

	/**
	 * current end point
	 */
	private HiResDate _endTime = null;

	/**
	 * the chart we need to update
	 */
	final MWC.GUI.PlainChart _theChart;

	/**
	 * the step control we need to constrain
	 */
	final Debrief.GUI.Tote.StepControl _theStepper;
	/**
	 * the list of operations we allow
	 */
	@SuppressWarnings("rawtypes")
	private JList _theOperationsList;
	/**
	 * the text box describing what the currently selected operation does
	 */
	private JTextArea _theDescription;

	/**
	 * the undo buffer for the operations we run
	 */
	private final MWC.GUI.Undo.UndoBuffer _theUndoBuffer;

	/**
	 * the list of time filter operations we can perform
	 */
	private final java.util.Vector<FilterOperation> _myOperations;

	/**
	 * the list of tracks which can be selected
	 */
	@SuppressWarnings("rawtypes")
	private JList _theTracksList;

	///////////////////////////////////
	// constructor
	//////////////////////////////////
	/**
	 * Constructor for time editor panel
	 *
	 * @param theParent     the properties window we are contained in
	 * @param theData       the data we are editing
	 * @param theChart      the chart we are updating
	 * @param theStepper    the step control which we are going to constrain
	 * @param theUndoBuffer place to store the undo operations
	 */
	public TimeEditorPanel(final MWC.GUI.Properties.PropertiesPanel theParent, final Layers theData,
			final MWC.GUI.PlainChart theChart, final Debrief.GUI.Tote.StepControl theStepper,
			final MWC.GUI.Undo.UndoBuffer theUndoBuffer) {

		// store the step control
		_theStepper = theStepper;

		// the chart object
		_theChart = theChart;

		// store the data
		_theData = theData;

		// store the parent
		_theParent = theParent;

		// store the undo buffer
		_theUndoBuffer = theUndoBuffer;

		// layout the form
		initForm();

		// prepare the data
		getLimits();

		// name the label, so it shows in the tabbed dialog
		setName("Time/Track Toolbox");

		// the list of operations we can perform
		_myOperations = new Vector<FilterOperation>(0, 1);

		// put the operations into the list
		populateLists(_theStepper);

		// put the tracks into the list
		populateTracks();

		final Layers.DataListener theListener = new Layers.DataListener() {
			@Override
			public void dataExtended(final Layers theData1) {
				// ok, find the time-data items, re-set slider limits
				reIntitialise();
			}

			@Override
			public void dataModified(final Layers theData1, final Layer changedLayer) {
			}

			@Override
			public void dataReformatted(final Layers theData1, final Layer changedLayer) {

				// we show a marker to indicate if an item is hidden or not, we need to update
				// our list following
				// a formatting change in case some items have been hidden, so re-do the list of
				// tracks
				populateTracks();
			}
		};

		// we also need to listen to the layers object, so that we can re-populate the
		// list when
		// new tracks are added or tracks are hidden/revealed
		_theData.addDataExtendedListener(theListener);
		_theData.addDataReformattedListener(theListener);

	}

	/**
	 * we're finished, just close
	 */
	void closeMe() {
		_theParent.remove(this);
	}

	/**
	 * reset/initialise the sliders, leaving the sliders themselves at the outer
	 * limits
	 *
	 * @param start start point for the sliders
	 * @param end   end point for the sliders
	 */
	void doResetMe(final HiResDate start, final HiResDate end) {
		// reset the original times
		resetMe(start, end, start, end);
	}

	/**
	 * get the time currently set in the finisher control
	 */
	public HiResDate getEndTime() {
		return _finisher.get_DTG();
	}

	/**
	 * find the outer time constraints of the current tracks
	 */
	private void getLimits() {
		HiResDate start = null;
		HiResDate end = null;
		boolean found = false;

		// find the limits
		final Vector<LabelledWatchableHolder> watches = getWatchables(_theData);

		final Enumeration<LabelledWatchableHolder> iter = watches.elements();
		while (iter.hasMoreElements()) {
			final LabelledWatchableHolder wh = iter.nextElement();
			final WatchableList wl = wh.getList();

			final HiResDate thisStart = wl.getStartDTG();
			final HiResDate thisEnd = wl.getEndDTG();

			if (thisStart != null) {
				if (!found) {
					start = thisStart;

					// just check that we're not getting a duff end value
					if (thisEnd == null)
						end = start;
					else
						end = thisEnd;

					end = thisEnd;
					found = true;
				} else {
					if (thisStart.lessThan(start))
						start = thisStart;

					// ok, start done. also manage the end point.
					// - we should have end points if we have start. But you can't be too sure..
					if (thisEnd != null)
						if (thisEnd.greaterThan(end))
							end = thisEnd;
				}
			}
		}

		_startTime = start;
		_endTime = end;

		if (found) {
			// set the limits in the panels
			doResetMe(start, end);
		}
	}

	/**
	 * find which operation is currently selected
	 *
	 * @return currently selected operation
	 */
	private FilterOperation getOperation() {
		FilterOperation res = null;

		final String val = (String) _theOperationsList.getSelectedValue();

		if (val != null) {
			final Enumeration<FilterOperation> iter = _myOperations.elements();
			while (iter.hasMoreElements()) {
				final FilterOperation fo = iter.nextElement();
				if (fo.getLabel().equals(val)) {
					res = fo;
					break;
				}
			}
		}

		return res;
	}

	/**
	 * find which tracks are currently selected
	 *
	 * @return the list of tracks
	 */
	@SuppressWarnings("deprecation")
	private java.util.Vector<WatchableList> getSelectedTracks() {
		java.util.Vector<WatchableList> res = null;

		// check we have some tracks selected
		final Object[] selections = _theTracksList.getSelectedValues();

		if (selections.length > 0) {
			res = new Vector<WatchableList>(0, 1);
			for (int i = 0; i < selections.length; i++) {
				final LabelledWatchableHolder wh = (LabelledWatchableHolder) selections[i];
				res.addElement(wh.getList());
			}
		}

		return res;
	}

	/**
	 * get the time currently set in the starter control
	 */
	public HiResDate getStartTime() {
		return _starter.get_DTG();
	}

	///////////////////////////////////
	// member functions
	//////////////////////////////////
	/**
	 * find which items in the data are watchable
	 *
	 * @param theData the data to search
	 * @return any watchable items on the plot
	 */
	Vector<LabelledWatchableHolder> getWatchables(final Layers theData) {
		final Vector<LabelledWatchableHolder> res = new Vector<LabelledWatchableHolder>(0, 1);

		// step through the layers
		final int cnt = theData.size();
		for (int i = 0; i < cnt; i++) {
			final Layer l = theData.elementAt(i);
			if (l instanceof WatchableList) {
				addElement((WatchableList) l, res);

				// hey, if this is a trackwrapper - we also want to look at it's lists of
				// sensors
				// and solutions
				if (l instanceof TrackWrapper) {
					final Enumeration<Editable> solutions = ((TrackWrapper) l).getSolutions().elements();
					if (solutions != null) {
						while (solutions.hasMoreElements()) {
							final TMAWrapper tmaWrapper = (TMAWrapper) solutions.nextElement();
							addElement(tmaWrapper, res);
						}
					}
					final Enumeration<Editable> sensors = ((TrackWrapper) l).getSensors().elements();
					if (sensors != null) {
						while (sensors.hasMoreElements()) {
							final SensorWrapper sensorWrapper = (SensorWrapper) sensors.nextElement();
							addElement(sensorWrapper, res);
						}
					}
				}
			} else {
				final Enumeration<Editable> iter = l.elements();
				while (iter.hasMoreElements()) {
					final Plottable p = (Plottable) iter.nextElement();
					if (p instanceof WatchableList) {
						addElement((WatchableList) p, res);
					}
				}
			}
		}

		return res;
	}

	///////////////////////////////////
	// member functions
	//////////////////////////////////
	/**
	 * construct the form
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initForm() {
		this.setLayout(new BorderLayout());
		final JPanel sliders = new JPanel();
		final JPanel buttons = new JPanel();

		// the form
		sliders.setLayout(new GridLayout(0, 1));
		buttons.setLayout(new GridLayout(1, 0));

		// the time ranges
		_starter = new SwingCompositeTimeEditor("Start:");
		_finisher = new SwingCompositeTimeEditor("End:");
		sliders.add(_starter);
		sliders.add(_finisher);

		// the do/close/reset buttons
		final JButton doBtn = new JButton("Apply");
		doBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				runOperation();
			}
		});
		final JButton resetBtn = new JButton("Reset");
		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				resetMe();
			}
		});
		final JButton closeBtn = new JButton("Close");
		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				closeMe();
			}
		});

		buttons.add(closeBtn);
		buttons.add(doBtn);
		buttons.add(resetBtn);

		// now create the panel holding the list of Actions we allow
		_theOperationsList = new JList();
		_theOperationsList.setModel(new DefaultListModel());
		final JScrollPane _theOperationsListScroller = new JScrollPane(_theOperationsList);

		// handle a selection from the list
		_theOperationsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			@Override
			public void valueChanged(final javax.swing.event.ListSelectionEvent e) {
				operationSelected();
			}

		});

		// the description of the current operation
		final JLabel tmp = new JLabel("blank");
		_theDescription = new JTextArea();
		_theDescription.setOpaque(false);
		_theDescription.setForeground(tmp.getForeground());
		_theDescription.setLineWrap(true);
		_theDescription.setWrapStyleWord(true);
		_theDescription.setMargin(new Insets(2, 2, 2, 2));
		final JScrollPane _theTextScroller = new JScrollPane(_theDescription);

		// the list of tracks we know about
		final JPanel theTracksPanel = new JPanel();
		theTracksPanel.setLayout(new BorderLayout());
		_theTracksList = new JList();

		final JScrollPane theTracksListScroller = new JScrollPane(_theTracksList);

		// lay out the tracks panel
		theTracksPanel.add("Center", theTracksListScroller);

		// and put them together
		final JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		final JPanel descPanel = new JPanel();
		descPanel.setLayout(new BorderLayout());
		descPanel.add("Center", _theTextScroller);

		listPanel.add("North", new JLabel("1. First select the operation to be performed"));
		listPanel.add("West", _theOperationsListScroller);
		listPanel.add("Center", descPanel);
		listPanel.add("South", theTracksPanel);

		//
		this.add("Center", listPanel);
		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add("South", buttons);
		bottomPanel.add("North", sliders);
		this.add("South", bottomPanel);

	}

	/**
	 * an operation has been selected, put the textual details into the description
	 * box
	 */
	void operationSelected() {
		// retrieve the operation currently selected
		final Debrief.Tools.FilterOperations.FilterOperation fo = getOperation();

		if (fo != null)
			_theDescription.setText(fo.getDescription());
	}

	/**
	 * setup the lists of operations and tracks
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateLists(final Debrief.GUI.Tote.StepControl theStepper) {
		// create our operations
		_myOperations.addElement(new SetTimeZero() {
			@Override
			public void setTimeZero(final HiResDate newDate) {
				_theStepper.setTimeZero(newDate);
			}
		});
		_myOperations.addElement(new DoFilter());
		_myOperations.addElement(new ShowTimeVariablePlot3(_theParent, theStepper));
		_myOperations.addElement(new CopyTimeDataToClipboard());
		_myOperations.addElement(new ReformatFixes(_theData));
		_myOperations.addElement(new HideRevealObjects(_theData));

		// now populate the list
		final Enumeration<FilterOperation> iter = _myOperations.elements();
		while (iter.hasMoreElements()) {
			final Debrief.Tools.FilterOperations.FilterOperation fo = iter.nextElement();

			final DefaultListModel dm = (DefaultListModel) _theOperationsList.getModel();
			dm.addElement(fo.getLabel());

		}

	}

	/**
	 * find out which tracks are watchable, then populate the JList
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void populateTracks() {
		// clear out the list to start with (by setting a new, blank, list)
		_theTracksList.setModel(new DefaultListModel());

		// find out what data is watchable
		final Vector<LabelledWatchableHolder> watches = getWatchables(_theData);

		// set the new size of the List item
		_theTracksList.setVisibleRowCount(Math.min(9, watches.size()));

		// get the list model
		final DefaultListModel wdm = (DefaultListModel) _theTracksList.getModel();

		// now for the watchables list
		final Enumeration<LabelledWatchableHolder> iter = watches.elements();
		while (iter.hasMoreElements()) {
			final LabelledWatchableHolder oj = iter.nextElement();
			wdm.addElement(oj);
		}
	}

	/**
	 * reset
	 */
	public void reIntitialise() {
		// re-do the list of tracks
		populateTracks();
		getLimits();
		resetMe();
	}

	/**
	 * push the sliders back out to the extreme values
	 */
	void resetMe() {
		resetMe(_startTime, _endTime, _starter.get_DTG(), _finisher.get_DTG());

		final FilterOperation currentOperation = getOperation();

		// just fire a reset event at the current operation, in case it's interested
		if (currentOperation != null) {
			// do we have an operation selected?
			getOperation().resetMe(_startTime, _endTime);
		}

	}

	/**
	 * set the start/end ranges and the current values for the start/end sliders
	 *
	 * @param start        start end value
	 * @param end          finish end value
	 * @param currentStart current value on the start slider
	 * @param currentEnd   current value on the end slider
	 */
	private void resetMe(final HiResDate start, final HiResDate end, final HiResDate currentStart,
			final HiResDate currentEnd) {
		// have a look at the elapsed period, to see if we should be
		// stepping in minutes or seconds
		final long delta = end.getMicros() - start.getMicros();

		final int step_size;

		// hmm, are we in hi res mode?
		if (HiResDate.inHiResProcessingMode()) {
			step_size = SwingCompositeTimeEditor.MICRO_STEPS;
		} else {
			// should we use seconds? (track shorter than 30 mins)
			if (delta < T_30_MINS) {
				// use seconds steps
				step_size = SwingCompositeTimeEditor.SECOND_STEPS;
			} else if (delta > T_30_DAYS) {
				// use hour steps (greater than 3 days)
				step_size = SwingCompositeTimeEditor.HOUR_STEPS;
			} else {
				// just use minutes
				step_size = SwingCompositeTimeEditor.MINUTE_STEPS;
			}
		}

		// set the steps
		_starter.setStepSize(step_size);
		_finisher.setStepSize(step_size);

		_starter.set_startDTG(start);
		_starter.set_endDTG(end);
		_starter.set_DTG(currentStart);
		_finisher.set_startDTG(start);
		_finisher.set_endDTG(end);
		_finisher.set_DTG(currentEnd);

	}

	/**
	 * run the selected operation
	 */
	void runOperation() {
		// retrieve the operation selected in the list
		final FilterOperation fo = getOperation();

		if (fo == null)
			return;

		// pass the necessary data to the operation
		fo.setPeriod(_starter.get_DTG(), _finisher.get_DTG());

		// sort out which tracks are selected
		fo.setTracks(getSelectedTracks());

		// get the data for the operation
		final MWC.GUI.Tools.Action res = fo.getData();

		// check we were able to prepare the data
		if (res != null) {
			// make the change
			res.execute();

			// tell the data that we've fiddled with it
			_theData.fireReformatted(null);

			// put the action on the buffer
			if (_theUndoBuffer != null)
				_theUndoBuffer.add(res);

			// update the screen
			_theChart.update();
		}
	}

	/**
	 * set the time currently set in the finisher control
	 */
	public void setFinishTime(final HiResDate val) {
		_finisher.set_DTG(val);
	}

	/**
	 * set the time currently set in the starter control
	 */
	public void setStartTime(final HiResDate val) {
		_starter.set_DTG(val);
	}

}
