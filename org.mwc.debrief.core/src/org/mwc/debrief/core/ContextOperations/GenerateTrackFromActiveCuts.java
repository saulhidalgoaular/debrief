/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package org.mwc.debrief.core.ContextOperations;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.mwc.cmap.core.CorePlugin;
import org.mwc.cmap.core.operations.CMAPOperation;
import org.mwc.cmap.core.property_support.RightClickSupport.RightClickContextItemGenerator;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.SensorContactWrapper;
import Debrief.Wrappers.SensorWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldVector;
import MWC.TacticalData.Fix;

/**
 * @author ian.mayo
 * 
 */
public class GenerateTrackFromActiveCuts implements RightClickContextItemGenerator {

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	// testing for this class
	// ////////////////////////////////////////////////////////////////////////////////////////////////
	static public final class testMe extends junit.framework.TestCase {
		static public final String TEST_ALL_TEST_TYPE = "UNIT";

		public testMe(final String val) {
			super(val);
		}

		public final void testIWork()
		{
			// /////////////////////////////////////////////////////////
			// test the position related stuff
			final TrackWrapper track = new TrackWrapper();

			// and add the fixes
			final java.util.Calendar cal = new java.util.GregorianCalendar(2001, 10,
					4, 4, 4, 0);

			cal.set(2001, 10, 4, 4, 4, 0);
			track.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
					.getTime().getTime(), 0), new WorldLocation(2.0, 2.0, 0.0), 12, 12)));

			cal.set(2001, 10, 4, 4, 4, 01);
			track
					.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
							.getTime().getTime(), 0), new WorldLocation(2.0, 2.25, 0.0), 12,
							12)));

			cal.set(2001, 10, 4, 4, 4, 02);
			track.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
					.getTime().getTime(), 0), new WorldLocation(2.0, 2.5, 0.0), 12, 12)));
			cal.set(2001, 10, 4, 4, 4, 05);
			track
					.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
							.getTime().getTime(), 0), new WorldLocation(2.0, 2.75, 0.0), 12,
							12)));
			cal.set(2001, 10, 4, 4, 4, 23);
			track
					.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
							.getTime().getTime(), 0), new WorldLocation(2.25, 2.0, 0.0), 12,
							12)));
			cal.set(2001, 10, 4, 4, 4, 25);
			track.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
					.getTime().getTime(), 0), new WorldLocation(2.5, 2.0, 0.0), 12, 12)));
			cal.set(2001, 10, 4, 4, 4, 28);
			final WorldLocation theLoc = new WorldLocation(2.75d, 2.0, 0.0);
			track.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
					.getTime().getTime(), 0), theLoc, 12, 12)));
			cal.set(2001, 10, 4, 4, 4, 55);
			track
					.addFix(new FixWrapper(new MWC.TacticalData.Fix(new HiResDate(cal
							.getTime().getTime(), 0), new WorldLocation(2.25, 2.25, 0.0), 12,
							12)));

			// and some sensor data, which goes past the end of O/S track
			SensorWrapper sensor = new SensorWrapper("SensorName");
			
			cal.set(2001, 10, 4, 4, 4, 25);
			sensor.add(new SensorContactWrapper("track", new HiResDate(cal.getTime().getTime()),
					new WorldDistance(1000, WorldDistance.YARDS), 23.3d, new WorldLocation(0d, 0d, 0d), Color.RED,
					"Some lable", 1, sensor.getName()));

			cal.set(2001, 10, 4, 4, 4, 27);
			sensor.add(new SensorContactWrapper("track", new HiResDate(cal.getTime().getTime()),
					new WorldDistance(1000, WorldDistance.YARDS), 23.3d, new WorldLocation(0d, 0d, 0d), Color.RED,
					"Some lable", 1, sensor.getName()));

			cal.set(2001, 10, 4, 4, 4, 55);
			sensor.add(new SensorContactWrapper("track", new HiResDate(cal.getTime().getTime()),
					new WorldDistance(1000, WorldDistance.YARDS), 23.3d, new WorldLocation(0d, 0d, 0d), Color.RED,
					"Some lable", 1, sensor.getName()));

			cal.set(2001, 10, 4, 4, 5, 05);
			sensor.add(new SensorContactWrapper("track", new HiResDate(cal.getTime().getTime()),
					new WorldDistance(1000, WorldDistance.YARDS), 23.3d, new WorldLocation(0d, 0d, 0d), Color.RED,
					"Some lable", 1, sensor.getName()));
			
			track.add(sensor);
			Layers layers = new Layers();
			layers.addThisLayer(track);
			
			
			// now try to offer cuts
			GenerateTrackFromActiveCuts genny = new GenerateTrackFromActiveCuts();
			Layer[] parentLayers = new Layer[] {track};
      Editable[] subjects = new Editable[] {sensor};
      DummyMenu parent = new DummyMenu();
      genny.generate(parent, layers, parentLayers, subjects);
      
      assertEquals("items added", 1, parent.items.size());
      
      Action item = (Action) parent.items.firstElement();
      item.run();
      
		}
	}
	
	private static class DummyMenu implements IMenuManager{

	  Vector<Object> items = new Vector<>();
	  
    @Override
    public void add(IAction action)
    {
      items.add(action);
    }

    @Override
    public void add(IContributionItem item)
    {
      items.add(item);
    }

    @Override
    public void appendToGroup(String groupName, IAction action)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void appendToGroup(String groupName, IContributionItem item)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IContributionItem find(String id)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public IContributionItem[] getItems()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public IContributionManagerOverrides getOverrides()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void insertAfter(String id, IAction action)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void insertAfter(String id, IContributionItem item)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void insertBefore(String id, IAction action)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void insertBefore(String id, IContributionItem item)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public boolean isDirty()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isEmpty()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void markDirty()
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void prependToGroup(String groupName, IAction action)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void prependToGroup(String groupName, IContributionItem item)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IContributionItem remove(String id)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public IContributionItem remove(IContributionItem item)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void removeAll()
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void update(boolean force)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void dispose()
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void fill(Composite parent)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void fill(Menu parent, int index)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void fill(ToolBar parent, int index)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void fill(CoolBar parent, int index)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public String getId()
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean isDynamic()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isGroupMarker()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isSeparator()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isVisible()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void saveWidgetState()
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void setParent(IContributionManager parent)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void setVisible(boolean visible)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void update()
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void update(String id)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void addMenuListener(IMenuListener listener)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public IMenuManager findMenuUsingPath(String path)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public IContributionItem findUsingPath(String path)
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public boolean getRemoveAllWhenShown()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public boolean isEnabled()
    {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void removeMenuListener(IMenuListener listener)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void setRemoveAllWhenShown(boolean removeAll)
    {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void updateAll(boolean force)
    {
      // TODO Auto-generated method stub
      
    }
	  
	}

	private static class TrackfromSensorCuts extends TrackfromSensorData {
		private final SensorContactWrapper[] _items;

		public TrackfromSensorCuts(final SensorContactWrapper[] items, final Layers theLayers) {
			super("Create Track from Active cuts", theLayers);
			_items = items;
		}

		protected SensorContactWrapper[] getCuts() {
			return _items;
		}
	}

	private static class TrackfromSensorWrappers extends TrackfromSensorData {
		private final SensorWrapper _wrapper;

		public TrackfromSensorWrappers(final SensorWrapper wrapper, final Layers theLayers) {
			super("Create Track from Active cuts", theLayers);
			_wrapper = wrapper;
		}

		protected SensorContactWrapper[] getCuts() {

			final Vector<SensorContactWrapper> wraps = new Vector<SensorContactWrapper>();

			if (_wrapper.size() > 0) {
				// ok, now check for range
				Editable first = _wrapper.elements().nextElement();
				SensorContactWrapper scw = (SensorContactWrapper) first;
				if (scw.getHasBearing() && scw.getRange() != null) {
					// ok, it's a goer.
					final Enumeration<Editable> numer = _wrapper.elements();
					while (numer.hasMoreElements()) {
						wraps.add((SensorContactWrapper) numer.nextElement());
					}
				}
			}

			final SensorContactWrapper[] _items;
			if (wraps.size() > 0) {
				SensorContactWrapper[] sample = new SensorContactWrapper[wraps.size()];
				_items = wraps.toArray(sample);
			} else {
				_items = null;
			}

			return _items;
		}
	}

	private abstract static class TrackfromSensorData extends CMAPOperation {

		private final Layers _layers;
		private TrackWrapper _newTrack;

		public TrackfromSensorData(String title, final Layers theLayers) {
			super(title);
			_layers = theLayers;
		}

		abstract protected SensorContactWrapper[] getCuts();

		@Override
		public IStatus execute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {

			SensorContactWrapper[] cuts = getCuts();

			_newTrack = new TrackWrapper();
			_newTrack.setName("Track_from_" + cuts[0].getSensorName());
			_newTrack.setColor(cuts[0].getColor());

			boolean fixesAdded = false;

			// ok, now loop through and assign the cuts
			for (int cnt = 0; cnt < cuts.length; cnt++) {
				SensorContactWrapper cut = cuts[cnt];

				// double-check this is suitable
				if (cut.getHasBearing() && cut.getRange() != null && !cut.getHasAmbiguousBearing()) {
					// represent rng/brg as a vector
					WorldVector vec = new WorldVector(Math.toRadians(cut.getBearing()),
							cut.getRange().getValueIn(WorldDistance.DEGS), 0);

					// check we have a location
					WorldLocation origin = cut.getLocation();
          if (origin != null)
          {
            // also do the far end
            WorldLocation loc = origin.add(vec);

            Fix fix = new Fix(cut.getDTG(), loc, 0d, 0d);
            FixWrapper fw = new FixWrapper(fix);

            // give it a sensible label
            fw.resetName();

            _newTrack.add(fw);

            fixesAdded = true;

          }
				}
			}

			if (fixesAdded) {
				// and auto-generate courses and speeds
				_newTrack.calcCourseSpeed();

				// done, store it!
				_layers.addThisLayerAllowDuplication(_newTrack);

				// sorted, do the update
				_layers.fireExtended();
			} else {
				CorePlugin.logError(Status.WARNING,
						"Failed to find fixes with range and bearing - " + "track not generated", null);
			}

			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
			// forget about the new tracks
			_layers.removeThisLayer(_newTrack);
			_layers.fireExtended();

			return Status.OK_STATUS;
		}

	}

	/**
	 * @param parent
	 * @param theLayers
	 * @param parentLayers
	 * @param subjects
	 */
	public void generate(final IMenuManager parent, final Layers theLayers, final Layer[] parentLayers,
			final Editable[] subjects) {
		Action _myAction = null;

		// so, see if it's something we can do business with
		if (subjects.length == 1) {
			// ok, do I know how to create a TMA segment from this?
			final Editable onlyOne = subjects[0];
			if (onlyOne instanceof SensorWrapper) {
				final SensorWrapper sw = (SensorWrapper) onlyOne;

				if (sw.size() > 0) {
					// ok, now check for range
					Editable first = sw.elements().nextElement();
					SensorContactWrapper scw = (SensorContactWrapper) first;

					if (scw.getHasBearing() && scw.getRange() != null && !scw.getHasAmbiguousBearing()) {

						// cool wrap it in an action.
						_myAction = new Action("Generate Track from Active Sensor Data") {
							@Override
							public void run() {
								// ok, go for it.
								// sort it out as an operation
								final IUndoableOperation convertToTrack1 = new TrackfromSensorWrappers(sw, theLayers);

								// ok, stick it on the buffer
								runIt(convertToTrack1);
							}
						};
					}
				}
			}
		} else if (subjects.length > 1) {
			// more than one item = maybe it's a series of sensor cuts

			//
			SensorContactWrapper[] sonarCuts = null;

			// see if it's a collection of cuts
			// so, it's a number of items, Are they all sensor contact wrappers
			boolean allGood = true;
			sonarCuts = new SensorContactWrapper[subjects.length];
			for (int i = 0; i < subjects.length; i++) {
				final Editable editable = subjects[i];
				if (editable instanceof SensorContactWrapper) {
					SensorContactWrapper scw = (SensorContactWrapper) editable;
					if (scw.getHasBearing() && scw.getRange() != null && !scw.getHasAmbiguousBearing()) {
						// cool, stick with it
						sonarCuts[i] = (SensorContactWrapper) editable;
					} else {
						allGood = false;
					}
				} else {
					allGood = false;
					break;
				}

			}
			// are we good to go?
			if (!allGood) {
				// nope, clear the items list
				sonarCuts = null;
			} else {
				// cool, go for it
				final SensorContactWrapper[] finalItems = sonarCuts;

				// cool wrap it in an action.
				_myAction = new Action("Generate Track from Active Sensor Data") {
					@Override
					public void run() {
						// ok, go for it.
						// sort it out as an operation
						final IUndoableOperation convertToTrack1 = new TrackfromSensorCuts(finalItems, theLayers);

						// ok, stick it on the buffer
						runIt(convertToTrack1);
					}
				};
			}
		}

		if (_myAction != null) {
			parent.add(_myAction);
		}
	}

	/**
	 * put the operation firer onto the undo history. We've refactored this into a
	 * separate method so testing classes don't have to simulate the CorePlugin
	 * 
	 * @param operation
	 */
	protected void runIt(final IUndoableOperation operation) {
		CorePlugin.run(operation);
	}
}
