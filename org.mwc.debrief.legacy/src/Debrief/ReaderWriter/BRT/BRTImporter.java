/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2018, Deep Blue C Technology Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package Debrief.ReaderWriter.BRT;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Assert;

import Debrief.Wrappers.SensorWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.Editable;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Tools.Action;
import MWC.TacticalData.NarrativeWrapper;
import junit.framework.TestCase;

public class BRTImporter
{

  public static class BRTImporterTest extends TestCase
  {

    static public final String TEST_ALL_TEST_TYPE = "UNIT";

    Layer[] allLayers = new Layer[]
    {new NarrativeWrapper("Test0"), new NarrativeWrapper("Test1"),
        new TrackWrapper("1", Color.BLUE), new NarrativeWrapper("Test2"),
        new TrackWrapper("2", Color.BLUE), new TrackWrapper("3", Color.RED)};

    public void findTrack()
    {
      final Layers testLayers = new Layers();
      for (final Layer l : allLayers)
      {
        testLayers.addThisLayer(l);
      }

      final BRTHelperHeadless headless = new BRTHelperHeadless(true, null, null,
          null, 0);

      BRTImporter importer = new BRTImporter(headless, testLayers);
      TrackWrapper selectedTrack;
      try
      {
        selectedTrack = importer.findTrack();
        assertEquals("Selecting first track, having several blue tracks",
            allLayers[2], selectedTrack);

        importer = new BRTImporter(new BRTHelperHeadless(true, null, null, null,
            1), testLayers);
        selectedTrack = importer.findTrack();
        assertEquals("Selecting first track, having several blue tracks",
            allLayers[4], selectedTrack);

        testLayers.clear();
        testLayers.addThisLayer(allLayers[0]);
        testLayers.addThisLayer(allLayers[4]);

        importer = new BRTImporter(headless, testLayers);
        selectedTrack = importer.findTrack();
        assertEquals("Selecting track from only one blue track", allLayers[4],
            selectedTrack);

        testLayers.clear();
        testLayers.addThisLayer(allLayers[0]);
        testLayers.addThisLayer(allLayers[4]);
        testLayers.addThisLayer(allLayers[5]);

        importer = new BRTImporter(headless, testLayers);
        selectedTrack = importer.findTrack();
        assertEquals("Selecting track from only one blue track", allLayers[4],
            selectedTrack);

        testLayers.clear();
        testLayers.addThisLayer(allLayers[0]);
        testLayers.addThisLayer(allLayers[5]);

        importer = new BRTImporter(headless, testLayers);
        selectedTrack = importer.findTrack();
        assertEquals("Selecting track from only one track", allLayers[5],
            selectedTrack);

      }
      catch (final Exception e)
      {
        Assert.fail(e.getMessage());
      }
    }

    public void testGetTracks()
    {
      final Layers testLayers = new Layers();
      for (final Layer l : allLayers)
      {
        testLayers.addThisLayer(l);
      }

      final BRTImporter importer = new BRTImporter(null, testLayers);
      final TrackWrapper[] tracks = importer.getTracks();
      assertEquals("getTrack getting TrackWrapper", allLayers[2], tracks[0]);
      assertEquals("getTrack getting TrackWrapper", allLayers[4], tracks[1]);
      assertEquals("getTrack getting TrackWrapper", allLayers[5], tracks[2]);
      assertEquals("amount of tracks extracted", 3, tracks.length);
    }

    public void testImport()
    {
      final String filename = "TestStringInputStreamFile";
      final String fileContent = "1263297600.000000, 69.00\n"
          + "1263297840.000000, 58.90\n" + "1263298080.000000, 56.70";

      final BRTHelperHeadless headless = new BRTHelperHeadless(true, null,
          Color.BLUE, null, 0);
      final Layers testLayers = new Layers();
      for (final Layer l : allLayers)
      {
        testLayers.addThisLayer(l);
      }

      final BRTImporter importer = new BRTImporter(headless, testLayers);
      try
      {
        importer.importThis(filename, new ByteArrayInputStream(fileContent
            .getBytes(StandardCharsets.UTF_8)));
      }
      catch (final Exception e)
      {
        Assert.fail(e.getMessage());
      }

      final BRTData data = importer.getBrtData();
      assertEquals("Size of bearing is correct", 3, data.getBearings().length);
      assertEquals("Size of time is correct", 3, data.getTimes().length);
      assertEquals("Reading first bearing correctly", 69.0, data
          .getBearings()[0], 0.001);
      assertEquals("Reading second bearing correctly", 58.9, data
          .getBearings()[1], 0.001);
      assertEquals("Reading third bearing correctly", 56.7, data
          .getBearings()[2], 0.001);

      assertEquals("Reading first date correctly", 1263297600000L, (long) data
          .getTimes()[0]);
      assertEquals("Reading first date correctly", 1263297840000L, (long) data
          .getTimes()[1]);
      assertEquals("Reading first date correctly", 1263298080000L, (long) data
          .getTimes()[2]);
    }
  }

  public static class ImportBRTAction implements Action
  {
    private final SensorWrapper sensorAdded;
    private final TrackWrapper track;

    public ImportBRTAction(final SensorWrapper sensorAdded,
        final TrackWrapper track)
    {
      super();
      this.sensorAdded = sensorAdded;
      this.track = track;
    }

    @Override
    public void execute()
    {
      track.add(sensorAdded);
    }

    @Override
    public boolean isRedoable()
    {
      return true;
    }

    @Override
    public boolean isUndoable()
    {
      return true;
    }

    @Override
    public void undo()
    {
      track.removeElement(sensorAdded);
    }

  }

  /**
   * layer that we are going to be load data to
   *
   */
  private final Layers _layers;

  /**
   * brt info given by user.
   */
  private final BRTHelper _brtHelper;

  private BRTData brtData;

  /**
   * Default Constructor
   *
   * @param hB
   *          Contains the information given by the user using the UI.
   * @param _layers
   *          layers available in the plot
   */
  public BRTImporter(final BRTHelper hB, final Layers _layers)
  {
    super();
    this._layers = _layers;
    this._brtHelper = hB;
  }

  private ImportBRTAction createImportAction(final String fName)
      throws Exception
  {
    // find track
    final TrackWrapper track = findTrack();
    final SensorWrapper sensor = new SensorWrapper(fName);
    // load data
    // determine color/cut-length to sensor
    // create sensor
    final ImportBRTAction action = new ImportBRTAction(sensor, track);
    // set default color
    // set array offset (if needed)
    // loadThese(Sensor…, color, cut-length)
    return action;
  }

  private TrackWrapper findTrack() throws Exception
  {
    return _brtHelper.select(getTracks());
  }

  public BRTData getBrtData()
  {
    return brtData;
  }

  /**
   * loop through layers, find tracks
   *
   * @param layers
   *          Layers to loop
   * @return Tracks available in the layers
   */
  private TrackWrapper[] getTracks()
  {
    final List<TrackWrapper> result = new ArrayList<>();

    final Enumeration<Editable> layerIterator = _layers.elements();

    while (layerIterator.hasMoreElements())
    {
      final Layer layer = (Layer) layerIterator.nextElement();
      if (TrackWrapper.class.isAssignableFrom(layer.getClass()))
      {
        result.add((TrackWrapper) layer);
      }
    }

    return result.toArray(new TrackWrapper[]
    {});
  }

  public ImportBRTAction importThis(final String fName, final InputStream is)
      throws Exception
  {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    int lineCounter = 0;

    final ArrayList<Long> times = new ArrayList<>();
    final ArrayList<Double> bearings = new ArrayList<>();
    while ((line = reader.readLine()) != null)
    {
      final String[] tokens = line.split(",");
      if (tokens.length != 2)
      {
        throw new Exception(
            "Wrong format in BRT file. Please consult the user manual");
      }
      final Long time = (long) (Double.parseDouble(tokens[0]) * 1000);
      final Double bearing = Double.parseDouble(tokens[1]);
      times.add(time);
      bearings.add(bearing);

      ++lineCounter;
    }

    brtData = new BRTData(times.toArray(new Long[]
    {}), bearings.toArray(new Double[]
    {}));

    System.out.println(lineCounter + " lines read successfully");

    // TODO.
    return createImportAction(fName);
  }
}
