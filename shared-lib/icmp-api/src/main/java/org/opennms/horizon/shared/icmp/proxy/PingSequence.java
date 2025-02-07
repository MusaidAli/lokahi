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

package org.opennms.horizon.shared.icmp.proxy;

import java.util.Objects;
import org.opennms.horizon.grpc.ping.contract.PingResponse;

public class PingSequence {
    private boolean timeout;
    private Throwable error;
    private PingResponse response;
    private int sequenceNumber;

    public PingSequence(int sequenceNumber, PingResponse response) {
        this.response = Objects.requireNonNull(response);
        this.sequenceNumber = sequenceNumber;
        this.timeout = response.getRtt() == Double.POSITIVE_INFINITY;
    }

    public PingSequence(int sequenceNumber, Throwable throwable) {
        this.error = Objects.requireNonNull(throwable);
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public boolean isError() {
        return error != null;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return !isTimeout() && !isError();
    }

    public PingResponse getResponse() {
        return response;
    }
}
