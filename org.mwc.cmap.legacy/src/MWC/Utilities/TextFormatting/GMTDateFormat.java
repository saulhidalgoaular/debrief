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
package MWC.Utilities.TextFormatting;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * date formatter that forces use of GMT timezone
 *
 * @author ian
 *
 */
public class GMTDateFormat extends SimpleDateFormat {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * pregenerate the timezone. we don't need it every time
	 *
	 */
	private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");

	public GMTDateFormat(final String format) {
		super(format);

		super.setTimeZone(GMT_ZONE);

		// don't let the format be lenient. Throw errors on malformed dates. This
		// may help catch USA-style MMDDYY being passed instead of DDMMYY
		setLenient(false);
	}

	public GMTDateFormat(final String pattern, final Locale us) {
		super(pattern, us);
		super.setTimeZone(GMT_ZONE);
	}

	@Override
	final public void setTimeZone(final TimeZone arg0) {
		throw new IllegalArgumentException("Can't override time zone. It's fixed to GMT");
	}
}
