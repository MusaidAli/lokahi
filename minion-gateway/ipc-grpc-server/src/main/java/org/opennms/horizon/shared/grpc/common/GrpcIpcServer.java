/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.horizon.shared.grpc.common;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;

import java.io.IOException;
import java.util.Properties;

/**
 *  This Interface allows us to have a common Grpc Server for all IPC Services.
 */
public interface GrpcIpcServer {

    /**
     * Starts server, this will not immediately start server but schedules server start after certain delay.
     *
     * @param bindableService The service that needs to be added */
    void startServer(BindableService bindableService) throws IOException;

    /**
     * Starts server, this will not immediately start server but schedules server start after certain delay.
     *
     * @param bindableService The service that needs to be added */
    void startServerWithInterceptors(BindableService bindableService, ServerInterceptor... interceptors) throws IOException;

    /**
     * Stops the Server.
     **/
    void stopServer();

    /**
     * Get properties with which the service has started.
     **/
    Properties getProperties();
}
