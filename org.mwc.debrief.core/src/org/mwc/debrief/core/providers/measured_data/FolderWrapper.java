package org.mwc.debrief.core.providers.measured_data;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.mwc.debrief.core.providers.MeasuredDataProvider;

import Debrief.Wrappers.Extensions.Measurements.DataFolder;
import MWC.GUI.Editable;
import MWC.GUI.FireExtended;
import MWC.GUI.HasEditables;

public class FolderWrapper implements Editable, HasEditables, Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  final private DataFolder _folder;

  public FolderWrapper(final DataFolder folder)
  {
    _folder = folder;
  }

  @Override
  public Enumeration<Editable> elements()
  {
    final Vector<Editable> res = new Vector<Editable>();

    // get the folder contents
    final List<Editable> items = MeasuredDataProvider.getItemsFor(_folder);

    // put into our vector
    res.addAll(items);

    return res.elements();
  }

  @Override
  public EditorType getInfo()
  {
    return null;
  }

  @Override
  public String getName()
  {
    return _folder.getName();
  }

  @Override
  public boolean hasEditor()
  {
    return false;
  }

  @Override
  public boolean hasOrderedChildren()
  {
    return false;
  }

  @Override
  public String toString()
  {
    return getName() + " (" + _folder.size() + " items)";
  }

  @Override
  @FireExtended
  public void add(Editable item)
  {
    if (item instanceof FolderWrapper)
    {
      FolderWrapper df = (FolderWrapper) item;
      _folder.add(df._folder);
    }
    else if (item instanceof DatasetWrapper)
    {
      DatasetWrapper dw = (DatasetWrapper) item;
      _folder.add(dw._data);
    }
  }

  @Override
  @FireExtended
  public void removeElement(Editable item)
  {
    if (item instanceof FolderWrapper)
    {
      FolderWrapper df = (FolderWrapper) item;
      _folder.remove(df._folder);
    }
    else if (item instanceof DatasetWrapper)
    {
      DatasetWrapper dw = (DatasetWrapper) item;
      _folder.remove(dw._data);
    }
  }
}