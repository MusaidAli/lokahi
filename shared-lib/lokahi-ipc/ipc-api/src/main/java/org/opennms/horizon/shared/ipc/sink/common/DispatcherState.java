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

package org.opennms.horizon.shared.ipc.sink.common;

import java.util.Collection;

import com.codahale.metrics.Counter;
import com.google.protobuf.Message;

import org.opennms.horizon.shared.ipc.sink.api.SinkModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * The state and metrics pertaining to a particular dispatches.
 *
 * @author jwhite
 */
public class DispatcherState<W, S extends Message, T extends Message> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherState.class);

    private final SinkModule<S, T> module;

    private final W metadata;

    private final MetricRegistry metrics;

    private final Timer dispatchTimer;

    private final Counter dispatchCounter;

    public DispatcherState(AbstractMessageDispatcherFactory<W> dispatcherFactory, SinkModule<S, T> module) {
        this.module = module;
        metadata = dispatcherFactory.getModuleMetadata(module);
        metrics = dispatcherFactory.getMetrics();

        this.dispatchTimer = metrics.timer(MetricRegistry.name(module.getId(), "dispatch", "time"));
        this.dispatchCounter = metrics.counter(MetricRegistry.name(module.getId(), "dispatch", "count"));
    }

    public SinkModule<S, T> getModule() {
        return module;
    }

    public W getMetaData() {
        return metadata;
    }

    protected MetricRegistry getMetrics() {
        return metrics;
    }

    public Timer getDispatchTimer() {
        return dispatchTimer;
    }

    public Counter getDispatchCounter() {
        return this.dispatchCounter;
    }

    @Override
    public void close() throws Exception {
        final String prefix = MetricRegistry.name(module.getId());
        metrics.removeMatching(new MetricFilter() {
            @Override
            public boolean matches(String name, Metric metric) {
                return name.startsWith(prefix);
            }
        });
    }
}
