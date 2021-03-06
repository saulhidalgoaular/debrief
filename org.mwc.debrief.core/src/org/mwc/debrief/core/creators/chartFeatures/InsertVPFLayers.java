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

package org.mwc.debrief.core.creators.chartFeatures;

import MWC.GUI.PlainChart;
import MWC.GUI.Plottable;
import MWC.GUI.Tools.Palette.CreateVPFLayers;

/**
 * @author ian.mayo
 *
 */
public class InsertVPFLayers extends CoreInsertChartFeature {

	public InsertVPFLayers() {
		// tell our parent that we want to be inserted as a top-level layer
		super(false);
	}

	/**
	 * @return
	 */
	@Override
	protected Plottable getPlottable(final PlainChart theChart) {
		return CreateVPFLayers.createMyLibrary(true);
	}

}
