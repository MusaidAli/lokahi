/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.grpc;

import org.apache.karaf.system.SystemService;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class GrpcShutdownHandlerTest {

    private final SystemService mockSystemService = mock(SystemService.class);
    private final GrpcShutdownHandler target = new GrpcShutdownHandler(mockSystemService);

    @Test
    void testShutdownWithMessage() throws Exception {
        target.shutdown("message");
        verify(mockSystemService, times(1)).halt("+0");
    }

    @Test
    void testShutdownWithThrowable() throws Exception {
        RuntimeException ex = new RuntimeException("exception");
        target.shutdown(ex);
        verify(mockSystemService, times(1)).halt("+0");
    }

    @Test
    void testShutdownException() throws Exception {
        doThrow(new RuntimeException()).when(mockSystemService).halt("+0");
        int statusCode = catchSystemExit(() -> target.shutdown("message"));
        verify(mockSystemService, times(1)).halt("+0");
        assertEquals(-1, statusCode);
    }

}
