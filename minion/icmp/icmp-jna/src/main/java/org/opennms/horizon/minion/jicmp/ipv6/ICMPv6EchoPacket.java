/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.jicmp.ipv6;

import java.nio.ByteBuffer;


/**
 * ICMPEchoReply
 *
 * @author brozow
 */
public class ICMPv6EchoPacket extends ICMPv6Packet {
    
    public ICMPv6EchoPacket(int size) {
        super(size);
    }

    public ICMPv6EchoPacket(ICMPv6Packet icmpPacket) {
        super(icmpPacket);
    }
    
    public ByteBuffer getContentBuffer() {
        ByteBuffer content = m_packetData.duplicate();
        content.position(8);
        return content.slice();
    }
    
    public byte[] toBytes() {
        return getContentBuffer().array();
    }
    
    public int getIdentifier() {
        return getUnsignedShort(4);
    }
    
    public void setIdentifier(int id) {
        setUnsignedShort(4, id);
    }
    
    public int getSequenceNumber() {
        return getUnsignedShort(6);
    }
    
    public void setSequenceNumber(int sn) {
        setUnsignedShort(6, sn);
    }

}
