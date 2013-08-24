/**
 * 
 */
package org.mwc.cmap.core.property_support;

import java.io.File;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public class FileNameHelper extends EditorHelper
{
	public static class FileDialogCellEditor extends DialogCellEditor
	{

		public FileDialogCellEditor(final Composite parent)
		{
			super(parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.DialogCellEditor#getDefaultLabel()
		 */
		protected Label getDefaultLabel()
		{
			return super.getDefaultLabel();
		}

		protected Object openDialogBox(final Control cellEditorWindow)
		{
			final FileDialog ftDialog = new FileDialog(cellEditorWindow.getShell(), SWT.SAVE);
			final File theFile = (File) getValue();
			if(theFile != null)
				ftDialog.setFileName(theFile.getAbsolutePath());
			final String thisFile = ftDialog.open();
			final File resFile = new File(thisFile);

			return resFile;
		}

	}

	public FileNameHelper()
	{
		super(File.class);
	}

	public CellEditor getCellEditorFor(final Composite parent)
	{
		final CellEditor editor = new FileDialogCellEditor(parent);
		return editor;
	}

	public ILabelProvider getLabelFor(final Object currentValue)
	{
		final ILabelProvider label1 = new LabelProvider()
		{
			public String getText(final Object element)
			{
				final File file = (File) element;
				return file.getAbsolutePath();
			}

			public Image getImage(final Object element)
			{
				final Image res = null;
				return res;
			}

		};
		return label1;
	}

	public Object translateToSWT(final Object value)
	{
		return value;
	}

	public Object translateFromSWT(final Object value)
	{
		return value;
	}

}