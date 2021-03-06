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

//--------------------------------------------------------------------------------------------------
// Copyright (c) 1996, 1996 Borland nternational, nc. All Rights Reserved.
//--------------------------------------------------------------------------------------------------

package MWC.GUI.SplitPanel;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/*
class PaneLayoutDivider extends Canvas
{
  public PaneLayoutDivider() {
    //setBackground(Color.red);
  }

  public void update(Graphics g) {
    //System.err.println("PaneLayoutDivider update");
  }
}
 */
public class SplitPanel extends BeanPanel implements MouseListener, MouseMotionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	PaneLayout layout = new PaneLayout();
	// PaneLayoutDivider paneLayoutDivider = new PaneLayoutDivider();
	Canvas paneLayoutDivider = new Canvas();
	Cursor cursor;
	boolean yChanges;
	int xDelta;
	int yDelta;
	private Rectangle dividerRect; // shape & location of the divider that is being dragged
	private Rectangle dividerBounds; // the bounds of the area that the divider can go
	boolean isSizing = false; // flag so drag operation knows if it was started on a divider
	Component enabledComponents[]; // contains list of components that where disabled during dragging operation

	/**
	 * default constructor- creates and hides the divider, sets its color to black
	 * sets the layout of the panel to PaneLayout sets the PaneLayout gap to 2
	 * listens for panel mouse events
	 */
	public SplitPanel() {
		setBackground(SystemColor.control); // IM@ experimenting
		add(paneLayoutDivider); // the layout will not know about this guy
		paneLayoutDivider.setVisible(false);
		paneLayoutDivider.setEnabled(false);
		setDividerColor(Color.black);
		layout.setGap(2);
		addMouseListener(this);
		addMouseMotionListener(this);
		super.setLayout(layout);
	}

	public Color getDividerColor() {
		return paneLayoutDivider.getBackground();
	}

	public int getGap() {
		return layout.getGap();
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension ps = super.getPreferredSize();
		if (ps.width == 10)
			ps.width = 100;
		if (ps.height == 10)
			ps.height = 100;
		return ps;
	}

	/**
	 * (Internal) Mouse Listener Interface method - no-op
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	/**
	 * (Internal) Mouse Motion Listener Interface method - move the selection bar to
	 * track the mouse
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		if (isSizing) {
			int xD = dividerRect.x;
			int yD = dividerRect.y;
			if (yChanges) {
				yD = e.getY() - yDelta;
				if (yD < dividerBounds.y)
					yD = dividerBounds.y;
				else if (yD > dividerBounds.height + dividerBounds.y - 1)
					yD = dividerBounds.height + dividerBounds.y - 1;
			} else {
				xD = e.getX() - xDelta;
				if (xD > dividerBounds.width + dividerBounds.x - 1)
					xD = dividerBounds.width + dividerBounds.x - 1;
				else if (xD < dividerBounds.x)
					xD = dividerBounds.x;
			}
			layout.dragDivider(xD, yD);
			dividerRect.x = xD;
			dividerRect.y = yD;
			paneLayoutDivider.setLocation(xD, yD);
		}
	}

	/**
	 * (Internal) Mouse Listener Interface method - remember cursor shape
	 */
	@Override
	public void mouseEntered(final MouseEvent e) {
		// System.err.println("mouseEntered");
		cursor = getCursor(); // remember cursor state
	}

	/**
	 * (Internal) Mouse Listener Interface method - restore cursor shape
	 */
	@Override
	public void mouseExited(final MouseEvent e) {
		// System.err.println("mouseExited");
		if (cursor != null)
			setCursor(cursor); // restore cursor state
	}

	/**
	 * (Internal) Change the cursor when the mouse is over a divider Mouse Motion
	 * Listener Interface method
	 */
	@Override
	public void mouseMoved(final MouseEvent e) {
		if (e.getComponent() != this)
			return;
		final Rectangle rect = layout.getDividerRect(e.getX(), e.getY());
		if (rect != null) {
			if (rect.width > rect.height)
				setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			else
				setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * (Internal) display the selection bar if user clicks on one disable all the
	 * panels components until mouse is released (so it is easy to follow the
	 * divider) Mouse Listener Interface method
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getComponent() != this)
			return;
		final int mouseX = e.getX();
		final int mouseY = e.getY();
		dividerRect = layout.getDividerRect(mouseX, mouseY);
		if (dividerRect != null) {
			dividerBounds = layout.getDividerBounds();
			if (dividerRect.width > dividerRect.height) {
				yChanges = true;
				yDelta = mouseY - dividerRect.y;
			} else {
				yChanges = false;
				xDelta = mouseX - dividerRect.x;
			}
			isSizing = true;
			final Component[] all = getComponents();
			enabledComponents = new Component[all.length];
			for (int i = 0; i < all.length; i++) {
				if (all[i].isEnabled()) {
					enabledComponents[i] = all[i];
					all[i].setEnabled(false);
				}
			}
			paneLayoutDivider.setBounds(dividerRect.x, dividerRect.y, dividerRect.width, dividerRect.height);
			paneLayoutDivider.setVisible(true);
			// System.err.println("mouse pressed");
			// if (!e.isShiftDown())
			// layout.deselectAll();
			// layout.select(e.isShiftDown(), mouseX, mouseY);
		}
	}

	/**
	 * re-enable components that where disabled. hide the divider (Internal) Mouse
	 * Listener Interface method
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		paneLayoutDivider.setVisible(false);
		if (isSizing) {
			isSizing = false;
			for (int i = 0; i < enabledComponents.length; i++) {
				if (enabledComponents[i] != null)
					enabledComponents[i].setEnabled(true);
			}
		}
		// if (!e.isShiftDown())
		// layout.deselectAll();
		// layout();
		validate();
	}

	/**
	 * set the color of the Pane divider default is Black
	 *
	 */
	public void setDividerColor(final Color color) {
		paneLayoutDivider.setBackground(color);
	}

	public void setGap(final int gap) {
		layout.setGap(gap);
	}

	/**
	 * No-op if LayoutManager is not a PaneLayout otherwise sets the LayoutManager
	 * to the supplied PaneLayout
	 */
	@Override
	public void setLayout(final LayoutManager mgr) {
		if (mgr instanceof PaneLayout) {
			layout = (PaneLayout) mgr;
			super.setLayout(mgr);
		}
//    else
//      throw new IllegalArgumentException(Res.getString(Res.PaneLayoutOnly));
	}
}
