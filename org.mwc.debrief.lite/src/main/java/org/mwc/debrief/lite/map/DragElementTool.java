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
package org.mwc.debrief.lite.map;

import java.awt.Point;

import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.mwc.debrief.lite.gui.GeoToolMapProjection;

import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.ToolParent;
import MWC.GUI.Shapes.FindNearest;
import MWC.GUI.Shapes.HasDraggableComponents;
import MWC.GUI.Shapes.HasDraggableComponents.ComponentConstruct;
import MWC.GUI.Tools.Action;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldVector;

public class DragElementTool extends GenericDragTool {

	private class DragWholeElementAction implements Action {

		public WorldLocation _component;
		public WorldVector _offset;
		public HasDraggableComponents _dragElement;
		public Layer _parentLayer;
		private final Layers _layers;

		public DragWholeElementAction(final WorldVector forward, final HasDraggableComponents hoverTarget,
				final WorldLocation hoverComponent, final Layers layers, final Layer parentLayer) {
			_dragElement = hoverTarget;
			_component = hoverComponent;
			_parentLayer = parentLayer;
			_layers = layers;
			_offset = forward;
		}

		@Override
		public void execute() {
			_dragElement.shift(_component, _offset);
			_mapPane.repaint();

		}

		@Override
		public boolean isRedoable() {
			return true;
		}

		@Override
		public boolean isUndoable() {
			return true;
		}

		@Override
		public String toString() {
			final String res = "Drag " + _dragElement.getName() + _offset.toString();
			return res;
		}

		@Override
		public void undo() {
			final WorldVector reverseVector = _offset.generateInverse();
			_dragElement.shift(_component, reverseVector);
			_layers.fireModified(_parentLayer);
		}
	}

	/**
	 * the thing we're currently hovering over
	 */
	protected HasDraggableComponents _hoverTarget;
	private final ToolParent _toolParent;
	private WorldLocation _startLocation;
	private WorldLocation _lastLocation;
	private Point _startPoint;

	private Point _lastPoint;

	private WorldVector offset;

	public DragElementTool(final Layers layers, final GeoToolMapProjection projection, final JMapPane mapPane,
			final ToolParent toolParent) {
		super(layers, projection, mapPane);
		_toolParent = toolParent;
	}

	/**
	 * Respond to a mouse dragged event. Calls
	 * {@link org.geotools.swing.MapPane#moveImage()}
	 *
	 * @param ev the mouse event
	 */
	@Override
	public void onMouseDragged(final MapMouseEvent ev) {
		if (panning) {
			final Point pos = mouseDelta(ev.getPoint());
			if ((_startPoint != null) && (_hoverTarget != null)) {
				if (_lastPoint == null) {
					// the first time
					_lastLocation = _startLocation;
				}

				_lastPoint = new Point(ev.getPoint().x, ev.getPoint().y);
				if (!pos.equals(panePos)) {

					final WorldLocation cursorLoc = _projection.toWorld(panePos);

					if (_hoverTarget != null) {
						final WorldLocation newLocation = new WorldLocation(_projection.toWorld(pos));

						// now work out the vector from the last place plotted to the current
						// place
						offset = newLocation.subtract(cursorLoc);
						_lastLocation = newLocation;
						_hoverTarget.shift(_hoverComponent, offset);
						_mapPane.repaint();
					}
					panePos = pos;
				}
			}

		}
	}

	/**
	 * Respond to a mouse button press event from the map mapPane. This may signal
	 * the start of a mouse drag. Records the event's window position.
	 *
	 * @param ev the mouse event
	 */
	@Override
	public void onMousePressed(final MapMouseEvent ev) {
		super.onMousePressed(ev);
		_startPoint = new Point(ev.getPoint().x, ev.getPoint().y);
		_lastPoint = null;
		_startLocation = new WorldLocation(_projection.toWorld(new java.awt.Point(ev.getPoint().x, ev.getPoint().y)));
		if (LiteMapPane.isMapViewportAcceptable(_mapPane) && !panning) {
			panePos = mouseDelta(ev.getPoint());

			final WorldLocation cursorLoc = _projection.toWorld(panePos);
			// find the nearest editable item
			final ComponentConstruct currentNearest = new ComponentConstruct();
			final int num = layers.size();
			for (int i = 0; i < num; i++) {
				final Layer thisL = layers.elementAt(i);
				if (thisL.getVisible()) {
					// find the nearest items, this method call will recursively pass down
					// through
					// the layers
					FindNearest.findNearest(thisL, cursorLoc, panePos, currentNearest, null);
				}
			}

			// did we find anything?
			if (currentNearest.populated()) {
				// generate a screen point from the cursor pos plus our distnace
				// NOTE: we're not basing this on the target location - we may not have
				// a
				// target location as such for a strangely shaped object
				final WorldLocation tgtPt = cursorLoc.add(new WorldVector(Math.PI / 2, currentNearest._distance, null));

				// is it close enough
				final Point tPoint = _projection.toScreen(tgtPt);

				// get click point
				final Point cursorPos = ev.getPoint();

				// get distance of click point from nearest object, in screen coords
				final double distance = tPoint.distance(cursorPos);

				if (distance < JITTER) {
					panning = true;

					_hoverTarget = currentNearest._object;
					_hoverComponent = currentNearest._draggableComponent;
					_parentLayer = currentNearest._topLayer;
				}
			}
		}
	}

	@Override
	public void onMouseReleased(final MapMouseEvent ev) {
		super.onMouseReleased(ev);
		if (_lastLocation != null && _startLocation != null) {
			final WorldVector forward = _lastLocation.subtract(_startLocation);

			// put it into our action
			final DragWholeElementAction dta = new DragWholeElementAction(forward, _hoverTarget, _hoverComponent,
					layers, _parentLayer);

			if (dta != null && dta.isUndoable() && _toolParent != null) {
				_toolParent.addActionToBuffer(dta);
			}
			_startPoint = null;
			_lastPoint = null;
			_lastLocation = null;
			_startLocation = null;
			_hoverTarget = null;
		}
	}
}
