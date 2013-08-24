//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.30 at 10:32:43 PM EDT 
//


package org.mwc.debrief.core.gpx;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for locationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="locationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="shortLocation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="Depth" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="Lat" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="Long" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="longLocation">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="LatDeg" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="LatMin" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="LatSec" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="LatHem" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="LongDeg" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *                 &lt;attribute name="LongMin" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="LongSec" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="LongHem" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *                 &lt;attribute name="Depth" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "locationType", propOrder = {
    "shortLocation",
    "longLocation"
})
public class LocationType {

    protected LocationType.ShortLocation shortLocation;
    protected LocationType.LongLocation longLocation;

    /**
     * Gets the value of the shortLocation property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType.ShortLocation }
     *     
     */
    public LocationType.ShortLocation getShortLocation() {
        return shortLocation;
    }

    /**
     * Sets the value of the shortLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType.ShortLocation }
     *     
     */
    public void setShortLocation(final LocationType.ShortLocation value) {
        this.shortLocation = value;
    }

    /**
     * Gets the value of the longLocation property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType.LongLocation }
     *     
     */
    public LocationType.LongLocation getLongLocation() {
        return longLocation;
    }

    /**
     * Sets the value of the longLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType.LongLocation }
     *     
     */
    public void setLongLocation(final LocationType.LongLocation value) {
        this.longLocation = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="LatDeg" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="LatMin" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="LatSec" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="LatHem" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="LongDeg" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
     *       &lt;attribute name="LongMin" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="LongSec" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="LongHem" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="Depth" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class LongLocation {

        @XmlAttribute(name = "LatDeg", required = true)
        protected BigInteger latDeg;
        @XmlAttribute(name = "LatMin", required = true)
        protected double latMin;
        @XmlAttribute(name = "LatSec", required = true)
        protected double latSec;
        @XmlAttribute(name = "LatHem", required = true)
        protected double latHem;
        @XmlAttribute(name = "LongDeg", required = true)
        protected BigInteger longDeg;
        @XmlAttribute(name = "LongMin", required = true)
        protected double longMin;
        @XmlAttribute(name = "LongSec", required = true)
        protected double longSec;
        @XmlAttribute(name = "LongHem", required = true)
        protected double longHem;
        @XmlAttribute(name = "Depth", required = true)
        protected double depth;

        /**
         * Gets the value of the latDeg property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getLatDeg() {
            return latDeg;
        }

        /**
         * Sets the value of the latDeg property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setLatDeg(final BigInteger value) {
            this.latDeg = value;
        }

        /**
         * Gets the value of the latMin property.
         * 
         */
        public double getLatMin() {
            return latMin;
        }

        /**
         * Sets the value of the latMin property.
         * 
         */
        public void setLatMin(final double value) {
            this.latMin = value;
        }

        /**
         * Gets the value of the latSec property.
         * 
         */
        public double getLatSec() {
            return latSec;
        }

        /**
         * Sets the value of the latSec property.
         * 
         */
        public void setLatSec(final double value) {
            this.latSec = value;
        }

        /**
         * Gets the value of the latHem property.
         * 
         */
        public double getLatHem() {
            return latHem;
        }

        /**
         * Sets the value of the latHem property.
         * 
         */
        public void setLatHem(final double value) {
            this.latHem = value;
        }

        /**
         * Gets the value of the longDeg property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getLongDeg() {
            return longDeg;
        }

        /**
         * Sets the value of the longDeg property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setLongDeg(final BigInteger value) {
            this.longDeg = value;
        }

        /**
         * Gets the value of the longMin property.
         * 
         */
        public double getLongMin() {
            return longMin;
        }

        /**
         * Sets the value of the longMin property.
         * 
         */
        public void setLongMin(final double value) {
            this.longMin = value;
        }

        /**
         * Gets the value of the longSec property.
         * 
         */
        public double getLongSec() {
            return longSec;
        }

        /**
         * Sets the value of the longSec property.
         * 
         */
        public void setLongSec(final double value) {
            this.longSec = value;
        }

        /**
         * Gets the value of the longHem property.
         * 
         */
        public double getLongHem() {
            return longHem;
        }

        /**
         * Sets the value of the longHem property.
         * 
         */
        public void setLongHem(final double value) {
            this.longHem = value;
        }

        /**
         * Gets the value of the depth property.
         * 
         */
        public double getDepth() {
            return depth;
        }

        /**
         * Sets the value of the depth property.
         * 
         */
        public void setDepth(final double value) {
            this.depth = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="Depth" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="Lat" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *       &lt;attribute name="Long" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ShortLocation {

        @XmlAttribute(name = "Depth", required = true)
        protected double depth;
        @XmlAttribute(name = "Lat", required = true)
        protected double lat;
        @XmlAttribute(name = "Long", required = true)
        protected double _long;

        /**
         * Gets the value of the depth property.
         * 
         */
        public double getDepth() {
            return depth;
        }

        /**
         * Sets the value of the depth property.
         * 
         */
        public void setDepth(final double value) {
            this.depth = value;
        }

        /**
         * Gets the value of the lat property.
         * 
         */
        public double getLat() {
            return lat;
        }

        /**
         * Sets the value of the lat property.
         * 
         */
        public void setLat(final double value) {
            this.lat = value;
        }

        /**
         * Gets the value of the long property.
         * 
         */
        public double getLong() {
            return _long;
        }

        /**
         * Sets the value of the long property.
         * 
         */
        public void setLong(final double value) {
            this._long = value;
        }

    }

}
