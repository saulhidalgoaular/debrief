
package Debrief.ReaderWriter.XML.GUI;

/*******************************************************************************
 * Debrief - the Open Source Maritime Analysis Application http://debrief.info
 *
 * (C) 2000-2020, Deep Blue C Technology Ltd
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html)
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *******************************************************************************/

abstract public class PrimarySecondaryHandler extends MWC.Utilities.ReaderWriter.XML.MWCXMLReader {
	String _name;

	public PrimarySecondaryHandler(final String type) {
		// inform our parent what type of class we are
		super(type);

		addAttributeHandler(new HandleAttribute("Name") {
			@Override
			public void setValue(final String name, final String value) {
				_name = value;
			}
		});

	}

	@Override
	public final void elementClosed() {
		setTrack(_name);

		// reset our variables
		_name = null;
	}

	// pass on to the parent the name of this track
	abstract public void setTrack(String name);
}