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
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcIpcServerBuilder implements GrpcIpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcIpcServerBuilder.class);

    private Properties properties;
    private final List<ServerInterceptor> interceptors;
    private NettyServerBuilder serverBuilder;
    private Server server;
    private final int port;
    private final Duration delay;
    private boolean serverStartScheduled = false;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Set<BindableService> services = new HashSet<>();

    public GrpcIpcServerBuilder(ConfigurationAdmin configAdmin, int port, String delay) {
        this(GrpcIpcUtils.getPropertiesFromConfig(configAdmin, GrpcIpcUtils.GRPC_SERVER_PID), port, delay, Collections.emptyList());
    }

    public GrpcIpcServerBuilder(Properties properties, int port, String delay) {
        this(properties, port, delay, Collections.emptyList());
    }

    public GrpcIpcServerBuilder(Properties properties, int port, String delay, List<ServerInterceptor> interceptors) {
        this.port = port;
        this.delay = Duration.parse(delay);
        this.properties = properties;
        this.interceptors = interceptors;
    }


    @Override
    public synchronized void startServer(BindableService bindableService) throws IOException {
        initializeServerFromConfig();

        if (!services.contains(bindableService)) {
            serverBuilder.addService(bindableService);
            services.add(bindableService);
        }
        if (!serverStartScheduled) {
            startServerWithDelay(delay.toMillis());
            serverStartScheduled = true;
        }
    }

    @Override
    public void startServerWithInterceptors(BindableService bindableService, ServerInterceptor... interceptors) throws IOException {
        initializeServerFromConfig();

        if (!services.contains(bindableService)) {
            serverBuilder.addService(bindableService);

            ServerServiceDefinition serverServiceDefinition = ServerInterceptors.intercept(bindableService, interceptors);
            serverBuilder.addService(serverServiceDefinition);
            services.add(bindableService);
        }

        if (!serverStartScheduled) {
            startServerWithDelay(delay.toMillis());
            serverStartScheduled = true;
        }
    }

    @Override
    public synchronized void stopServer() {
        if (server != null && !server.isShutdown()) {
            server.shutdownNow();
        }
        services.clear();
        serverStartScheduled = false;
    }

    private void initializeServerFromConfig() {
        if (serverBuilder == null) {
            int maxInboundMessageSize = PropertiesUtils.getProperty(properties, GrpcIpcUtils.GRPC_MAX_INBOUND_SIZE, GrpcIpcUtils.DEFAULT_MESSAGE_SIZE);
            boolean tlsEnabled = PropertiesUtils.getProperty(properties, GrpcIpcUtils.TLS_ENABLED, false);

            serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(this.port))
                .maxInboundMessageSize(maxInboundMessageSize)
                ;

            // Add the interceptors
            interceptors.forEach(serverBuilder::intercept);

            if (tlsEnabled) {
                SslContextBuilder sslContextBuilder = GrpcIpcUtils.getSslContextBuilder(properties);
                if (sslContextBuilder != null) {
                    try {
                        serverBuilder.sslContext(sslContextBuilder.build());
                        LOG.info("TLS enabled for Grpc IPC Server");
                    } catch (SSLException e) {
                        LOG.error("Couldn't initialize ssl context from {}", properties, e);
                    }
                }
            }
        }
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    private void startServerWithDelay(long delay) {
        executor.schedule(this::startServerNow, delay, TimeUnit.MILLISECONDS);
    }

    private void startServerNow() {
        server = serverBuilder.build();
        try {
            server.start();
            LOG.info("OpenNMS IPC gRPC server started with {} services", services.size());
        } catch (IOException e) {
            LOG.error("Exception while starting IPC Grpc Server", e);
        }
    }
}
