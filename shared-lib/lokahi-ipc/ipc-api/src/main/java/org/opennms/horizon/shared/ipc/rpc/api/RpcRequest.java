/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.horizon.shared.ipc.rpc.api;

import io.opentracing.Span;
import java.util.Map;

/**
 * The request of an RPC call.
 *
 * @author jwhite
 */
@Deprecated
public interface RpcRequest {

    public static final String TAG_NODE_ID = "nodeId";
    public static final String TAG_NODE_LABEL = "nodeLabel";
    public static final String TAG_CLASS_NAME = "className";
    public static final String TAG_IP_ADDRESS = "ipAddress";
    public static final String TAG_DESCRIPTION = "description";
    /**
     * Used to route the request to the appropriate location.
     */
    String getLocation();

    /**
     * Used to route the request to a particular system at the given location.
     */
    String getSystemId();

    /**
     * When using JMS, the request will fail if no response was received in this
     * many milliseconds.
     */
    Long getTimeToLiveMs();

    /**
     * RPC clients expose tracing info as tags there by giving more context to each RPC trace.
     * Implementations should add tags defined above if they are available.
     */
    Map<String, String> getTracingInfo();

    Span getSpan();
}
