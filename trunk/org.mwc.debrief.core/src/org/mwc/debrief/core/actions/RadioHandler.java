package org.mwc.debrief.core.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.mwc.cmap.plotViewer.actions.Pan;
import org.mwc.cmap.plotViewer.actions.RangeBearing;
import org.mwc.cmap.plotViewer.actions.ZoomIn;

public class RadioHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (HandlerUtil.matchesRadioState(event))
			return null; 

		String currentState = event.getParameter(RadioState.PARAMETER_ID);

		if ("ZoomIn".equals(currentState))
		{
			new ZoomIn().execute(event);
		} else if ("Pan".equals(currentState))
		{
			new Pan().execute(event);
		} else if ("RangeBearing".equals(currentState))
		{
			new RangeBearing().execute(event);
		} else if ("DragFeature".equals(currentState))
		{
			new DragFeature().execute(event);
		} else if ("DragComponent".equals(currentState))
		{
			new DragComponent().execute(event);
		} else if ("DragSegment".equals(currentState))
		{
			new DragSegment().execute(event);
		}
		
		HandlerUtil.updateRadioState(event.getCommand(), currentState);
		return null;
	}

}