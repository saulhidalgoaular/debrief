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

package org.mwc.asset.netasset2.part;

public interface IVPartControl extends IVPart {

	public static interface NewDemStatus {
		public void demanded(double course, double speed, double depth);
	}

	String getDemCourse();

	String getDemDepth();

	String getDemSpeed();

	void setDemStatusListener(NewDemStatus newDemStatus);

	void setEnabled(boolean val);

}
