/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.horizon.shared.snmp;

import java.util.Objects;

/**
 * Transport object, to transport the {@link SnmpObjId} and {@link SnmpValue} objects as a tuple.
 */
public class SnmpVarBindDTO {

    private final SnmpObjId objId;
    private final SnmpValue value;

    public SnmpVarBindDTO(SnmpObjId objId, SnmpValue value) {
        this.objId = Objects.requireNonNull(objId);
        this.value = Objects.requireNonNull(value);
    }

    public SnmpObjId getSnmpObjectId() {
        return objId;
    }

    public SnmpValue getSnmpValue() {
        return value;
    }
}
