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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.StringJoiner;

/**
 * A varbind from the trap
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name="parm")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class Parm implements Serializable {
	private static final long serialVersionUID = 2841420030575276257L;

	//--------------------------/
	//- Class/Member Variables -/
    //--------------------------/

    /**
     * parm name
     */
	@XmlElement(name="parmName", required=true)
	@NotNull
    private String _parmName;

    /**
     * parm value
     */
	@XmlElement(name="value", required=true)
	@NotNull
	@Valid
	private Value _value;


      //----------------/
     //- Constructors -/
    //----------------/

    public Parm() {
        super();
    }


      //-----------/
     //- Methods -/
    //-----------/

    public Parm(final String name, final String value) {
    	this();
    	setParmName(name);
    	this._value = new Value(value);
	}


	/**
     * Returns the value of field 'parmName'. The field 'parmName'
     * has the following description: parm name
     * 
     * @return the value of field 'ParmName'.
     */
    public String getParmName(
    ) {
        return this._parmName;
    }

    /**
     * Returns the value of field 'value'. The field 'value' has
     * the following description: parm value
     * 
     * @return the value of field 'Value'.
     */
    public Value getValue(
    ) {
        return this._value;
    }

    /**
     * Sets the value of field 'parmName'. The field 'parmName' has
     * the following description: parm name
     * 
     * @param parmName the value of field 'parmName'.
     */
    public void setParmName(
            final String parmName) {
        this._parmName = parmName;
    }

    /**
     * Sets the value of field 'value'. The field 'value' has the
     * following description: parm value
     * 
     * @param value the value of field 'value'.
     */
    public void setValue(
            final Value value) {
        this._value = value;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Parm.class.getSimpleName() + "[", "]")
                .add("parmName='" + _parmName + "'")
                .add("value=" + _value)
                .toString();
    }

    public boolean isValid() {
		return getParmName() != null && getValue() != null;
	}
}
