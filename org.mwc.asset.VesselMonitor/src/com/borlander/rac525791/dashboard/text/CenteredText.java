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

package com.borlander.rac525791.dashboard.text;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;

public class CenteredText implements TextDrawer {
	/**
	 * @see FigureUtilities#getLargestSubstringConfinedTo
	 */
	public static int getLargestSubstringConfinedTo(final GCProxy gc, final String s, final Font font,
			final int availableWidth) {

		int min, max;
		final float avg = gc.getAverageCharWidth(font);
		min = 0;
		max = s.length() + 1;

		// The size of the current guess
		int guess = 0, guessSize = 0;
		while ((max - min) > 1) {
			// Pick a new guess size
			// New guess is the last guess plus the missing width in pixels
			// divided by the average character size in pixels
			guess = guess + (int) ((availableWidth - guessSize) / avg);

			if (guess >= max)
				guess = max - 1;
			if (guess <= min)
				guess = min + 1;

			// Measure the current guess
			guessSize = gc.getExtent(s.substring(0, guess), font).x;

			if (guessSize < availableWidth)
				// We did not use the available width
				min = guess;
			else
				// We exceeded the available width
				max = guess;
		}
		return min;

	}

	private String myText;

	private final Rectangle myBounds = new Rectangle();

	private final Rectangle TEMP = new Rectangle();

	private Font myFont;

	private Point myCachedTextSize;

	/**
	 * Constructs not initialized instance. At least <code>setFont()</code> method
	 * should be called to complete initialziation.
	 */
	public CenteredText() {
		this(null, "");
	}

	public CenteredText(final Font font) {
		this(font, "");
	}

	public CenteredText(final Font font, final int x, final int y, final int width, final int height) {
		this(font, "", x, y, width, height);
	}

	public CenteredText(final Font font, final Rectangle rect) {
		this(font, "", rect.x, rect.y, rect.width, rect.height);
	}

	public CenteredText(final Font font, final String text) {
		this(font, text, 0, 0, 0, 0);
	}

	public CenteredText(final Font font, final String text, final int x, final int y, final int w, final int h) {
		setFont(font);
		setText(text);
		setBounds(x, y, w, h);
	}

	@Override
	public void drawText(final GCProxy gcFactory, final Graphics g) {
		if (myFont == null) {
			return;
		}
		placeText(gcFactory, TEMP);
		g.pushState();
		g.setFont(myFont);
		g.drawText(myText, TEMP.x, TEMP.y);

//		g.setForegroundColor(ColorConstants.red);
//		g.drawRectangle(TEMP);

		g.popState();
	}

	public int getLargestSubstringConfinedTo(final GCProxy gc, final String s) {
		return getLargestSubstringConfinedTo(gc, s, myFont, myBounds.width);
	}

	public boolean isFitHorizontally(final GCProxy gc, final String text) {
		final Point textSize = gc.getExtent(text, myFont);
		return textSize.x <= myBounds.width;
	}

	private void placeText(final GCProxy gc, final Rectangle output) {
		if (myCachedTextSize == null) {
			myCachedTextSize = gc.getExtent(myText, myFont);
		}

		final int centerX = myBounds.x + myBounds.width / 2;
		final int centerY = myBounds.y + myBounds.height / 2;

		final int x = centerX - myCachedTextSize.x / 2;
		final int y = centerY - myCachedTextSize.y / 2;

		output.setLocation(x, y);
		output.setSize(myCachedTextSize.x, myCachedTextSize.y);
	}

	public void setBounds(final int x, final int y, final int width, final int height) {
		myBounds.setLocation(x, y);
		myBounds.setSize(width, height);
	}

	public void setBounds(final Rectangle rectangle) {
		myBounds.setBounds(rectangle);
	}

	@Override
	public boolean setFont(final Font font) {
		final boolean changed = (myFont != font);
		if (changed) {
			myFont = font;
			myCachedTextSize = null;
		}
		return changed;
	}

	@Override
	public void setText(final String text) {
		myText = text;
		if (myText == null) {
			myText = "";
		}
		myCachedTextSize = null;
	}

}