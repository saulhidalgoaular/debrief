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
package Debrief.ReaderWriter.FlatFile;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import Debrief.Wrappers.FixWrapper;
import Debrief.Wrappers.ShapeWrapper;
import Debrief.Wrappers.TrackWrapper;
import MWC.GUI.BaseLayer;
import MWC.GUI.Editable;
import MWC.GUI.ErrorLogger;
import MWC.GUI.Layer;
import MWC.GUI.Layers;
import MWC.GUI.Properties.DebriefColors;
import MWC.GUI.Shapes.EllipseShape;
import MWC.GUI.Tools.Action;
import MWC.GenericData.HiResDate;
import MWC.GenericData.WorldDistance;
import MWC.GenericData.WorldLocation;
import MWC.GenericData.WorldSpeed;
import MWC.TacticalData.Fix;
import MWC.Utilities.TextFormatting.FormatRNDateTime;
import MWC.Utilities.TextFormatting.GMTDateFormat;
import junit.framework.TestCase;

public class OTH_Importer
{

  private final static int YEAR_UNKNOWN = -1;

  private static interface ExtractValue<T extends Object>
  {
    T extract(String txt);
  }

  private static class ImportOTHAction implements Action
  {

    private final List<TrackWrapper> _tracks;
    private final List<BaseLayer> _ellipseLayers;
    private final Layers _layers;
    private List<Layer> _newlyAdded = new ArrayList<Layer>();

    public ImportOTHAction(final List<TrackWrapper> tracks,
        final List<BaseLayer> ellipseLayers, final Layers layers,
        final boolean importEllipses)
    {
      super();

      // prevent these objects from being null, to reduce null-checking when we execute
      _tracks = tracks != null ? tracks : Collections
          .<TrackWrapper> emptyList();
      _ellipseLayers = ellipseLayers != null && importEllipses ? ellipseLayers
          : Collections.<BaseLayer> emptyList();
      _layers = layers;
    }

    @Override
    public void execute()
    {
      for (final TrackWrapper t : _tracks)
      {
        TrackWrapper thisTrack = (TrackWrapper) _layers.findLayer(t.getName());
        if(thisTrack != null)
        {
          // ok, add the points to this existing track
          Enumeration<Editable> iter = t.getPositionIterator();
          while(iter.hasMoreElements())
          {
            FixWrapper next = (FixWrapper) iter.nextElement();
            thisTrack.addFix(next);
          }
        }
        else
        {
          _layers.addThisLayer(t);
          _newlyAdded.add(t);
        }
      }

      for (final BaseLayer b : _ellipseLayers)
      {
        BaseLayer thisLayer = (BaseLayer) _layers.findLayer(b.getName());
        if(thisLayer != null)
        {
          // ok, add the points to this existing track
          Enumeration<Editable> iter = b.elements();
          while(iter.hasMoreElements())
          {
            FixWrapper next = (FixWrapper) iter.nextElement();
            thisLayer.add(next);
          }
        }
        else
        {
          _layers.addThisLayer(b);
          _newlyAdded.add(b);
        }
      }
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
      for (final TrackWrapper t : _tracks)
      {
        if(_newlyAdded.contains(t))
        {
          _layers.removeThisLayer(t);
        }
        else
        {
          TrackWrapper track = (TrackWrapper) _layers.findLayer(t.getName());
          Enumeration<Editable> iter = t.getPositionIterator();
          while(iter.hasMoreElements())
          {
            track.removeElement(iter.nextElement());
          }
        }
      }

      for (final BaseLayer b : _ellipseLayers)
      {
        _layers.removeThisLayer(b);
        if(_newlyAdded.contains(b))
        {
          _layers.removeThisLayer(b);
        }
        else
        {
          TrackWrapper track = (TrackWrapper) _layers.findLayer(b.getName());
          Enumeration<Editable> iter = b.elements();
          while(iter.hasMoreElements())
          {
            track.removeElement(iter.nextElement());
          }
        }
      }
      
      // clear the list of newly created items, we'll re-generate on the next execute
      _newlyAdded.clear();
    }

  }

  public static class OTH_ImporterTest extends TestCase
  {
    static class Logger implements ErrorLogger
    {
      List<String> messages = new ArrayList<String>();

      private final boolean console = true;

      private void clear()
      {
        messages.clear();
      }

      private boolean isEmpty()
      {
        return messages.isEmpty();
      }

      private String last()
      {
        return messages.get(messages.size() - 1);
      }

      @Override
      public void logError(final int status, final String text,
          final Exception e)
      {
        output(text, e);
      }

      @Override
      public void logError(final int status, final String text,
          final Exception e, final boolean revealLog)
      {
        output(text, e);
      }

      @Override
      public void logStack(final int status, final String text)
      {
        output(text, null);
      }

      public void output(final String text, final Exception e)
      {
        messages.add(text);
        if (console)
        {
          System.out.println(text);
          if (e != null)
          {
            e.printStackTrace();
          }
        }
      }
    }

    static public final String TEST_ALL_TEST_TYPE = "UNIT";

    final String root = "../org.mwc.debrief.legacy/test_data/OTH_Import";

    Logger _logger = new Logger();

    public void setup()
    {
      _logger.clear();
    }

    public void testBadlyFormattedFields()
    {

      /*
       * unexpected units in the bagging area
       * 
       */
      _logger.clear();
      assertEquals("not T markers", 0d, courseFor(
          "POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM/300a/2K/",
          _logger));
      assertEquals("correct message",
          "Failed to parse course:For input string: \"300a\"", _logger.last());

      _logger.clear();
      assertEquals("got speed", 0d, speedFor(
          "POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM/200T/2MS/",
          _logger));
      assertEquals("correct message",
          "Failed to parse speed:For input string: \"2MS\"", _logger.last());

    }

    public void testCanLoad()
    {
      assertFalse("doesn't load dodgy file", canLoad(root + "/really_bad.txt",
          new Logger()));
      assertTrue("loads valid file", canLoad(root + "/valid.txt",
          new Logger()));

      // this time, verify the message
      assertFalse("missing pos", canLoad(root + "/missing_pos.txt", _logger));
      assertEquals("correct error message",
          "OTH Import rejecting file, Header:true Track:true Pos:false", _logger
              .last());
    }

    public void testGoodLoad() throws Exception
    {
      OTH_Importer importer = new OTH_Importer();
      Layers layers = new Layers();

      OTH_Helper brtHelper = new OTH_Helper_Headless(true);
      InputStream is = new FileInputStream(root + "/valid.txt");
      ImportOTHAction action = importer.importThis(brtHelper, is, layers, _logger);
      action.execute();
      
      assertEquals("has data", 2, layers.size());

      TrackWrapper track = (TrackWrapper) layers.elementAt(0);
      BaseLayer ellipses = (BaseLayer) layers.elementAt(1);
     
      assertEquals("correct fixes",3, track.numFixes());
      assertEquals("correct ellipses",3, ellipses.size());
      
      // and undo it
      action.undo();
      
      assertEquals("has data", 0, layers.size());
    }
    
    public void testTrackAlreadyPresent() throws Exception
    {
      OTH_Importer importer = new OTH_Importer();
      Layers layers = new Layers();

      TrackWrapper existing_track = new TrackWrapper();
      existing_track.setName("TYPE 12-HOOD");
      existing_track.addFix(new FixWrapper(new Fix(new HiResDate(10000000), new WorldLocation(33,4,0d), 33, 22)));
      layers.addThisLayer(existing_track);
      assertEquals("just one position",1, existing_track.numFixes());
      
      OTH_Helper brtHelper = new OTH_Helper_Headless(true);
      InputStream is = new FileInputStream(root + "/valid.txt");
      ImportOTHAction action = importer.importThis(brtHelper, is, layers, _logger);
      action.execute();
      
      assertEquals("has data", 2, layers.size());

      TrackWrapper track = (TrackWrapper) layers.elementAt(0);
      BaseLayer ellipses = (BaseLayer) layers.elementAt(1);
     
      assertEquals("correct fixes",4, track.numFixes());
      assertEquals("correct ellipses",3, ellipses.size());
      
      // and undo it
      action.undo();
      
      assertEquals("has data", 1, layers.size());
      assertEquals("just one position",1, existing_track.numFixes());
    }

    public void testGetLocation()
    {
      assertEquals("got location", " 46°12'00.00\"N 021°22'00.00\"E ",
          locationFrom(
              "POS/112313Z1/AUG/4612N34/02122E7//170T/11NM/13NM/000T/0K/",
              _logger).toString());

      assertEquals("got location", " 22°11'00.00\"N 021°42'00.00\"W ",
          locationFrom(
              "POS/120502Z0/DEC/2211N6/02142W9///170T/11NM/13NM/000T/0K/",
              _logger).toString());
    }

//    public void testParseDate() throws ParseException
//    {
//      // 112313Z1/AUG
//
////      DateFormat df = new GMTDateFormat("ddHHmm");
////      assertEquals("correct date", "Sun Jan 11 23:13:00 GMT 1970", df.format(df.parse(
////          "112313")));
////
////      DateFormat df2 = new GMTDateFormat("ddHHmm'Z'");
////      assertEquals("correct date", "Sun Jan 11 23:13:00 GMT 1970", df2.parse(
////          "112313Z").toString());
////
////      DateFormat df3 = new GMTDateFormat("ddHHmm'Z'MMM");
////      assertEquals("correct date", "Tue Aug 11 23:13:00 GMT 1970", df3.parse(
////          "112313ZAUG").toString());
////
////      DateFormat df4 = new GMTDateFormat("ddHHmm'Z'MMMyy");
////      assertEquals("correct date", "Sat Aug 11 23:13:00 BST 2018", df4.parse(
////          "112313ZAUG18").toString());
//
//      assertEquals("correct date", "Fri Aug 11 21:13:00 BST 2017", dateFor(
//          "POS/112113Z1/AUG/4612N34/02122W7//170T/11NM/13NM/000T/0K/", _logger,
//          17).getDate().toString());
//
//      assertEquals("correct date", "Sun Aug 11 21:13:00 BST 2019", dateFor(
//          "POS/112113Z1/AUG/4612N34/02122W7//170T/11NM/13NM/000T/0K/", _logger,
//          YEAR_UNKNOWN).getDate().toString());
//
//      assertEquals("correct date", "Sun Nov 11 21:13:00 GMT 2018", dateFor(
//          "POS/112113Z1/NOV/4612N34/02122W7//170T/11NM/13NM/000T/0K/", _logger,
//          YEAR_UNKNOWN).getDate().toString());
//
//      assertEquals("correct date", null, dateFor(
//          "POS/112113Z1/NaV/4612N34/02122W7//170T/11NM/13NM/000T/0K/", _logger,
//          YEAR_UNKNOWN));
//
//    }

    public void testParseYear()
    {
      _logger.clear();
      assertEquals("good year", 19, parseYear(_logger,
          "some string more string 19", YEAR_UNKNOWN));
      assertTrue("empty logger", _logger.isEmpty());

      _logger.clear();
      assertEquals("bad year", YEAR_UNKNOWN, parseYear(_logger,
          "some string more string 1a9", YEAR_UNKNOWN));
      assertEquals("correct message",
          "Failed to extract year from last token in:some string more string 1a9",
          _logger.last());

      _logger.clear();
      assertEquals("bad year", YEAR_UNKNOWN, parseYear(_logger, "",
          YEAR_UNKNOWN));
      assertEquals("correct message",
          "Failed to extract year from empty first line",
          _logger.last());

    }

    public void testGetName()
    {
      assertEquals("got good name", "TYPE 12-HOOD", nameFrom(
          "CTC/12345AB6/TYPE 12-HOOD//DDGH/NAV/D 32/UK/"));
      assertNull("not got name", nameFrom("CTC/12345AB6"));
    }

    public void testGoodFields()
    {

      assertEquals("got course", Math.toRadians(300d), courseFor(
          "POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM/300T/2K/",
          _logger));
      assertEquals("got speed", new WorldSpeed(2, WorldSpeed.Kts).getValueIn(
          WorldSpeed.ft_sec) / 3d, speedFor(
              "POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM/200T/2K/",
              _logger));
    }

    public void testInsufficientFields()
    {
      /*
       * missing fields
       * 
       */
      _logger.clear();
      assertEquals("not got speed", 0d, speedFor(
          "POS/112313Z1/AUG/4612N34/02122W7", _logger));
      assertEquals("correct message",
          "Insufficient fields in POS line:POS/112313Z1/AUG/4612N34/02122W7",
          _logger.last());

      _logger.clear();
      assertEquals("not got course", 0d, courseFor(
          "POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM", _logger));
      assertEquals("correct message",
          "Insufficient fields in POS line:POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM",
          _logger.last());
    }
  }

  private static final String HEADER_STR = "MSGID";

  private static final String TRACK_STR = "CTC";

  private static final String POS_STR = "POS";

  public static boolean canLoad(final String fileName, final ErrorLogger logger)
  {
    boolean res = false;
    BufferedReader r = null;
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(fileName);
      r = new BufferedReader(new InputStreamReader(fis));
      boolean hasHeader = false;
      boolean hasPosition = false;
      boolean hasTrack = false;

      final int MAX_LINES = 100;
      final int ctr = 0;
      while (ctr < MAX_LINES && !(hasHeader && hasPosition && hasTrack))
      {
        // try this line
        final String line = r.readLine();

        if (line == null)
        {
          break;
        }

        if (!hasHeader)
        {
          hasHeader = line.startsWith(HEADER_STR);
        }
        if (!hasPosition)
        {
          hasPosition = line.startsWith(POS_STR);
        }
        if (!hasTrack)
        {
          hasTrack = line.startsWith(TRACK_STR);
        }

        // are we ready?
        if (hasHeader && hasPosition && hasTrack)
        {
          res = true;
          break;
        }
      }

      if (!res)
      {
        logger.logError(ErrorLogger.INFO, "OTH Import rejecting file, Header:"
            + hasHeader + " Track:" + hasTrack + " Pos:" + hasPosition, null);
      }

    }
    catch (final Exception e)
    {
      logger.logError(ErrorLogger.ERROR, "Trouble whilst checking valid OTH",
          e);
    }
    finally
    {
      try
      {
        if (r != null)
          r.close();
        if (fis != null)
          fis.close();
      }
      catch (final IOException e)
      {
        logger.logError(ErrorLogger.ERROR, "Couldn't close file:" + fileName,
            e);
      }
    }
    return res;
  }

  private static double courseFor(final String line, final ErrorLogger logger)
  {
    Double res = getField(line, logger, "T", 9, "course",
        new ExtractValue<Double>()
        {

          @Override
          public Double extract(final String txt)
          {
            final double courseDegs = Double.parseDouble(txt);
            return Math.toRadians(courseDegs);
          }
        });
    return res != null ? res : 0d;
  }

  private static ImportOTHAction createImportAction(final OTH_Helper helper,
      final OTH_Data brtData, final Layers layers)
  {
    // are we loading ellipses?
    final boolean doEllipses = helper.generateEllipses();

    return new ImportOTHAction(brtData.getTracks(), brtData.getEllipseLayers(),
        layers, doEllipses);
  }

  private static HiResDate dateFor(final String line, final ErrorLogger logger,
      final int year)
  {
    final String[] tokens = line.split("/");
    HiResDate res = null;
    if (tokens.length >= 3)
    {
      try
      {

        final String dateStr = tokens[1].substring(0, 7);
        final String monStr = tokens[2];

        // sort out the year
        final int useYear;
        if (year == YEAR_UNKNOWN)
        {
          // ok. is the month before or after this one?
          DateFormat dm2 = new GMTDateFormat("MMMyy");
          final int thisYear = LocalDate.now().getYear() - 2000;
          Date monDate = dm2.parse(monStr + thisYear);
          if (monDate.getTime() > new Date().getTime())
          {
            // ok, later in the year. use previous year
            useYear = thisYear - 1;
          }
          else
          {
            useYear = thisYear;
          }
        }
        else
        {
          useYear = year;
        }

        final String wholeStr = dateStr + monStr + useYear;
        DateFormat df = new GMTDateFormat("ddHHmm'Z'MMMyy");
        Date date = df.parse(wholeStr);
        res = new HiResDate(date);
      }
      catch (final ParseException fe)
      {
        logger.logError(ErrorLogger.ERROR, "Failed to parse date:" + fe
            .getMessage(), null);
      }
    }
    else
    {
      logger.logError(ErrorLogger.WARNING, "Insufficient fields in POS line:"
          + line, null);
    }
    return res;
  }

  public static TrackWrapper findTrack(final TrackWrapper[] allTracks)
  {
    if (allTracks.length == 1)
    {
      return allTracks[0];
    }
    int amountOfBlueTracks = 0;
    int indexOfBlueTrack = 0;
    for (int i = 0; i < allTracks.length; i++)
    {
      if (DebriefColors.BLUE.equals(allTracks[i].getTrackColor()))
      {
        ++amountOfBlueTracks;
        indexOfBlueTrack = i;
      }
    }
    if (amountOfBlueTracks == 1)
    {
      return allTracks[indexOfBlueTrack];
    }
    return null;
  }

  private static <T> T getField(final String line, final ErrorLogger logger,
      final String separator, final int tokenId, final String fieldName,
      final ExtractValue<T> extractor)
  {
    final String[] tokens = line.split("/");
    T res = null;
    if (tokens.length > tokenId)
    {
      try
      {
        final String courseStr = tokens[tokenId];
        final String[] innerTokens = courseStr.split(separator);
        if(innerTokens.length > 0)
        {
          res = extractor.extract(innerTokens[0]);
        }
      }
      catch (final NumberFormatException fe)
      {
        logger.logError(ErrorLogger.ERROR, "Failed to parse " + fieldName + ":"
            + fe.getMessage(), null);
      }
    }
    else
    {
      logger.logError(ErrorLogger.WARNING, "Insufficient fields in POS line:"
          + line, null);
    }
    return res;
  }

  /**
   * loop through layers, find tracks
   *
   * @param layers
   *          Layers to loop
   * @return Tracks available in the layers
   */
  public static TrackWrapper[] getTracks(final Layers layers)
  {
    final List<TrackWrapper> result = new ArrayList<>();

    final Enumeration<Editable> layerIterator = layers.elements();

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

  private static double latFor(final String string) throws NumberFormatException
  {
    // 4612N4
    final double degs = Double.parseDouble(string.substring(0, 2));
    final double mins = Double.parseDouble(string.substring(2, 4));
    final String hemi = string.substring(4, 5);
    final double hemiVal = "N".equals(hemi) ? 1d : -1d;
    return hemiVal * (degs + mins / 60d);
  }

  private static WorldLocation locationFrom(final String line,
      final ErrorLogger logger)
  {
    // POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM/000T/0K/

    WorldLocation res = null;

    final String[] tokens = line.split("/");
    if (tokens.length >= 5)
    {
      try
      {
        final double dLat = latFor(tokens[3]);
        final double dLong = longFor(tokens[4]);
        res = new WorldLocation(dLat, dLong, 0d);
      }
      catch (final NumberFormatException fe)
      {
        logger.logError(ErrorLogger.ERROR, "Failed to parse location:" + fe
            .getMessage(), null);
      }
    }
    else
    {
      logger.logError(ErrorLogger.WARNING, "Insufficient fields in POS line:"
          + line, null);
    }

    return res;
  }

  private static double longFor(final String string)
  {
    // 4612N34
    final double degs = Double.parseDouble(string.substring(0, 3));
    final double mins = Double.parseDouble(string.substring(3, 5));
    final String hemi = string.substring(5, 6);
    final double hemiVal = "E".equals(hemi) ? 1d : -1d;
    return hemiVal * (degs + mins / 60d);
  }

  private static String nameFrom(final String line)
  {
    final String[] tokens = line.split("/");
    if (tokens.length >= 3)
    {
      return tokens[2];
    }
    else
    {
      return null;
    }
  }

  private static EllipseShape produceEllipse(final ErrorLogger logger,
      final String line, final HiResDate thisDate, final WorldLocation origin)
  {
    // POS/112313Z1/AUG/4612N34/02122W7//170T/11NM/13NM/300T/2K/"
    
    // ok, get the orientation
    Double orient = getField(line, logger, "T", 6, "Orientation", new ExtractValue<Double>() {

      @Override
      public Double extract(String txt)
      {
        return Double.parseDouble(txt);
      }});

    Double maxima = getField(line, logger, "N", 7, "Maxima", new ExtractValue<Double>() {

      @Override
      public Double extract(String txt)
      {
        return Double.parseDouble(txt);
      }});

    Double minima = getField(line, logger, "N", 8, "Minima", new ExtractValue<Double>() {

      @Override
      public Double extract(String txt)
      {
        return Double.parseDouble(txt);
      }});
    
    EllipseShape shp;
    if(orient != null && maxima != null && minima != null)
    {
      shp = new EllipseShape(origin, orient, new WorldDistance(maxima, WorldDistance.NM),  new WorldDistance(minima, WorldDistance.NM));
    }
    else
    {
      shp = null;
    }
    
    return shp;
  }

  public static FixWrapper produceFix(final ErrorLogger logger,
      final String line, final int year)
  {
    final WorldLocation loc = locationFrom(line, logger);
    final HiResDate date = dateFor(line, logger, year);
    final double courseRads = courseFor(line, logger);
    final double speedYps = speedFor(line, logger);
    final Fix fix = new Fix(date, loc, courseRads, speedYps);
    final FixWrapper wrapped = new FixWrapper(fix);
    return wrapped;
  }

  private static OTH_Data read_OTH(final InputStream is,
      final ErrorLogger logger) throws IOException
  {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    String line;

    int year = YEAR_UNKNOWN;
    int ctr = 0;

    final List<TrackWrapper> tracks = new ArrayList<TrackWrapper>();
    final List<BaseLayer> ellipseLayers = new ArrayList<BaseLayer>();

    TrackWrapper thisTrack = null;
    BaseLayer thisLayer = null;

    while ((line = reader.readLine()) != null)
    {
      // if it's the first line, look for a date
      if (ctr == 0)
      {
        year = parseYear(logger, line, year);
      }

      // looking for new track
      if (line.startsWith(TRACK_STR))
      {
        // is there already a track?
        thisTrack = tidyTrack(tracks, thisTrack);

        // is there already a layer
        thisLayer = tidyLayer(ellipseLayers, thisLayer);

        // ok, time to move on.

        final String trackName = nameFrom(line);

        if (trackName == null)
        {
          logger.logError(ErrorLogger.ERROR, "Failed to get track name from:"
              + line, null);
        }
        else
        {
          thisTrack = new TrackWrapper();
          thisTrack.setName(trackName);
          thisLayer = new BaseLayer();
          thisLayer.setName(trackName + " TUAs");
        }
      }
      else if (line.startsWith(POS_STR))
      {
        HiResDate thisDate = null;
        WorldLocation origin = null;
        
        // ok, generate a position
        final FixWrapper wrapped = produceFix(logger, line, year);
        if (wrapped != null)
        {
          thisTrack.addFix(wrapped);
          thisDate = wrapped.getDateTimeGroup();
          origin = wrapped.getLocation();
        }
        

        // also generate an ellipse
        final EllipseShape ellipse = produceEllipse(logger, line, thisDate, origin);
        if (ellipse != null)
        {
          String label = FormatRNDateTime.toMediumString(thisDate.getDate().getTime());
          ShapeWrapper sw = new ShapeWrapper(label, ellipse, Color.red, thisDate);
          thisLayer.add(sw);
        }
      }

      ctr++;
    }

    // tidy any pending items
    tidyTrack(tracks, thisTrack);
    tidyLayer(ellipseLayers, thisLayer);



    final OTH_Data brtData = new OTH_Data(tracks, ellipseLayers);

    return brtData;
  }

  public static BaseLayer tidyLayer(final List<BaseLayer> ellipseLayers,
      BaseLayer thisLayer)
  {
    if (thisLayer != null)
    {
      if (thisLayer.size() > 0)
      {
        ellipseLayers.add(thisLayer);
      }
      thisLayer = null;
    }
    return thisLayer;
  }

  public static TrackWrapper tidyTrack(final List<TrackWrapper> tracks,
      TrackWrapper thisTrack)
  {
    if (thisTrack != null)
    {
      // ok, store it, it has contents
      if (thisTrack.getPositionIterator().hasMoreElements())
      {
        tracks.add(thisTrack);
      }
      thisTrack = null;
    }
    return thisTrack;
  }

  private static int parseYear(final ErrorLogger logger, String line, final int year)
  {
    int res = YEAR_UNKNOWN;
    final String[] tokens = line.split(" ");
    if (tokens.length <= 1)
    {
      logger.logError(ErrorLogger.WARNING,
          "Failed to extract year from empty first line", null);
    }
    else
    {
      final String lastToken = tokens[tokens.length - 1];
      try
      {
        final int yr = Integer.parseInt(lastToken);
        if (yr > 0 && yr < 100)
        {
          res = yr;
        }
      }
      catch (final NumberFormatException ne)
      {
        logger.logError(ErrorLogger.WARNING,
            "Failed to extract year from last token in:" + line, null);
      }
    }
    return res;
  }

  private static double speedFor(final String line, final ErrorLogger logger)
  {
    final Double res = getField(line, logger, "K", 10, "speed",
        new ExtractValue<Double>()
        {
          @Override
          public Double extract(final String txt)
          {
            final double speedKts = Double.parseDouble(txt);
            return new WorldSpeed(speedKts, WorldSpeed.Kts).getValueIn(
                WorldSpeed.ft_sec) / 3;
          }
        });

    return res != null ? res : 0d;
  }

  /**
   * Default Constructor
   *
   * @param hB
   *          Contains the information given by the user using the UI.
   * @param _layers
   *          layers available in the plot
   */
  public OTH_Importer()
  {
    super();
  }

  public ImportOTHAction importThis(final OTH_Helper brtHelper,
      final InputStream is, final Layers layers, final ErrorLogger logger)
      throws Exception
  {
    final OTH_Data brtData = read_OTH(is, logger);

    return createImportAction(brtHelper, brtData, layers);
  }
}
