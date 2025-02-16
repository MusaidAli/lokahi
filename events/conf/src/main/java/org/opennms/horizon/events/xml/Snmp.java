/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.events.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;


/**
 * The snmp information from the trap
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name="snmp")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Snmp implements Serializable {
	private static final long serialVersionUID = -3623082421217325379L;

	//--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The snmp enterprise id
     */
	@XmlElement(name="id", required=true)
	@NotNull
    private String _id;

	@XmlElement(name="trapOID")
	private String _trapOID;

    /**
     * The snmp enterprise id text
     */
	@XmlElement(name="idtext")
    private String _idtext;

    /**
     * The snmp version
     */
	@XmlElement(name="version", required=true)
	@NotNull
    private String _version;

    /**
     * The specific trap number
     */
	@XmlElement(name="specific")
    private Integer _specific;

    /**
     * The generic trap number
     */
	@XmlElement(name="generic")
    private Integer _generic;

    /**
     * The community name
     */
	@XmlElement(name="community")
    private String _community;

    /**
     * The time stamp
     */
	@XmlElement(name="time-stamp")
    private Long _timeStamp;


      //----------------/
     //- Constructors -/
    //----------------/

    public Snmp() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     */
    public void deleteGeneric(
    ) {
    	this._generic = null;
    }

    /**
     */
    public void deleteSpecific(
    ) {
    	this._specific = null;
    }

    /**
     */
    public void deleteTimeStamp(
    ) {
    	this._timeStamp = null;
    }

    /**
     * Returns the value of field 'community'. The field
     * 'community' has the following description: The community
     * name
     * 
     * @return the value of field 'Community'.
     */
    public String getCommunity(
    ) {
        return this._community;
    }

    /**
     * Returns the value of field 'generic'. The field 'generic'
     * has the following description: The generic trap number
     * 
     * @return the value of field 'Generic'.
     */
    public Integer getGeneric(
    ) {
        return this._generic == null? 0 : this._generic;
    }

    /**
     * Returns the value of field 'id'. The field 'id' has the
     * following description: The snmp enterprise id
     * 
     * @return the value of field 'Id'.
     */
    public String getId(
    ) {
        return this._id;
    }

    /**
     * Returns the value of field 'idtext'. The field 'idtext' has
     * the following description: The snmp enterprise id text
     * 
     * @return the value of field 'Idtext'.
     */
    public String getIdtext(
    ) {
        return this._idtext;
    }

    /**
     * Returns the value of field 'specific'. The field 'specific'
     * has the following description: The specific trap number
     * 
     * @return the value of field 'Specific'.
     */
    public Integer getSpecific(
    ) {
        return this._specific == null? 0 : this._specific;
    }

    /**
     * Returns the value of field 'timeStamp'. The field
     * 'timeStamp' has the following description: The time stamp
     * 
     * @return the value of field 'TimeStamp'.
     */
    public Long getTimeStamp(
    ) {
        return this._timeStamp == null? 0 : this._timeStamp;
    }

    /**
     * Returns the value of field 'version'. The field 'version'
     * has the following description: The snmp version
     * 
     * @return the value of field 'Version'.
     */
    public String getVersion(
    ) {
        return this._version;
    }

    /**
     * Method hasGeneric.
     * 
     * @return true if at least one Generic has been added
     */
    public boolean hasGeneric(
    ) {
        return this._generic != null;
    }

    /**
     * Method hasSpecific.
     * 
     * @return true if at least one Specific has been added
     */
    public boolean hasSpecific(
    ) {
    	return this._specific != null;
    }

    public boolean hasTrapOID() {
        return this._trapOID != null;
    }

    /**
     * Method hasTimeStamp.
     * 
     * @return true if at least one TimeStamp has been added
     */
    public boolean hasTimeStamp(
    ) {
        return this._timeStamp != null;
    }

    /**
     * Sets the value of field 'community'. The field 'community'
     * has the following description: The community name
     * 
     * @param community the value of field 'community'.
     */
    public void setCommunity(
            final String community) {
        this._community = community;
    }

    /**
     * Sets the value of field 'generic'. The field 'generic' has
     * the following description: The generic trap number
     * 
     * @param generic the value of field 'generic'.
     */
    public void setGeneric(
            final Integer generic) {
        this._generic = generic;
    }

    /**
     * Sets the value of field 'id'. The field 'id' has the
     * following description: The snmp enterprise id
     * 
     * @param id the value of field 'id'.
     */
    public void setId(
            final String id) {
        this._id = id;
    }

    public String getTrapOID() {
        return _trapOID;
    }

    public void setTrapOID(String _trapOID) {
        this._trapOID = _trapOID;
    }

    /**
     * Sets the value of field 'idtext'. The field 'idtext' has the
     * following description: The snmp enterprise id text
     * 
     * @param idtext the value of field 'idtext'.
     */
    public void setIdtext(
            final String idtext) {
        this._idtext = idtext;
    }

    /**
     * Sets the value of field 'specific'. The field 'specific' has
     * the following description: The specific trap number
     * 
     * @param specific the value of field 'specific'.
     */
    public void setSpecific(
            final Integer specific) {
        this._specific = specific;
    }

    /**
     * Sets the value of field 'timeStamp'. The field 'timeStamp'
     * has the following description: The time stamp
     * 
     * @param timeStamp the value of field 'timeStamp'.
     */
    public void setTimeStamp(
            final Long timeStamp) {
        this._timeStamp = timeStamp;
    }

    /**
     * Sets the value of field 'version'. The field 'version' has
     * the following description: The snmp version
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(
            final String version) {
        this._version = version;
    }

        @Override
    public String toString() {
    	return new OnmsStringBuilder(this).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snmp snmp = (Snmp) o;
        return Objects.equals(_id, snmp._id) && Objects.equals(_idtext, snmp._idtext) && Objects.equals(_version, snmp._version) && Objects.equals(_specific, snmp._specific) && Objects.equals(_generic, snmp._generic) && Objects.equals(_community, snmp._community) && Objects.equals(_timeStamp, snmp._timeStamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, _idtext, _version, _specific, _generic, _community, _timeStamp);
    }
}
