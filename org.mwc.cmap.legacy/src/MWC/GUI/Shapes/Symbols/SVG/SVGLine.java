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
package MWC.GUI.Shapes.Symbols.SVG;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.w3c.dom.Element;

import MWC.GUI.CanvasType;

public class SVGLine extends SVGElement
{

  private double _x1;

  private double _y1;

  private double _x2;

  private double _y2;

  public SVGLine(Element dom)
  {
    super(dom);
    try
    {
      _x1 = Double.parseDouble(getDom().getAttribute("x1"));
      _y1 = Double.parseDouble(getDom().getAttribute("y1"));
      _x2 = Double.parseDouble(getDom().getAttribute("x2"));
      _y2 = Double.parseDouble(getDom().getAttribute("y2"));
    }
    catch (Exception e)
    {
      MWC.Utilities.Errors.Trace.trace("Invalid SVG Format");
    }
  }

  @Override
  public void render(CanvasType dest, double sym_size, Point origin_coords,
      double rotation_degs, final java.awt.Point rotationPoint)
  {
 // We want the icon to be aligned with the track
    rotation_degs += 90.0 / 180.0 * Math.PI;

    // Lets assume that the viewbox is 0 0 100 100
    double magnitude = Math.sqrt(100 * 100 + 100 * 100);

    // centering and scaling to 1.0
    
    Point2D firstPoint = new Point2D.Double((_x1 - rotationPoint.x)
        / magnitude * wid,  (_y1 - rotationPoint.y) / magnitude * wid);
    Point2D secondPoint = new Point2D.Double((_x2 - rotationPoint.x)
        / magnitude * wid,  (_y2 - rotationPoint.y) / magnitude * wid);

    final AffineTransform thisRotation = AffineTransform.getRotateInstance(
        rotation_degs, 0, 0);

    thisRotation.transform(firstPoint, firstPoint);
    thisRotation.transform(secondPoint, secondPoint);
    
    final double x1 = firstPoint.getX() * sym_size + origin_coords.getX();
    final double y1 = firstPoint.getY() * sym_size + origin_coords.getY();
    final double x2 = secondPoint.getX() * sym_size + origin_coords.getX();
    final double y2 = secondPoint.getY() * sym_size + origin_coords.getY();

    dest.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
  }

}
