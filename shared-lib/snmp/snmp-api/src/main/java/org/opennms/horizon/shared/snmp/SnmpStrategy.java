/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.opennms.horizon.shared.snmp.traps.TrapNotificationListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface SnmpStrategy {

    SnmpWalker createWalker(SnmpAgentConfig agentConfig, String name, CollectionTracker tracker);

    SnmpValue set(SnmpAgentConfig agentConfig, SnmpObjId oid, SnmpValue value);

    SnmpValue[] set(SnmpAgentConfig agentConfig, SnmpObjId[] oid, SnmpValue[] value);

    SnmpValue get(SnmpAgentConfig agentConfig, SnmpObjId oid);
    SnmpValue[] get(SnmpAgentConfig agentConfig, SnmpObjId[] oids);
    CompletableFuture<SnmpValue[]> getAsync(SnmpAgentConfig agentConfig, SnmpObjId[] oids);

    SnmpValue getNext(SnmpAgentConfig agentConfig, SnmpObjId oid);
    SnmpValue[] getNext(SnmpAgentConfig agentConfig, SnmpObjId[] oids);
    
    SnmpValue[] getBulk(SnmpAgentConfig agentConfig, SnmpObjId[] oids);

    void registerForTraps(TrapNotificationListener listener, InetAddress address, int snmpTrapPort, List<SnmpV3User> snmpv3Users) throws IOException;

    void registerForTraps(TrapNotificationListener listener, InetAddress address, int snmpTrapPort) throws IOException;
    
    void registerForTraps(TrapNotificationListener listener, int snmpTrapPort) throws IOException;

    void unregisterForTraps(TrapNotificationListener listener) throws IOException;

    SnmpValueFactory getValueFactory();

    SnmpV1TrapBuilder getV1TrapBuilder();
    
    SnmpTrapBuilder getV2TrapBuilder();

    SnmpV3TrapBuilder getV3TrapBuilder();

    SnmpV2TrapBuilder getV2InformBuilder();

    SnmpV3TrapBuilder getV3InformBuilder();
    
    byte[] getLocalEngineID();

}
