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
package Debrief.GUI.Tote.Painters;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import Debrief.GUI.Tote.Painters.SnailDrawTacticalContact.HostedList;
import Debrief.GUI.Tote.Painters.SnailDrawTacticalContact.PlottableWrapperWithTimeAndOverrideableColor;
import Debrief.GUI.Tote.Painters.SnailPainter2.ColorFadeCalculator;
import Debrief.Wrappers.SensorContactWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Defaults;
import MWC.GUI.Editable;
import MWC.GenericData.Duration;
import MWC.GenericData.HiResDate;
import MWC.GenericData.Watchable;
import MWC.GenericData.WatchableList;
import MWC.GenericData.WorldArea;
import MWC.GenericData.WorldLocation;

/**
 * Created by IntelliJ IDEA. User: ian.mayo Date: 22-Feb-2005 Time: 09:10:33 To
 * change this template use File | Settings | File Templates.
 */
public abstract class SnailDrawTacticalContact2 implements SnailPainter2.drawHighLight2, MWC.GUI.Editable {
	/**
	 * the snail plotter we are using = we look at this to determine plotting
	 * characteristics
	 */
	protected SnailDrawFix2 _fixPlotter = null;

	@Override
	public abstract boolean canPlot(Watchable wt);

	/**
	 * do the plotting
	 */
	@Override
	public final Rectangle drawMe(final MWC.Algorithms.PlainProjection proj, final Graphics dest,
			final WatchableList list, final Watchable watch, final TotePainter parent, final HiResDate dtg,
			final ColorFadeCalculator fader) {

		Rectangle thisR = null;

		final boolean keepItSimple = true;

		// get a pointer to the fix
		final PlottableWrapperWithTimeAndOverrideableColor contact = (PlottableWrapperWithTimeAndOverrideableColor) watch;
		final HostedList wrapper = (HostedList) list;

		// wrap the canvas
		final MWC.GUI.Canvas.CanvasAdaptor adaptor = new MWC.GUI.Canvas.CanvasAdaptor(proj, dest);

		// how long? (convert to millis)
		final long trail_len = (long) _fixPlotter.getTrailLength().getValueIn(Duration.MICROSECONDS);

		final String alphaStr = Defaults.getPreference(SensorContactWrapper.TRANSPARENCY);
		int alpha;
		if (alphaStr != null && alphaStr.length() > 0) {
			try {
				alpha = Integer.parseInt(alphaStr);
			} catch (final NumberFormatException e) {
				alpha = 255;
				e.printStackTrace();
			}
		} else {
			alpha = 255;
		}

		// are we plotting a back-track?
		if (trail_len > 0) {
			// calculate the start time
			final HiResDate start_time = new HiResDate(0, dtg.getMicros() - trail_len);

			// get the list of contacts
			final java.util.Collection<Editable> contacts = list.getItemsBetween(start_time, dtg);

			// get the parent track - so we can plot relative to it..
			final TrackWrapper hostTrack = wrapper.getHost();

			// how long is it?
			if (contacts != null) {
				final float len = contacts.size();

				if (len > 0) {
					// ok, work back from the last one
					final java.util.Iterator<Editable> cons = contacts.iterator();
					while (cons.hasNext()) {
						// get the next contact
						final PlottableWrapperWithTimeAndOverrideableColor scw = (PlottableWrapperWithTimeAndOverrideableColor) cons
								.next();

						// sort out the area for this tua
						final WorldArea wa = scw.getBounds();

						// did we find an area?
						// there's a chance that we have tua data which extends beyond the time period
						// of the
						// track,
						// and for relative tua data we shouldn't even try to plot it
						if (wa != null) {

							// so, take a safe copy of the actual colour (which we will restore later)
							final Color oldCol = scw.getActualColor();

							// now take the colour in use (which may in fact belong to the parent class)
							final Color thisCol = scw.getColor();

							final Color newCol = fader.fadeColorAt(thisCol, scw.getDTG());

							// and put this colour back into the data item
							scw.setColor(newCol);

							// paint the object
							scw.paint(hostTrack, adaptor, keepItSimple, alpha);

							// restore the colour, if it's non-null
							scw.setColor(oldCol);

							// cool, we've found the location. sorted.
							final WorldLocation tl = wa.getTopLeft();
							final WorldLocation br = wa.getBottomRight();
							final Point pTL = new Point(proj.toScreen(tl));
							final Point pBR = new Point(proj.toScreen(br));
							final Rectangle thisArea = new Rectangle(pTL);
							thisArea.add(pBR);
							if (thisR == null)
								thisR = thisArea;
							else
								thisR.add(thisArea);
						}

					} // while there are still contacts
				} // if one or more contacts got returned
			} // if a list of contacts got returned
		} // if we are plotting a back-track at all
		else {
			// just plot the most recent one
			// work out the area covered
			final WorldArea wa = watch.getBounds();
			final WorldLocation tl = wa.getTopLeft();
			final WorldLocation br = wa.getBottomRight();
			final Point pTL = new Point(proj.toScreen(tl));
			final Point pBR = new Point(proj.toScreen(br));
			final Rectangle thisArea = new Rectangle(pTL);
			thisArea.add(pBR);
			thisR = thisArea;

			// and plot in the line
			contact.paint(null, adaptor, keepItSimple, alpha);

		}

		return thisR;
	}

	@Override
	public final EditorType getInfo() {
		return null;
	}

	@Override
	public final String getName() {
		return "Snail";
	}

	@Override
	public final boolean hasEditor() {
		return false;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
