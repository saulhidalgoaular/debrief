//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.29 at 07:49:34 PM EDT 
//


package org.mwc.debrief.core.gpx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for chartReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="chartReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="font" type="{org.mwc.debrief.core}fontType" minOccurs="0"/>
 *         &lt;element name="colour" type="{org.mwc.debrief.core}colourType" minOccurs="0"/>
 *         &lt;element name="TopLeft" type="{org.mwc.debrief.core}locationType" minOccurs="0"/>
 *         &lt;element name="BottomRight" type="{org.mwc.debrief.core}locationType" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attribute name="FileName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="LabelLocation" type="{org.mwc.debrief.core}labelLocationType" />
 *       &lt;attribute name="LabelVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="Visible" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "chartReferenceType", propOrder = {

})
public class ChartReferenceType {

    protected FontType font;
    protected ColourType colour;
    @XmlElement(name = "TopLeft")
    protected LocationType topLeft;
    @XmlElement(name = "BottomRight")
    protected LocationType bottomRight;
    @XmlAttribute(name = "FileName")
    protected String fileName;
    @XmlAttribute(name = "Label")
    protected String label;
    @XmlAttribute(name = "LabelLocation")
    protected LabelLocationType labelLocation;
    @XmlAttribute(name = "LabelVisible")
    protected Boolean labelVisible;
    @XmlAttribute(name = "Visible")
    protected Boolean visible;

    /**
     * Gets the value of the font property.
     * 
     * @return
     *     possible object is
     *     {@link FontType }
     *     
     */
    public FontType getFont() {
        return font;
    }

    /**
     * Sets the value of the font property.
     * 
     * @param value
     *     allowed object is
     *     {@link FontType }
     *     
     */
    public void setFont(FontType value) {
        this.font = value;
    }

    /**
     * Gets the value of the colour property.
     * 
     * @return
     *     possible object is
     *     {@link ColourType }
     *     
     */
    public ColourType getColour() {
        return colour;
    }

    /**
     * Sets the value of the colour property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColourType }
     *     
     */
    public void setColour(ColourType value) {
        this.colour = value;
    }

    /**
     * Gets the value of the topLeft property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getTopLeft() {
        return topLeft;
    }

    /**
     * Sets the value of the topLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setTopLeft(LocationType value) {
        this.topLeft = value;
    }

    /**
     * Gets the value of the bottomRight property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getBottomRight() {
        return bottomRight;
    }

    /**
     * Sets the value of the bottomRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setBottomRight(LocationType value) {
        this.bottomRight = value;
    }

    /**
     * Gets the value of the fileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the value of the fileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the labelLocation property.
     * 
     * @return
     *     possible object is
     *     {@link LabelLocationType }
     *     
     */
    public LabelLocationType getLabelLocation() {
        return labelLocation;
    }

    /**
     * Sets the value of the labelLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LabelLocationType }
     *     
     */
    public void setLabelLocation(LabelLocationType value) {
        this.labelLocation = value;
    }

    /**
     * Gets the value of the labelVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLabelVisible() {
        if (labelVisible == null) {
            return true;
        } else {
            return labelVisible;
        }
    }

    /**
     * Sets the value of the labelVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLabelVisible(Boolean value) {
        this.labelVisible = value;
    }

    /**
     * Gets the value of the visible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVisible() {
        if (visible == null) {
            return true;
        } else {
            return visible;
        }
    }

    /**
     * Sets the value of the visible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisible(Boolean value) {
        this.visible = value;
    }

}